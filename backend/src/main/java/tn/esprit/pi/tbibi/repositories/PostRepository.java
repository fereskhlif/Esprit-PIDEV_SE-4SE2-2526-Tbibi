package tn.esprit.pi.tbibi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.tbibi.entities.Category;
import tn.esprit.pi.tbibi.entities.Post;
import tn.esprit.pi.tbibi.entities.PostStatus;
import tn.esprit.pi.tbibi.entities.User;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // get all posts — pinned first, newest after
    List<Post> findByDeletedFalseOrderByPinnedDescCreatedDateDesc();
    // get all posts in a category — pinned first, newest after
    List<Post> findByCategoryAndDeletedFalseOrderByPinnedDescCreatedDateDesc(Category category);
    // get posts by author
    List<Post> findByAuthorAndDeletedFalse(User author);
    // get posts by status
    List<Post> findByPostStatusAndDeletedFalse(PostStatus status);
    // search posts by title
    List<Post> findByTitleContainingIgnoreCaseAndDeletedFalse(String keyword);
}