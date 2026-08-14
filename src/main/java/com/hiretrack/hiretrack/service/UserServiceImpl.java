package com.hiretrack.hiretrack.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hiretrack.hiretrack.dto.UserResponseDTO;
import com.hiretrack.hiretrack.entity.User;
import com.hiretrack.hiretrack.exception.DuplicateResourceException;
import com.hiretrack.hiretrack.exception.ForbiddenException;
import com.hiretrack.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.hiretrack.repository.UserRepository;
import com.hiretrack.hiretrack.util.SecurityUtils;
import com.hiretrack.hiretrack.util.UserMapper;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDTO registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already registered.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(normalizeStoredRole(user.getRole()));
        User saved = userRepository.save(user);
        return UserMapper.toResponse(saved);
    }

    @Override
    public UserResponseDTO createUser(User user) {
        return registerUser(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toResponse).toList();
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponseDTO getCurrentUserProfile() {
        String email = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponseDTO updateUser(Long id, User user) {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        User current = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        String currentRole = SecurityUtils.normalizeRole(current.getRole());
        if (!existingUser.getEmail().equals(currentEmail) && !"ADMIN".equals(currentRole)) {
            throw new ForbiddenException("You can only update your own profile.");
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            existingUser.setName(user.getName());
        }

        if (user.getEmail() != null && !user.getEmail().isBlank() && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new DuplicateResourceException("Email already registered.");
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getCompany() != null) {
            existingUser.setCompany(user.getCompany().trim());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone().trim());
        }
        if (user.getBio() != null) {
            existingUser.setBio(user.getBio().trim());
        }

        if (user.getRole() != null && "ADMIN".equals(currentRole)) {
            existingUser.setRole(normalizeStoredRole(user.getRole()));
        }

        return UserMapper.toResponse(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        userRepository.delete(existingUser);
    }

    private String normalizeStoredRole(String role) {
        return SecurityUtils.normalizeRole(role);
    }
}
