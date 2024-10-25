package com.project.alm

import java.util.Base64;

import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKeyFactory;
import com.cloudbees.groovy.cps.NonCPS

/**
 * Cipher utility to encrypt / decript data 
 */
class AbsisCipher implements Serializable {
    private String algorithm = "AES/CBC/PKCS5Padding"
    private String cipherAlg = "PBKDF2WithHmacSHA256"
    private String password
    private String salt
    private String ivString 

    AbsisCipher(password_salt_vector, iv_vector) {
        this.password = password_salt_vector[0]
        this.salt = password_salt_vector[1]
        this.ivString = iv_vector
    }

    /**
     * Encrypt some input text
     */    
     @NonCPS
    def encrypt(String input) {
        Cipher encrypt = initializeEncrypt();
        return Base64.getEncoder().encodeToString(encrypt.doFinal(input.getBytes()));
    }

    /**
     * Decrypt some input text
     */
    @NonCPS
    def decrypt(String input) {
        Cipher decrypt = initializeDecrypt();
        return new String(decrypt.doFinal(Base64.getDecoder().decode(input.getBytes())));
    }

    /**
     * Decrypt some input text to byte array
     */
    @NonCPS
    def decryptBytes(String input) {
        Cipher decrypt = initializeDecrypt();
        return decrypt.doFinal(Base64.getDecoder().decode(input.getBytes()));
    }

    /**
     * Initialize Cipher alg, secrets, etc
     */
    @NonCPS
    def initializeEncrypt() {
        Cipher encrypt

        IvParameterSpec iv = new IvParameterSpec(this.ivString.getBytes())

        SecretKeyFactory factory = SecretKeyFactory.getInstance(this.cipherAlg);
        KeySpec spec = new PBEKeySpec(this.password.toCharArray(), this.salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        encrypt = Cipher.getInstance(this.algorithm);
        encrypt.init(Cipher.ENCRYPT_MODE, secret, iv);
        
        return encrypt
    }

    @NonCPS
    def initializeDecrypt() {
        Cipher decrypt

        IvParameterSpec iv = new IvParameterSpec(ivString.getBytes())

        SecretKeyFactory factory = SecretKeyFactory.getInstance(this.cipherAlg);
        KeySpec spec = new PBEKeySpec(this.password.toCharArray(), this.salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        decrypt = Cipher.getInstance(this.algorithm);
        decrypt.init(Cipher.DECRYPT_MODE, secret, iv);

        return decrypt;
    }
}
