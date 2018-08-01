/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.to;

import java.util.Date;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class RequestTO {

    private Long id;
    private String fileName;
    private String mimeType;
    private byte[] content;
    private int size;
    private Boolean userVote;
    private String userName;
    private String recognizedObject;
    private Float probability;
    private Date created;
    private Date modified;

    private Error error;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Boolean getUserVote() {
        return userVote;
    }

    public void setUserVote(Boolean userVote) {
        this.userVote = userVote;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRecognizedObject() {
        return recognizedObject;
    }

    public void setRecognizedObject(String recognizedObject) {
        this.recognizedObject = recognizedObject;
    }

    public Float getProbability() {
        return probability;
    }

    public void setProbability(Float probability) {
        this.probability = probability;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

}
