package io.github.honhimw.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.security.Security;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author hon_him
 * @since 2024-11-26
 */

public class CryptoUtilsTest {

    @Test
    @SneakyThrows
    void rsa() {
        CryptoUtils.Asymmetric rsa = CryptoUtils.rsa();
        assertThatThrownBy(rsa::publicKey).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(rsa::privateKey).isInstanceOf(NullPointerException.class);
        rsa = rsa.generateKeyPair();
        Bytes sign = rsa.signSha256("test".getBytes());
        String x = sign.asBase64();
        assertThat(x).hasSize(344);
        assertThat(rsa.verifySha256(sign.unwrap(), "test".getBytes())).isTrue();
        Bytes bytes = rsa.encryptByPublicKey("test".getBytes());
        Bytes bytes1 = rsa.decryptByPrivateKey(bytes.unwrap());
        assertThat(new String(bytes1.unwrap())).isEqualTo("test");
    }

    @Test
    @SneakyThrows
    void ecc() {
        CryptoUtils.Asymmetric ecc = CryptoUtils.ecc();
        assertThatThrownBy(ecc::publicKey).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(ecc::privateKey).isInstanceOf(NullPointerException.class);
        ecc = ecc.generateKeyPair();
        Bytes sign = ecc.signSha256("test".getBytes());
        String x = sign.asBase64();
        assertThat(x).hasSize(96);
        assertThat(ecc.verifySha256(sign.unwrap(), "test".getBytes())).isTrue();
        Bytes bytes = ecc.encryptByPublicKey("test".getBytes());
        Bytes bytes1 = ecc.decryptByPrivateKey(bytes.unwrap());
        assertThat(new String(bytes1.unwrap())).isEqualTo("test");
    }

    @Test
    @SneakyThrows
    void aes() {
        CryptoUtils.Symmetric aes = CryptoUtils.aes();
        aes = aes.generateKey();
        Bytes encrypt = aes.encrypt("test".getBytes());
        Bytes decrypt = aes.decrypt(encrypt.unwrap());
        assertThat(decrypt.asString()).isEqualTo("test");

        byte[] bytes = "hello world".getBytes(StandardCharsets.US_ASCII);
        Bytes wrap = Bytes.wrap(bytes);
        Bytes bytes1 = Bytes.fromAscii(wrap.asAscii());
        assertThat(bytes1.asString()).isEqualTo("hello world");
    }

    @Test
    @SneakyThrows
    void listTypes() {
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            Set<Provider.Service> services = provider.getServices();
            for (Provider.Service service : services) {
                System.out.printf("name: [%s], type: [%s], algorithm: [%s]%n", provider.getName(), service.getType(), service.getAlgorithm());
            }
        }
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

}
