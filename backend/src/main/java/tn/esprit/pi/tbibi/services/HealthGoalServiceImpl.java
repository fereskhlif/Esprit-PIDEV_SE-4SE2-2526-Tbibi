package tn.esprit.pi.tbibi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.tbibi.entities.HealthGoal;
import tn.esprit.pi.tbibi.exceptions.ResourceNotFoundException;
import tn.esprit.pi.tbibi.repositories.HealthGoalRepo;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthGoalServiceImpl implements IHealthGoalService {

    private final HealthGoalRepo goalRepo;

    @Override
    public HealthGoal createHealthGoal(HealthGoal goal) {
        return goalRepo.save(goal);
    }

    @Override
    public List<HealthGoal> getGoalsByUser(Long userId) {
        return goalRepo.findByUserUserId(userId)
                .stream()
                .sorted(Comparator.comparing(HealthGoal::getCreatedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public HealthGoal updateGoal(Long id, HealthGoal goal) {
        HealthGoal existingGoal = goalRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health goal not found with id " + id));

        existingGoal.setGoalTitle(goal.getGoalTitle());
        existingGoal.setGoalDescription(goal.getGoalDescription());
        existingGoal.setAchieved(goal.getAchieved());
        existingGoal.setCreatedDate(goal.getCreatedDate());

        if (goal.getUser() != null) {
            existingGoal.setUser(goal.getUser());
        }

        return goalRepo.save(existingGoal);
    }

    @Override
    public void deleteGoal(Long id) {
        if (!goalRepo.existsById(id)) {
            throw new ResourceNotFoundException("Health goal not found with id " + id);
        }
        goalRepo.deleteById(id);
    }
}
