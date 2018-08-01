/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.to;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class ActivityTO {

    private Long id;
    private PersonTO requestor;
    private RequestTO request;
    private InceptionModelTO model;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonTO getRequestor() {
        return requestor;
    }

    public void setRequestor(PersonTO requestor) {
        this.requestor = requestor;
    }

    public RequestTO getRequest() {
        return request;
    }

    public void setRequest(RequestTO request) {
        this.request = request;
    }

    public InceptionModelTO getModel() {
        return model;
    }

    public void setModel(InceptionModelTO model) {
        this.model = model;
    }

}
