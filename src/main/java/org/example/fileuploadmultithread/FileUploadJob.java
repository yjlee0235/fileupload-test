package org.example.fileuploadmultithread;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fileuploadmultithread.domain.FileEntity;
import org.example.fileuploadmultithread.domain.FileRepository;
import org.example.fileuploadmultithread.service.FileUploadService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadJob {

    private final FileRepository fileRepository;
    private final FileUploadService fileUploadService;

    /**
     * 100 files upload
     * execution time = ? second
     */
    @Scheduled(initialDelay = 10000, fixedDelay = 120000)
    public void uploadFilesWithSingleThread() {
        List<FileEntity> fileEntities = fileRepository.findAll();

        long startTimeMillis = System.currentTimeMillis();
        log.info("[UPLOAD_FILES_SINGLE_THREAD] start");

        fileEntities.forEach(fileUploadService::uploadFileToS3);

        long endTimeMillis = System.currentTimeMillis();
        log.info("[UPLOAD_FILES_SINGLE_THREAD] end. execution time = {} ms", endTimeMillis - startTimeMillis);
    }

    //    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void uploadFilesToS3WithThreadPool() {
        List<FileEntity> fileEntities = fileRepository.findAll();

        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            CountDownLatch countDownLatch = new CountDownLatch(fileEntities.size());

            long startTimeMillis = System.currentTimeMillis();
            log.info("[UPLOAD_FILES_THREAD_POOL] start");

            executorService.execute(() ->
                    fileEntities.forEach(file -> {
                        fileUploadService.uploadFileToS3(file);
                        countDownLatch.countDown();
                    })
            );

            countDownLatch.await();

            long endTimeMillis = System.currentTimeMillis();
            log.info("[UPLOAD_FILES_THREAD_POOL] end. execution time = {} ms", endTimeMillis - startTimeMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

//
//    public void uploadFilesToS3WithComputableFuture() {
//    }
//
//    public void uploadFilesToS3WithVirtualThreads() {
//    }
}
