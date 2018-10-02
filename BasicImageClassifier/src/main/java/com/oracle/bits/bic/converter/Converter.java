/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.converter;

import com.oracle.bits.bic.domain.AccountEntity;
import com.oracle.bits.bic.domain.ActivityEntity;
import com.oracle.bits.bic.domain.InceptionModelEntity;
import com.oracle.bits.bic.domain.PersonEntity;
import com.oracle.bits.bic.domain.RequestEntity;
import com.oracle.bits.bic.domain.TrainingImageEntity;
import com.oracle.bits.bic.domain.TrainingLabelEntity;
import com.oracle.bits.bic.domain.TrainingRequestEntity;
import com.oracle.bits.bic.to.ActivityTO;
import com.oracle.bits.bic.to.InceptionModelTO;
import com.oracle.bits.bic.to.LabelsToTrainTO;
import com.oracle.bits.bic.to.PersonTO;
import com.oracle.bits.bic.to.RequestTO;
import com.oracle.bits.bic.to.TrainRequestTO;
import com.oracle.bits.bic.to.TrainingImageTO;
import com.oracle.bits.bic.to.UserTO;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class Converter {

    public static void convertAccountEntityToUserTO(AccountEntity accEntity, UserTO user) {
        user.setActive(accEntity.getActive());
        user.setAdminUser(accEntity.getUser().getRole().getRoleKey().equalsIgnoreCase("ADMIN"));
        user.setEmailId(accEntity.getUser().getEmailId());
        user.setFirstName(accEntity.getUser().getFirstName());
        user.setLastName(accEntity.getUser().getLastName());
        user.setLocked(accEntity.getLocked());
        user.setUserName(accEntity.getUserName());
    }

    public static void convertModelEntityToModelTO(InceptionModelEntity entity, InceptionModelTO to) {
        to.setCreatedDate(entity.getCreationDate());
        to.setId(entity.getId());
        to.setLabel(entity.getLabel());
        to.setLabelFileName(entity.getLabelFileName());
        to.setLicenseInfo(entity.getLicenseInfo());
        //to.setModel(entity.getModel());
        to.setInfo(entity.getInfo());
        to.setAutoDownloaded(entity.getAutoDownloaded());
        to.setModelFileName(entity.getModelFileName());
        to.setModificationDate(entity.getModificationDate());
        to.setUserName(entity.getAdministrator().getUserName());
    }

    public static void convertModelEntityToModelTOWithModelContent(InceptionModelEntity entity, InceptionModelTO to) {
        to.setCreatedDate(entity.getCreationDate());
        to.setId(entity.getId());
        to.setLabel(entity.getLabel());
        to.setLabelFileName(entity.getLabelFileName());
        to.setLicenseInfo(entity.getLicenseInfo());
        to.setInfo(entity.getInfo());
        to.setModel(entity.getModel());
        to.setAutoDownloaded(entity.getAutoDownloaded());
        to.setModelFileName(entity.getModelFileName());
        to.setModificationDate(entity.getModificationDate());
        to.setUserName(entity.getAdministrator().getUserName());
    }

    public static void convertRequestEntityToRequestTOWithoutContent(RequestEntity entity, RequestTO to) {
        to.setFileName(entity.getFileName());
        to.setId(entity.getId());
        to.setMimeType(entity.getMimeType());
        to.setProbability(entity.getProbability());
        to.setRecognizedObject(entity.getRecObj());
        to.setUserName(entity.getRequestor().getUserName());
        to.setUserVote(entity.isUserVote());
        to.setCreated(entity.getCreationDate());
        to.setModified(entity.getModificationDate());
    }

    public static void convertRequestEntityToRequestTOWithContent(RequestEntity entity, RequestTO to) {
        to.setFileName(entity.getFileName());
        to.setId(entity.getId());
        to.setMimeType(entity.getMimeType());
        to.setProbability(entity.getProbability());
        to.setRecognizedObject(entity.getRecObj());
        to.setUserName(entity.getRequestor().getUserName());
        to.setUserVote(entity.isUserVote());
        to.setContent(entity.getContent());
        to.setCreated(entity.getCreationDate());
        to.setModified(entity.getModificationDate());
    }

    public static void convertPersonEntityToPersonTOWithOutPwd(PersonEntity entity, PersonTO to) {
        to.setEmailId(entity.getEmailId());
        to.setFirstName(entity.getFirstName());
        to.setId(entity.getId());
        to.setLastName(entity.getLastName());
        to.setUserName(entity.getUserName());
    }

    public static void convertActivityEntityToActivityTO(ActivityEntity entity, ActivityTO to) {
        to.setId(entity.getId());
        PersonTO pto = new PersonTO();
        convertPersonEntityToPersonTOWithOutPwd(entity.getRequestor(), pto);
        to.setRequestor(pto);
        if (entity.getRequest() != null) {
            RequestTO rto = new RequestTO();
            convertRequestEntityToRequestTOWithoutContent(entity.getRequest(), rto);
            to.setRequest(rto);
        }
        if (entity.getModel() != null) {
            InceptionModelTO inceptTo = new InceptionModelTO();
            convertModelEntityToModelTO(entity.getModel(), inceptTo);
            to.setModel(inceptTo);
        }
    }

    public static void convertTrainingLabelQueryResultToLabelsToTrainTO(List<Object[]> queryResult, List<LabelsToTrainTO> listOfTO) {
        if (listOfTO == null  || queryResult == null || queryResult.isEmpty()) {
            return;
        }
        listOfTO.clear();
        for (Object[] row : queryResult) {
            LabelsToTrainTO to = new LabelsToTrainTO();
            to.setImageCount(((BigDecimal) row[2]).longValue());
            to.setLabelId(((BigDecimal) row[0]).longValue());
            to.setLabelName((String) row[1]);
            listOfTO.add(to);
        }
    }
    
    
    public static void convertNewTrainingReqEntityListToNewTrainingReqTOList(List<TrainingRequestEntity> queryResult, List<TrainRequestTO> listOfTO) {
        if (listOfTO == null  || queryResult == null || queryResult.isEmpty()) {
            return;
        }
        listOfTO.clear();
        for (TrainingRequestEntity row : queryResult) {
            TrainRequestTO to = new TrainRequestTO();
            to.setComment(row.getComment());
            to.setContent(row.getContent());
            to.setCreated(row.getCreationDate());
            to.setFileName(row.getFileName());
            to.setId(row.getId());
            to.setMimeType(row.getMimeType());
            to.setModified(row.getModificationDate());
            to.setObjName(row.getObjName());
            to.setSize(row.getSizeBytes());
            to.setUserName(row.getRequestor().getUserName());
            listOfTO.add(to);
        }
    }
    
    public static void convertNewTrainingReqEntityListToNewTrainingReqTOListNoContent(List<TrainingRequestEntity> queryResult, List<TrainRequestTO> listOfTO) {
        if (listOfTO == null  || queryResult == null || queryResult.isEmpty()) {
            return;
        }
        listOfTO.clear();
        for (TrainingRequestEntity row : queryResult) {
            TrainRequestTO to = new TrainRequestTO();
            to.setComment(row.getComment());
            to.setCreated(row.getCreationDate());
            to.setFileName(row.getFileName());
            to.setId(row.getId());
            to.setMimeType(row.getMimeType());
            to.setModified(row.getModificationDate());
            to.setObjName(row.getObjName());
            to.setSize(row.getSizeBytes());
            to.setUserName(row.getRequestor().getUserName());
            listOfTO.add(to);
        }
    }

}
