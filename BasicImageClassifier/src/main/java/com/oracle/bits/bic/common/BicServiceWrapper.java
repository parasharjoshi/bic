/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.common;

import com.oracle.bits.bic.em.util.JavaUtility;
import com.oracle.bits.bic.server.ActivityServiceBean;
import com.oracle.bits.bic.server.LoginServiceBean;
import com.oracle.bits.bic.server.InceptionModelServiceBean;
import com.oracle.bits.bic.server.RequestServiceBean;
import com.oracle.bits.bic.to.ActivityTO;
import com.oracle.bits.bic.to.Credential;
import com.oracle.bits.bic.to.FrameworkReadyTO;
import com.oracle.bits.bic.to.InceptionModelTO;
import com.oracle.bits.bic.to.LabelsToTrainTO;
import com.oracle.bits.bic.to.RequestTO;
import com.oracle.bits.bic.to.RestoreModelTO;
import com.oracle.bits.bic.to.TrainRequestTO;
import com.oracle.bits.bic.to.TrainingImageTO;
import com.oracle.bits.bic.to.TrainingModelTO;
import com.oracle.bits.bic.to.UserTO;
import java.util.List;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class BicServiceWrapper {

    final LoginServiceBean loginServBean;
    final RequestServiceBean requestServiceBean;
    final InceptionModelServiceBean modelServiceBean;
    final ActivityServiceBean activityServiceBean;

    public BicServiceWrapper() {
        loginServBean = new LoginServiceBean();
        requestServiceBean = new RequestServiceBean();
        modelServiceBean = InceptionModelServiceBean.getInstance();
        activityServiceBean = new ActivityServiceBean();
    }

    public UserTO doLogin(Credential creds) {
        return loginServBean.performLoginAction(creds);
    }

    public UserTO signUp(UserTO user) {
        loginServBean.signUp(user);
        return user;
    }

    public FrameworkReadyTO canRecognize() {
        return requestServiceBean.isFrameworkAvailable();
    }

    public List<RequestTO> getRecogStats() {
        System.out.println("Got request to getRecogStats....");
        return requestServiceBean.getRecogStats();
    }

    public RequestTO recognizeImage(RequestTO request) {
        requestServiceBean.recognizeImage(request);
        return request;
    }
    
    public TrainingImageTO uploadTrainingImage(TrainingImageTO request) {
        return modelServiceBean.pushImagesForTraining(request);
    }
    
    public TrainingModelTO trainModel(TrainingModelTO request) {
        return modelServiceBean.trainModel(request);
    }
    
    public RestoreModelTO restoreModel(RestoreModelTO request) {
        return modelServiceBean.restoreModel(request);
    }
    
    public boolean isTrainingInProgress() {
        return modelServiceBean.isTrainingInProgress();
    }

    public String recogFeedback(RequestTO req) {
        return requestServiceBean.updateRecognizeFeedback(req);
    }

    public InceptionModelTO downloadInitModel(InceptionModelTO modelTo) {
        return modelServiceBean.downloadAndInitializeModel(modelTo);
    }

    public InceptionModelTO getCurrentModel() {
        return modelServiceBean.getCurrentModelWithoutModelContent();
    }

    public InceptionModelTO getCurrentModelInfoForAdmin() {
        return modelServiceBean.getCurrentModelInfoForAdmin();
    }
    
    public List<InceptionModelTO> getAllModelForAdmin() {
        return modelServiceBean.getAllModelWithoutModelContent();
    }

    public List<ActivityTO> getAllActivity() {
        return activityServiceBean.getAllActivities();
    }
    
    public Boolean isUserNameExists(String userName) {
        return loginServBean.isUserNameTaken(userName);
    }
    
    public Boolean addImagesToTrain(String userName) {
        return loginServBean.isUserNameTaken(userName);
    }
    
    public String getUniqueToken(){
        return JavaUtility.generateUniqueId();
    }
    
    public List<LabelsToTrainTO> getLabelsToTrain(){
        return modelServiceBean.getNewLabelsToTrain();
    }
    
    public TrainRequestTO newTrainRequest(TrainRequestTO request) {
        requestServiceBean.saveNewTrainRequestToDB(request);
        return request;
    }
    
    public List<TrainRequestTO> getAllTrainingRequestForAdmin(){
        return requestServiceBean.getAllTrainingRequestForAdmin();
    }
    
    public List<TrainRequestTO> getAllTrainingRequestForAdminNoContent(){
        return requestServiceBean.getAllTrainingRequestForAdminNoContent();
    }
}
