package tn.esprit.pi.tbibi.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalChatUpdateDto {

    @NotBlank(message = "Message is required")
    @Size(min = 2, max = 1000, message = "Message must contain between 2 and 1000 characters")
    private String message;

    @NotNull(message = "Current user is required")
    @Positive(message = "Current user id must be positive")
    private Long currentUserId;
}
