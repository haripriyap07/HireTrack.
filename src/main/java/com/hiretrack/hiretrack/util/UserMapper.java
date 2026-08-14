package com.hiretrack.hiretrack.util;

import com.hiretrack.hiretrack.dto.UserResponseDTO;
import com.hiretrack.hiretrack.entity.Role;
import com.hiretrack.hiretrack.entity.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponseDTO toResponse(User user) {
        Role role = parseRole(user.getRole());
        String memberSince = user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate().toString() : null;
        UserResponseDTO dto = new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), role, memberSince);
        dto.setCompany(user.getCompany());
        dto.setPhone(user.getPhone());
        dto.setBio(user.getBio());
        return dto;
    }

    public static Role parseRole(String role) {
        if (role == null) {
            return Role.CANDIDATE;
        }
        String normalized = SecurityUtils.normalizeRole(role);
        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return Role.CANDIDATE;
        }
    }
}
