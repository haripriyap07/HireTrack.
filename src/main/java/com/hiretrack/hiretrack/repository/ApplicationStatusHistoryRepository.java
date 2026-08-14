package com.hiretrack.hiretrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hiretrack.hiretrack.entity.ApplicationStatusHistory;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {

    List<ApplicationStatusHistory> findByApplicationIdOrderByChangedAtAsc(Long applicationId);
}
