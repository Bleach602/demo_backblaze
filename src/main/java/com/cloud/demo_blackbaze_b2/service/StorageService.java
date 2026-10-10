package com.cloud.demo_blackbaze_b2.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;

    @Value("${backblaze.b2.bucket-name}")
    private String bucket;

    /** Upload et retourne la clé B2 générée. */
    public String upload(MultipartFile file, String folder) throws IOException {
        String original = file.getOriginalFilename() == null ? "fichier" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf("."))
                : "";
        String key = (folder == null || folder.isBlank() ? "documents" : folder)
                + "/" + UUID.randomUUID() + ext;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
        return key;
    }

    /** Récupère le contenu d'un fichier. */
    public byte[] download(String key) {
        ResponseBytes<GetObjectResponse> resp = s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(key).build()
        );
        return resp.asByteArray();
    }

    /** Supprime un fichier. */
    public void delete(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucket).key(key).build()
        );
    }

    /** Récupère le content-type d'un objet (utile pour servir inline). */
    public String getContentType(String key) {
        HeadObjectResponse head = s3Client.headObject(
                HeadObjectRequest.builder().bucket(bucket).key(key).build()
        );
        return head.contentType();
    }
}