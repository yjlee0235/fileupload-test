package org.example.fileuploadmultithread.domain;

import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface FileRepository extends ListCrudRepository<FileEntity, Long> {

    List<FileEntity> findByUploadedUrlIsNull();
}
