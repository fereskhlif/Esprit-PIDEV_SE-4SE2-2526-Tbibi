package tn.esprit.pi.tbibi.services;


import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.tbibi.DTO.comment.CommentRequest;
import tn.esprit.pi.tbibi.DTO.comment.CommentResponse;
import tn.esprit.pi.tbibi.DTO.post.PostRequest;
import tn.esprit.pi.tbibi.DTO.post.PostResponse;
import tn.esprit.pi.tbibi.DTO.category.*;
import tn.esprit.pi.tbibi.DTO.vote.VoteRequest;
import tn.esprit.pi.tbibi.DTO.vote.VoteResponse;


import java.util.List;

public interface IForumService {

    // Category
    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getAllCategories();
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);

    // Post
    List<PostResponse> getAllPosts();
    PostResponse createPost(PostRequest request);
    PostResponse getPostById(Long id);
    List<PostResponse> getPostsByCategory(Long categoryId);
    List<PostResponse> getPostsByAuthor(Integer authorId);
    List<PostResponse> searchPosts(String keyword);
    PostResponse updatePost(Long id, PostRequest request);
    PostResponse updatePostStatus(Long id, String status);
    PostResponse togglePin(Long id);
    void deletePost(Long id);

    // Comment
    CommentResponse addComment(CommentRequest request);
    List<CommentResponse> getCommentsByPost(Long postId);
    CommentResponse updateComment(Long id, String newComment);
    void deleteComment(Long id);

    // Vote
    VoteResponse votePost(VoteRequest request);
    void unvotePost(Integer userId, Long postId);
    long getVoteCount(Long postId);
    boolean hasUserVoted(Integer userId, Long postId);
    PostResponse uploadPostMedia(Long postId, List<MultipartFile> files);
}