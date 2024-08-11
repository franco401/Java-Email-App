package com.example.emailapp;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class Security {
    //used for file uploading
    public static String fileDirectory = "emailapp\\src\\main\\java\\com\\example\\emailapp\\Files";

    //randomly created in Python
    public static String sk = "Yy,eFdEffT4nlss::)|ZBu]lst3K|80A/8v8r(zqYY)Z_,yB*Ol0gC;4xR.0>C{KW]Ooj].JkTbY/f:)w6r+(Uj{O}jM73j],NWqQ3x^D.vmFF5zzbzvl5(=*gI2g_92B)B=9])t62dLZY3oOQ^.`r`*eqbZ@(}o]*uLV1n^tvg?/yc1XUP1CM7fR)*?)a=VT6ZCGJ^kJTHuRUbS6Ta2YNJavZOM6/-NDpXaW62yc_l@6K.cM})FTvtAu+*@d";
    public static String s = "@seSZgOMm1iAJ[+.N(`h=P6+`e8}825'";

    private static final int keyLength = 256;
    private static final int iterationCount = 65536;

    public static String encrypt(String stringToEncrypt) {
        try {
            /*
             * create initialization vector (array) for
             * secret key by creating an array of
             * random bytes
             */
            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            //create secret key for encryption
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(sk.toCharArray(), s.getBytes(), iterationCount, keyLength);
            SecretKey temp = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(temp.getEncoded(), "AES");

            //using the AES/CBC/PKCS5Padding algorithm
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);

            byte[] cipherText = cipher.doFinal(stringToEncrypt.getBytes("UTF-8"));
            byte[] encryptedData = new byte[iv.length + cipherText.length];

            //copy the iv array into the encryptedData array
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);

            //copy the cipherText array into the encryptedData array
            System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

            //encode the encrpyted data with Base64 string encoding
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            System.out.println("Encryption error: " + e);
            return null;
        }
    }

    public static String decrypt(String stringToDecrypt) {
        try {
            //decode the Base64 encoded string
            byte[] encryptedData = Base64.getDecoder().decode(stringToDecrypt);
            byte[] iv = new byte[16];

            //copy the encryptedData array into the iv array
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);

            IvParameterSpec ivspec = new IvParameterSpec(iv);

            //create secret key for decryption
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(sk.toCharArray(), s.getBytes(), iterationCount, keyLength);
            SecretKey temp = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(temp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);

            byte[] cipherText = new byte[encryptedData.length - 16];
            System.arraycopy(encryptedData, 16, cipherText, 0, cipherText.length);

            byte[] decryptedText = cipher.doFinal(cipherText);
            return new String(decryptedText, "UTF-8");

        } catch (Exception e) {
            System.out.println("Decryption error: " + e);
            return null;
        }
    }

}
