package com.fsa.syums.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncryptUtilsTest {

    /**
     * 对称加密
     */
    @Test
    public void testDesEncrypt() {
        try {
            Assertions.assertEquals("7772841DC6099402", EncryptUtils.desEncrypt("123456"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对称解密
     */
    @Test
    public void testDesDecrypt() {
        try {
            Assertions.assertEquals("123456", EncryptUtils.desDecrypt("7772841DC6099402"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
