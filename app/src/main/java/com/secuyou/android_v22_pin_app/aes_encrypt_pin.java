package com.secuyou.android_v22_pin_app;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
/* loaded from: classes.dex */
public class aes_encrypt_pin {
    private static byte[] key = {43, 126, 21, 22, 40, -82, -46, -90, -85, -9, 21, -120, 9, -49, 79, 60};
    private static SecretKeySpec secretKey;

    public static void setKey() {
        new SecretKeySpec(key, "AES");
    }

    public static byte[] encrypt(byte[] bArr) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(1, secretKeySpec);
            return cipher.doFinal(bArr);
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
            return null;
        }
    }

    public static String decrypt(String str, String str2) {
        try {
            setKey();
            Cipher.getInstance("AES/ECB/PKCS5PADDING").init(2, secretKey);
            return null;
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
            return null;
        }
    }
}
