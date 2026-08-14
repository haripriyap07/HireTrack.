package com.hiretrack.hiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hiretrack.hiretrack.entity.Application;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    List<Application> findByJobId(Long jobId);

    List<Application> findByApplicantEmail(String applicantEmail);

    Optional<Application>
    findByApplicantEmailAndJobId(
            String applicantEmail,
            Long jobId
    );

    List<Application> findByJobIdIn(List<Long> jobIds);

    List<Application> findByResumeUrl(String resumeUrl);
}