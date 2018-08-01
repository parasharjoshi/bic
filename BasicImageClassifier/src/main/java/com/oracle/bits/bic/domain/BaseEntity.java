/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.domain;

import java.util.Date;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
@MappedSuperclass
public abstract class BaseEntity {

    private Long id;
    private Date creationDate;
    private Date modificationDate;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @PrePersist
    public void prePersist() {
        fillCreationDate();
        fillModificationDate();
    }

    @PreUpdate
    public void preUpdate() {
        fillModificationDate();
    }

    private void fillCreationDate() {
        if (creationDate == null) {
            creationDate = new Date();
        }
    }

    private void fillModificationDate() {
        modificationDate = new Date();
    }
}
