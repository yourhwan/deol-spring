package com.deol.deolspring.service;

import com.deol.deolspring.entity.Post;
import com.deol.deolspring.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final S3Service s3Service;

    public PostService(PostRepository postRepository, S3Service s3Service) {
        this.postRepository = postRepository;
        this.s3Service = s3Service;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Transactional
    public Post createPost(Post post, MultipartFile file) throws IOException {
        // 파일이 존재할 경우 S3에 업로드
        if (file != null && !file.isEmpty()) {
            String key = s3Service.generateFileKey(file); // S3에서 사용할 파일 키 생성
            String fileUrl = s3Service.upload(file); // S3에 파일 업로드 후 URL 반환
            post.setImageUrl(fileUrl); // 게시물의 이미지 URL 설정
        }
        return postRepository.save(post); // 게시물 저장
    }

    @Transactional
    public Post updatePost(Long id, Post postDetails, MultipartFile file) throws IOException {
        Post post = getPostById(id);
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());

        if (file != null && !file.isEmpty()) {
            String key = s3Service.generateFileKey(file); // S3에서 사용할 파일 키 생성
            String fileUrl = s3Service.upload(file); // S3에 파일 업로드 후 URL 반환
            post.setImageUrl(fileUrl); // 게시물의 이미지 URL 설정
        }

        return postRepository.save(post); // 업데이트된 게시물 저장
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = getPostById(id);
        if (post.getImageUrl() != null) {
            String key = s3Service.extractKeyFromUrl(post.getImageUrl()); // URL에서 키 추출
            s3Service.delete(key); // S3에서 파일 삭제
        }
        postRepository.deleteById(id); // 게시물 삭제
    }
}
