package com.lab.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SHA256Util {

    private static final Logger LOGGER = Logger.getLogger(SHA256Util.class.getName());


    /**
     * 对字符串进行SHA256加密（固定UTF-8编码，环境统一）
     * @param str 待加密的字符串
     * @return 加密后的十六进制字符串
     */
    public static String encrypt(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // 强制使用UTF-8，跨系统加密结果完全一致
            byte[] digest = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "SHA-256 算法不可用", e);
            return null;
        }
    }

    /**
     * 手动验证加密结果。
     *
     * @param args 运行参数（未使用）
     */
    public static void main(String[] args) {
        // SHA256('123456') = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
        LOGGER.info("SHA256(123456) = " + encrypt("123456"));
    }
}