/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.server;

import com.oracle.bits.bic.converter.Converter;
import com.oracle.bits.bic.domain.ActivityEntity;
import com.oracle.bits.bic.domain.InceptionModelEntity;
import com.oracle.bits.bic.domain.RequestEntity;
import com.oracle.bits.bic.em.util.EntityUtil;
import com.oracle.bits.bic.to.ActivityTO;
import com.oracle.bits.bic.to.RequestTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.apache.log4j.Logger;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class ActivityServiceBean {

    private static final Logger LOG = Logger.getLogger("ActivityServiceBean");

    private void persistActivityEvent(ActivityEntity accEnt) {
        if (accEnt != null) {
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                entityManager.persist(accEnt);
                transaction.commit();
            }
            catch (Exception e) {
                transaction.rollback();
            }
        }
    }

    public void saveActivity(ActivityTO activityTO) {
        //Convert TO to entity
    }

    public void saveActivity(RequestEntity reqEnt) {
        //Convert TO to entity
        ActivityEntity activityEntity = new ActivityEntity();
        activityEntity.setCreationDate(new Date());
        activityEntity.setModificationDate(new Date());
        activityEntity.setRequest(reqEnt);
        activityEntity.setRequestor(reqEnt.getRequestor());
        persistActivityEvent(activityEntity);
    }
    
    public void saveActivity(InceptionModelEntity inceptEntity) {
        //Convert TO to entity
        ActivityEntity activityEntity = new ActivityEntity();
        activityEntity.setCreationDate(new Date());
        activityEntity.setModificationDate(new Date());
        activityEntity.setModel(inceptEntity);
        activityEntity.setRequestor(inceptEntity.getAdministrator());
        persistActivityEvent(activityEntity);
    }

    public List<ActivityTO> getAllActivities() {
        List<ActivityTO> activityTo = new ArrayList<>();
        EntityManager entityManager = EntityUtil.getNewEntityManager();
        Query query = entityManager.createQuery("select e from " + ActivityEntity.class
                .getSimpleName()
                + " e ORDER BY e.creationDate DESC");
        try {
            List<ActivityEntity> activityEntityList = query.getResultList();
            for (ActivityEntity actEntity : activityEntityList) {
                ActivityTO ato = new ActivityTO();
                Converter.convertActivityEntityToActivityTO(actEntity, ato);
                activityTo.add(ato);
            }
        }
        catch (Exception e) {
            LOG.error("Encountered error: " + e.getMessage(), e);
        }
        return activityTo;
    }
}
