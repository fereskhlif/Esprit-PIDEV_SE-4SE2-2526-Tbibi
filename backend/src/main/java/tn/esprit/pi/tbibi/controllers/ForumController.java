package tn.esprit.pi.tbibi.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.tbibi.DTO.post.*;
import tn.esprit.pi.tbibi.DTO.category.*;
import tn.esprit.pi.tbibi.DTO.comment.*;
import tn.esprit.pi.tbibi.DTO.vote.*;
import tn.esprit.pi.tbibi.services.IForumService;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class ForumController {

    IForumService forumService;

    // ─── Category ─────────────────────────────────────────────────────────────

    @PostMapping("/categories")                          // ← CREATE category
    public CategoryResponse createCategory(@RequestBody CategoryRequest request) {
        return forumService.createCategory(request);
    }

    @GetMapping("/categories")                           // ← GET all categories
    public List<CategoryResponse> getAllCategories() {
        return forumService.getAllCategories();
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(@PathVariable("id") Long id, @RequestBody CategoryRequest request) {
        return forumService.updateCategory(id, request);
    }

    @DeleteMapping("/categories/{id}")
    public void deleteCategory(@PathVariable("id") Long id) {
        forumService.deleteCategory(id);
    }

// ─── Post ─────────────────────────────────────────────────────────────────

    @GetMapping("/posts")
    public List<PostResponse> getAllPosts() {
        return forumService.getAllPosts();
    }

    @PostMapping("/posts")                               // ← CREATE post
    public PostResponse createPost(@RequestBody PostRequest request) {
        return forumService.createPost(request);
    }

    @GetMapping("/posts/{id}")
    public PostResponse getPostById(@PathVariable("id") Long id) {
        return forumService.getPostById(id);
    }

    @GetMapping("/posts/category/{categoryId}")
    public List<PostResponse> getPostsByCategory(@PathVariable("categoryId") Long categoryId) {
        return forumService.getPostsByCategory(categoryId);
    }

    @GetMapping("/posts/author/{authorId}")
    public List<PostResponse> getPostsByAuthor(@PathVariable("authorId") Integer authorId) {
        return forumService.getPostsByAuthor(authorId);
    }

    @GetMapping("/posts/search")
    public List<PostResponse> searchPosts(@RequestParam("keyword") String keyword) {
        return forumService.searchPosts(keyword);
    }

    @PutMapping("/posts/{id}")
    public PostResponse updatePost(@PathVariable("id") Long id, @RequestBody PostRequest request) {
        return forumService.updatePost(id, request);
    }

    @PutMapping("/posts/{id}/status")
    public PostResponse updatePostStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
        return forumService.updatePostStatus(id, status);
    }

    @PutMapping("/posts/{id}/pin")
    public PostResponse togglePin(@PathVariable("id") Long id) {
        return forumService.togglePin(id);
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(@PathVariable("id") Long id) {
        forumService.deletePost(id);
    }

// ─── Comment ──────────────────────────────────────────────────────────────

    @PostMapping("/comments")                            // ← ADD comment
    public CommentResponse addComment(@RequestBody CommentRequest request) {
        return forumService.addComment(request);
    }


    @GetMapping("/comments/post/{postId}")
    public List<CommentResponse> getCommentsByPost(@PathVariable("postId") Long postId) {
        return forumService.getCommentsByPost(postId);
    }

    @PutMapping("/comments/{id}")
    public CommentResponse updateComment(@PathVariable("id") Long id, @RequestParam("comment") String comment) {
        return forumService.updateComment(id, comment);
    }

    @DeleteMapping("/comments/{id}")
    public void deleteComment(@PathVariable("id") Long id) {
        forumService.deleteComment(id);
    }

// ─── Vote ─────────────────────────────────────────────────────────────────

    @PostMapping("/votes")                               // ← VOTE on post
    public VoteResponse votePost(@RequestBody VoteRequest request) {
        return forumService.votePost(request);
    }

    @DeleteMapping("/votes")
    public void unvotePost(@RequestParam("userId") Integer userId, @RequestParam("postId") Long postId) {
        forumService.unvotePost(userId, postId);
    }

    @GetMapping("/votes/count/{postId}")
    public long getVoteCount(@PathVariable("postId") Long postId) {
        return forumService.getVoteCount(postId);
    }

    @GetMapping("/votes/check")
    public boolean hasUserVoted(@RequestParam("userId") Integer userId, @RequestParam("postId") Long postId) {
        return forumService.hasUserVoted(userId, postId);
    }

    @PostMapping(value = "/posts/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostResponse uploadPostMedia(
            @PathVariable("id") Long postId,
            @RequestParam("files") List<MultipartFile> files) {
        return forumService.uploadPostMedia(postId, files);
    }
}