package org.example.fileuploadmultithread.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

@Configuration
@RequiredArgsConstructor
public class AwsConfig {

    @Value("${aws.assume-role}")
    private String assumeRole;

    @Bean
    public StsClient stsClient() {
        return StsClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public S3Client s3Client() {
        StsAssumeRoleCredentialsProvider credentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                .stsClient(stsClient())
                .refreshRequest(() -> AssumeRoleRequest.builder()
                        .roleArn(assumeRole)
                        .roleSessionName("s3-file-upload-session")
                        .build()
                )
                .build();

        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
