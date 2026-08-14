package com.hiretrack.hiretrack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hiretrack.hiretrack.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    long countByRole(String role);
}
