package org.example.fileuploadmultithread.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    public void uploadFile(@Nonnull String bucketName,
                           @Nonnull String key,
                           @Nonnull InputStream inputStream,
                           @Nonnull String contentType,
                           long contentLength) {

        long startTimeMillis = System.currentTimeMillis();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        RequestBody requestBody = RequestBody.fromInputStream(inputStream, contentLength);

        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, requestBody);
        logPutObjectResult(bucketName, key, putObjectResponse, startTimeMillis);
    }

    public void uploadFile(String bucketName, String key, MultipartFile file) throws IOException {
        long startTimeMillis = System.currentTimeMillis();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        RequestBody requestBody = RequestBody.fromBytes(file.getBytes());

        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, requestBody);
        logPutObjectResult(bucketName, key, putObjectResponse, startTimeMillis);
    }

    private void logPutObjectResult(String bucketName, String key, PutObjectResponse putObjectResponse, long startTimeMillis) {
        long endTimeMillis = System.currentTimeMillis();

        if (putObjectResponse.sdkHttpResponse().isSuccessful()) {
            log.info("[S3_PUT_OBJECT] S3 putObject success. Bucket: {}, Key: {}, ETag: {}, VersionId: {}, RequestId: {}, total time: {} ms",
                    bucketName,
                    key,
                    putObjectResponse.eTag(),
                    putObjectResponse.versionId(),
                    putObjectResponse.responseMetadata().requestId(),
                    endTimeMillis - startTimeMillis
            );
            return;
        }

        int statusCode = putObjectResponse.sdkHttpResponse().statusCode();
        String statusText = putObjectResponse.sdkHttpResponse().statusText().orElse("");
        String awsRequestId = putObjectResponse.responseMetadata().requestId();

        log.error("[S3_PUT_OBJECT] S3 putObject failed. Bucket: {}, Key: {}, StatusCode: {}, StatusText: {}, RequestId: {}, total titme: {} ms",
                bucketName,
                key,
                statusCode,
                statusText,
                awsRequestId,
                endTimeMillis - startTimeMillis
        );
    }
}
