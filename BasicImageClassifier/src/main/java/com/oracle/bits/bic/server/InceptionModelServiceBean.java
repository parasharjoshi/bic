/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.server;

import com.oracle.bits.bic.common.exception.EntityNotFoundException;
import com.oracle.bits.bic.common.exception.EnvironmentException;
import com.oracle.bits.bic.common.exception.InvalidOperation;
import com.oracle.bits.bic.common.exception.ModelUnavailableException;
import com.oracle.bits.bic.common.exception.ValidationException;
import com.oracle.bits.bic.converter.Converter;
import com.oracle.bits.bic.domain.InceptionModelEntity;
import com.oracle.bits.bic.domain.PersonEntity;
import com.oracle.bits.bic.domain.TrainingImageEntity;
import com.oracle.bits.bic.domain.TrainingLabelEntity;
import com.oracle.bits.bic.em.util.EntityUtil;
import com.oracle.bits.bic.to.InceptionModelTO;
import com.oracle.bits.bic.to.LabelsToTrainTO;
import com.oracle.bits.bic.to.RequestTO;
import com.oracle.bits.bic.to.RestoreModelTO;
import com.oracle.bits.bic.to.TrainingImageTO;
import com.oracle.bits.bic.to.TrainingModelTO;
import com.oracle.bits.bic.util.ProjectConstants;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class InceptionModelServiceBean {

    private static final String SEPERATOR = ProjectConstants.FILE_PATH_SEPERATOR;
    private static final String MODEL_DIR_NAME = ProjectConstants.APPLICATION_FOLDER + SEPERATOR + "model";
    private static final String RETRAINED_MODEL_DIR_NAME = ProjectConstants.APPLICATION_FOLDER + SEPERATOR + "retrainedModel";
    private static final String RETRAINED_SCRIPT_FILE_PATH = ProjectConstants.APPLICATION_FOLDER + SEPERATOR + "retrain.py";
    private static final String IMAGES_DIR_NAME = ProjectConstants.APPLICATION_FOLDER + SEPERATOR + "trainingImages";
    private static final String MODEL_ZIP_FILE_NAME = "inception5h.zip";

    private static boolean training = false;

    private static final Logger LOG = Logger.getLogger("InceptionModelServiceBean");
    private static byte[] graphDef = null;
    private static List<String> labels = null;
    private static boolean inceptionModel;

    LoginServiceBean lsb = new LoginServiceBean();
    ActivityServiceBean asb = new ActivityServiceBean();
    static InceptionModelServiceBean self;

    private InceptionModelServiceBean() {
        loadModelFromDB();
    }

    public static synchronized InceptionModelServiceBean getInstance() {
        if (self == null) {
            self = new InceptionModelServiceBean();
        }
        return self;
    }

    public synchronized void setTrainingFlag() {
        if (!training) {
            training = true;
        }
    }

    public synchronized void resetTrainingFlag() {
        if (training) {
            training = false;
        }
    }

    public static boolean canRecognize() {
        return graphDef != null && labels != null && !labels.isEmpty();
    }

    public InceptionModelTO downloadAndInitializeModel(InceptionModelTO modelTo) {
        try {
            final String zipFilePath = MODEL_DIR_NAME + SEPERATOR + MODEL_ZIP_FILE_NAME;
            if (lsb.isAdmin(modelTo.getUserName())) {
                verifyOrCreateModelDir(MODEL_DIR_NAME);
                downloadModelToDir(zipFilePath);
                extractModelZip(zipFilePath);
                processFilesAndPushtoDB(modelTo);
            }
            else {
                throw new InvalidOperation();
            }
            return modelTo;
        }
        catch (InvalidOperation ex) {
            LOG.error("com.oracle.bits.bic.server.ModelServiceBean.downloadAndInitializeModel()" + ex.getMessage(), ex);
            modelTo.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.INVALID_OPERATION_ERROR_CODE, ProjectConstants.INVALID_OPERATION_ERROR_MSG));
        }
        catch (EntityNotFoundException ex) {
            LOG.error("com.oracle.bits.bic.server.ModelServiceBean.downloadAndInitializeModel()" + ex.getMessage(), ex);
            modelTo.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.UNKNOWN_ERROR_CODE, ProjectConstants.UNKNOWN_ERROR_MSG));
        }
        catch (FileNotFoundException ex) {
            LOG.error("com.oracle.bits.bic.server.ModelServiceBean.downloadAndInitializeModel()" + ex.getMessage(), ex);
            modelTo.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.UNKNOWN_ERROR_CODE, ProjectConstants.UNKNOWN_ERROR_MSG));
        }
        catch (MalformedURLException ex) {
            LOG.error("Encountered exception in downloadModelToDir(). Exception: " + ex.getMessage(), ex);
            System.out.println("Encountered exception in downloadModelToDir(). Exception: " + ex.getMessage());
        }
        catch (IOException ex) {
            LOG.error("Encountered exception in downloadAndInitializeModel(). Exception: " + ex.getMessage(), ex);
            System.out.println("Encountered exception in downloadAndInitializeModel(). Exception: " + ex.getMessage());
        }
        return null;
    }

    private void downloadModelToDir(String zipPath) throws MalformedURLException, FileNotFoundException, IOException {
        LOG.debug("Start of  downloadModelToDir");
        System.out.println("Start of  downloadModelToDir");
        URL url = new URL(ProjectConstants.INCEPTION_MODEL_ZIP_URL);
        try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
            FileOutputStream fis = new FileOutputStream(zipPath);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = bis.read(buffer, 0, 1024)) != -1) {
                fis.write(buffer, 0, count);
            }
            fis.close();
        }
        LOG.debug("Downloaded file to -" + zipPath);
        System.out.println("Downloaded file to -" + zipPath);
    }

    private void verifyOrCreateModelDir(String dirPath) {
        File f = new File(dirPath);
        if (!f.exists()) {
            //clean directory contents
            f.mkdir();
        }
    }

    private void extractModelZip(String zipPath) throws FileNotFoundException, IOException {
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        fis = new FileInputStream(zipPath);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze = zis.getNextEntry();
        while (ze != null) {
            String fileName = ze.getName();
            File newFile = new File(MODEL_DIR_NAME + SEPERATOR + fileName);
            System.out.println("Unzipping to " + newFile.getAbsolutePath());
            //create directories for sub directories in zip
            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            //close this ZipEntry
            zis.closeEntry();
            ze = zis.getNextEntry();
        }
        //close last ZipEntry
        zis.closeEntry();
        zis.close();
        fis.close();
    }

    private void processFilesAndPushtoDB(InceptionModelTO modelTo) throws IOException, EntityNotFoundException {
        //graphDef = readAllBytesOfFile(Paths.get(MODEL_DIR_NAME, "tensorflow_inception_graph.pb"));
        //System.out.println("Trying to load data from bytes \n\n" + new String(readAllBytesOfFile(Paths.get(MODEL_DIR_NAME, "imagenet_comp_graph_label_strings.txt"))));
        InceptionModelEntity entity = new InceptionModelEntity();
        entity.setAdministrator(lsb.getPersonByUsername(modelTo.getUserName()));
        entity.setCreationDate(new Date());
        entity.setLabel(new String(readAllBytesOfFile(Paths.get(MODEL_DIR_NAME, "imagenet_comp_graph_label_strings.txt"))));
        entity.setLabelFileName("imagenet_comp_graph_label_strings.txt");
        entity.setLicenseInfo(new String(readAllBytesOfFile(Paths.get(MODEL_DIR_NAME, "LICENSE"))));
        entity.setModel(readAllBytesOfFile(Paths.get(MODEL_DIR_NAME, "tensorflow_inception_graph.pb")));
        entity.setModelFileName("tensorflow_inception_graph.pb");
        entity.setInfo("Auto download and initialization of model");
        entity.setDeleted(Boolean.FALSE);
        entity.setAutoDownloaded(Boolean.TRUE);
        entity.setModificationDate(new Date());
        persistModelEntity(entity);
        initModel(entity);
        Converter.convertModelEntityToModelTO(entity, modelTo);
        triggerActivity(entity);
    }

    public synchronized TrainingImageTO pushImagesForTraining(TrainingImageTO trainingImageTO) {
        if (trainingImageTO != null) {
            EntityTransaction transaction = null;
            try {
                //First check if the label exists if not create label
                TrainingLabelEntity labelEntity = createOrGetLabelEntity(trainingImageTO);
                PersonEntity personEntity = lsb.getPersonByUsername(trainingImageTO.getUserName());
                EntityManager entityManager = EntityUtil.getNewEntityManager();
                transaction = entityManager.getTransaction();
                transaction.begin();
                TrainingImageEntity tie = new TrainingImageEntity();
                tie.setContent(trainingImageTO.getContent());
                tie.setUploadToken(trainingImageTO.getUploadToken());
                tie.setFileName(trainingImageTO.getFileName());
                tie.setMimeType(trainingImageTO.getMimeType());
                tie.setSizeBytes(trainingImageTO.getSize());
                tie.setTrainingLabel(labelEntity);
                tie.setUploader(personEntity);
                entityManager.persist(tie);

                transaction.commit();
                //entityManager.flush();

                trainingImageTO.setId(tie.getId());
            }
            catch (ValidationException ex) {
                ex.printStackTrace();
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                trainingImageTO.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.VALIDATION_GENERIC_ERROR_CODE, ProjectConstants.VALIDATION_GENERIC_ERROR_MSG + " Detail: " + ex.getMessage()));
            }
            catch (EntityNotFoundException ex) {
                ex.printStackTrace();
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                trainingImageTO.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.INVALID_OPERATION_ERROR_CODE, ProjectConstants.INVALID_OPERATION_ERROR_MSG + " Detail: " + ex.getMessage()));
            }
            catch (Exception ex) {
                ex.printStackTrace();
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                trainingImageTO.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.UNKNOWN_ERROR_CODE, ProjectConstants.UNKNOWN_ERROR_MSG + " Detail: " + ex.getMessage()));
            }
        }
        return trainingImageTO;
    }

    private void initModel(InceptionModelEntity entity) {
        graphDef = entity.getModel();
        labels
                = new ArrayList<String>(Arrays.asList(new String(entity.getLabel()).split("\n")));
        inceptionModel = entity.getAutoDownloaded();
    }

    private synchronized TrainingLabelEntity createOrGetLabelEntity(TrainingImageTO trainingImageTO) throws ValidationException {
        try {
            TrainingLabelEntity tle = findLabelEntityByLabel(trainingImageTO.getLabel());
            tle.setTrained("N");
            tle.setModificationDate(new Date());
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            entityManager.getTransaction().begin();
            entityManager.merge(tle);
            entityManager.getTransaction().commit();
            return tle;
        }
        catch (EntityNotFoundException ex) {
            //Label is not available we need to create it
            return createLabelEntity(trainingImageTO);
        }
    }

    private synchronized TrainingLabelEntity createLabelEntity(TrainingImageTO trainingImageTO) throws ValidationException {
        if (trainingImageTO == null || trainingImageTO.getLabel() == null || trainingImageTO.getLabel().isEmpty()) {
            throw new ValidationException();
        }
        TrainingLabelEntity entity = new TrainingLabelEntity();
        entity.setLabel(trainingImageTO.getLabel().toLowerCase());
        entity.setTrained("N");
        EntityManager entityManager = EntityUtil.getNewEntityManager();

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(entity);
            entityManager.getTransaction().commit();
            return entity;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private synchronized TrainingLabelEntity findLabelEntityByLabel(String label) throws EntityNotFoundException {
        EntityManager entityManager = EntityUtil.getNewEntityManager();
        Query query = entityManager.createQuery("select e from " + TrainingLabelEntity.class
                .getSimpleName()
                + " e where e.label = :label ");
        query.setParameter("label", label.toLowerCase());
        try {
            return (TrainingLabelEntity) query.getSingleResult();
        }
        catch (NoResultException e) {
            e.printStackTrace();
            throw new EntityNotFoundException();
        }
    }

    private synchronized List<Object[]> findLabelsToBeTrained() throws EntityNotFoundException {
        EntityManager entityManager = EntityUtil.getNewEntityManager();
        Query query = entityManager.createNativeQuery("select tle.id,tle.label,count(tle.id) from TRAINING_LABELS tle,  TRAINING_IMAGE tie "
                + "where tle.trained = ?1 and tle.id = tie.LABEL_ID "
                + "group by tle.id,tle.label");
        query.setParameter(1, "N");
        try {
            return (List<Object[]>) query.getResultList();
        }
        catch (NoResultException e) {
            e.printStackTrace();
            throw new EntityNotFoundException();
        }
    }

    private synchronized String getNewLabelAsCsv() throws EntityNotFoundException {
        List<Object[]> s = findLabelsToBeTrained();
        List<String> labelsString = new ArrayList();
        for (Object[] objects : s) {
            labelsString.add((String) objects[1]);
        }
        return String.join(", ", labelsString);
    }

    public List<LabelsToTrainTO> getNewLabelsToTrain() {
        List<LabelsToTrainTO> returnList = new ArrayList<>();
        try {
            Converter.convertTrainingLabelQueryResultToLabelsToTrainTO(findLabelsToBeTrained(), returnList);
        }
        catch (EntityNotFoundException ex) {
            ex.printStackTrace();
        }
        return returnList;
    }

    public List<LabelsToTrainTO> getAllLabelsToTrain() {
        List<LabelsToTrainTO> returnList = new ArrayList<>();
        try {
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            Query query = entityManager.createNativeQuery("select tle.id,tle.label,count(tle.id) from TRAINING_LABELS tle,  TRAINING_IMAGE tie "
                    + "where tle.id = tie.LABEL_ID "
                    + "group by tle.id,tle.label");
            List<Object[]> result = null;
            try {
                result = (List<Object[]>) query.getResultList();
            }
            catch (NoResultException e) {
                e.printStackTrace();
                throw new EntityNotFoundException();
            }
            Converter.convertTrainingLabelQueryResultToLabelsToTrainTO(result, returnList);
        }
        catch (EntityNotFoundException ex) {
            ex.printStackTrace();
        }
        return returnList;
    }

    private InceptionModelEntity getLatestModelFromDB() {
        EntityManager entityManager = EntityUtil.getNewEntityManager();
        Query query = entityManager.createQuery("select e from " + InceptionModelEntity.class
                .getSimpleName()
                + " e where e.id = (select max(a.id) from " + InceptionModelEntity.class.getSimpleName() + " a where a.deleted = 'FALSE')");
        return (InceptionModelEntity) query.getSingleResult();
    }

    private InceptionModelEntity getModelByIdFromDB(Long modelId) throws EntityNotFoundException {
        EntityManager entityManager = EntityUtil.getNewEntityManager();
        InceptionModelEntity entity = entityManager.find(InceptionModelEntity.class, modelId);
        if (entity == null || entity.getDeleted()) {
            throw new EntityNotFoundException();
        }
        return entity;
    }

    private void loadModelFromDB() {
        System.out.println("com.oracle.bits.bic.server.InceptionModelServiceBean.loadModelFromDB() - Initializing model");
        try {
            initModel(getLatestModelFromDB());
        }
        catch (NoResultException e) {
            LOG.error("No model found in the database..." + e.getMessage(), e);
        }
    }

    public InceptionModelTO getCurrentModelWithoutModelContent() {
        System.out.println("com.oracle.bits.bic.server.InceptionModelServiceBean.getCurrentModelWithoutModelContent() - Initializing model");
        InceptionModelTO inceptionModelTO = new InceptionModelTO();
        try {
            InceptionModelEntity entity = getLatestModelFromDB();
            Converter.convertModelEntityToModelTO(entity, inceptionModelTO);
        }
        catch (NoResultException e) {
            LOG.error("No model found in the database..." + e.getMessage(), e);
        }
        return inceptionModelTO;
    }

    public List<InceptionModelTO> getAllModelWithoutModelContent() {
        System.out.println("com.oracle.bits.bic.server.InceptionModelServiceBean.getAllModelWithoutModelContent()");
        List<InceptionModelTO> modelList = new ArrayList();
        try {
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            Query query = entityManager.createQuery("select e from " + InceptionModelEntity.class
                    .getSimpleName()
                    + " e where e.deleted = 'FALSE' ORDER BY e.id desc");
            List<InceptionModelEntity> modelEntityList = query.getResultList();
            for (InceptionModelEntity inceptionModelEntity : modelEntityList) {
                InceptionModelTO inceptionModelTO = new InceptionModelTO();
                Converter.convertModelEntityToModelTO(inceptionModelEntity, inceptionModelTO);
                modelList.add(inceptionModelTO);
            }
        }
        catch (NoResultException e) {
            LOG.error("No model found in the database..." + e.getMessage(), e);
        }
        return modelList;
    }

    public InceptionModelTO getCurrentModelInfoForAdmin() {
        System.out.println("com.oracle.bits.bic.server.InceptionModelServiceBean.getCurrentModelWithoutModelContent() - Initializing model");
        InceptionModelTO inceptionModelTO = new InceptionModelTO();
        try {
            InceptionModelEntity entity = getLatestModelFromDB();
            Converter.convertModelEntityToModelTO(entity, inceptionModelTO);
            Map<String, String> map = new HashMap<String, String>();
            if (entity.getAutoDownloaded()) {
                map.put("URL", ProjectConstants.INCEPTION_MODEL_ZIP_URL);
            }
            inceptionModelTO.setAttributes(map);
        }
        catch (NoResultException e) {
            LOG.error("No model found in the database..." + e.getMessage(), e);
        }
        return inceptionModelTO;
    }

    public void recognizeObject(RequestTO rto) throws ModelUnavailableException {
        if (canRecognize()) {
            System.out.println("Recognizing object");
            if (inceptionModel) {
                try (Tensor<Float> image = constructAndExecuteGraphToNormalizeImage(rto.getContent())) {
                    float[] labelProbabilities = executeInceptionGraph(graphDef, image);
                    int bestLabelIdx = maxIndex(labelProbabilities);
                    rto.setRecognizedObject(labels.get(bestLabelIdx));
                    rto.setProbability(labelProbabilities[bestLabelIdx] * 100f);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            else {
                // This is a trained model
                try (Tensor<Float> image = constructImageTensor(rto.getContent());
                        Graph g = new Graph()) {
                    g.importGraphDef(graphDef);
                    try (Session s = new Session(g);
                            // Note the change to the name of the node and the fact
                            // that it is being provided the raw imageBytes as input
                            Tensor<?> result = s.runner().feed("Placeholder", image).fetch("final_result").run().get(0)) {
                        final long[] rshape = result.shape();
                        if (result.numDimensions() != 2 || rshape[0] != 1) {
                            throw new RuntimeException(
                                    String.format(
                                            "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                                            Arrays.toString(rshape)));
                        }
                        int nlabels = (int) rshape[1];
                        float[] labelProbabilities = result.copyTo(new float[1][nlabels])[0];
                        // At this point nlabels = number of classes in your retrained model
                        int bestLabelIdx = maxIndex(labelProbabilities);
                        rto.setRecognizedObject(labels.get(bestLabelIdx));
                        rto.setProbability(labelProbabilities[bestLabelIdx] * 100f);
                    }
                }
            }
        }
        else {
            throw new ModelUnavailableException();
        }
    }

    private static int maxIndex(float[] probabilities) {
        int best = 0;
        for (int i = 1; i < probabilities.length; ++i) {
            if (probabilities[i] > probabilities[best]) {
                best = i;
            }
        }
        return best;
    }

    private static byte[] readAllBytesOfFile(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    private static List<String> readAllLines(Path path) throws IOException {
        return Files.readAllLines(path, Charset.forName("UTF-8"));
    }

    private void persistModelEntity(InceptionModelEntity modelEntity) {
        if (modelEntity != null) {
            EntityManager entityManager = EntityUtil.getNewEntityManager();

            entityManager.getTransaction().begin();
            try {
                entityManager.persist(modelEntity);
                entityManager.flush();
                entityManager.getTransaction().commit();
                System.out.println("Model persisted is " + modelEntity.getId());
            }
            catch (Exception e) {
                LOG.error("com.oracle.bits.bic.server.RequestServiceBean.persistRequestToDB()" + e.getMessage());
                entityManager.getTransaction().rollback();
                throw e;
            }
        }
    }

    //Tensorflow framework methods
    private static Tensor<Float> constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) {
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);
            // Some constants specific to the pre-trained model at:
            // https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
            //
            // - The model was trained with images scaled to 224x224 pixels.
            // - The colors, represented as R, G, B in 1-byte each were converted to
            //   float using (value - Mean)/Scale.
            final int H = 224;
            final int W = 224;
            final float mean = 117f;
            final float scale = 1f;

            // Since the graph is being constructed once per execution here, we can use a constant for the
            // input image. If the graph were to be re-used for multiple input images, a placeholder would
            // have been more appropriate.
            final Output<String> input = b.constant("input", imageBytes);
            final Output<Float> output
                    = b.div(
                            b.sub(
                                    b.resizeBilinear(
                                            b.expandDims(
                                                    b.cast(b.decodeJpeg(input, 3), Float.class
                                                    ),
                                                    b.constant("make_batch", 0)),
                                            b.constant("size", new int[]{H, W})),
                                    b.constant("mean", mean)),
                            b.constant("scale", scale));

            try (Session s = new Session(g)) {
                return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class
                );
            }
        }
    }

    private static float[] executeInceptionGraph(byte[] graphDef, Tensor<Float> image) {
        try (Graph g = new Graph()) {
            g.importGraphDef(graphDef);
            try (Session s = new Session(g);
                    Tensor<Float> result
                    = s.runner().feed("input", image).fetch("output").run().get(0).expect(Float.class
                    )) {
                final long[] rshape = result.shape();
                if (result.numDimensions() != 2 || rshape[0] != 1) {
                    throw new RuntimeException(
                            String.format(
                                    "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                                    Arrays.toString(rshape)));
                }
                int nlabels = (int) rshape[1];
                return result.copyTo(new float[1][nlabels])[0];
            }
        }
    }

    public void triggerActivity(InceptionModelEntity req) {
        try {
            asb.saveActivity(req);
        }
        catch (Exception e) {
            LOG.error("Encountered error while triggering an activity", e);
        }
    }

    public void triggerActivity(InceptionModelEntity req, PersonEntity pe) {
        try {
            asb.saveActivity(req, pe);
        }
        catch (Exception e) {
            LOG.error("Encountered error while triggering an activity", e);
        }
    }

    private static Tensor<Float> constructImageTensor(byte[] imageBytes) {
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);
            final int H = 224;
            final int W = 224;
            final float mean = 0f;
            final float scale = 255f;
            final Output<String> input = b.constant("placeholder", imageBytes);
            final Output<Float> output
                    = b.div(
                            b.sub(
                                    b.resizeBilinear(
                                            b.expandDims(
                                                    b.cast(b.decodeJpeg(input, 3), Float.class
                                                    ),
                                                    b.constant("make_batch", 0)),
                                            b.constant("size", new int[]{H, W})),
                                    b.constant("mean", mean)),
                            b.constant("scale", scale));

            try (Session s = new Session(g)) {
                return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class
                );
            }
        }
    }

    public boolean isTrainingInProgress() {
        return training;
    }

    public synchronized TrainingModelTO trainModel(TrainingModelTO tmt) {
        if (this.training) {
            tmt.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.ALREADY_TRAINING_ERROR_CODE, ProjectConstants.ALREADY_TRAINING_ERROR_CODE));
            return tmt;
        }
        setTrainingFlag();
        try {
            if (lsb.isAdmin(tmt.getUserName())) {
                cleanUpDirectories(IMAGES_DIR_NAME);
                List<LabelsToTrainTO> LabelsToTrain = getAllLabelsToTrain();
                createTrainingImageDirectories(LabelsToTrain);
                saveImagesToDirectories(LabelsToTrain);
                kickStartTraining(tmt);
                refreshModelAndPushModelToDB(tmt);
                updateFlagForLabelsTrained(LabelsToTrain);
            }
            else {
                throw new InvalidOperation();
            }
            return tmt;
        }
        catch (InvalidOperation ex) {
            LOG.error("com.oracle.bits.bic.server.ModelServiceBean.downloadAndInitializeModel()" + ex.getMessage(), ex);
            tmt.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.INVALID_OPERATION_ERROR_CODE, ProjectConstants.INVALID_OPERATION_ERROR_MSG));
        }
        catch (EntityNotFoundException ex) {
            LOG.error("com.oracle.bits.bic.server.ModelServiceBean.downloadAndInitializeModel()" + ex.getMessage(), ex);
            tmt.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.UNKNOWN_ERROR_CODE, ProjectConstants.UNKNOWN_ERROR_MSG));
        }
        catch (IOException ex) {
            LOG.error("Encountered exception in trainModel(). Exception: " + ex.getMessage(), ex);
            System.out.println("Encountered exception in trainModel(). Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        catch (InterruptedException ex) {
            System.out.println("Encountered exception in trainModel(). Exception: " + ex.getMessage());
        }
        catch (EnvironmentException ex) {
            LOG.error("Encountered exception in trainModel(). Exception: " + ex.getMessage(), ex);
            System.out.println("Encountered exception in trainModel(). Exception: " + ex.getMessage());
            tmt.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.ENV_ERROR_CODE, ProjectConstants.ENV_ERROR_MSG));
        }
        finally {
            resetTrainingFlag();
        }
        return tmt;
    }

    private synchronized void cleanUpDirectories(String dir) throws IOException {
        System.out.println("com.oracle.bits.bic.server.InceptionModelServiceBean.cleanUpDirectories() - Starting cleanup.");
        Path directory = Paths.get(dir);
        File file = new File(dir);
        if (file.exists() && file.isDirectory()) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        System.out.println("com.oracle.bits.bic.server.InceptionModelServiceBean.cleanUpDirectories() - Cleanup complete.");
    }

    private synchronized void createDirectory(String dirPath) throws IOException {
        System.out.println("com.oracle.bits.bic.server.InceptionModelServiceBean.createDirector() -- Creating directory " + dirPath);
        Path directory = Paths.get(dirPath);
        Files.createDirectories(directory);
    }

    private synchronized void createTrainingImageDirectories(List<LabelsToTrainTO> LabelsToTrain) throws IOException {
        for (LabelsToTrainTO labelsToTrainTO : LabelsToTrain) {
            if (labelsToTrainTO.getImageCount() > 0) {
                createDirectory(IMAGES_DIR_NAME + SEPERATOR + labelsToTrainTO.getLabelName());
            }
        }
    }

    private synchronized void saveImagesToDirectories(List<LabelsToTrainTO> LabelsToTrain) {
        for (LabelsToTrainTO labelsToTrainTO : LabelsToTrain) {
            try {
                List<TrainingImageEntity> listOfImages = getTrainingImageEntitiesByLabel(labelsToTrainTO.getLabelName());
                for (TrainingImageEntity imageEntity : listOfImages) {
                    try (FileOutputStream fos = new FileOutputStream(IMAGES_DIR_NAME + SEPERATOR + labelsToTrainTO.getLabelName() + SEPERATOR + imageEntity.getFileName())) {
                        fos.write(imageEntity.getContent());
                        //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
                    }
                    catch (IOException ex) {
                        java.util.logging.Logger.getLogger(InceptionModelServiceBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            catch (EntityNotFoundException ex) {
                System.out.println("Could not fine entity for label " + labelsToTrainTO.getLabelName());
            }
        }
    }

    private synchronized List<TrainingImageEntity> getTrainingImageEntitiesByLabel(String label) throws EntityNotFoundException {
        EntityManager entityManager = EntityUtil.getNewEntityManager();
        TrainingLabelEntity labelEntity = findLabelEntityByLabel(label);
        Query query = entityManager.createQuery("select e from " + TrainingImageEntity.class
                .getSimpleName()
                + " e where e.trainingLabel = :label ");
        query.setParameter("label", labelEntity);
        try {
            return (List<TrainingImageEntity>) query.getResultList();
        }
        catch (NoResultException e) {
            e.printStackTrace();
            throw new EntityNotFoundException();
        }
    }

    private synchronized void kickStartTraining(TrainingModelTO tmt) throws IOException, InterruptedException, EnvironmentException {
        String logFileName = RETRAINED_MODEL_DIR_NAME + SEPERATOR + "trainLogs.txt";
        String modelFileName = RETRAINED_MODEL_DIR_NAME + SEPERATOR + "graph.pb";
        String labelFileName = RETRAINED_MODEL_DIR_NAME + SEPERATOR + "labels.txt";
        ProcessBuilder builder = new ProcessBuilder("python", "-m", "retrain",
                "--how_many_training_steps=" + tmt.getTrainSteps(),
                "--output_graph=" + modelFileName,
                "--output_labels=" + labelFileName,
                "--image_dir=" + IMAGES_DIR_NAME,
                "--tfhub_module=" + tmt.getTfHubModule());
        builder.directory(new File("D:/BIC/"));
        createDirectory(RETRAINED_MODEL_DIR_NAME);
        Files.deleteIfExists(Paths.get(logFileName));
        Files.deleteIfExists(Paths.get(modelFileName));
        Files.deleteIfExists(Paths.get(labelFileName));

        builder.redirectError(new File(logFileName));
        final Process p = builder.start();
        int errCode = p.waitFor();
        if (errCode != 0) {
            System.out.println("Process ended in error. Error Code : " + errCode);
            throw new EnvironmentException();
        }
    }

    private synchronized void refreshModelAndPushModelToDB(TrainingModelTO tmt) throws IOException, EntityNotFoundException {
        InceptionModelEntity entity = new InceptionModelEntity();
        entity.setAdministrator(lsb.getPersonByUsername(tmt.getUserName()));
        entity.setCreationDate(new Date());
        entity.setLabel(new String(readAllBytesOfFile(Paths.get(RETRAINED_MODEL_DIR_NAME, "labels.txt"))));
        entity.setLabelFileName("labels.txt");
        entity.setDeleted(Boolean.FALSE);
        entity.setModel(readAllBytesOfFile(Paths.get(RETRAINED_MODEL_DIR_NAME, "graph.pb")));
        entity.setModelFileName("graph.pb");
        entity.setInfo("Trained model for new labels: " + getNewLabelAsCsv());
        entity.setAutoDownloaded(Boolean.FALSE);
        entity.setModificationDate(new Date());
        persistModelEntity(entity);
        initModel(entity);
        InceptionModelTO ito = new InceptionModelTO();
        Converter.convertModelEntityToModelTO(entity, ito);
        tmt.setIncepTo(ito);
        triggerActivity(entity);
    }

    private synchronized void updateFlagForLabelsTrained(List<LabelsToTrainTO> labelsToTrain) {
        for (LabelsToTrainTO lto : labelsToTrain) {
            try {
                TrainingLabelEntity tle = findLabelEntityByLabel(lto.getLabelName());
                tle.setTrained("Y");
                tle.setModificationDate(new Date());
                EntityManager entityManager = EntityUtil.getNewEntityManager();
                entityManager.getTransaction().begin();
                entityManager.merge(tle);
                entityManager.getTransaction().commit();
            }
            catch (EntityNotFoundException ex) {
                java.util.logging.Logger.getLogger(InceptionModelServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized RestoreModelTO restoreModel(RestoreModelTO rto) {
        try {
            if (lsb.isAdmin(rto.getUserName())) {
                EntityManager entityManager = EntityUtil.getNewEntityManager();
                InceptionModelEntity modelToRestore = getModelByIdFromDB(rto.getModelId());
                InceptionModelEntity newEntity = new InceptionModelEntity();
                newEntity.setAdministrator(modelToRestore.getAdministrator());
                newEntity.setAutoDownloaded(modelToRestore.getAutoDownloaded());
                newEntity.setDeleted(Boolean.FALSE);
                newEntity.setInfo("Restored the model Id " + rto.getModelId());
                newEntity.setLabel(modelToRestore.getLabel());
                newEntity.setLabelFileName(modelToRestore.getLabelFileName());
                newEntity.setLicenseInfo(modelToRestore.getLicenseInfo());
                newEntity.setModel(modelToRestore.getModel());
                newEntity.setModelFileName(modelToRestore.getModelFileName());
                Date date = new Date();
                newEntity.setModificationDate(date);
                newEntity.setCreationDate(date);

                entityManager.getTransaction().begin();
                //Mark old entries as deleted
                Query query = entityManager.createQuery("UPDATE " + InceptionModelEntity.class
                        .getSimpleName()
                        + " e SET e.deleted = 'TRUE' where e.id > :modelId ");
                query.setParameter("modelId", rto.getModelId());
                int rowsUpdated = query.executeUpdate();
                //Store New Entity
                entityManager.persist(newEntity);
                entityManager.getTransaction().commit();
                InceptionModelEntity entity = getLatestModelFromDB();
                loadModelFromDB();
                triggerActivity(entity, lsb.getPersonByUsername(rto.getUserName()));
            }
            else {
                throw new InvalidOperation();
            }
        }
        catch (InvalidOperation e) {
            e.printStackTrace();
            rto.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.INVALID_OPERATION_ERROR_CODE, ProjectConstants.INVALID_OPERATION_ERROR_MSG));
        }
        catch (NoResultException e) {
            e.printStackTrace();
            rto.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.UNKNOWN_ERROR_CODE, ProjectConstants.UNKNOWN_ERROR_MSG));
        }
        catch (Exception e) {
            e.printStackTrace();
            rto.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.UNKNOWN_ERROR_CODE, ProjectConstants.UNKNOWN_ERROR_MSG + " Detail: " + e.getMessage()));
        }
        return rto;
    }

    public static void main(String[] args) {
        try {
            InceptionModelServiceBean instance = InceptionModelServiceBean.getInstance();
//            instance.cleanUpDirectories(IMAGES_DIR_NAME);
//            List<LabelsToTrainTO> s = instance.getAllLabelsToTrain();
//            instance.createTrainingImageDirectories(s);
//            instance.saveImagesToDirectories(s);
//            instance.kickStartTraining(tmt);

            TrainingModelTO tmt = new TrainingModelTO();
            tmt.setTfHubModule("https://tfhub.dev/google/imagenet/inception_v1/feature_vector/1");
            //tmt.setTfHubModule("https://tfhub.dev/google/imagenet/inception_v2/feature_vector/1");
            tmt.setTrainSteps(500);
            tmt.setUserName("parasjos");
            instance.trainModel(tmt);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
