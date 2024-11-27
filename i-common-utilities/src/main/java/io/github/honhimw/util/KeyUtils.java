package io.github.honhimw.util;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;

import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.*;

/**
 * @author hon_him
 * @since 2024-07-26
 */

public class KeyUtils {

    private KeyUtils() {
    }

    public static KeyPair generateRsaKey() {
        return generateRsaKey(2048);
    }

    public static KeyPair generateRsaKey(int size) {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(size);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    public static KeyPair generateEccKey() {
        return generateEccKey(256);
    }

    public static KeyPair generateEccKey(int size) {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(size);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    public static PublicKey genRSAPublicKey(PrivateKey privateKey) throws Exception {
        RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) privateKey;
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(rsaPrivateCrtKey.getModulus(), rsaPrivateCrtKey.getPublicExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(rsaPublicKeySpec);
    }

    public static PublicKey genECCPublicKey(PrivateKey privateKey) throws Exception {
        ECPrivateKey ecPrivateKey = (ECPrivateKey) privateKey;

        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(ecPrivateKey.getParams());
        ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
        KeyFactory kf = KeyFactory.getInstance("EC");

        ECParameterSpec params = ecPrivateKey.getParams();
        org.bouncycastle.math.ec.ECPoint ecPoint = EC5Util.convertPoint(params, params.getGenerator());
        org.bouncycastle.math.ec.ECPoint multiply = ecPoint.multiply(ecPrivateKey.getS());
        ECPoint pPoint = EC5Util.convertPoint(multiply);
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(pPoint, ecParameters);
        return kf.generatePublic(pubSpec);
    }

    public static PublicKey getPublicKeyFromX509(String algorithm, String publicKey) throws GeneralSecurityException {
        if (publicKey == null) {
            return null;
        }
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = Base64.decodeBase64(publicKey);

        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    }

    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, String privateKey)
        throws GeneralSecurityException {
        if (privateKey == null) {
            return null;
        }
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = Base64.decodeBase64(privateKey);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    public static PublicKey toRSAPubKey(String publicKey) throws GeneralSecurityException {
        return getPublicKeyFromX509("RSA", publicKey);
    }

    public static PublicKey toECCPubKey(String publicKey) throws GeneralSecurityException {
        return getPublicKeyFromX509("EC", publicKey);
    }

    public static PrivateKey toRSAPriKey(String privateKey)
        throws GeneralSecurityException {
        return getPrivateKeyFromPKCS8("RSA", privateKey);
    }

    public static PrivateKey toECCPriKey(String privateKey)
        throws GeneralSecurityException {
        return getPrivateKeyFromPKCS8("EC", privateKey);
    }

}
