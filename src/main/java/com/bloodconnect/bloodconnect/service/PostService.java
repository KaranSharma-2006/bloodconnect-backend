package com.bloodconnect.bloodconnect.service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.bloodconnect.bloodconnect.model.Post;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.PostRepository;
import com.bloodconnect.bloodconnect.repository.UserRepository;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;
    private final RateLimitingService rateLimitingService;
    private final UserRepository userRepository;
    private static final Logger logger = Logger.getLogger(PostService.class.getName());

    public PostService(PostRepository postRepository, 
                       FileStorageService fileStorageService, 
                       RateLimitingService rateLimitingService,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.fileStorageService = fileStorageService;
        this.rateLimitingService = rateLimitingService;
        this.userRepository = userRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post createPostWithEmail(String email, Post post, MultipartFile image) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            post.setAuthorId(user.getId());
            logger.info("Creating post for user: " + email + " with ID: " + user.getId());
            return createPost(post, image);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error creating post for user " + email + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create post: " + e.getMessage());
        }
    }

    public Post createPost(Post post, MultipartFile image) {
        try {
            if (!rateLimitingService.isAllowed("POST_CREATE_" + post.getAuthorId())) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded. Please wait before posting again.");
            }
            if (image != null && !image.isEmpty()) {
                String imageUrl = fileStorageService.storeFile(image);
                post.setImageUrl(imageUrl);
            }
            Post savedPost = postRepository.save(post);
            logger.info("Post created successfully with ID: " + savedPost.getId());
            return savedPost;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error saving post: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save post: " + e.getMessage());
        }
    }

    public void deletePost(Long postId, Long userId, String userRole) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if ("ROLE_ADMIN".equals(userRole) || post.getAuthorId().equals(userId)) {
            postRepository.delete(post);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this post");
        }
    }

    public Post likePostWithEmail(String email, Long postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return likePost(postId, user.getId());
    }

    public Post likePost(Long postId, Long userId) {
        if (!rateLimitingService.isAllowed("POST_LIKE_" + userId)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please wait before liking/unliking again.");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        
        if (post.getLikedUserIds().contains(userId)) {
            post.getLikedUserIds().remove(userId);
        } else {
            post.getLikedUserIds().add(userId);
        }
        
        post.setLikesCount(post.getLikedUserIds().size());
        return postRepository.save(post);
    }
}
