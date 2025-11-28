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

    @Test
    @SneakyThrows
    void test() {
        String base64aes = "U6T0NTt7qk1OjiZoDy+69DxmEc0wo72PxRJgo8A7Qmsip3/bFAiJC+UzGLzGnpeMfjZsnDuqUIGdpFtl/scbiv8jM5804ZETl63xPXR+USaJEKxMjRRi4cQ6ys+2REfl+ffX/2TlT5ZTOiUuWRDPnY9Y0MtnAB7Snx/ZyLjeNyXBdO+I010B5mepCuQUERrMvemm5xcnRxF1Cxg/QLWhjuGX9Cg5W19gtXIyOgXItHmb0rx0Mr15Y25k0c47XbgV3EdLOrdtMYMpzGU0c2AhCQg72ldaKicLepSIe7L0LzrlEvLIdEZ5irJt4dBONWwhiaVMvI1RzcXGwvHg31Zjxg==";
        String payload = "J0ABsAC86jqFeAueDJozIErnjHTOFuuwWqe+/UuYT6iXy+nfKjqG8AppP9EBx7bbZI/X8nvjrW8AkQ6dkakN6vGwWERS5Z00hXU/UkoRaa3ZMEtJgE1pxa1w54JNkoMMmYfOe9nlAqV8Wip0yVxdox+WYiX1aAqNgq/3y3CLKFtH0c4Gu3RBC8eFv4/5iXRJ0Ac0J6eLcuinIsHN5PgIAVuEFb0FQJrBcBB1RL/CWz/LY601dtvCFES+6bebSRUt";
        String rsa = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCjE+hPM+X7JdRRZHy6HEt9e68fH35Ei+v1ymx1JMUvrFQioXtqOMDV7KPYvmTyshASIZR2CBx+2ZE4DbrH/r/QSKH/tqqgOuyRG9i+qcuV1XYljY7oQ7KAMtRhxmoORlATR7BdMWKb44pms/c9T10UgvVswZb2JR5ESduUd2fDd1lry685DM3tBOfJTQvTq7MxJW/NgP7x3l345gTsdT71llceb9DxFbhBurm4wnPtOim3uHxZg09QB7Taiu6l3IqCDxy90yum03HWwrOHusifuWVY5s58IaWoDmEN5o0orW/7JHjojARsIpj+oiVEgl4MZ7jREDbAqx4jKj51GaQXAgMBAAECggEAEcr1wVKByL2vZTQIEcxNhkLs8AHhJnx3wckUvVFGPHXadi7VRtkrXQvlMpxPalV4QGouhxRKe6KkYA/Lbc/DQOfz2PEmu7XCIvD9XIwtHMt+CQnhakEcpDUnjAV4g9czHG9pdDmTrzuL2bnQA335w2eofHXA0VlqbHv8e73tcduSnz1KptejkR5hy37dI/EI5f2gxxYs5Z0TIqK55GsgVIm8kSmH+gczjhUJzsZqo1We3bXfr7q3bODBcWdVa0S5HqNqL6sp1xbKR+oW9KUAR/nWsb3QHiEL06n5ys/E2dSkGC7qtfW3fDPswphbMdLHHrhWkMgMECdVJOh1gpmmEQKBgQDWxp0hg1j94bQhcJPBZrtNuXsZJdRdFp7fvwpTH6oqW1JasUuHbVuFEYyiVb0iHHcQBSqWixH+VbaZ184BVLdzTmhPj7CJGQKoCNkn+wet92tgOUAr6ZnIqvnkCt/WpDY76cBGxOKmZZxKblT6GcOVzrJMLGX2P/1/Azja8gBEcQKBgQDCYQS9tw1tdHwdp0Rbhx8oup0VDv/s7Bf+sV6DTmmjJCKvy+MtQ9Cgcd9wYOsZFCZII2kY5Ej/gfaEB31eEhxlE02yhdrVmR/OqoJQymfHsBZcNVizJpCZta49gdn9pt8RGJG4tFIoBYHsKXmNZn/PHXPu0Fti6zW6nKLzPGKVBwKBgHk1mdQiBoUxbbE5kYmiaq6QsRIeMV1fuMXZUQBcbHJ/G1knmdKSOwY8QzwSWyz3F5Ko98ICNmNtCCKGkjJaFzYx19ie5ShaPw+J/tP7bfgWHDkv1jFyImIHbAPwxyahgiIJ54OI9cimq6+t2LNU+vZ+sk0e+WQh9PRLYzjdPp1hAoGAP40e0rQtpLKn/b35YH9uFY/l2cO8swaB1djB0OZrLVacLKPZk6wPYtW4OvLFCr8GwSEqtGO25irkoUgdmgsL05QbHBodcSaOOrCOyMFUavpMZTtf07rxHgBDhoKU9hR/7AP/aMcz8TfTwZXR0wRHFz1G8TaYGmBSgb/CmwoMJTMCgYA+z7e/UeaHNmMC7vdFzFDFKdLARyPRMFG0VGtjNMznr7chuydlOUrZ9lTQPJp5K+q9bz0l65cSos4aa7ZJCh6/vwEMxx1pwKs9RpkW6uuYItSy3lTHPd6XxwdhX5mEbaOVoLDAz+Z1lDsliKgxFF8/jBNkOZ+uYtW+ECZQtuOrvQ==";
        CryptoUtils.Asymmetric rsa1 = CryptoUtils.rsa(rsa);
        Bytes bytes = rsa1.decryptByPrivateKey(Bytes.fromBase64(base64aes).unwrap());
        CryptoUtils.Symmetric aes = CryptoUtils.aes(bytes.unwrap());
        Bytes decrypt = aes.decrypt(Bytes.fromBase64(payload).unwrap());
        System.out.println(decrypt.asString());
    }

}
