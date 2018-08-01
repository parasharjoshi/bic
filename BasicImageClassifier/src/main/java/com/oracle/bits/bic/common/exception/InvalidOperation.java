/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.common.exception;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class InvalidOperation extends Exception{

    public InvalidOperation() {
        super("Invalid/unpermitted operation for user");
    }

    public InvalidOperation(String message) {
        super(message);
    }
}
