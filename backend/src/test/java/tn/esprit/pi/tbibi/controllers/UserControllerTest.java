package tn.esprit.pi.tbibi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.pi.tbibi.DTO.UserCreateDto;
import tn.esprit.pi.tbibi.DTO.UserDto;
import tn.esprit.pi.tbibi.exceptions.DuplicateResourceException;
import tn.esprit.pi.tbibi.exceptions.GlobalExceptionHandler;
import tn.esprit.pi.tbibi.services.IUserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IUserService userService;

    @Test
    void getUsersShouldReturnJsonList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserDto(1L, "Amal Ben Salah", "amal@tbibi.tn", "Tunis Centre"),
                new UserDto(2L, "Youssef Trabelsi", "youssef@tbibi.tn", "Sousse Medina")
        ));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Amal Ben Salah"))
                .andExpect(jsonPath("$[1].email").value("youssef@tbibi.tn"));
    }

    @Test
    void getUserByEmailShouldReturnSelectedUser() throws Exception {
        when(userService.getUserByEmail("amal@tbibi.tn"))
                .thenReturn(new UserDto(1L, "Amal Ben Salah", "amal@tbibi.tn", "Tunis Centre"));

        mockMvc.perform(get("/users/email/amal@tbibi.tn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.adresse").value("Tunis Centre"));
    }

    @Test
    void createUserShouldRejectInvalidPayload() throws Exception {
        UserCreateDto payload = new UserCreateDto("A", "amal@tbibi.tn", "secret", "Tunis Centre");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: Name must contain between 2 and 80 characters"));

        verifyNoInteractions(userService);
    }

    @Test
    void createUserShouldTranslateDuplicateEmailToConflict() throws Exception {
        UserCreateDto payload = new UserCreateDto("Amal Ben Salah", "amal@tbibi.tn", "secret", "Tunis Centre");

        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new DuplicateResourceException("A user already exists with email amal@tbibi.tn"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A user already exists with email amal@tbibi.tn"));
    }
}
