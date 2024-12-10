package io.github.honhimw.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.PublicKey;
import java.util.Map;

/**
 * <pre>{@code
 * Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
 * }</pre>
 *
 * @author hon_him
 * @since 2024-07-29
 */

public class CipherUtils {

    /**
     * Encrypt data with AES, random generate a 256 bit AES key
     *
     * @param content data to encrypt
     * @return (AES key base64 encode) - (content base64 encode)
     */
    public static Map.Entry<String, String> encryptByAes(byte[] content) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey aesKey = keyGenerator.generateKey();
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encryptedMessage = aesCipher.doFinal(content);
            String base64Content = Base64.encodeBase64String(encryptedMessage);

            byte[] aesKeyEncoded = aesKey.getEncoded();

            String base64ContentKey = Base64.encodeBase64String(aesKeyEncoded);
            return Map.entry(base64ContentKey, base64Content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encrypt data with RSA public key, the content length is limited, commonly used with AES {@link #encryptByRsaAndAes(byte[], String)}
     *
     * @param content   data to encrypt
     * @param publicKey RSA public key
     * @return encrypted data
     */
    public static byte[] encryptByRsa(byte[] content, String publicKey) {
        try {
            PublicKey _publicKey = KeyUtils.getPublicKeyFromX509("RSA", publicKey);
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, _publicKey);
            return rsaCipher.doFinal(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encrypt data with ECC public key, the content length is unlimited
     *
     * @param content   data to encrypt
     * @param publicKey ECC public key
     * @return encrypted data
     */
    public static byte[] encryptByEcc(byte[] content, String publicKey) {
        try {
            PublicKey _publicKey = KeyUtils.getPublicKeyFromX509("EC", publicKey);
            Cipher eccCipher = Cipher.getInstance("ECIES", "BC");
            eccCipher.init(Cipher.ENCRYPT_MODE, _publicKey);
            return eccCipher.doFinal(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Use the given rsa public key to encrypt a random generated AES key, use this AES key to encrypt content
     *
     * @param content   data to encrypt
     * @param publicKey RSA public key
     * @return (AES key base64 encode) - (content base64 encode)
     */
    public static Map.Entry<String, String> encryptByRsaAndAes(byte[] content, String publicKey) {
        try {
            PublicKey _publicKey = KeyUtils.getPublicKeyFromX509("RSA", publicKey);
            // Generate a AES key
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey aesKey = keyGenerator.generateKey();
            // Init a RSA-cipher for AES key encrypt
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, _publicKey);
            // Encrypt the AES key

            byte[] encodedAesKey = aesKey.getEncoded();
            byte[] encryptedAesKey = rsaCipher.doFinal(encodedAesKey);

            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encryptedMessage = aesCipher.doFinal(content);

            String base64AESKey = Base64.encodeBase64String(encryptedAesKey);
            String base64Content = Base64.encodeBase64String(encryptedMessage);

            return Map.entry(base64AESKey, base64Content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Use the given ecc public key to encrypt a random generated AES key, use this AES key to encrypt content.
     * This method is only for a similar rsa+aes method, actually ecc can be used to directly encrypt any length of content.
     *
     * @param content   data to encrypt
     * @param publicKey ECC public key
     * @return (AES key base64 encode) - (content base64 encode)
     */
    public static Map.Entry<String, String> encryptByEccAndAes(byte[] content, String publicKey) {
        try {
            PublicKey _publicKey = KeyUtils.getPublicKeyFromX509("EC", publicKey);
            // Generate a AES key
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey aesKey = keyGenerator.generateKey();
            // Init a RSA-cipher for AES key encrypt
            Cipher eccCipher = Cipher.getInstance("ECIES", "BC");
            eccCipher.init(Cipher.ENCRYPT_MODE, _publicKey);
            // Encrypt the AES key

            byte[] encodedAesKey = aesKey.getEncoded();
            byte[] encryptedAesKey = eccCipher.doFinal(encodedAesKey);

            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encryptedMessage = aesCipher.doFinal(content);

            String base64AESKey = Base64.encodeBase64String(encryptedAesKey);
            String base64Content = Base64.encodeBase64String(encryptedMessage);

            return Map.entry(base64AESKey, base64Content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypt data with AES
     *
     * @param base64AesKey AES key base64 encoded
     * @param payload      data to decrypt
     * @return decrypted data
     */
    public static byte[] decryptByAes(String base64AesKey, byte[] payload) throws Exception {
        byte[] decryptedSecretKey = Base64.decodeBase64(base64AesKey);
        SecretKey aesKey = new SecretKeySpec(decryptedSecretKey, 0, decryptedSecretKey.length, "AES");
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        return aesCipher.doFinal(payload);
    }

    /**
     * Decrypt data with RSA, public key or private key
     *
     * @param payload encrypted data
     * @param key     public key or private key
     * @return decrypted data
     */
    public static byte[] decryptByRsa(byte[] payload, Key key) {
        if (payload == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            cipher.update(payload);
            return cipher.doFinal();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decrypt data with ECC, public key or private key
     *
     * @param payload encrypted data
     * @param key     public key or private key
     * @return decrypted data
     */
    public static byte[] decryptByEcc(byte[] payload, Key key) {
        try {
            Cipher eccCipher = Cipher.getInstance("ECIES", "BC");
            eccCipher.init(Cipher.DECRYPT_MODE, key);
            return eccCipher.doFinal(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
