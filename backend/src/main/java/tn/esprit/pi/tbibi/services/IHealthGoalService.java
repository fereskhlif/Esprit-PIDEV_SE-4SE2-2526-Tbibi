package tn.esprit.pi.tbibi.services;

import tn.esprit.pi.tbibi.entities.HealthGoal;

import java.util.List;

public interface IHealthGoalService {

    HealthGoal createHealthGoal(HealthGoal goal);

    List<HealthGoal> getGoalsByUser(Long userId);

    HealthGoal updateGoal(Long id, HealthGoal goal);

    void deleteGoal(Long id);
}
