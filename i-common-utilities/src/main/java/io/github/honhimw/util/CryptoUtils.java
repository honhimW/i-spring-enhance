package io.github.honhimw.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-11-26
 */

public class CryptoUtils {

    public static final String RSA = "RSA";
    public static final String ECC = "EC";
    public static final String AES = "AES";

    public static final String MD5_WITH_RSA = "MD5WithRSA";
    public static final String MD5_WITH_ECDSA = "MD5WithECDSA";
    public static final String MD5_WITH_ECDDSA = "MD5WithECDDSA";
    public static final String SHA256_WITH_RSA = "SHA256WithRSA";
    public static final String SHA256_WITH_ECDSA = "SHA256WithECDSA";
    public static final String SHA256_WITH_ECDDSA = "SHA256WithECDDSA";

    public static Asymmetric rsa() {
        return new RSA(null, null);
    }

    public static Asymmetric rsa(PrivateKey privateKey) {
        return new RSA(null, privateKey);
    }

    public static Asymmetric rsa(PublicKey publicKey, PrivateKey privateKey) {
        return new RSA(publicKey, privateKey);
    }

    public static Asymmetric rsa(String privateKey) throws GeneralSecurityException {
        PrivateKey privateKeyFromPKCS8 = getPrivateKeyFromPKCS8(RSA, privateKey);
        return new RSA(null, privateKeyFromPKCS8);
    }

    public static Asymmetric ecc() {
        return new ECC(null, null);
    }

    public static Asymmetric ecc(PrivateKey privateKey) {
        return new ECC(null, privateKey);
    }

    public static Asymmetric ecc(PublicKey publicKey, PrivateKey privateKey) {
        return new ECC(publicKey, privateKey);
    }

    public static Asymmetric ecc(String privateKey) throws GeneralSecurityException {
        PrivateKey privateKeyFromPKCS8 = getPrivateKeyFromPKCS8(ECC, privateKey);
        return new ECC(null, privateKeyFromPKCS8);
    }

    public static Symmetric aes() {
        return new AES(null);
    }

    public static Symmetric aes(SecretKey secretKey) {
        return new AES(secretKey);
    }

    public static Symmetric aes(byte[] secretKey) {
        SecretKey aesKey = new SecretKeySpec(secretKey, 0, secretKey.length, AES);
        return new AES(aesKey);
    }

    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, String privateKey) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = Base64.decodeBase64(privateKey);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    public static PublicKey getPublicKeyFromX509(String algorithm, String publicKey) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = Base64.decodeBase64(publicKey);
        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    }


    public interface Asymmetric {

        PublicKey publicKey();

        PrivateKey privateKey();

        /**
         * Sign data with private key
         *
         * @param data to be signed
         * @return signature
         */
        Bytes sign(String type, byte[] data);

        /**
         * Sign data with private key
         *
         * @param data to be signed
         * @return signature
         */
        Bytes signSha256(byte[] data);

        /**
         * Sign data with private key
         *
         * @param data to be signed
         * @return signature
         */
        Bytes signMd5(byte[] data);

        /**
         * Verify signature with public key
         *
         * @param signature signature
         * @param data      data
         * @return true if signature is valid, false otherwise
         */
        boolean verify(String type, byte[] signature, byte[] data);

        /**
         * Verify signature with public key
         *
         * @param signature signature
         * @param data      data
         * @return true if signature is valid, false otherwise
         */
        boolean verifySha256(byte[] signature, byte[] data);

        /**
         * Verify signature with public key
         *
         * @param signature signature
         * @param data      data
         * @return true if signature is valid, false otherwise
         */
        boolean verifyMd5(byte[] signature, byte[] data);

        /**
         * Generate key pair
         *
         * @return new instance
         */
        Asymmetric generateKeyPair();

        /**
         * Generate key pair
         *
         * @param size key size
         * @return new instance
         */
        Asymmetric generateKeyPair(int size);

        /**
         * Generate public key from private key
         *
         * @return new public key
         */
        PublicKey generatePublicKey();

        /**
         * Encrypt data with public key
         *
         * @param data to be encrypted
         * @return encrypted data
         */
        Bytes encryptByPublicKey(byte[] data);

        /**
         * Encrypt data with public key
         *
         * @param data to be encrypted
         * @return encrypted data
         */
        Bytes encryptByPrivateKey(byte[] data);

        /**
         * Decrypt data with private key
         *
         * @param data to be decrypted
         * @return decrypted data
         */
        Bytes decryptByPublicKey(byte[] data);

        /**
         * Decrypt data with private key
         *
         * @param data to be decrypted
         * @return decrypted data
         */
        Bytes decryptByPrivateKey(byte[] data);
    }

    public interface Symmetric {

        SecretKey key();

        /**
         * Generate secret key
         *
         * @return new instance
         */
        Symmetric generateKey();

        /**
         * Generate secret key
         *
         * @param size key size
         * @return new instance
         */
        Symmetric generateKey(int size);

        /**
         * Encrypt data with key
         *
         * @param data to be encrypted
         * @return encrypted data
         */
        Bytes encrypt(byte[] data);

        /**
         * Decrypt data with key
         *
         * @param data to be decrypted
         * @return decrypted data
         */
        Bytes decrypt(byte[] data);

    }

    private static class RSA implements Asymmetric {

        private PublicKey publicKey;

        private final PrivateKey privateKey;

        public RSA(PublicKey publicKey, PrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        @Override
        public PublicKey publicKey() {
            if (Objects.isNull(publicKey)) {
                publicKey = generatePublicKey();
            }
            return publicKey;
        }

        @Override
        public PrivateKey privateKey() {
            return Objects.requireNonNull(privateKey, "Private key is null");
        }

        @Override
        public Bytes sign(String type, byte[] data) {
            try {
                Signature signature = Signature.getInstance(type);
                signature.initSign(privateKey());
                signature.update(data);
                return Bytes.wrap(signature.sign());
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes signSha256(byte[] data) {
            return sign(SHA256_WITH_RSA, data);
        }

        @Override
        public Bytes signMd5(byte[] data) {
            return sign(MD5_WITH_RSA, data);
        }

        @Override
        public boolean verify(String type, byte[] signature, byte[] data) {
            try {
                Signature _signature = Signature.getInstance(type);
                _signature.initVerify(publicKey());
                _signature.update(data);
                return _signature.verify(signature);
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean verifySha256(byte[] signature, byte[] data) {
            return verify(SHA256_WITH_RSA, signature, data);
        }

        @Override
        public boolean verifyMd5(byte[] signature, byte[] data) {
            return verify(MD5_WITH_RSA, signature, data);
        }

        @Override
        public Asymmetric generateKeyPair() {
            return generateKeyPair(2048);
        }

        @Override
        public Asymmetric generateKeyPair(int size) {
            KeyPair keyPair;
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
                keyPairGenerator.initialize(size);
                keyPair = keyPairGenerator.generateKeyPair();
                return new RSA(keyPair.getPublic(), keyPair.getPrivate());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public PublicKey generatePublicKey() {
            RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) privateKey();
            try {
                RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(rsaPrivateCrtKey.getModulus(), rsaPrivateCrtKey.getPublicExponent());
                KeyFactory keyFactory = KeyFactory.getInstance(RSA);
                return keyFactory.generatePublic(rsaPublicKeySpec);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes encryptByPublicKey(byte[] data) {
            try {
                Cipher rsaCipher = Cipher.getInstance(RSA);
                rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey());
                return Bytes.wrap(rsaCipher.doFinal(data));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes encryptByPrivateKey(byte[] data) {
            try {
                Cipher rsaCipher = Cipher.getInstance(RSA);
                rsaCipher.init(Cipher.ENCRYPT_MODE, privateKey());
                return Bytes.wrap(rsaCipher.doFinal(data));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes decryptByPublicKey(byte[] data) {
            try {
                Cipher cipher = Cipher.getInstance(RSA);
                cipher.init(Cipher.DECRYPT_MODE, publicKey());
                cipher.update(data);
                return Bytes.wrap(cipher.doFinal());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public Bytes decryptByPrivateKey(byte[] data) {
            try {
                Cipher cipher = Cipher.getInstance(RSA);
                cipher.init(Cipher.DECRYPT_MODE, privateKey());
                cipher.update(data);
                return Bytes.wrap(cipher.doFinal());
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Please make sure add 'org.bouncycastle:bcprov-jdk18on' to classpath.
     * <pre>{@code implementation 'org.bouncycastle:bcprov-jdk18on'}</pre>
     * Enabled by calling:
     * <pre>{@code Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider();}</pre>
     */
    private static class ECC implements Asymmetric {

        private PublicKey publicKey;

        private final PrivateKey privateKey;

        public ECC(PublicKey publicKey, PrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        @Override
        public PublicKey publicKey() {
            if (Objects.isNull(publicKey)) {
                publicKey = generatePublicKey();
            }
            return publicKey;
        }

        @Override
        public PrivateKey privateKey() {
            return Objects.requireNonNull(privateKey, "Private key is null");
        }

        @Override
        public Bytes sign(String type, byte[] data) {
            try {
                Signature signature = Signature.getInstance(type);
                signature.initSign(privateKey());
                signature.update(data);
                return Bytes.wrap(signature.sign());
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes signSha256(byte[] data) {
            return sign(SHA256_WITH_ECDDSA, data);
        }

        @Override
        public Bytes signMd5(byte[] data) {
            return sign(MD5_WITH_ECDDSA, data);
        }

        @Override
        public boolean verify(String type, byte[] signature, byte[] data) {
            try {
                Signature _signature = Signature.getInstance(type);
                _signature.initVerify(publicKey());
                _signature.update(data);
                return _signature.verify(signature);
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean verifySha256(byte[] signature, byte[] data) {
            return verify(SHA256_WITH_ECDDSA, signature, data);
        }

        @Override
        public boolean verifyMd5(byte[] signature, byte[] data) {
            return verify(MD5_WITH_ECDDSA, signature, data);
        }

        @Override
        public Asymmetric generateKeyPair() {
            return generateKeyPair(256);
        }

        @Override
        public Asymmetric generateKeyPair(int size) {
            KeyPair keyPair;
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ECC);
                keyPairGenerator.initialize(size);
                keyPair = keyPairGenerator.generateKeyPair();
                return new ECC(keyPair.getPublic(), keyPair.getPrivate());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public PublicKey generatePublicKey() {
            RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) privateKey();
            try {
                RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(rsaPrivateCrtKey.getModulus(), rsaPrivateCrtKey.getPublicExponent());
                KeyFactory keyFactory = KeyFactory.getInstance(ECC);
                return keyFactory.generatePublic(rsaPublicKeySpec);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes encryptByPublicKey(byte[] data) {
            try {
                Cipher rsaCipher = Cipher.getInstance("ECIES", "BC");
                rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey());
                return Bytes.wrap(rsaCipher.doFinal(data));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes encryptByPrivateKey(byte[] data) {
            try {
                Cipher rsaCipher = Cipher.getInstance("ECIES", "BC");
                rsaCipher.init(Cipher.ENCRYPT_MODE, privateKey());
                return Bytes.wrap(rsaCipher.doFinal(data));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes decryptByPublicKey(byte[] data) {
            try {
                Cipher cipher = Cipher.getInstance("ECIES", "BC");
                cipher.init(Cipher.DECRYPT_MODE, publicKey());
                cipher.update(data);
                return Bytes.wrap(cipher.doFinal());
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes decryptByPrivateKey(byte[] data) {
            try {
                Cipher cipher = Cipher.getInstance("ECIES", "BC");
                cipher.init(Cipher.DECRYPT_MODE, privateKey());
                cipher.update(data);
                return Bytes.wrap(cipher.doFinal());
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static class AES implements Symmetric {

        private final SecretKey secretKey;

        public AES(SecretKey secretKey) {
            this.secretKey = secretKey;
        }

        @Override
        public SecretKey key() {
            return Objects.requireNonNull(secretKey, "Secret key is null");
        }

        @Override
        public Symmetric generateKey() {
            return generateKey(256);
        }

        @Override
        public Symmetric generateKey(int size) {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
                keyGenerator.init(256);
                SecretKey aesKey = keyGenerator.generateKey();
                return new AES(aesKey);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes encrypt(byte[] data) {
            try {
                Cipher aesCipher = Cipher.getInstance(AES);
                aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return Bytes.wrap(aesCipher.doFinal(data));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Bytes decrypt(byte[] data) {
            try {
                Cipher aesCipher = Cipher.getInstance(AES);
                aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
                return Bytes.wrap(aesCipher.doFinal(data));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

}
