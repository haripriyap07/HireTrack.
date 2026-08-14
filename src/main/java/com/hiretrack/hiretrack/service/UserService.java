package com.hiretrack.hiretrack.service;

import java.util.List;

import com.hiretrack.hiretrack.dto.UserResponseDTO;
import com.hiretrack.hiretrack.entity.User;

public interface UserService {

    UserResponseDTO createUser(User user);

    UserResponseDTO registerUser(User user);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long id);

    UserResponseDTO getCurrentUserProfile();

    UserResponseDTO updateUser(Long id, User user);

    void deleteUser(Long id);
}
