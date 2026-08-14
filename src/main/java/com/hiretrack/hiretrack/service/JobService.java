package com.hiretrack.hiretrack.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.hiretrack.hiretrack.dto.DashboardStatsDTO;
import com.hiretrack.hiretrack.dto.JobResponseDTO;
import com.hiretrack.hiretrack.entity.Job;

public interface JobService {

    Page<JobResponseDTO> getJobs(int page, int size);

    JobResponseDTO createJob(Job job);

    JobResponseDTO updateJob(Long id, Job job);

    JobResponseDTO closeJob(Long id);

    JobResponseDTO reopenJob(Long id);

    List<JobResponseDTO> getAllJobs();

    JobResponseDTO getJobById(Long id);

    void deleteJob(Long id);

    List<JobResponseDTO> getMyJobs();

    DashboardStatsDTO getRecruiterStats();

    DashboardStatsDTO getAdminStats();
}
