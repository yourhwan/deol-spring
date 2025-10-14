package com.deol.deolspring.controller;

import com.deol.deolspring.entity.Post;
import com.deol.deolspring.service.PostService;
import com.deol.deolspring.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/posts")
@Tag(name = "Post API", description = "게시물 관련 API")
public class PostController {

    private final PostService postService;
    private final S3Service s3Service;

    public PostController(PostService postService, S3Service s3Service) {
        this.postService = postService;
        this.s3Service = s3Service;
    }

    @GetMapping
    @Operation(summary = "모든 게시물 조회", description = "저장된 모든 게시물을 조회합니다.")
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "게시물 조회", description = "특정 ID에 해당하는 게시물을 조회합니다.")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping
    @Operation(summary = "게시물 생성", description = "새로운 게시물을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "게시물 생성 성공")
    public ResponseEntity<Post> createPost(@RequestPart("post") Post post, @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(postService.createPost(post, file));
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시물 수정", description = "특정 ID에 해당하는 게시물을 수정합니다.")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestPart("post") Post post, @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(postService.updatePost(id, post, file));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게시물 삭제", description = "특정 ID에 해당하는 게시물을 삭제합니다.")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/image/{key}")
    @Operation(summary = "게시물 이미지 삭제", description = "게시물에 연결된 S3 파일을 삭제합니다.")
    public ResponseEntity<String> deleteFile(@PathVariable Long id, @PathVariable String key) {
        s3Service.delete(key); // S3에서 파일 삭제
        return ResponseEntity.ok("File deleted successfully");
    }
}
