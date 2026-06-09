package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.ForumGroupRequest;
import com.mgr.campusbridge.dto.request.ForumPostRequest;
import com.mgr.campusbridge.dto.response.ForumCommentResponse;
import com.mgr.campusbridge.entity.*;
import com.mgr.campusbridge.exception.UnauthorizedException;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumPostRepository postRepository;
    private final ForumCommentRepository commentRepository;
    private final ForumGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SkillAnalyticsService skillAnalyticsService;

    public List<ForumPost> getPublicPosts() {
        return postRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    public List<ForumGroup> getPrivateGroups() {
        return groupRepository.findByIsPrivateTrue();
    }

    public ForumPost createPost(String email, ForumPostRequest request) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        requireApproved(author);
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
        ForumPost saved = postRepository.save(post);
        // Trigger: new forum post -> record fresh PRI snapshot.
        skillAnalyticsService.recordSnapshot(author);
        return saved;
    }

    public ForumComment addComment(String email, Long postId, String content) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        requireApproved(author);
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        return commentRepository.save(ForumComment.builder()
                .post(post).author(author).content(content).build());
    }

    /** List comments on a post (oldest first). */
    public List<ForumCommentResponse> getComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream().map(ForumCommentResponse::from).toList();
    }

    /** Create a forum group ("private forum"). The creator becomes the first member. */
    public ForumGroup createGroup(String email, ForumGroupRequest request) {
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        requireApproved(creator);
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Group name is required");
        }
        ForumGroup group = ForumGroup.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .isPrivate(request.getIsPrivate() == null || request.getIsPrivate())
                .members(new ArrayList<>(List.of(creator)))
                .memberCount(1)
                .build();
        return groupRepository.save(group);
    }

    /** Posts belonging to a group (newest first). */
    public List<ForumPost> getGroupPosts(Long groupId) {
        return postRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
    }

    public ForumPost viewPost(Long postId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setViews(post.getViews() + 1);
        return postRepository.save(post);
    }

    /** Block users whose account is not yet approved by an admin (admins bypass). */
    private void requireApproved(User user) {
        if (user.getRole() == User.Role.ADMIN) return;
        if (user.getAccountStatus() != User.AccountStatus.APPROVED) {
            throw new UnauthorizedException(
                    "Your account is pending admin approval. You can post and comment once an admin approves you.");
        }
    }
}