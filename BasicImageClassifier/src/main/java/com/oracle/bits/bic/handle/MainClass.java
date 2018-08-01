/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.handle;

import com.oracle.bits.bic.em.util.EntityUtil;
import com.oracle.bits.bic.server.LoginServiceBean;
import com.oracle.bits.bic.to.PersonTO;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class MainClass {

    public static void main(String[] args) {
        System.out.println("Start of main method");
        EntityManager em = null;

        try {
            em = EntityUtil.getNewEntityManager();
            if (em == null) {
                System.out.println("em is null...");
            }
            else {
                executeDummySql(em);
                //validateLogin();
                //createPerson();
            }
        }
        catch (Exception e) {
            System.out.println("Exception : "+e.getMessage());
            e.printStackTrace();
        }
        finally {
            EntityUtil.closeEM(em);
        }

    }

    private static void executeDummySql(EntityManager em) {

        try {
            Query query = em.createNativeQuery("Select sysdate from dual");
            Timestamp dbDate = (Timestamp) query.getSingleResult();
            System.out.println("Date fetched from Db is : " + dbDate);
        }
        catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
        }
    }

    private static void validateLogin() {
        try {
            LoginServiceBean lsb = new LoginServiceBean();
            //System.out.println("Validate for creds : "+lsb.validateCredentials("parasjos", "welcome"));
        }
        catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createPerson() {
        try {
            LoginServiceBean lsb = new LoginServiceBean();
            PersonTO perTo = new PersonTO();
            perTo.setEmailId("testuser@bic.com");
            perTo.setFirstName("Test");
            perTo.setLastName("User");
            perTo.setPassword("newPwd");
            perTo.setUserName("tu2");
            //lsb.createAccountPerson(perTo);
            System.out.println("User created successfully with ID : " + perTo.getId());
        }
        catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
        }
    }

}
