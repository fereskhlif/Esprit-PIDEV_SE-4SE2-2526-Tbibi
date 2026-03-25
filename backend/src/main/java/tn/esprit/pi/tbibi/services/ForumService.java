package tn.esprit.pi.tbibi.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.tbibi.DTO.post.*;
import tn.esprit.pi.tbibi.DTO.comment.*;
import tn.esprit.pi.tbibi.DTO.category.*;
import tn.esprit.pi.tbibi.DTO.vote.*;
import tn.esprit.pi.tbibi.entities.*;
import tn.esprit.pi.tbibi.mappers.*;
import tn.esprit.pi.tbibi.repositories.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ForumService implements IForumService {

    CategoryRepository categoryRepo;
    PostRepository postRepo;
    CommentRepository commentRepo;
    VoteRepository voteRepo;
    UserRepo userRepo;
    CategoryMapper categoryMapper;
    PostMapper postMapper;
    CommentMapper commentMapper;
    VoteMapper voteMapper;

    @Autowired
    CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public PostResponse uploadPostMedia(Long postId, List<MultipartFile> files) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        List<String> urls = cloudinaryService.uploadForumMediaFiles(files);
        if (post.getMediaUrls() == null) post.setMediaUrls(new ArrayList<>());
        post.getMediaUrls().addAll(urls);
        return mapPostWithCounts(postRepo.save(post));
    }

    // ─── Helper: map post with counts ─────────────────────────────────────────
    private PostResponse mapPostWithCounts(Post post) {
        PostResponse dto = postMapper.toDto(post);
        dto.setCommentCount((int) commentRepo.countByPostAndDeletedFalse(post));
        dto.setVoteCount((int) voteRepo.countByPost(post));
        return dto;
    }

    // ─── Helper: map comment with replies ─────────────────────────────────────
    private CommentResponse mapCommentWithReplies(Comment comment) {
        CommentResponse dto = commentMapper.toDto(comment);
        List<CommentResponse> replies = commentRepo
                .findByParentCommentAndDeletedFalse(comment)
                .stream()
                .map(this::mapCommentWithReplies) // ← recursive call
                .collect(Collectors.toList());
        dto.setReplies(replies);
        return dto;
    }

    // ─── Category ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category(); // ← create manually, don't use mapper
        category.setCategoryName(request.getCategoryName());
        category.setCategoryDescription(request.getCategoryDescription());
        category.setCreatedAt(LocalDateTime.now());
        category.setActive(true); // ← set explicitly

        Category saved = categoryRepo.save(category);

        CategoryResponse dto = categoryMapper.toDto(saved);
        dto.setPostCount(0);
        return dto;
    }

    @Override
    @Transactional
    public List<CategoryResponse> getAllCategories() {
        return categoryRepo.findByActiveTrue()
                .stream()
                .map(c -> {
                    CategoryResponse dto = categoryMapper.toDto(c);
                    dto.setPostCount(c.getPosts() != null ? c.getPosts().size() : 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        category.setCategoryName(request.getCategoryName());
        category.setCategoryDescription(request.getCategoryDescription());
        CategoryResponse dto = categoryMapper.toDto(categoryRepo.save(category));
        dto.setPostCount(category.getPosts() != null ? category.getPosts().size() : 0);
        return dto;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        category.setActive(false); // soft disable
        categoryRepo.save(category);
    }

    // ─── Post ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<PostResponse> getAllPosts() {
        return postRepo.findByDeletedFalseOrderByPinnedDescCreatedDateDesc()
                .stream()
                .map(this::mapPostWithCounts)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostResponse createPost(PostRequest request) {
        User author = userRepo.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + request.getAuthorId()));
        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setMediaUrls(new ArrayList<>());
        post.setContent(request.getContent());
        post.setAuthor(author);
        post.setCategory(category);
        post.setCreatedDate(LocalDateTime.now());
        post.setUpdatedDate(LocalDateTime.now());
        post.setPostStatus(PostStatus.OPEN);
        post.setViews(0);
        post.setPinned(false);
        post.setDeleted(false);

        return mapPostWithCounts(postRepo.save(post));
    }

    @Override
    @Transactional
    public PostResponse getPostById(Long id) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        // increment views
        post.setViews(post.getViews() + 1);
        postRepo.save(post);
        return mapPostWithCounts(post);
    }

    @Override
    @Transactional
    public List<PostResponse> getPostsByCategory(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        return postRepo
                .findByCategoryAndDeletedFalseOrderByPinnedDescCreatedDateDesc(category)
                .stream()
                .map(this::mapPostWithCounts)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PostResponse> getPostsByAuthor(Integer authorId) {
        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + authorId));
        return postRepo.findByAuthorAndDeletedFalse(author)
                .stream()
                .map(this::mapPostWithCounts)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PostResponse> searchPosts(String keyword) {
        return postRepo.findByTitleContainingIgnoreCaseAndDeletedFalse(keyword)
                .stream()
                .map(this::mapPostWithCounts)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUpdatedDate(LocalDateTime.now());
        return mapPostWithCounts(postRepo.save(post));
    }

    @Override
    @Transactional
    public PostResponse updatePostStatus(Long id, String status) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        post.setPostStatus(PostStatus.valueOf(status));
        return mapPostWithCounts(postRepo.save(post));
    }

    @Override
    @Transactional
    public PostResponse togglePin(Long id) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        post.setPinned(!post.isPinned()); // toggle
        return mapPostWithCounts(postRepo.save(post));
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        post.setDeleted(true); // soft delete
        postRepo.save(post);
    }

    // ─── Comment ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CommentResponse addComment(CommentRequest request) {
        User author = userRepo.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + request.getAuthorId()));
        Post post = postRepo.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + request.getPostId()));

        Comment comment = new Comment();
        comment.setComment(request.getComment());
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setCommentDate(LocalDateTime.now());
        comment.setUpdatedDate(LocalDateTime.now());
        comment.setDeleted(false);

        // handle reply
        if (request.getParentCommentId() != null) {
            Comment parent = commentRepo.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + request.getParentCommentId()));
            // prevent reply to a reply — max 1 level

            comment.setParentComment(parent);
        }

        return mapCommentWithReplies(commentRepo.save(comment));
    }

    @Override
    @Transactional
    public List<CommentResponse> getCommentsByPost(Long postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return commentRepo.findByPostAndParentCommentIsNullAndDeletedFalse(post)
                .stream()
                .map(this::mapCommentWithReplies)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long id, String newComment) {
        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        comment.setComment(newComment);
        comment.setUpdatedDate(LocalDateTime.now());
        return mapCommentWithReplies(commentRepo.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        comment.setDeleted(true); // soft delete
        commentRepo.save(comment);
    }

    // ─── Vote ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VoteResponse votePost(VoteRequest request) {
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
        Post post = postRepo.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + request.getPostId()));

        // prevent double voting
        if (voteRepo.existsByUserAndPost(user, post)) {
            throw new RuntimeException("You already voted on this post.");
        }

        Vote vote = new Vote();
        vote.setUser(user);
        vote.setPost(post);
        vote.setCreatedAt(LocalDateTime.now());

        return voteMapper.toDto(voteRepo.save(vote));
    }

    @Override
    @Transactional
    public void unvotePost(Integer userId, Long postId) {
        if (userId == null || postId == null) return;
        userRepo.findById(userId).ifPresent(user -> {
            postRepo.findById(postId).ifPresent(post -> {
                voteRepo.findByUserAndPost(user, post).ifPresent(voteRepo::delete);
            });
        });
    }

    @Override
    public long getVoteCount(Long postId) {
        if (postId == null) return 0;
        return postRepo.findById(postId)
                .map(post -> voteRepo.countByPost(post))
                .orElse(0L);
    }

    @Override
    public boolean hasUserVoted(Integer userId, Long postId) {
        if (userId == null || postId == null) return false;
        return userRepo.findById(userId)
                .flatMap(user -> postRepo.findById(postId)
                        .map(post -> voteRepo.existsByUserAndPost(user, post)))
                .orElse(false);
    }
}