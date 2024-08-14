package com.deol.deolspring.controller;

import com.deol.deolspring.entity.Post;
import com.deol.deolspring.service.PostService;
import com.deol.deolspring.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final S3Service s3Service;

    public PostController(PostService postService, S3Service s3Service) {
        this.postService = postService;
        this.s3Service = s3Service;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestPart("post") Post post, @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(postService.createPost(post, file));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestPart("post") Post post, @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(postService.updatePost(id, post, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<String> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String key = s3Service.generateFileKey(file); // 파일 키 생성
        s3Service.upload(file, key); // S3에 파일 업로드
        String fileUrl = s3Service.getFileUrl(key);
        // 게시물의 이미지 URL을 업데이트하거나 반환할 수 있습니다.
        return ResponseEntity.ok(fileUrl);
    }

    @DeleteMapping("/{id}/image/{key}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id, @PathVariable String key) {
        s3Service.delete(key); // S3에서 파일 삭제
        // 게시물의 이미지 URL을 업데이트하거나 삭제할 수 있습니다.
        return ResponseEntity.ok("File deleted successfully");
    }
}
