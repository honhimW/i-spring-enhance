package io.github.honhimw.util;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * @author hon_him
 * @since 2024-07-26
 */

public class SignUtils {

    public static final String MD5_WITH_RSA = "MD5WithRSA";
    public static final String SHA256_WITH_RSA = "SHA256WithRSA";
    public static final String SHA256_WITH_ECDSA = "SHA256WithECDSA";
    public static final String SHA256_WITH_ECDDSA = "SHA256WithECDDSA";

    public static final String RSA = "RSA";
    public static final String ECC = "EC";

    public static final SignUtils RSA_SIGN = SignUtils.newInstance(RSA);
    public static final SignUtils ECC_SIGN = SignUtils.newInstance(ECC);

    private final String algorithm;

    private SignUtils(String algorithm) {
        this.algorithm = algorithm;
    }

    public static SignUtils newInstance(String algorithm) {
        return new SignUtils(algorithm);
    }

    public String sign(String type, String content, String privateKey) throws GeneralSecurityException {
        if (content == null) {
            return null;
        }
        PrivateKey priKey = KeyUtils.getPrivateKeyFromPKCS8(algorithm, privateKey);
        Signature signature = Signature.getInstance(type);
        signature.initSign(priKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();
        return Base64.encodeBase64String(signed);
    }

    public boolean verify(String type, String content, String sign, String publicKey) {
        if (type == null || content == null || sign == null || publicKey == null) {
            return false;
        }
        try {
            PublicKey pubKey = KeyUtils.getPublicKeyFromX509(algorithm, publicKey);
            byte[] signed = Base64.decodeBase64(sign);
            Signature signature = Signature.getInstance(type);
            signature.initVerify(pubKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return signature.verify(signed);
        } catch (Exception e) {
            return false;
        }
    }

}
