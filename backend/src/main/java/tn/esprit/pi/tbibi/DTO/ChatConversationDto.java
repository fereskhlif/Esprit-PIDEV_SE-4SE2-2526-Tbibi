package tn.esprit.pi.tbibi.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationDto {

    private Long userId;
    private String name;
    private String email;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
