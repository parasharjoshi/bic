package com.oracle.bits.bic.util;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;

public class DecryptPassword {
    public DecryptPassword() {
        super();
    }
    
    public static String getPassword(String encryptedValue){
        Key key;
        String returnValue=null;
        Cipher c=null;
        key = new SecretKeySpec("hdteno98r53lo1mn".getBytes(), "AES");
        try{
        c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedValue);
        byte[] decValue = c.doFinal(decordedValue);
        returnValue = new String(decValue);
        }catch(Exception e ){
            e.printStackTrace();
        }
        return returnValue;
    }
}
