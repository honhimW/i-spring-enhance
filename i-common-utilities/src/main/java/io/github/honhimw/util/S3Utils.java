package io.github.honhimw.util;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpHeaders;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.internal.util.Mimetype;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

/**
 * required 'software.amazon.awssdk:s3'
 *
 * @author honhimW
 * @since 2025-07-15
 */

@Getter
public class S3Utils {

    private static Mimetype MIME_TYPE;

    private final String url;
    private final String region;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String bucket;
    private final String baseFilePath;
    private final String cdnUrl;

    private final S3Client client;
    private final S3Presigner preSigner;

    public static String mimeType(String link) {
        Optional<String> contentType;
        try {
            HttpUtils.HttpResult result = HttpUtils.getSharedInstance().request(configurer -> configurer
                .method("HEAD")
                .url(link)
            );
            contentType = result.getHeader(HttpHeaders.CONTENT_TYPE);
        } catch (Exception ignored) {
            contentType = Optional.empty();
        }
        return contentType.orElseGet(() -> {
            URIBuilder uriBuilder = URIBuilder.from(link);
            List<String> pathSegments = uriBuilder.getPathSegments();
            if (!pathSegments.isEmpty()) {
                String last = pathSegments.get(pathSegments.size() - 1);
                if (MIME_TYPE == null) {
                    MIME_TYPE = Mimetype.getInstance();
                }
                return MIME_TYPE.getMimetype(Path.of(last));
            }
            return "application/octet-stream";
        });
    }

    public static S3Utils.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String url;
        private String region;
        private String accessKeyId;
        private String secretAccessKey;
        private String bucket;
        private String baseFilePath = "";
        private String cdnUrl;

        private Builder() {
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder accessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
            return this;
        }

        public Builder secretAccessKey(String secretAccessKey) {
            this.secretAccessKey = secretAccessKey;
            return this;
        }

        public Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public Builder baseFilePath(String baseFilePath) {
            baseFilePath = Strings.CS.removeStart(baseFilePath, "/");
            baseFilePath = Strings.CS.removeEnd(baseFilePath, "/");
            this.baseFilePath = baseFilePath;
            return this;
        }

        public Builder cdnUrl(String cdnUrl) {
            this.cdnUrl = cdnUrl;
            return this;
        }

        public S3Utils build() {
            if (StringUtils.isAnyBlank(this.url, this.region, this.accessKeyId, this.secretAccessKey)) {
                throw new IllegalArgumentException("url, region, accessKeyId, secretAccessKey should not be blank.");
            }
            if (StringUtils.isBlank(cdnUrl)) {
                cdnUrl = url;
            }
            return new S3Utils(url, region, accessKeyId, secretAccessKey, bucket, baseFilePath, cdnUrl);
        }

    }

    private S3Utils(
        String url,
        String region,
        String accessKeyId,
        String secretAccessKey,
        String bucket,
        String baseFilePath,
        String cdnUrl) {
        this.url = url;
        this.cdnUrl = cdnUrl;
        this.baseFilePath = baseFilePath;
        this.bucket = bucket;
        this.secretAccessKey = secretAccessKey;
        this.accessKeyId = accessKeyId;
        this.region = region;
        this.client = S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
            .endpointOverride(URI.create(url))
            .region(Region.of(region))
            .build();
        this.preSigner = S3Presigner.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(url))
            .credentialsProvider(() -> AwsBasicCredentials.create(accessKeyId, secretAccessKey))
            .build();
    }

    public PreSign preSign(Consumer<Cfg> configurer) {
        Cfg cfg = new Cfg(this);
        configurer.accept(cfg);
        return cfg.preSign;
    }

    public String put(Blob blob) {
        try {
            String objectKey = getObjectKey(blob.name);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(this.bucket)
                .key(objectKey)
                .contentType(blob.contentType)
                .build();

            client.putObject(putObjectRequest, RequestBody.fromBytes(blob.getBlob()));
            return objectKey;
        } catch (Exception e) {
            throw new IllegalStateException("S3 Upload Error", e);
        }
    }

    public String putViaPreSign(Blob blob) {
        try {
            String objectKey = getObjectKey(blob.name);
            PreSign preSign = preSign(cfg -> cfg
                .expiration(Duration.ofMinutes(5))
                .key(blob.name)
                .put(put -> put
                    .contentType(blob.contentType)
                )
            );
            HttpUtils.HttpResult httpResult = HttpUtils.getSharedInstance().execute(configurer -> {
                configurer
                    .method(preSign.getMethod())
                    .url(preSign.getUrl());
                preSign.getHeaders().forEach((k, vals) -> vals
                    .forEach(v -> configurer.header(k, v)));
                configurer.body(body -> body.binary(binary -> binary.bytes(blob.getBlob())));
            });
            if (!httpResult.isOK()) {
                throw new IOException(httpResult.getStatusLine());
            }
            return objectKey;
        } catch (IOException e) {
            throw new IllegalStateException("S3 Upload Error", e);
        }
    }

    public Blob get(String key) {
        try {
            String
                objectKey = getObjectKey(key);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(this.bucket)
                .key(getObjectKey(key))
                .build();

            ResponseInputStream<GetObjectResponse> object = client.getObject(getObjectRequest);
            byte[] bytes = object.readAllBytes();
            GetObjectResponse response = object.response();
            String contentType = response.contentType();
            return Blob.of(objectKey, contentType, bytes);
        } catch (Exception e) {
            throw new IllegalStateException("S3 Get Object Error", e);
        }
    }

    private String getObjectKey(String objectKey) {
        objectKey = Strings.CS.removeStart(objectKey, "/");
        objectKey = Strings.CS.removeEnd(objectKey, "/");
        if (StringUtils.isBlank(baseFilePath)) {
            return objectKey;
        } else {
            return baseFilePath + '/' + objectKey;
        }
    }

    @Getter
    @ToString
    public static class PreSign {
        private String method;
        private String url;
        private String host;
        private String path;
        private Map<String, List<String>> headers;
        private String cdnUrl;
        private String payload;
        private LocalDateTime expiredAt;

        private PreSign() {
        }
    }

    public static class Cfg {
        S3Utils self;
        String bucket;
        /**
         * PreSign Expires
         */
        Duration expiration = Duration.ofMinutes(5);
        String key;
        PreSign preSign;

        Cfg(S3Utils self) {
            this.self = self;
            this.bucket = self.bucket;
        }

        public Cfg bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public Cfg expiration(Duration expiration) {
            this.expiration = expiration;
            return this;
        }

        public Cfg key(String key) {
            this.key = self.getObjectKey(key);
            return this;
        }

        public Cfg put(Consumer<Put> put) {
            Put _put = new Put();
            put.accept(_put);
            LocalDateTime now = LocalDateTime.now();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(this.expiration)
                .putObjectRequest(builder -> {
                    builder
                        .bucket(this.bucket)
                        .key(this.key);
                    if (MapUtils.isNotEmpty(_put.checksums)) {
                        _put.checksums.forEach((checksumAlgorithm, checksum) -> {
                            switch (checksumAlgorithm) {
                                case SHA1 -> builder.checksumSHA1(checksum);
                                case SHA256 -> builder.checksumSHA256(checksum);
                                case CRC32 -> builder.checksumCRC32(checksum);
                                case CRC32_C -> builder.checksumCRC32C(checksum);
                                case CRC64_NVME -> builder.checksumCRC64NVME(checksum);
                            }
                        });
                    }
                    if (MapUtils.isNotEmpty(_put.metadata)) {
                        builder.metadata(_put.metadata);
                    }
                    if (StringUtils.isNotBlank(_put.md5)) {
                        builder.contentMD5(_put.md5);
                    }
                    if (Objects.nonNull(_put.expires)) {
                        builder.expires(_put.expires);
                    }
                    if (StringUtils.isNotBlank(_put.contentType)) {
                        builder.contentType(_put.contentType);
                    }
                    if (StringUtils.isNotBlank(_put.contentDisposition)) {
                        builder.contentDisposition(_put.contentDisposition);
                    }
                })
                .build();
            this.preSign = new PreSign();
            PresignedPutObjectRequest presignedPutObjectRequest = self.preSigner.presignPutObject(presignRequest);
            URL pUrl = presignedPutObjectRequest.url();
            preSign.url = pUrl.toString();
            preSign.cdnUrl = self.cdnUrl;
            preSign.expiredAt = now.plus(this.expiration);
            preSign.method = "PUT";
            preSign.headers = presignedPutObjectRequest.signedHeaders();
            preSign.host = pUrl.getHost();
            preSign.path = pUrl.getPath();
            presignedPutObjectRequest.signedPayload().ifPresent(sdkBytes -> preSign.payload = sdkBytes.asUtf8String());
            return this;
        }

        public Cfg get() {
            LocalDateTime now = LocalDateTime.now();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(this.expiration)
                .getObjectRequest(builder -> builder
                    .bucket(this.bucket)
                    .key(this.key))
                .build();

            PresignedGetObjectRequest presignedGetObjectRequest = self.preSigner.presignGetObject(getObjectPresignRequest);
            URL pUrl = presignedGetObjectRequest.url();
            this.preSign = new PreSign();
            preSign.url = pUrl.toString();
            preSign.cdnUrl = self.cdnUrl;
            preSign.expiredAt = now.plus(this.expiration);
            preSign.method = "GET";
            preSign.headers = presignedGetObjectRequest.signedHeaders();
            preSign.host = pUrl.getHost();
            preSign.path = pUrl.getPath();
            presignedGetObjectRequest.signedPayload().ifPresent(sdkBytes -> preSign.payload = sdkBytes.asUtf8String());
            return this;
        }

        public Cfg createMultiPart(Consumer<CreateMultiPart> createMultiPart) {
            CreateMultiPart _createMultiPart = new CreateMultiPart();
            createMultiPart.accept(_createMultiPart);
            LocalDateTime now = LocalDateTime.now();

            PresignedCreateMultipartUploadRequest presignedCreateMultipartUploadRequest = self.preSigner.presignCreateMultipartUpload(builder -> builder
                .signatureDuration(this.expiration)
                .createMultipartUploadRequest(builder1 -> {
                    builder1
                        .bucket(this.bucket)
                        .key(this.key);
                    if (MapUtils.isNotEmpty(_createMultiPart.metadata)) {
                        builder1.metadata(_createMultiPart.metadata);
                    }
                    if (Objects.nonNull(_createMultiPart.expires)) {
                        builder1.expires(_createMultiPart.expires);
                    }
                    if (StringUtils.isNotBlank(_createMultiPart.contentType)) {
                        builder1.contentType(_createMultiPart.contentType);
                    }
                    if (StringUtils.isNotBlank(_createMultiPart.contentDisposition)) {
                        builder1.contentDisposition(_createMultiPart.contentDisposition);
                    }
                })
            );

            URL pUrl = presignedCreateMultipartUploadRequest.url();
            preSign.url = pUrl.toString();
            preSign.cdnUrl = self.cdnUrl;
            preSign.expiredAt = now.plus(this.expiration);
            preSign.method = "POST";
            preSign.headers = presignedCreateMultipartUploadRequest.signedHeaders();
            preSign.host = pUrl.getHost();
            preSign.path = pUrl.getPath();
            presignedCreateMultipartUploadRequest.signedPayload().ifPresent(sdkBytes -> preSign.payload = sdkBytes.asUtf8String());
            return this;
        }

        public Cfg uploadPart(Consumer<UploadPart> uploadPart) {
            UploadPart _uploadPart = new UploadPart();
            uploadPart.accept(_uploadPart);
            LocalDateTime now = LocalDateTime.now();
            PresignedUploadPartRequest presignedUploadPartRequest = self.preSigner.presignUploadPart(builder -> builder
                .signatureDuration(this.expiration)
                .uploadPartRequest(builder1 -> {
                    builder1
                        .bucket(this.bucket)
                        .key(this.key)
                        .uploadId(_uploadPart.uploadId)
                        .partNumber(_uploadPart.partNumber);
                    if (MapUtils.isNotEmpty(_uploadPart.checksums)) {
                        _uploadPart.checksums.forEach((checksumAlgorithm, checksum) -> {
                            switch (checksumAlgorithm) {
                                case SHA1 -> builder1.checksumSHA1(checksum);
                                case SHA256 -> builder1.checksumSHA256(checksum);
                                case CRC32 -> builder1.checksumCRC32(checksum);
                                case CRC32_C -> builder1.checksumCRC32C(checksum);
                                case CRC64_NVME -> builder1.checksumCRC64NVME(checksum);
                            }
                        });
                    }
                    if (StringUtils.isNotBlank(_uploadPart.md5)) {
                        builder1.contentMD5(_uploadPart.md5);
                    }
                }));
            URL pUrl = presignedUploadPartRequest.url();
            preSign.url = pUrl.toString();
            preSign.cdnUrl = self.cdnUrl;
            preSign.expiredAt = now.plus(this.expiration);
            preSign.method = "PUT";
            preSign.headers = presignedUploadPartRequest.signedHeaders();
            preSign.host = pUrl.getHost();
            preSign.path = pUrl.getPath();
            presignedUploadPartRequest.signedPayload().ifPresent(sdkBytes -> preSign.payload = sdkBytes.asUtf8String());
            return this;
        }

        public Cfg completeMultiPart(Consumer<CompleteMultiPart> completeMultiPart) {
            CompleteMultiPart _completeMultiPart = new CompleteMultiPart();
            completeMultiPart.accept(_completeMultiPart);
            LocalDateTime now = LocalDateTime.now();
            CompleteMultipartUploadPresignRequest completeMultipartUploadPresignRequest = CompleteMultipartUploadPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .completeMultipartUploadRequest(builder -> {
                    builder
                        .bucket(this.bucket)
                        .key(this.key)
                        .uploadId(_completeMultiPart.uploadId)
                    ;
                    builder.multipartUpload(builder1 -> builder1.parts(_completeMultiPart.eTags));
                    if (MapUtils.isNotEmpty(_completeMultiPart.checksums)) {
                        _completeMultiPart.checksums.forEach((checksumAlgorithm, checksum) -> {
                            switch (checksumAlgorithm) {
                                case SHA1 -> builder.checksumSHA1(checksum);
                                case SHA256 -> builder.checksumSHA256(checksum);
                                case CRC32 -> builder.checksumCRC32(checksum);
                                case CRC32_C -> builder.checksumCRC32C(checksum);
                                case CRC64_NVME -> builder.checksumCRC64NVME(checksum);
                            }
                        });
                    }
                })
                .build();
            PresignedCompleteMultipartUploadRequest presignedCompleteMultipartUploadRequest = self.preSigner.presignCompleteMultipartUpload(completeMultipartUploadPresignRequest);

            URL pUrl = presignedCompleteMultipartUploadRequest.url();
            preSign.url = pUrl.toString();
            preSign.cdnUrl = self.cdnUrl;
            preSign.expiredAt = now.plus(this.expiration);
            preSign.method = "PUT";
            preSign.headers = presignedCompleteMultipartUploadRequest.signedHeaders();
            preSign.host = pUrl.getHost();
            preSign.path = pUrl.getPath();
            presignedCompleteMultipartUploadRequest.signedPayload().ifPresent(sdkBytes -> preSign.payload = sdkBytes.asUtf8String());
            return this;
        }

    }

    public static class Put {
        private final Map<ChecksumAlgorithm, String> checksums = new HashMap<>();
        private final Map<String, String> metadata = new HashMap<>();
        private String md5;
        /**
         * Content Expires
         */
        private Instant expires;
        private String contentType;
        private String contentDisposition;

        public Put checksums(Map<ChecksumAlgorithm, String> checksums) {
            this.checksums.putAll(checksums);
            return this;
        }

        public Put crc32(String checksum) {
            this.checksums.put(ChecksumAlgorithm.CRC32, checksum);
            return this;
        }

        public Put crc32c(String checksum) {
            this.checksums.put(ChecksumAlgorithm.CRC32_C, checksum);
            return this;
        }

        public Put sha1(String checksum) {
            this.checksums.put(ChecksumAlgorithm.SHA1, checksum);
            return this;
        }

        public Put sha256(String checksum) {
            this.checksums.put(ChecksumAlgorithm.SHA256, checksum);
            return this;
        }

        public Put crc64nvme(String checksum) {
            this.checksums.put(ChecksumAlgorithm.CRC64_NVME, checksum);
            return this;
        }

        public Put metadata(Map<String, String> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        public Put metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public Put md5(String md5) {
            this.md5 = md5;
            return this;
        }

        public Put expires(LocalDateTime expires) {
            this.expires = expires.toInstant(DateTimeUtils.getSystemOffset());
            return this;
        }

        public Put expires(Instant expires) {
            this.expires = expires;
            return this;
        }

        public Put expires(Duration expires) {
            this.expires = Instant.now().plusMillis(expires.toMillis());
            return this;
        }

        public Put contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Put contentDisposition(String contentDisposition) {
            this.contentDisposition = contentDisposition;
            return this;
        }

    }

    public static class CreateMultiPart {
        private final Map<ChecksumAlgorithm, String> checksums = new HashMap<>();
        private final Map<String, String> metadata = new HashMap<>();
        private String md5;
        /**
         * Content Expires
         */
        private Instant expires;
        private String contentType;
        private String contentDisposition;

        public CreateMultiPart metadata(Map<String, String> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        public CreateMultiPart metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public CreateMultiPart expires(LocalDateTime expires) {
            this.expires = expires.toInstant(DateTimeUtils.getSystemOffset());
            return this;
        }

        public CreateMultiPart expires(Instant expires) {
            this.expires = expires;
            return this;
        }

        public CreateMultiPart expires(Duration expires) {
            this.expires = Instant.now().plusMillis(expires.toMillis());
            return this;
        }

        public CreateMultiPart contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public CreateMultiPart contentDisposition(String contentDisposition) {
            this.contentDisposition = contentDisposition;
            return this;
        }
    }

    public static class UploadPart {
        private final Map<ChecksumAlgorithm, String> checksums = new HashMap<>();
        private String md5;
        private String uploadId;
        private Integer partNumber;

        public UploadPart checksums(Map<ChecksumAlgorithm, String> checksums) {
            this.checksums.putAll(checksums);
            return this;
        }

        public UploadPart crc32(String checksum) {
            this.checksums.put(ChecksumAlgorithm.CRC32, checksum);
            return this;
        }

        public UploadPart crc32c(String checksum) {
            this.checksums.put(ChecksumAlgorithm.CRC32_C, checksum);
            return this;
        }

        public UploadPart sha1(String checksum) {
            this.checksums.put(ChecksumAlgorithm.SHA1, checksum);
            return this;
        }

        public UploadPart sha256(String checksum) {
            this.checksums.put(ChecksumAlgorithm.SHA256, checksum);
            return this;
        }

        public UploadPart crc64nvme(String checksum) {
            this.checksums.put(ChecksumAlgorithm.CRC64_NVME, checksum);
            return this;
        }

        public UploadPart md5(String md5) {
            this.md5 = md5;
            return this;
        }

        public UploadPart uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        public UploadPart partNumber(Integer partNumber) {
            this.partNumber = partNumber;
            return this;
        }
    }

    public static class CompleteMultiPart {
        private final Map<ChecksumAlgorithm, String> checksums = new HashMap<>();
        private final List<CompletedPart> eTags = new ArrayList<>();
        private String uploadId;

        public CompleteMultiPart checksums(Map<ChecksumAlgorithm, String> checksums) {
            this.checksums.putAll(checksums);
            return this;
        }

        public CompleteMultiPart crc32(String checksum) {
            this.checksums.put(ChecksumAlgorithm.CRC32, checksum);
            return this;
        }

        public CompleteMultiPart crc32c(String checksum) {
            this.checksums.put(ChecksumAlgorithm.CRC32_C, checksum);
            return this;
        }

        public CompleteMultiPart sha1(String checksum) {
            this.checksums.put(ChecksumAlgorithm.SHA1, checksum);
            return this;
        }

        public CompleteMultiPart sha256(String checksum) {
            this.checksums.put(ChecksumAlgorithm.SHA256, checksum);
            return this;
        }

        public CompleteMultiPart crc64nvme(String checksum) {
            this.checksums.put(ChecksumAlgorithm.CRC64_NVME, checksum);
            return this;
        }

        public CompleteMultiPart eTags(List<CompletedPart> eTags) {
            this.eTags.addAll(eTags);
            return this;
        }

        public CompleteMultiPart eTag(Integer number, String eTag) {
            CompletedPart part = CompletedPart.builder().partNumber(number).eTag(eTag).build();
            this.eTags.add(part);
            return this;
        }

        public CompleteMultiPart uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

    }

}
