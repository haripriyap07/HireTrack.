package com.hiretrack.hiretrack.dto;

import com.hiretrack.hiretrack.entity.Role;

public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private String memberSince;
    private String company;
    private String phone;
    private String bio;

    public UserResponseDTO() {}

    public UserResponseDTO(Long id, String name, String email, Role role, String memberSince) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.memberSince = memberSince;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }
}
