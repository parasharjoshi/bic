/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.server;

import com.oracle.bits.bic.common.exception.EntityNotFoundException;
import com.oracle.bits.bic.common.exception.InvalidOperation;
import com.oracle.bits.bic.common.exception.ModelUnavailableException;
import com.oracle.bits.bic.converter.Converter;
import com.oracle.bits.bic.domain.AccountEntity;
import com.oracle.bits.bic.domain.InceptionModelEntity;
import com.oracle.bits.bic.domain.PersonEntity;
import com.oracle.bits.bic.em.util.EntityUtil;
import com.oracle.bits.bic.to.InceptionModelTO;
import com.oracle.bits.bic.to.RequestTO;
import com.oracle.bits.bic.util.ProjectConstants;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.persistence.EntityManager;
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
    private static final String MODEL_ZIP_FILE_NAME = "inception5h.zip";

    private static final Logger LOG = Logger.getLogger("InceptionModelServiceBean");
    private static byte[] graphDef = null;
    private static List<String> labels = null;

    LoginServiceBean lsb = new LoginServiceBean();
    ActivityServiceBean asb = new ActivityServiceBean();

    public InceptionModelServiceBean() {
        loadModelFromDB();
    }

    public static boolean canRecognize() {
        return graphDef != null && labels != null && !labels.isEmpty();
    }

    public InceptionModelTO downloadAndInitializeModel(InceptionModelTO modelTo) {
        try {
            final String zipFilePath = MODEL_DIR_NAME + SEPERATOR + MODEL_ZIP_FILE_NAME;
            PersonEntity person = lsb.getPersonByUsername(modelTo.getUserName());
            if (person.getRole().getRoleKey().equalsIgnoreCase(ProjectConstants.ADMIN_ROLE)) {
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
        entity.setAutoDownloaded(Boolean.TRUE);
        entity.setModificationDate(new Date());
        persistModelEntity(entity);
        initModel(entity);
        Converter.convertModelEntityToModelTO(entity, modelTo);
        triggerActivity(entity);
    }

    private void initModel(InceptionModelEntity entity) {
        graphDef = entity.getModel();
        labels
                = new ArrayList<String>(Arrays.asList(new String(entity.getLabel()).split("\n")));
    }

    private InceptionModelEntity getLatestModelFromDB() {
        EntityManager entityManager = EntityUtil.getNewEntityManager();
        Query query = entityManager.createQuery("select e from " + InceptionModelEntity.class
                .getSimpleName()
                + " e where e.id = (select max(a.id) from " + InceptionModelEntity.class.getSimpleName() + " a)");
        return (InceptionModelEntity) query.getSingleResult();
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

    public InceptionModelTO getCurrentModelInfoForAdmin() {
        System.out.println("com.oracle.bits.bic.server.InceptionModelServiceBean.getCurrentModelWithoutModelContent() - Initializing model");
        InceptionModelTO inceptionModelTO = new InceptionModelTO();
        try {
            InceptionModelEntity entity = getLatestModelFromDB();
            Converter.convertModelEntityToModelTO(entity, inceptionModelTO);
            Map<String, String> map = new HashMap<String, String>();
            if(entity.getAutoDownloaded()){
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

}
