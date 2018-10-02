/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.domain;

import java.io.Serializable;
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
@Table(name = "TRAIN_REQUEST")
@SequenceGenerator(name = "TrainingRequestIdGenerator", sequenceName = "TRAIN_REQUEST_ID_SEQ", allocationSize = 1)
public class TrainingRequestEntity extends BaseEntity implements Serializable {

    private Long id;
    private PersonEntity requestor;
    private String objName;
    private String fileName;
    private byte[] content;
    private String mimeType;
    private int sizeBytes;
    private String comment;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TrainingRequestIdGenerator")
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
    @JoinColumn(name = "user_id", nullable = false)
    public PersonEntity getRequestor() {
        return requestor;
    }

    public void setRequestor(PersonEntity requestor) {
        this.requestor = requestor;
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

    @Column(name = "OBJECT_DESCRIPTION", nullable = false, length = 2000)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Column(name = "OBJECT_NAME", nullable = false)
    public String getObjName() {
        return objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }
    

}
