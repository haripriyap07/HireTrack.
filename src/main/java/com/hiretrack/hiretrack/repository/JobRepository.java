package com.hiretrack.hiretrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hiretrack.hiretrack.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByRecruiterEmail(String recruiterEmail);
}
