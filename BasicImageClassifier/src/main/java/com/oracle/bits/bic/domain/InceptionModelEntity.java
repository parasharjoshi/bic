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
@Table(name = "INCEPTION_MODEL")
@SequenceGenerator(name = "InceptModelIdGenerator", sequenceName = "INCEPTION_MODEL_ID_SEQ", allocationSize = 1)
public class InceptionModelEntity extends BaseEntity implements Serializable {

    private Long id;
    private byte[] model;
    private String modelFileName;
    private String label;
    private String labelFileName;
    private String licenseInfo;
    private String info;
    private Boolean deleted;
    private Boolean autoDownloaded;
    private PersonEntity Administrator;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "InceptModelIdGenerator")
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "MODEL", nullable = false, length = 16777215)
    public byte[] getModel() {
        return model;
    }

    public void setModel(byte[] model) {
        this.model = model;
    }

    @Column(name = "MODEL_FILE_NAME", nullable = false, length = 255)
    public String getModelFileName() {
        return modelFileName;
    }

    public void setModelFileName(String modelFileName) {
        this.modelFileName = modelFileName;
    }

    @Column(name = "LABEL", nullable = false, length = 35000)
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Column(name = "INFO", nullable = false, length = 35000)
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Column(name = "DELETED", nullable = false)
    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Column(name = "LABEL_FILE_NAME", nullable = false, length = 255)
    public String getLabelFileName() {
        return labelFileName;
    }

    public void setLabelFileName(String labelFileName) {
        this.labelFileName = labelFileName;
    }

    @Column(name = "LICENSE_INFO", nullable = false, length = 35000)
    public String getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(String licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public PersonEntity getAdministrator() {
        return Administrator;
    }

    public void setAdministrator(PersonEntity Administrator) {
        this.Administrator = Administrator;
    }

    @Column(name = "AUTO_DOWNLOADED")
    public Boolean getAutoDownloaded() {
        return autoDownloaded;
    }

    public void setAutoDownloaded(Boolean autoDownloaded) {
        this.autoDownloaded = autoDownloaded;
    }

}
