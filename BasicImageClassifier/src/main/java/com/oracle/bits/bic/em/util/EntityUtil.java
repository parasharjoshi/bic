/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.em.util;

import com.oracle.bits.bic.util.ProjectConstants;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class EntityUtil {
    private static final Logger LOG = Logger.getLogger("SchedulerUtil");
    
    /**
     * Method to get an entityManager.
     * @return 
     */
    public static EntityManager getNewEntityManager(){
        EntityManager em=null;
        try{
            LOG.info("Entity Manager Obj before getEntityManager :"+em);
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(ProjectConstants.PERSISTANCE_UNIT_NAME);
            em = emf.createEntityManager();
            LOG.info("Entity Manager Obj after getEntityManager :"+em);
        }catch(Exception ex){
            System.out.println("Exception while getEntityManager connection :"+ex.getMessage());
            LOG.error("Exception while getEntityManager connection :"+ex.getMessage(),ex);
        }
        return em;
    }
    
    /**
     * Method to close an entityManager
     * @param em 
     */
    public static void closeEM(EntityManager em) {
        if (em != null) {
            LOG.info(" Clearing and closing existing em connection");
            if (em.isOpen()) {
                try {
                    LOG.info(" Transaction active status: " + em.getTransaction().isActive());
                    if (em.getTransaction().isActive()){
                        em.getTransaction().commit();
                    }
                } catch (Exception ex) {
                    LOG.error(" Exception while commiting transaction",ex);
                }
                em.clear();
                em.close();
            }
            em = null;
        }
    }
}
