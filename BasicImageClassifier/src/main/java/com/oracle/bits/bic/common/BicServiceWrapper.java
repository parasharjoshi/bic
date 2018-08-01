/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.common;

import com.oracle.bits.bic.server.ActivityServiceBean;
import com.oracle.bits.bic.server.LoginServiceBean;
import com.oracle.bits.bic.server.InceptionModelServiceBean;
import com.oracle.bits.bic.server.RequestServiceBean;
import com.oracle.bits.bic.to.ActivityTO;
import com.oracle.bits.bic.to.Credential;
import com.oracle.bits.bic.to.FrameworkReadyTO;
import com.oracle.bits.bic.to.InceptionModelTO;
import com.oracle.bits.bic.to.RequestTO;
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
        modelServiceBean = new InceptionModelServiceBean();
        activityServiceBean = new ActivityServiceBean();
    }

    public UserTO doLogin(Credential creds) {
        System.out.println("Got request to DoLogin....");
        return loginServBean.performLoginAction(creds);
    }

    public UserTO signUp(UserTO user) {
        System.out.println("Got request to signUp....");
        loginServBean.signUp(user);
        return user;
    }

    public FrameworkReadyTO canRecognize() {
        System.out.println("Got request to canRecognize....");
        //requestServiceBean.writeImageToFile(request);
        return requestServiceBean.isFrameworkAvailable();
    }

    public List<RequestTO> getRecogStats() {
        System.out.println("Got request to getRecogStats....");
        //requestServiceBean.writeImageToFile(request);
        return requestServiceBean.getRecogStats();
    }

    public RequestTO recognizeImage(RequestTO request) {
        System.out.println("Got request to recognizeImage....");
        //requestServiceBean.writeImageToFile(request);
        requestServiceBean.recognizeImage(request);
        return request;
    }

    public String recogFeedback(RequestTO req) {
        System.out.println("Got request to recognizeImage....");
        return requestServiceBean.updateRecognizeFeedback(req);
    }

    public InceptionModelTO downloadInitModel(InceptionModelTO modelTo) {
        System.out.println("Got request to recognizeImage....");
        return modelServiceBean.downloadAndInitializeModel(modelTo);
    }

    public InceptionModelTO getCurrentModel() {
        System.out.println("Got request to recognizeImage....");
        return modelServiceBean.getCurrentModelWithoutModelContent();
    }

    public InceptionModelTO getCurrentModelInfoForAdmin() {
        System.out.println("Got request to recognizeImage....");
        return modelServiceBean.getCurrentModelInfoForAdmin();
    }

    public List<ActivityTO> getAllActivity() {
        System.out.println("Got request to getAllActivity....");
        return activityServiceBean.getAllActivities();
    }
}
