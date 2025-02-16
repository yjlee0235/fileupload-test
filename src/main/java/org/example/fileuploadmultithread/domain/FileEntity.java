package org.example.fileuploadmultithread.domain;

import lombok.Builder;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Objects;

@Table(name = "file")
@ToString
public class FileEntity {

    @Id
    private Long id;

    @Column("original_url")
    private String originalUrl;

    @Column("uploaded_url")
    private String uploadedUrl;

    @Column("uploading_started_at")
    private LocalDateTime uploadingStartedAt;

    @Column("uploading_ended_at")
    private LocalDateTime uploadingEndedAt;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @Builder
    public FileEntity(String originalUrl, String uploadedUrl, LocalDateTime uploadingEndedAt, LocalDateTime uploadingStartedAt) {
        this.originalUrl = originalUrl;
        this.uploadedUrl = uploadedUrl;
        this.uploadingEndedAt = uploadingEndedAt;
        this.uploadingStartedAt = uploadingStartedAt;
    }

    public void startUploading() {
        this.uploadingStartedAt = LocalDateTime.now();
    }

    public void completeUploading() {
        this.uploadingEndedAt = LocalDateTime.now();
    }

    public void updateUploadedUrl(String uploadedUrl) {
        if (!StringUtils.hasText(uploadedUrl)) {
            return;
        }
        this.uploadedUrl = uploadedUrl;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getUploadingEndedAt() {
        return uploadingEndedAt;
    }

    public LocalDateTime getUploadingStartedAt() {
        return uploadingStartedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getRawOriginalUrl() {
        return originalUrl;
    }

    public String getRawUploadedUrl() {
        return uploadedUrl;
    }

    public URI getOriginalUri() {
        try {
            return new URI(originalUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public URI getUploadedUri() {
        try {
            return new URI(uploadedUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FileEntity that = (FileEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
