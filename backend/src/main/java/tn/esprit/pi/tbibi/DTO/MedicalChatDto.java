package tn.esprit.pi.tbibi.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalChatDto {

    private Long id;

    @NotBlank(message = "Message is required")
    @Size(min = 2, max = 1000, message = "Message must contain between 2 and 1000 characters")
    private String message;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @NotNull(message = "Sender is required")
    @Positive(message = "Sender id must be positive")
    private Long senderId;

    private String senderName;
    private String senderEmail;

    @NotNull(message = "Receiver is required")
    @Positive(message = "Receiver id must be positive")
    private Long receiverId;

    private String receiverName;
    private String receiverEmail;
}
