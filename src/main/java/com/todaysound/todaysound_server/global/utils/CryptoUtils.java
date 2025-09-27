package com.todaysound.todaysound_server.global.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 암호화 관련 유틸리티 클래스
 */
public class CryptoUtils {
    
    /**
     * SHA-256 해시 생성
     * @param input 입력 문자열
     * @return SHA-256 해시값 (64자리 hex 문자열)
     */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
    
    /**
     * MD5 해시 생성 (필요시 사용)
     * @param input 입력 문자열
     * @return MD5 해시값 (32자리 hex 문자열)
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}
