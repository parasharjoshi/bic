/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.to;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class TrainingModelTO {

    private String userName;
    private String tfHubModule;
    private Integer trainSteps;
    private String jobId;
    private String errorFile;
    private String logFile;
    private InceptionModelTO incepTo;

    private Error error;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTfHubModule() {
        return tfHubModule;
    }

    public void setTfHubModule(String tfHubModule) {
        this.tfHubModule = tfHubModule;
    }

    public Integer getTrainSteps() {
        return trainSteps;
    }

    public void setTrainSteps(Integer trainSteps) {
        this.trainSteps = trainSteps;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getErrorFile() {
        return errorFile;
    }

    public void setErrorFile(String errorFile) {
        this.errorFile = errorFile;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public InceptionModelTO getIncepTo() {
        return incepTo;
    }

    public void setIncepTo(InceptionModelTO incepTo) {
        this.incepTo = incepTo;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

}
