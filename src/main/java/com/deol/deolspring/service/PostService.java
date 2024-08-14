package com.deol.deolspring.service;

import com.deol.deolspring.entity.Post;
import com.deol.deolspring.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        return postRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Post createPost(Post post, MultipartFile file) throws IOException {
        String key = s3Service.generateFileKey(file); // S3Service에서 키 생성
        s3Service.upload(file, key); // S3Service를 통한 파일 업로드
        post.setImageUrl(s3Service.getFileUrl(key)); // 파일 URL 설정
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long id, Post postDetails, MultipartFile file) throws IOException {
        Post post = getPostById(id);
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());

        if (file != null && !file.isEmpty()) {
            String key = s3Service.generateFileKey(file); // S3Service에서 키 생성
            s3Service.upload(file, key); // S3Service를 통한 파일 업로드
            post.setImageUrl(s3Service.getFileUrl(key)); // 파일 URL 설정
        }

        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = getPostById(id);
        if (post.getImageUrl() != null) {
            String key = s3Service.extractKeyFromUrl(post.getImageUrl()); // URL에서 키 추출
            s3Service.delete(key); // S3 파일 삭제
        }
        postRepository.deleteById(id);
    }
}
