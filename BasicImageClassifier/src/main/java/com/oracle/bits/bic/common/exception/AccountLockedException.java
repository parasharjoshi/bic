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
public class AccountLockedException extends Exception{

    public AccountLockedException() {
        super("User account Locked");
    }

    public AccountLockedException(String message) {
        super(message);
    }
}
