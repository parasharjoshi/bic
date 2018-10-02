package com.oracle.bits.bic.util;

import java.io.File;

public class ProjectConstants {

    public static final String DB_JDBC_URL = "jdbc:oracle:thin:@localhost:1521:bitsproj";
    public static final String DB_USERNAME = "bic";
    public static final String DB_USER_PASSWORD = "welcome";
    
    public static final String INCEPTION_MODEL_ZIP_URL= "https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip";
    public static final String FILE_PATH_SEPERATOR = File.separator;
    public static final String APPLICATION_FOLDER = "D:"+FILE_PATH_SEPERATOR+"BIC";

    public static final String PERSISTANCE_UNIT_NAME = "bitsbic";

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";

    public static final String UNKNOWN_ERROR_CODE = "UNKNOWN_ERROR";
    public static final String UNKNOWN_ERROR_MSG = "Something went wrong. Please contact system administrator.";
    
    public static final String VALIDATION_GENERIC_ERROR_CODE = "VALIDATION_ERROR";
    public static final String VALIDATION_GENERIC_ERROR_MSG = "Invalid value(s) specified. Please contact system administrator.";
    
    public static final String INVALID_CREDS_ERROR_CODE = "INVALID_CREDENTIALS";
    public static final String INVALID_CREDS_ERROR_MSG = "Invalid credentials. Please try with valid credentials.";

    public static final String ACCOUNT_LOCKED_ERROR_CODE = "ACCOUNT_LOCKED";
    public static final String ACCOUNT_LOCKED_ERROR_MSG = "Your account is locked. Please contact Admin to get it unlocked.";

    public static final String ACCOUNT_INACTIVE_ERROR_CODE = "ACCOUNT_INACTIVE";
    public static final String ACCOUNT_INACTIVE_ERROR_MSG = "Your account is locked. Please contact Admin to get it activated.";

    public static final String SIGNUP_FAILED_ERROR_CODE = "SIGN_UP_FAILED";
    public static final String SIGNUP_FAILED_ERROR_MSG = "Could not sign you up. Please contact Admin if the problem persists.";

    public static final String SIGNUP_USERNAME_TAKEN_ERROR_CODE = "USERNAME_TAKEN";
    public static final String SIGNUP_USERNAME_TAKEN_ERROR_MSG = "Could not sign you up. The username is already taken. Please try using another username or contact Admin if the problem persists.";
    
    public static final String RECOG_FAILED_ERROR_CODE = "RECOG_FAILED";
    public static final String RECOG_FAILED_ERROR_MSG = "Could not recognize the image. Please contact Admin if the problem persists.";
    
    public static final String INVALID_OPERATION_ERROR_CODE = "INVALID_OPERATION";
    public static final String INVALID_OPERATION_ERROR_MSG = "Operation not allowed for this user. Please contact Admin if the problem persists.";
    
    public static final String ALREADY_TRAINING_ERROR_CODE = "TRAINING_IN_PROGRESS";
    public static final String ALREADY_TRAINING_ERROR_MSG = "A training job is already under progress. Cannot start another one.";
    
    public static final String ENV_ERROR_CODE = "TRAINING_IN_PROGRESS";
    public static final String ENV_ERROR_MSG = "A training job is already under progress. Cannot start another one.";

}
