package org.example.fileuploadmultithread.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fileuploadmultithread.domain.FileEntity;
import org.example.fileuploadmultithread.domain.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileUploadService {

    private final FileRepository fileRepository;
    private final S3Service s3Service;
    private final ImageFileUploadProvider imageFileUploadProvider;

    public void uploadFileToS3(@Nonnull FileEntity file) {
        String fileName = UUID.randomUUID().toString();
        String s3Key = imageFileUploadProvider.getKey(fileName);

        log.info("[FILE_UPLOAD_TO_S3] original file url: {}, upload file s3 key: {}", file.getOriginalUri(), s3Key);

        file.startUploading();

        try {
            URLConnection connection = file.getOriginalUri().toURL().openConnection();

            try (InputStream inputStream = connection.getInputStream()) {
                int contentLength = connection.getContentLength();
                String contentType = connection.getContentType();

                s3Service.uploadFile(imageFileUploadProvider.getBucketName(), s3Key, inputStream, contentType, contentLength);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        file.updateUploadedUrl(imageFileUploadProvider.getFileUrl(s3Key));
        file.completeUploading();

        fileRepository.save(file);

        log.info("[FILE_UPLOAD_TO_S3] uploaded url: {}", file.getRawUploadedUrl());
    }
}
