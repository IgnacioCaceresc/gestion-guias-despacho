package com.transportista.guias.service;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

    private final String bucket;
    private final boolean enabled;
    private final S3Client s3Client;

    public S3StorageService(
            @Value("${app.s3.bucket}") String bucket,
            @Value("${app.s3.enabled:false}") boolean enabled,
            @Value("${app.aws.region:us-east-1}") String region
    ) {
        this.bucket = bucket;
        this.enabled = enabled;
        this.s3Client = S3Client.builder().region(Region.of(region)).build();
    }

    public String subir(Path archivo, String key) {
        if (!enabled) {
            return "s3://" + bucket + "/" + key + " (simulado)";
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/pdf")
                .build();

        s3Client.putObject(request, RequestBody.fromFile(archivo));
        return "s3://" + bucket + "/" + key;
    }
}
