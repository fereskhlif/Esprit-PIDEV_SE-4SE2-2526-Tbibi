package tn.esprit.pi.tbibi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.pi.tbibi.DTO.HealthGoalDto;
import tn.esprit.pi.tbibi.entities.HealthGoal;
import tn.esprit.pi.tbibi.entities.User;
import tn.esprit.pi.tbibi.exceptions.GlobalExceptionHandler;
import tn.esprit.pi.tbibi.repositories.UserRepo;
import tn.esprit.pi.tbibi.services.HealthGoalMapper;
import tn.esprit.pi.tbibi.services.IHealthGoalService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthGoalController.class)
@Import({GlobalExceptionHandler.class, HealthGoalMapper.class})
class HealthGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IHealthGoalService healthGoalService;

    @MockBean
    private UserRepo userRepo;

    @Test
    void createGoalShouldAttachUserAndReturnDto() throws Exception {
        User user = user(1L, "Amal Ben Salah");
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(healthGoalService.createHealthGoal(any(HealthGoal.class))).thenAnswer(invocation -> {
            HealthGoal savedGoal = invocation.getArgument(0);
            savedGoal.setId(10L);
            return savedGoal;
        });

        HealthGoalDto payload = new HealthGoalDto(
                null,
                "Stay hydrated",
                "Drink at least two liters of water every day",
                false,
                LocalDate.of(2026, 3, 20),
                1L
        );

        mockMvc.perform(post("/api/health-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.goalTitle").value("Stay hydrated"))
                .andExpect(jsonPath("$.userId").value(1));

        ArgumentCaptor<HealthGoal> goalCaptor = ArgumentCaptor.forClass(HealthGoal.class);
        verify(healthGoalService).createHealthGoal(goalCaptor.capture());
        assertThat(goalCaptor.getValue().getUser().getUserId()).isEqualTo(1L);
    }

    @Test
    void createGoalShouldRejectInvalidPayload() throws Exception {
        HealthGoalDto payload = new HealthGoalDto(
                null,
                "ab",
                "Long enough description",
                false,
                LocalDate.of(2026, 3, 20),
                1L
        );

        mockMvc.perform(post("/api/health-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("goalTitle: Goal title must contain between 3 and 120 characters"));

        verifyNoInteractions(healthGoalService, userRepo);
    }

    @Test
    void getGoalsByUserShouldReturnMappedGoals() throws Exception {
        when(healthGoalService.getGoalsByUser(1L)).thenReturn(List.of(goal(5L, "Sleep plan", 1L)));

        mockMvc.perform(get("/api/health-goals/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].goalTitle").value("Sleep plan"))
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void deleteGoalShouldDelegateToService() throws Exception {
        mockMvc.perform(delete("/api/health-goals/8"))
                .andExpect(status().isOk());

        verify(healthGoalService).deleteGoal(8L);
    }

    private HealthGoal goal(Long id, String title, Long userId) {
        HealthGoal goal = new HealthGoal();
        goal.setId(id);
        goal.setGoalTitle(title);
        goal.setGoalDescription(title + " description");
        goal.setAchieved(false);
        goal.setCreatedDate(LocalDate.of(2026, 3, 21));
        goal.setUser(user(userId, "User " + userId));
        return goal;
    }

    private User user(Long id, String name) {
        User user = new User();
        user.setUserId(id);
        user.setName(name);
        user.setEmail(name.toLowerCase().replace(" ", ".") + "@tbibi.tn");
        return user;
    }
}
