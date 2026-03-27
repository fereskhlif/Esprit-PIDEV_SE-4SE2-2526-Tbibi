package tn.esprit.pi.tbibi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.tbibi.DTO.HealthGoalDto;
import tn.esprit.pi.tbibi.entities.HealthGoal;
import tn.esprit.pi.tbibi.entities.User;
import tn.esprit.pi.tbibi.exceptions.ResourceNotFoundException;
import tn.esprit.pi.tbibi.repositories.UserRepo;
import tn.esprit.pi.tbibi.services.HealthGoalMapper;
import tn.esprit.pi.tbibi.services.IHealthGoalService;

import java.util.List;

@RestController
@RequestMapping("/api/health-goals")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class HealthGoalController {

    private final IHealthGoalService service;
    private final HealthGoalMapper mapper;
    private final UserRepo userRepo;

    @PostMapping
    public HealthGoalDto createGoal(@Valid @RequestBody HealthGoalDto dto) {
        return mapper.toDto(service.createHealthGoal(attachUser(dto)));
    }

    @PutMapping("/{id}")
    public HealthGoalDto updateGoal(@PathVariable Long id, @Valid @RequestBody HealthGoalDto dto) {
        return mapper.toDto(service.updateGoal(id, attachUser(dto)));
    }

    @GetMapping("/user/{userId}")
    public List<HealthGoalDto> getGoalsByUser(@PathVariable Long userId) {
        return service.getGoalsByUser(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void deleteGoal(@PathVariable Long id) {
        service.deleteGoal(id);
    }

    private HealthGoal attachUser(HealthGoalDto dto) {
        HealthGoal goal = mapper.toEntity(dto);

        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + dto.getUserId()));
        goal.setUser(user);

        return goal;
    }
}
