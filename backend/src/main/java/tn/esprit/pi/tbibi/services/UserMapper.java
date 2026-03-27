package tn.esprit.pi.tbibi.services;

import org.springframework.stereotype.Component;
import tn.esprit.pi.tbibi.DTO.UserCreateDto;
import tn.esprit.pi.tbibi.DTO.UserDto;
import tn.esprit.pi.tbibi.entities.User;

@Component
public class UserMapper {

    public UserDto toDto(User entity) {
        return new UserDto(
                entity.getUserId(),
                entity.getName(),
                entity.getEmail(),
                entity.getAdresse()
        );
    }

    public User toEntity(UserCreateDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setAdresse(dto.getAdresse());
        return user;
    }
}
