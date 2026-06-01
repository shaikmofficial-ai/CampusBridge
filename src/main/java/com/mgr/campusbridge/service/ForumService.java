package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.ForumPostRequest;
import com.mgr.campusbridge.entity.*;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumPostRepository postRepository;
    private final ForumCommentRepository commentRepository;
    private final ForumGroupRepository groupRepository;
    private final UserRepository userRepository;

    public List<ForumPost> getPublicPosts() {
        return postRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    public List<ForumGroup> getPrivateGroups() {
        return groupRepository.findByIsPrivateTrue();
    }

    public ForumPost createPost(String email, ForumPostRequest request) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ForumGroup group = null;
        if (request.getGroupId() != null) {
            group = groupRepository.findById(request.getGroupId()).orElse(null);
        }
        ForumPost post = ForumPost.builder()
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .isPublic(request.isPublic())
                .group(group)
                .build();
        author.setCommunityPoints(author.getCommunityPoints() + 10);
        userRepository.save(author);
        return postRepository.save(post);
    }

    public ForumComment addComment(String email, Long postId, String content) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        return commentRepository.save(ForumComment.builder()
                .post(post).author(author).content(content).build());
    }

    public ForumPost viewPost(Long postId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setViews(post.getViews() + 1);
        return postRepository.save(post);
    }
}