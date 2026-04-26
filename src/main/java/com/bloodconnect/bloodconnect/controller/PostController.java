package com.bloodconnect.bloodconnect.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bloodconnect.bloodconnect.model.Post;
import com.bloodconnect.bloodconnect.model.PostType;
import com.bloodconnect.bloodconnect.service.PostService;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postService.getPostById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Post createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("type") PostType type,
            @RequestParam("authorName") String authorName,
            @RequestParam("authorRole") String authorRole,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setType(type);
        post.setAuthorName(authorName);
        post.setAuthorRole(authorRole);
        post.setCreatedAt(LocalDateTime.now());
        post.setLikesCount(0);

        return postService.createPostWithEmail(email, post, image);
    }

    @DeleteMapping("/{id}")
    public void deletePost(
            @PathVariable Long id,
            @RequestParam("userId") Long userId,
            @RequestParam("userRole") String userRole) {
        postService.deletePost(id, userId, userRole);
    }

    @PutMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public Post likePost(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return postService.likePostWithEmail(email, id);
    }
}
