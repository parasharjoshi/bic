/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.domain;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
@Entity
@Table(name = "ACTIVITY")
@SequenceGenerator(name = "ActivityIdGenerator", sequenceName = "ACTIVITY_ID_SEQ", allocationSize = 1)
public class ActivityEntity extends BaseEntity implements Serializable {

    private Long id;
    private PersonEntity requestor;
    private RequestEntity request;
    private InceptionModelEntity model;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ActivityIdGenerator")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    public PersonEntity getRequestor() {
        return requestor;
    }

    public void setRequestor(PersonEntity requestor) {
        this.requestor = requestor;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQ_ID", nullable = false)
    public RequestEntity getRequest() {
        return request;
    }

    public void setRequest(RequestEntity request) {
        this.request = request;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    public InceptionModelEntity getModel() {
        return model;
    }

    public void setModel(InceptionModelEntity model) {
        this.model = model;
    }

}
