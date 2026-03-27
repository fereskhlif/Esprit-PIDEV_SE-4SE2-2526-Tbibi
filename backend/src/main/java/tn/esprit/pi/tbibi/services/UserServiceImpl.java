package tn.esprit.pi.tbibi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.tbibi.DTO.UserCreateDto;
import tn.esprit.pi.tbibi.DTO.UserDto;
import tn.esprit.pi.tbibi.entities.User;
import tn.esprit.pi.tbibi.exceptions.DuplicateResourceException;
import tn.esprit.pi.tbibi.exceptions.ResourceNotFoundException;
import tn.esprit.pi.tbibi.repositories.UserRepo;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toDto(findEntityById(id));
    }

    @Override
    public UserDto getUserByEmail(String email) {
        return userRepo.findByEmailIgnoreCase(email)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
    }

    @Override
    public UserDto createUser(UserCreateDto user) {
        if (userRepo.existsByEmailIgnoreCase(user.getEmail())) {
            throw new DuplicateResourceException("A user already exists with email " + user.getEmail());
        }

        User savedUser = userRepo.save(userMapper.toEntity(user));
        return userMapper.toDto(savedUser);
    }

    private User findEntityById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }
}
