package tn.esprit.pi.tbibi.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthGoalDto {

    private Long id;

    @NotBlank(message = "Goal title is required")
    @Size(min = 3, max = 120, message = "Goal title must contain between 3 and 120 characters")
    private String goalTitle;

    @NotBlank(message = "Goal description is required")
    @Size(min = 10, max = 500, message = "Goal description must contain between 10 and 500 characters")
    private String goalDescription;

    @NotNull(message = "Goal status is required")
    private Boolean achieved;

    @NotNull(message = "Created date is required")
    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDate createdDate;

    @NotNull(message = "User is required")
    @Positive(message = "User id must be positive")
    private Long userId;
}
