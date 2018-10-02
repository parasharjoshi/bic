/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.server;

import com.oracle.bits.bic.common.exception.EntityNotFoundException;
import com.oracle.bits.bic.converter.Converter;
import com.oracle.bits.bic.domain.RequestEntity;
import com.oracle.bits.bic.domain.TrainingRequestEntity;
import com.oracle.bits.bic.em.util.EntityUtil;
import com.oracle.bits.bic.to.FrameworkReadyTO;
import com.oracle.bits.bic.to.RequestTO;
import com.oracle.bits.bic.to.TrainRequestTO;
import com.oracle.bits.bic.util.ProjectConstants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class RequestServiceBean {

    private static final Logger LOG = Logger.getLogger("RequestServiceBean");

    LoginServiceBean lsb = new LoginServiceBean();
    ActivityServiceBean asb = new ActivityServiceBean();
    InceptionModelServiceBean isb = InceptionModelServiceBean.getInstance();

    public FrameworkReadyTO isFrameworkAvailable() {
        FrameworkReadyTO fto = new FrameworkReadyTO();
        fto.setFrameworkReady(InceptionModelServiceBean.canRecognize());
        return fto;
    }

    public RequestEntity persistRequestToDB(RequestTO rto) throws EntityNotFoundException {
        if (rto != null) {
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            RequestEntity reqEntity = new RequestEntity();
            reqEntity.setContent(rto.getContent());
            reqEntity.setCreationDate(new Date());
            reqEntity.setFileName(rto.getFileName());
            reqEntity.setMimeType(rto.getMimeType());
            reqEntity.setModificationDate(new Date());
            reqEntity.setRecObj(rto.getRecognizedObject());
            reqEntity.setProbability(rto.getProbability());
            reqEntity.setSizeBytes(rto.getSize());
            reqEntity.setRequestor(lsb.getPersonByUsername(rto.getUserName()));
            entityManager.getTransaction().begin();
            try {
                entityManager.persist(reqEntity);
                entityManager.getTransaction().commit();
                System.out.println("Request persisted is " + reqEntity.getId());
                rto.setId(reqEntity.getId());
                return reqEntity;
            }
            catch (Exception e) {
                LOG.error("com.oracle.bits.bic.server.RequestServiceBean.persistRequestToDB()" + e.getMessage());
                entityManager.getTransaction().rollback();
                throw e;
            }
        }
        return null;
    }

    public RequestEntity findRequestById(Long reqId) throws EntityNotFoundException {
        if (reqId != null && reqId > 0) {
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            RequestEntity reqEntity = entityManager.find(RequestEntity.class, reqId);
            if (reqEntity != null) {
                return reqEntity;
            }
        }
        throw new EntityNotFoundException();
    }

    public void writeImageToFile(RequestTO rto) {
        System.out.println("Start of RequestServiceBean.writeImageToFile...");
        if (rto != null) {
            try {
                System.out.println("Saving to file /" + rto.getFileName());
                java.io.FileOutputStream fos = new java.io.FileOutputStream("/" + rto.getFileName());
                fos.write(rto.getContent());
                fos.close();
            }
            catch (Exception e) {
                LOG.error("Encountered exception in com.oracle.bits.bic.server.RequestServiceBean.writeImageToFile()" + e.getMessage());
            }
        }
    }

    public void recognizeImage(RequestTO rto) {
        System.out.println("Start of RequestServiceBean.recognizeImage...");
        if (rto != null) {
            try {
                isb.recognizeObject(rto);
                RequestEntity reqEnt = persistRequestToDB(rto);
                System.out.println("Saved request to DB with req id " + rto.getId());
                //Propagate an activity
                triggerActivity(reqEnt);
            }
            catch (Exception e) {
                LOG.error("Encountered exception in com.oracle.bits.bic.server.RequestServiceBean.recognizeImage()" + e.getMessage(), e);
                rto.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.RECOG_FAILED_ERROR_CODE, ProjectConstants.RECOG_FAILED_ERROR_MSG + " Details: " + e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    public String updateRecognizeFeedback(RequestTO req) {
        System.out.println("Start of RequestServiceBean.recognizeImage...");
        if (req != null && req.getUserVote() != null) {
            try {
                EntityManager entityManager = EntityUtil.getNewEntityManager();
                RequestEntity reqEnt = findRequestById(req.getId());
                reqEnt.setUserVote(req.getUserVote());
                entityManager.getTransaction().begin();
                entityManager.merge(reqEnt);
                entityManager.getTransaction().commit();
                return "Success";
            }
            catch (Exception e) {
                LOG.error("Encountered exception in com.oracle.bits.bic.server.RequestServiceBean.recognizeImage()" + e.getMessage(), e);
            }
        }
        else {
            return "Invalid Request ID";
        }
        return "Failure";
    }

    public void triggerActivity(RequestEntity req) {
        try {
            asb.saveActivity(req);
        }
        catch (Exception e) {
            LOG.error("Encountered error while triggering an activity", e);
        }
    }

    public List<RequestTO> getRecogStats() {
        List<RequestTO> requestTOList = new ArrayList<RequestTO>();
        EntityManager entityManager = EntityUtil.getNewEntityManager();
        Query query = entityManager.createQuery("select e from " + RequestEntity.class
                .getSimpleName()
                + " e");
        try {
            List<RequestEntity> requestEntityList = query.getResultList();
            for (RequestEntity requestEntity : requestEntityList) {
                RequestTO rto = new RequestTO();
                Converter.convertRequestEntityToRequestTOWithoutContent(requestEntity, rto);
                requestTOList.add(rto);
            }
        }
        catch (Exception e) {
            LOG.error("Encountered error: " + e.getMessage(), e);
        }
        return requestTOList;
    }

    public void saveNewTrainRequestToDB(TrainRequestTO rto) {
        if (rto != null) {
            try {
                EntityManager entityManager = EntityUtil.getNewEntityManager();
                TrainingRequestEntity reqEntity = new TrainingRequestEntity();
                reqEntity.setContent(rto.getContent());
                reqEntity.setCreationDate(new Date());
                reqEntity.setFileName(rto.getFileName());
                reqEntity.setMimeType(rto.getMimeType());
                reqEntity.setModificationDate(new Date());
                reqEntity.setSizeBytes(rto.getSize());
                reqEntity.setComment(rto.getComment());
                reqEntity.setObjName(rto.getObjName());
                reqEntity.setRequestor(lsb.getPersonByUsername(rto.getUserName()));
                entityManager.getTransaction().begin();
                try {
                    entityManager.persist(reqEntity);
                    entityManager.getTransaction().commit();
                    System.out.println("TrainingRequestEntity persisted is " + reqEntity.getId());
                    rto.setId(reqEntity.getId());
                }
                catch (Exception e) {
                    LOG.error("com.oracle.bits.bic.server.RequestServiceBean.persistRequestToDB()" + e.getMessage());
                    entityManager.getTransaction().rollback();
                    throw e;
                }
            }
            catch (EntityNotFoundException ex) {
                rto.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.INVALID_OPERATION_ERROR_CODE, ProjectConstants.INVALID_OPERATION_ERROR_MSG + " Details: " + ex.getMessage()));
            }
        }
    }

    private List<TrainingRequestEntity> getAllTrainingRequests() throws EntityNotFoundException {
        EntityManager entityManager = EntityUtil.getNewEntityManager();
        Query query = entityManager.createQuery("select e from " + TrainingRequestEntity.class
                .getSimpleName()
                + " e ORDER BY e.id desc");
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            throw new EntityNotFoundException();
        }
    }
    
    public List<TrainRequestTO> getAllTrainingRequestForAdmin(){
        List<TrainRequestTO> returnList = new ArrayList<>();
        try {
            Converter.convertNewTrainingReqEntityListToNewTrainingReqTOList(getAllTrainingRequests(), returnList);
        }
        catch (EntityNotFoundException ex) {
            ex.printStackTrace();
        }
        return returnList;
    }
    
    public List<TrainRequestTO> getAllTrainingRequestForAdminNoContent(){
        List<TrainRequestTO> returnList = new ArrayList<>();
        try {
            Converter.convertNewTrainingReqEntityListToNewTrainingReqTOListNoContent(getAllTrainingRequests(), returnList);
        }
        catch (EntityNotFoundException ex) {
            ex.printStackTrace();
        }
        return returnList;
    }

}
