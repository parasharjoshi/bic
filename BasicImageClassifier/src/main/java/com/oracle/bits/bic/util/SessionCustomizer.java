package com.oracle.bits.bic.util;


import org.apache.log4j.Logger;

import org.eclipse.persistence.sessions.Session;

/**
 * Database session customizer
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class SessionCustomizer implements org.eclipse.persistence.config.SessionCustomizer {
    private static final Logger LOG = Logger.getLogger("SessionCustomizer");
    
    /**
     * Default constructor
     */
    public SessionCustomizer() {
        super();
    }
    
    /**
     * Customize the session to the database.
     * @param session 
     */
    public void customize(Session session) {
        try{
            LOG.debug("Initiating session customization...");
            session.getLogin().setPassword(ProjectConstants.DB_USER_PASSWORD); 
            session.getLogin().setUserName(ProjectConstants.DB_USERNAME);
            LOG.info(String.format("DB Connection details \n JDBC URL - %s \n User - %s", ProjectConstants.DB_JDBC_URL, ProjectConstants.DB_USERNAME));
            session.getLogin().setShouldTrimStrings(false); 
            session.getLogin().setQueryRetryAttemptCount(0);             
            session.getLogin().setConnectionString(ProjectConstants.DB_JDBC_URL);
        }catch(Exception e){
            LOG.error("####################Issue with session customizer####################",e);
        }
    }    
}
