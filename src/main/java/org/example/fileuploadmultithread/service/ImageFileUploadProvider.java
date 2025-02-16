package org.example.fileuploadmultithread.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Utilities;

@Component
public class ImageFileUploadProvider {

    private static final String KEY_PREFIX = "images/";

    private String bucketName;
    private S3Utilities s3Utilities;

    public ImageFileUploadProvider(@Value("${aws.s3.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.s3Utilities = S3Utilities.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }

    public String getKey(String fileName) {
        return KEY_PREFIX + fileName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getFileUrl(String key) {
        return s3Utilities.getUrl(builder -> builder.bucket(bucketName).key(key))
                .toString();
    }
}
