package tn.esprit.pi.tbibi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.tbibi.entities.HealthGoal;
import tn.esprit.pi.tbibi.entities.User;
import tn.esprit.pi.tbibi.exceptions.ResourceNotFoundException;
import tn.esprit.pi.tbibi.repositories.HealthGoalRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthGoalServiceTest {

    @Mock
    private HealthGoalRepo goalRepo;

    private HealthGoalServiceImpl healthGoalService;

    @BeforeEach
    void setUp() {
        healthGoalService = new HealthGoalServiceImpl(goalRepo);
    }

    @Test
    void createHealthGoalShouldPersistGoal() {
        HealthGoal goal = goal(1L, "Hydration", LocalDate.of(2026, 3, 20), false, 1L);
        when(goalRepo.save(goal)).thenReturn(goal);

        HealthGoal saved = healthGoalService.createHealthGoal(goal);

        assertThat(saved).isSameAs(goal);
        verify(goalRepo).save(goal);
    }

    @Test
    void getGoalsByUserShouldSortByCreatedDateDescendingAndKeepNullDatesLast() {
        HealthGoal older = goal(1L, "Walk 20 minutes", LocalDate.of(2026, 3, 10), false, 1L);
        HealthGoal undated = goal(2L, "Book follow-up", null, false, 1L);
        HealthGoal newest = goal(3L, "Drink more water", LocalDate.of(2026, 3, 21), true, 1L);

        when(goalRepo.findByUserUserId(1L)).thenReturn(List.of(older, undated, newest));

        List<HealthGoal> goals = healthGoalService.getGoalsByUser(1L);

        assertThat(goals).extracting(HealthGoal::getId).containsExactly(3L, 1L, 2L);
    }

    @Test
    void updateGoalShouldReplaceEditableFieldsAndAssignedUser() {
        HealthGoal existingGoal = goal(5L, "Initial title", LocalDate.of(2026, 3, 11), false, 1L);
        HealthGoal updatePayload = goal(null, "Updated title", LocalDate.of(2026, 3, 22), true, 2L);

        when(goalRepo.findById(5L)).thenReturn(Optional.of(existingGoal));
        when(goalRepo.save(existingGoal)).thenReturn(existingGoal);

        HealthGoal updatedGoal = healthGoalService.updateGoal(5L, updatePayload);

        assertThat(updatedGoal.getGoalTitle()).isEqualTo("Updated title");
        assertThat(updatedGoal.getGoalDescription()).isEqualTo(updatePayload.getGoalDescription());
        assertThat(updatedGoal.getAchieved()).isTrue();
        assertThat(updatedGoal.getCreatedDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        assertThat(updatedGoal.getUser().getUserId()).isEqualTo(2L);
        verify(goalRepo).save(existingGoal);
    }

    @Test
    void updateGoalShouldThrowWhenGoalDoesNotExist() {
        when(goalRepo.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> healthGoalService.updateGoal(77L, new HealthGoal()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Health goal not found with id 77");
    }

    @Test
    void deleteGoalShouldThrowWhenGoalDoesNotExist() {
        when(goalRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> healthGoalService.deleteGoal(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Health goal not found with id 99");
    }

    @Test
    void deleteGoalShouldRemoveExistingGoal() {
        when(goalRepo.existsById(15L)).thenReturn(true);

        healthGoalService.deleteGoal(15L);

        verify(goalRepo).deleteById(15L);
    }

    private HealthGoal goal(Long id, String title, LocalDate createdDate, boolean achieved, Long userId) {
        HealthGoal goal = new HealthGoal();
        goal.setId(id);
        goal.setGoalTitle(title);
        goal.setGoalDescription(title + " description");
        goal.setAchieved(achieved);
        goal.setCreatedDate(createdDate);
        goal.setUser(user(userId));
        return goal;
    }

    private User user(Long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setName("User " + userId);
        user.setEmail("user" + userId + "@tbibi.tn");
        return user;
    }
}
