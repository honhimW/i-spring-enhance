package io.github.honhimw.util;

import io.github.honhimw.util.tool.Timer;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;

/**
 * @author honhimW
 * @since 2025-07-15
 */

public class S3UtilsTests {

    static S3Utils s3 = S3Utils.builder()
        .url("http://127.0.0.1:9000")
        .region("us-east-1")
        .accessKeyId("Qr5jHho1aPY4Y1uBwizE")
        .secretAccessKey("vkvZzpDRTnlwLvvsOjJCGvuJkFLghzMAVjcMyZn7")
        .baseFilePath("utils/test")
        .bucket("spring")
        .cdnUrl("http://127.0.0.1:9001")
        .build();

    @Test
    @SneakyThrows
    void preSign() {
        S3Utils.PreSign preSign = s3.preSign(cfg -> cfg
            .expiration(Duration.ofMinutes(1))
            .key("foo")
            .put(put -> put
                .contentType("image/jpeg")
                .expires(Duration.ofDays(1000))
            ));
        System.out.println(preSign);
    }

    @Test
    @SneakyThrows
    void put() {
        byte[] bytes = FileUtils.readFileToByteArray(new File("E:\\wall paper\\玛莲妮亚.png"));
        Blob mlny1 = Blob.of("mlny", "image/png", bytes);
        String put = s3.put(mlny1);
        System.out.println(put);
    }

    @Test
    @SneakyThrows
    void pubViaHttp() {
        byte[] bytes = FileUtils.readFileToByteArray(new File("E:\\wall paper\\玛莲妮亚.png"));
        Blob mlny1 = Blob.of("mlny2.png", "image/png", bytes);
        String put = s3.putViaPreSign(mlny1);
        System.out.println(put);
    }

    @Test
    @SneakyThrows
    void mimeType() {
        Timer instance = Timer.getInstance();
        {
            String s = S3Utils.mimeType("http://fjeiwo/fejiwof/jfieow/fake.mp4");
            System.out.println(s);
        }
        instance.sout();
    }

}
