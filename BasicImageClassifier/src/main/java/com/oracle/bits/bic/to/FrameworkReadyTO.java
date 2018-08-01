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
public class FrameworkReadyTO {

    private Boolean frameworkReady;
    private Error error;

    public Boolean getFrameworkReady() {
        return frameworkReady;
    }

    public void setFrameworkReady(Boolean frameworkReady) {
        this.frameworkReady = frameworkReady;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

}
