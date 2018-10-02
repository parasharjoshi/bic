/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.domain;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
@Entity
@Table(name = "TRAINING_IMAGE")
@SequenceGenerator(name = "TrainingImageIdGenerator", sequenceName = "TRAINING_IMAGE_ID_SEQ", allocationSize = 1)
public class TrainingImageEntity extends BaseEntity implements Serializable {

    private Long id;
    private TrainingLabelEntity trainingLabel;
    private PersonEntity uploader;
    private String uploadToken;
    private String fileName;
    private byte[] content;
    private String mimeType;
    private int sizeBytes;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RequestIdGenerator")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "CONTENT", nullable = false, length = 16777215)
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Column(name = "SIZEBYTES", nullable = false)
    public int getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(int sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    public PersonEntity getUploader() {
        return uploader;
    }

    public void setUploader(PersonEntity uploader) {
        this.uploader = uploader;
    }

    @Column(name = "MIME_TYPE", nullable = false)
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Column(name = "FILE_NAME", nullable = false)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LABEL_ID", nullable = false)
    public TrainingLabelEntity getTrainingLabel() {
        return trainingLabel;
    }

    public void setTrainingLabel(TrainingLabelEntity trainingLabel) {
        this.trainingLabel = trainingLabel;
    }

    @Column(name = "UPLOAD_TOKEN", nullable = false)
    public String getUploadToken() {
        return uploadToken;
    }

    public void setUploadToken(String uploadToken) {
        this.uploadToken = uploadToken;
    }
    
    
}
