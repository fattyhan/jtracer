package com.jdjr.tracer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by hanxiaofei on 2018/8/13.
 */
public class MD5Util {
    private static Logger log = LoggerFactory.getLogger(MD5Util.class);
    public static String generatorMD5Hash(String val){
        String myHash = "FAIL";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(val.getBytes());
            byte[] digest = md.digest();
            myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
        }catch (NoSuchAlgorithmException e) {
            log.error("generatorMD5Hash error{}",e.getMessage());
        }finally {
            return myHash;
        }
    }

    public static void main(String[] args) {
        System.out.println(generatorMD5Hash("622222******222222222"));
    }
}
