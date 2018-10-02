/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.em.util;

import java.util.UUID;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class JavaUtility {

    public static String generateUniqueId() {
        return UUID.randomUUID().toString();

    }

    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            System.out.println("Unique id is " + generateUniqueId());
        }

    }
}
