package org.example.fileuploadmultithread;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fileuploadmultithread.domain.FileEntity;
import org.example.fileuploadmultithread.domain.FileRepository;
import org.example.fileuploadmultithread.service.FileUploadService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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
     * t3.micro(ec2) -> S3
     * execution time = 23.001 seconds
     */
//    @Scheduled(initialDelay = 10000, fixedDelay = 120000)
    public void uploadFilesWithSingleThread() {
        List<FileEntity> fileEntities = fileRepository.findAll();

        long startTimeMillis = System.currentTimeMillis();
        log.info("[UPLOAD_FILES_SINGLE_THREAD] start");

        fileEntities.forEach(fileUploadService::uploadFileToS3);

        long endTimeMillis = System.currentTimeMillis();
        log.info("[UPLOAD_FILES_SINGLE_THREAD] end. execution time = {} ms", endTimeMillis - startTimeMillis);
    }

    /**
     * 100 files upload
     * t3.micro(ec2) -> S3
     * execution time
     * 4 threads = 8.237 seconds
     * 10 threads = 3.866 seconds
     * 30 threads = 6.028 seconds
     */
//    @Scheduled(initialDelay = 10000, fixedDelay = 120000)
    public void uploadFilesToS3WithThreadPool() {
        List<FileEntity> fileEntities = fileRepository.findAll();

        try (ExecutorService executorService = Executors.newFixedThreadPool(30)) {
            CountDownLatch countDownLatch = new CountDownLatch(fileEntities.size());

            long startTimeMillis = System.currentTimeMillis();
            log.info("[UPLOAD_FILES_THREAD_POOL] start");

            fileEntities.forEach(file ->
                    executorService.execute(() -> {
                                fileUploadService.uploadFileToS3(file);
                                countDownLatch.countDown();
                            }
                    )
            );

            countDownLatch.await();

            long endTimeMillis = System.currentTimeMillis();
            log.info("[UPLOAD_FILES_THREAD_POOL] end. execution time = {} ms", endTimeMillis - startTimeMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 100 files upload
     * t3.micro(ec2) -> S3
     * execution time
     * 4 threads = ? seconds
     * 10 threads = ? seconds
     * 30 threads = ? seconds
     */
//    @Scheduled(initialDelay = 10000, fixedDelay = 120000)
    public void uploadFilesToS3WithComputableFuture() {
        List<FileEntity> fileEntities = fileRepository.findAll();

        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            long startTimeMillis = System.currentTimeMillis();
            log.info("[UPLOAD_FILES_THREAD_POOL] start");

            List<CompletableFuture<Void>> completableFutures = fileEntities.stream()
                    .map(file -> CompletableFuture.runAsync(() -> fileUploadService.uploadFileToS3(file), executorService))
                    .toList();

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new));

            allFutures.join();

            long endTimeMillis = System.currentTimeMillis();
            log.info("[UPLOAD_FILES_THREAD_POOL] end. execution time = {} ms", endTimeMillis - startTimeMillis);
        }
    }

    /**
     * 100 files upload
     * t3.micro(ec2) -> S3
     * execution time = ? seconds
     */
    @Scheduled(initialDelay = 10000, fixedDelay = 120000)
    public void uploadFilesToS3WithVirtualThreads() {
        List<FileEntity> fileEntities = fileRepository.findAll();

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch countDownLatch = new CountDownLatch(fileEntities.size());

            long startTimeMillis = System.currentTimeMillis();
            log.info("[UPLOAD_FILES_THREAD_POOL] start");

            fileEntities.forEach(file ->
                    executorService.execute(() -> {
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
}
