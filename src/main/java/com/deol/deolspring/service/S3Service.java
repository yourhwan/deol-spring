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

    // 파일 키를 생성하는 메서드
    public String generateFileKey(MultipartFile file) {
        return UUID.randomUUID().toString() + "-" + Paths.get(file.getOriginalFilename()).getFileName();
    }

    // S3 URL을 생성하는 메서드
    public String getFileUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

    // URL에서 키를 추출하는 메서드
    public String extractKeyFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    // 파일을 S3에 업로드하는 메서드
    @Transactional
    public String upload(MultipartFile multipartFile) throws IOException {
        String contentType = multipartFile.getContentType();
        System.out.println("contentType: "+ contentType);
        if (!MediaType.IMAGE_PNG.toString().equals(contentType) &&
                !MediaType.IMAGE_JPEG.toString().equals(contentType) &&
                !"image/webp".equals(contentType) &&
                !"audio/mpeg".equals(contentType)) { // MP3 오디오 파일 검증
            throw new IOException("지원하지 않는 파일 형식입니다. 이미지 또는 오디오 파일만 업로드 가능합니다.");
        }

        String key = generateFileKey(multipartFile); // 파일 키 생성
        try (InputStream is = multipartFile.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(is, multipartFile.getSize()));
        }
        return getFileUrl(key); // 업로드된 파일의 S3 URL 반환
    }

    // S3에서 파일을 다운로드하는 메서드
    @Transactional
    public ResponseEntity<?> download(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            String contentType = s3Object.response().contentType();

            // PNG 또는 JPEG 이미지인 경우 처리
            if (MediaType.IMAGE_PNG.toString().equals(contentType)) {
                return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(s3Object);
            }

            if (MediaType.IMAGE_JPEG.toString().equals(contentType)) {
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(s3Object);
            }

            // 오디오 파일인 경우 처리
            if ("audio/mpeg".equals(contentType)) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(s3Object);
            }

            return ResponseEntity.badRequest().body("지원하지 않는 파일 형식입니다.");
        } catch (NoSuchKeyException e) {
            return ResponseEntity.status(404).body("파일을 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("파일 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // S3에서 파일을 삭제하는 메서드
    @Transactional
    public ResponseEntity<?> delete(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            return ResponseEntity.ok().body("파일이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}