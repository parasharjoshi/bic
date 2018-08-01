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
@Table(name = "REQUEST")
@SequenceGenerator(name = "RequestIdGenerator", sequenceName = "REQUEST_ID_SEQ", allocationSize = 1)
public class RequestEntity extends BaseEntity implements Serializable {

    private Long id;
    private PersonEntity requestor;
    private String fileName;
    private byte[] content;
    private String mimeType;
    private int sizeBytes;
    private Boolean userVote;
    private String recObj;
    private Float probability;

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

    @Column(name = "USER_VOTE", nullable = false)
    public Boolean isUserVote() {
        return userVote;
    }

    public void setUserVote(Boolean userVote) {
        this.userVote = userVote;
    }

    @Column(name = "FILE_NAME", nullable = false)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "RECOGNIZED_OBJECT", nullable = true)
    public String getRecObj() {
        return recObj;
    }

    public void setRecObj(String recObj) {
        this.recObj = recObj;
    }

    @Column(name = "PROBABILITY", nullable = true)
    public Float getProbability() {
        return probability;
    }

    public void setProbability(Float probability) {
        this.probability = probability;
    }

}
