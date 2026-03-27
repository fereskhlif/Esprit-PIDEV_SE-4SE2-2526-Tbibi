package tn.esprit.pi.tbibi.services;


import org.springframework.stereotype.Component;
import tn.esprit.pi.tbibi.DTO.HealthGoalDto;
import tn.esprit.pi.tbibi.entities.HealthGoal;

@Component
public class HealthGoalMapper {

    public HealthGoalDto toDto(HealthGoal entity) {
        HealthGoalDto dto = new HealthGoalDto();

        dto.setId(entity.getId());
        dto.setGoalTitle(entity.getGoalTitle());
        dto.setGoalDescription(entity.getGoalDescription());
        dto.setAchieved(entity.getAchieved());
        dto.setCreatedDate(entity.getCreatedDate());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getUserId());
        }

        return dto;
    }

    public HealthGoal toEntity(HealthGoalDto dto) {
        HealthGoal entity = new HealthGoal();

        entity.setId(dto.getId());
        entity.setGoalTitle(dto.getGoalTitle());
        entity.setGoalDescription(dto.getGoalDescription());
        entity.setAchieved(dto.getAchieved());
        entity.setCreatedDate(dto.getCreatedDate());

        return entity;
    }
}