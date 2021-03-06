--Hack for creating user without c##
-- alter session set "_ORACLE_SCRIPT"=true;

-- create user bic identified by welcome;
-- GRANT CONNECT TO bic;
-- GRANT CREATE SESSION TO bic;
-- GRANT CREATE TABLE TO bic;
-- GRANT CREATE SEQUENCE TO bic;
-- GRANT RESOURCE TO bic;
-- GRANT UNLIMITED TABLESPACE TO bic;

DROP TABLE ROLE CASCADE CONSTRAINTS;
DROP SEQUENCE ROLE_ID_SEQ;

CREATE TABLE ROLE (
ID NUMBER(19) PRIMARY KEY,
ROLE_NAME VARCHAR(255) NOT NULL,
ROLE_DESC VARCHAR(2000),
ROLE_KEY VARCHAR(255) NOT NULL,
CREATIONDATE DATE DEFAULT SYSDATE,
MODIFICATIONDATE DATE DEFAULT SYSDATE
);

INSERT INTO ROLE(ID,ROLE_NAME,ROLE_DESC,ROLE_KEY) VALUES ('1','Admin Role', 'Role for users who administrator the application', 'ADMIN');
INSERT INTO ROLE(ID,ROLE_NAME,ROLE_DESC,ROLE_KEY) VALUES ('2','User Role', 'Role for users who use the application', 'USER');
commit;


CREATE SEQUENCE ROLE_ID_SEQ
  START WITH 3
  INCREMENT BY 1 NOCYCLE;

DROP TABLE PERSON CASCADE CONSTRAINTS;
DROP SEQUENCE PERSON_ID_SEQ;

CREATE TABLE PERSON (
ID NUMBER(19) PRIMARY KEY,
FIRST_NAME VARCHAR(50) NOT NULL,
LAST_NAME VARCHAR(50),
USER_NAME VARCHAR(50) NOT NULL UNIQUE,
EMAIL_ID VARCHAR(100) NOT NULL,
ROLE_ID NUMBER(19) REFERENCES ROLE(ID) NOT NULL,
CREATIONDATE DATE DEFAULT SYSDATE,
MODIFICATIONDATE DATE DEFAULT SYSDATE
);

INSERT INTO PERSON(ID,FIRST_NAME,LAST_NAME,USER_NAME,EMAIL_ID,ROLE_ID) VALUES ('1','Parashar', 'Joshi', 'parasjos','parashar.joshi@bic.com',1);
INSERT INTO PERSON(ID,FIRST_NAME,LAST_NAME,USER_NAME,EMAIL_ID,ROLE_ID) VALUES ('2','Vishal', 'Joshi', 'visjos','vishal.joshi@bic.com',2);
INSERT INTO PERSON(ID,FIRST_NAME,LAST_NAME,USER_NAME,EMAIL_ID,ROLE_ID) VALUES ('3','PK', 'Joshi', 'pkj','parashar.joshi@bic.com',1);
COMMIT;

CREATE SEQUENCE PERSON_ID_SEQ
  START WITH 4
  INCREMENT BY 1 NOCYCLE;

DROP TABLE ACCOUNT CASCADE CONSTRAINTS;
DROP SEQUENCE ACCOUNT_ID_SEQ;

CREATE TABLE ACCOUNT (
ID NUMBER(19) PRIMARY KEY,
USER_NAME VARCHAR(50) REFERENCES PERSON(USER_NAME) NOT NULL UNIQUE,
PASSWORD VARCHAR(50) NOT NULL,
USER_ID NUMBER(19) REFERENCES PERSON(ID) NOT NULL UNIQUE,
ACTIVE NUMBER(1) DEFAULT 1 NOT NULL,
LOCKED NUMBER(1) DEFAULT 0 NOT NULL,
CREATIONDATE DATE DEFAULT SYSDATE,
MODIFICATIONDATE DATE DEFAULT SYSDATE
);

INSERT INTO ACCOUNT(ID,USER_NAME, PASSWORD,USER_ID) VALUES ('1','parasjos','welcome','1');
INSERT INTO ACCOUNT(ID,USER_NAME, PASSWORD,USER_ID) VALUES ('2','visjos','welcome','2');
INSERT INTO ACCOUNT(ID,USER_NAME, PASSWORD,USER_ID) VALUES ('3','pkj','pkj','3');
COMMIT;

CREATE SEQUENCE ACCOUNT_ID_SEQ
  START WITH 4
  INCREMENT BY 1 NOCYCLE;


DROP TABLE REQUEST CASCADE CONSTRAINTS;
DROP SEQUENCE REQUEST_ID_SEQ;


CREATE TABLE REQUEST (
ID NUMBER(19) PRIMARY KEY,
USER_ID NUMBER(19) REFERENCES PERSON(ID) NOT NULL,
FILE_NAME VARCHAR(255) NOT NULL,
"CONTENT" BLOB NOT NULL,
MIME_TYPE VARCHAR(255) NOT NULL,
SIZEBYTES NUMBER(10,0) NOT NULL,
USER_VOTE NUMBER(1,0), 
RECOGNIZED_OBJECT VARCHAR(255),
PROBABILITY NUMBER(5,2),
CREATIONDATE DATE DEFAULT SYSDATE,
MODIFICATIONDATE DATE DEFAULT SYSDATE
);


CREATE SEQUENCE REQUEST_ID_SEQ
  START WITH 1
  INCREMENT BY 1 NOCYCLE;


DROP TABLE INCEPTION_MODEL CASCADE CONSTRAINTS;
DROP SEQUENCE INCEPTION_MODEL_ID_SEQ;

CREATE TABLE INCEPTION_MODEL (
ID NUMBER(19) PRIMARY KEY,
USER_ID NUMBER(19) REFERENCES PERSON(ID) NOT NULL,
MODEL_FILE_NAME VARCHAR(255) NOT NULL,
"MODEL" BLOB NOT NULL,
LABEL CLOB NOT NULL,
LABEL_FILE_NAME VARCHAR(255) NOT NULL,
LICENSE_INFO CLOB,
INFO CLOB NOT NULL,
AUTO_DOWNLOADED NUMBER(1,0),
DELETED NUMBER(1,0)  NOT NULL,
CREATIONDATE DATE DEFAULT SYSDATE,
MODIFICATIONDATE DATE DEFAULT SYSDATE
);

CREATE SEQUENCE INCEPTION_MODEL_ID_SEQ
  START WITH 1
  INCREMENT BY 1 NOCYCLE;


DROP TABLE ACTIVITY CASCADE CONSTRAINTS;
DROP SEQUENCE ACTIVITY_ID_SEQ;

CREATE TABLE ACTIVITY (
ID NUMBER(19) PRIMARY KEY,
USER_ID NUMBER(19) REFERENCES PERSON(ID) NOT NULL,
REQ_ID NUMBER(19) REFERENCES REQUEST(ID) UNIQUE,
MODEL_ID NUMBER(19) REFERENCES INCEPTION_MODEL(ID) UNIQUE,
CREATIONDATE DATE DEFAULT SYSDATE,
MODIFICATIONDATE DATE DEFAULT SYSDATE
);

CREATE SEQUENCE ACTIVITY_ID_SEQ
  START WITH 1
  INCREMENT BY 1 NOCYCLE;
  

DROP TABLE TRAINING_LABELS CASCADE CONSTRAINTS;
DROP SEQUENCE TRAINING_LABELS_ID_SEQ;

  
CREATE TABLE TRAINING_LABELS (
ID NUMBER(19) PRIMARY KEY, 
LABEL VARCHAR(1000) NOT NULL,
TRAINED VARCHAR(2) DEFAULT 'N',
CREATIONDATE DATE DEFAULT SYSDATE,
MODIFICATIONDATE DATE DEFAULT SYSDATE
);


CREATE SEQUENCE TRAINING_LABELS_ID_SEQ
  START WITH 1
  INCREMENT BY 1 NOCYCLE;  
  
  
DROP TABLE TRAINING_IMAGE CASCADE CONSTRAINTS;
DROP SEQUENCE TRAINING_IMAGE_ID_SEQ;  

CREATE TABLE TRAINING_IMAGE (
ID NUMBER(19) PRIMARY KEY,
LABEL_ID NUMBER(19) REFERENCES TRAINING_LABELS(ID) NOT NULL,
USER_ID NUMBER(19) REFERENCES PERSON(ID) NOT NULL,
UPLOAD_TOKEN VARCHAR(255) NOT NULL,
FILE_NAME VARCHAR(255) NOT NULL,
"CONTENT" BLOB NOT NULL,
MIME_TYPE VARCHAR(255) NOT NULL,
SIZEBYTES NUMBER(10,0) NOT NULL,
CREATIONDATE DATE DEFAULT SYSDATE,
MODIFICATIONDATE DATE DEFAULT SYSDATE
);


CREATE SEQUENCE TRAINING_IMAGE_ID_SEQ
  START WITH 1
  INCREMENT BY 1 NOCYCLE;    
  
DROP TABLE TRAIN_REQUEST CASCADE CONSTRAINTS;
DROP SEQUENCE TRAIN_REQUEST_ID_SEQ;

CREATE TABLE TRAIN_REQUEST (
ID NUMBER(19) PRIMARY KEY,
USER_ID NUMBER(19) REFERENCES PERSON(ID) NOT NULL,
OBJECT_NAME VARCHAR(255) NOT NULL,
FILE_NAME VARCHAR(255) NOT NULL,
"CONTENT" BLOB NOT NULL,
MIME_TYPE VARCHAR(255) NOT NULL,
SIZEBYTES NUMBER(10,0) NOT NULL,
OBJECT_DESCRIPTION VARCHAR(2000) NOT NULL,
CREATIONDATE DATE DEFAULT SYSDATE,
MODIFICATIONDATE DATE DEFAULT SYSDATE
);

CREATE SEQUENCE TRAIN_REQUEST_ID_SEQ
  START WITH 1
  INCREMENT BY 1 NOCYCLE; 