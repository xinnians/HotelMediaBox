package com.ufistudio.hotelmediabox.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5Utils {

    private static final String MD_5 = "MD5";
    private static final String _02X = "%02x";

    /**
     *
     * @param file
     * @return
     */
    public static String getMD5CheckSum(File file) {
        StringBuilder hashMD5 = new StringBuilder();
        try {
            hashMD5.append(getMD5CheckSum(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return hashMD5.toString();
    }

    public static String getMD5CheckSum(InputStream inputStream) {
        StringBuilder hashMD5 = new StringBuilder();
        try {
            MessageDigest md5 = MessageDigest.getInstance(MD_5);

            byte[] buffer = new byte[1024];
            int count;
            while ((count = inputStream.read(buffer)) > 0) {
                md5.update(buffer, 0, count);
            }

            byte[] digest = md5.digest();
            for (byte b : digest) {
                hashMD5.append(String.format(_02X, b));
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashMD5.toString();
    }

    /**
     *
     * @param value
     * @return
     */
    public static String getMD5CheckSum(String value) {
        if (value == null)
            return null;
        StringBuilder hashMD5 = new StringBuilder();
        try {
            MessageDigest md5 = MessageDigest.getInstance(MD_5);
            byte[] digest = md5.digest(value.getBytes(StandardCharsets.UTF_8));
            for (byte b : digest) {
                hashMD5.append(String.format(_02X, b));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashMD5.toString();
    }

}