package com.deol.deolspring.service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(S3Client s3Client, @Value("${cloud.aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public String generateFileKey(MultipartFile file) {
        return UUID.randomUUID().toString() + "-" + Paths.get(file.getOriginalFilename()).getFileName();
    }

    public String getFileUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

    public String extractKeyFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    @Transactional
    public ResponseEntity<?> upload(MultipartFile multipartFile, String key) throws IOException {
        if (!MediaType.IMAGE_PNG.toString().equals(multipartFile.getContentType()) &&
                !MediaType.IMAGE_JPEG.toString().equals(multipartFile.getContentType())) {
            return ResponseEntity.badRequest().body("사진 파일만 업로드 가능합니다");
        }

        try (InputStream is = multipartFile.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(multipartFile.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(is, multipartFile.getSize()));
        }

        return ResponseEntity.accepted().build();
    }

    @Transactional
    public ResponseEntity<?> download(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            String contentType = s3Object.response().contentType();

            if (MediaType.IMAGE_PNG.toString().equals(contentType)) {
                return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(s3Object);
            }

            if (MediaType.IMAGE_JPEG.toString().equals(contentType)) {
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(s3Object);
            }

            return ResponseEntity.badRequest().body("사진 파일만 다운로드 가능합니다");
        } catch (NoSuchKeyException e) {
            return ResponseEntity.status(404).body("파일을 찾을 수 없습니다");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("파일 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> delete(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            return ResponseEntity.ok().body("파일이 성공적으로 삭제되었습니다");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
