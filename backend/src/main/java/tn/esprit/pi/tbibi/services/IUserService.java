package tn.esprit.pi.tbibi.services;

import tn.esprit.pi.tbibi.DTO.UserCreateDto;
import tn.esprit.pi.tbibi.DTO.UserDto;

import java.util.List;

public interface IUserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto getUserByEmail(String email);

    UserDto createUser(UserCreateDto user);
}
