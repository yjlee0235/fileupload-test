package org.example.fileuploadmultithread;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fileuploadmultithread.domain.FileEntity;
import org.example.fileuploadmultithread.domain.FileRepository;
import org.example.fileuploadmultithread.service.FileUploadService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadJob {

    private final FileRepository fileRepository;
    private final FileUploadService fileUploadService;

    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void uploadFilesWithSingleThread() {
        long startTimeMillis = System.currentTimeMillis();
        log.info("[UPLOAD_FILES_SINGLE_THREAD] start");
        List<FileEntity> fileEntities = fileRepository.findAll();

        fileEntities.forEach(fileUploadService::uploadFileToS3);

        long endTimeMillis = System.currentTimeMillis();
        log.info("[UPLOAD_FILES_SINGLE_THREAD] end. execute time = {} ms", endTimeMillis - startTimeMillis);
    }

//    public void uploadFilesToS3WithThreadPool() {
//    }
//
//    public void uploadFilesToS3WithComputableFuture() {
//    }
//
//    public void uploadFilesToS3WithVirtualThreads() {
//    }
}
