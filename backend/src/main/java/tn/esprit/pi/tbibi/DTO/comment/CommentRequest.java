package tn.esprit.pi.tbibi.DTO.comment;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentRequest {
    String comment;
    Integer authorId;
    Long postId;
    Long parentCommentId; // null if top level
}