package tn.esprit.pi.tbibi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.tbibi.DTO.UserCreateDto;
import tn.esprit.pi.tbibi.DTO.UserDto;
import tn.esprit.pi.tbibi.entities.User;
import tn.esprit.pi.tbibi.exceptions.DuplicateResourceException;
import tn.esprit.pi.tbibi.exceptions.ResourceNotFoundException;
import tn.esprit.pi.tbibi.repositories.UserRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepo userRepo;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepo, new UserMapper());
    }

    @Test
    void getAllUsersShouldReturnAlphabeticallySortedDtos() {
        User youssef = user(2L, "Youssef Trabelsi", "youssef@tbibi.tn", "Sousse");
        User amal = user(1L, "Amal Ben Salah", "amal@tbibi.tn", "Tunis");
        User mariem = user(3L, "Mariem Gharbi", "mariem@tbibi.tn", "Sfax");

        when(userRepo.findAll()).thenReturn(List.of(youssef, amal, mariem));

        List<UserDto> users = userService.getAllUsers();

        assertThat(users).extracting(UserDto::getName)
                .containsExactly("Amal Ben Salah", "Mariem Gharbi", "Youssef Trabelsi");
    }

    @Test
    void getUserByIdShouldThrowWhenUserDoesNotExist() {
        when(userRepo.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(44L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id 44");
    }

    @Test
    void getUserByEmailShouldReturnMappedDto() {
        User user = user(7L, "Amal Ben Salah", "amal@tbibi.tn", "Tunis Centre");
        when(userRepo.findByEmailIgnoreCase("amal@tbibi.tn")).thenReturn(Optional.of(user));

        UserDto result = userService.getUserByEmail("amal@tbibi.tn");

        assertThat(result.getUserId()).isEqualTo(7L);
        assertThat(result.getName()).isEqualTo("Amal Ben Salah");
        assertThat(result.getAdresse()).isEqualTo("Tunis Centre");
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {
        UserCreateDto payload = new UserCreateDto("Amal", "amal@tbibi.tn", "secret", "Tunis");
        when(userRepo.existsByEmailIgnoreCase("amal@tbibi.tn")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(payload))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("A user already exists with email amal@tbibi.tn");

        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void createUserShouldPersistMappedEntityAndReturnDto() {
        UserCreateDto payload = new UserCreateDto("Mariem Gharbi", "mariem@tbibi.tn", "pass1234", "Sfax");

        when(userRepo.existsByEmailIgnoreCase("mariem@tbibi.tn")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setUserId(11L);
            return savedUser;
        });

        UserDto result = userService.createUser(payload);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Mariem Gharbi");
        assertThat(savedUser.getEmail()).isEqualTo("mariem@tbibi.tn");
        assertThat(savedUser.getPassword()).isEqualTo("pass1234");
        assertThat(savedUser.getAdresse()).isEqualTo("Sfax");

        assertThat(result.getUserId()).isEqualTo(11L);
        assertThat(result.getEmail()).isEqualTo("mariem@tbibi.tn");
    }

    private User user(Long id, String name, String email, String address) {
        User user = new User();
        user.setUserId(id);
        user.setName(name);
        user.setEmail(email);
        user.setAdresse(address);
        user.setPassword("secret");
        return user;
    }
}
