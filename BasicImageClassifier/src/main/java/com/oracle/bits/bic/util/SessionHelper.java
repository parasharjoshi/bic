package com.oracle.bits.bic.util;


import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;
import org.apache.log4j.Logger;

public class SessionHelper {
    public SessionHelper() {
        super();
    }
    private static final String PROPERTIES_FILE_PATH = "DB.properties";
    private static final Logger LOG = Logger.getLogger("SessionHelper");
    
    public static String getCustomPropertyFromFile(String propertyName) throws Exception {
        return getCustomProperty(propertyName, PROPERTIES_FILE_PATH);
    }
    
    public static String getCustomProperty(String propertyName, String filePath) throws Exception {
        String returnValue=null;
        File propertiesFile = new File(filePath);
        if (propertiesFile.exists()){
            Properties prop = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(propertiesFile);
                prop.load(fis);
                returnValue=prop.getProperty(propertyName);
            } catch (Exception e) {
                LOG.info("Problem getting custom property " + propertyName+ "  : error message : "+e.getMessage());
                throw e;
            } finally {
                if (fis != null)
                    fis.close();
            }
        } else {
            LOG.info("Cannot find "+filePath+'!');
        }
        
        if (returnValue == null)
            LOG.info("No value found for " + propertyName + " property");
        return returnValue;
    }
}
