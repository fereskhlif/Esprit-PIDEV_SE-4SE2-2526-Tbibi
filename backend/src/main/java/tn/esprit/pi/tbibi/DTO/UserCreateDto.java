package tn.esprit.pi.tbibi.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 80, message = "Name must contain between 2 and 80 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 100, message = "Password must contain between 4 and 100 characters")
    private String password;

    @NotBlank(message = "Address is required")
    @Size(min = 4, max = 120, message = "Address must contain between 4 and 120 characters")
    private String adresse;
}
