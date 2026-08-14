package com.hiretrack.hiretrack.service;

import java.util.List;

import com.hiretrack.hiretrack.dto.ApplicationResponseDTO;
import com.hiretrack.hiretrack.dto.ApplicationStatusHistoryDTO;
import com.hiretrack.hiretrack.dto.DashboardStatsDTO;
import com.hiretrack.hiretrack.entity.Application;

public interface ApplicationService {

    ApplicationResponseDTO applyForJob(Application application);

    List<ApplicationResponseDTO> getAllApplications();

    List<ApplicationResponseDTO> getApplicationsByJob(Long jobId);

    List<ApplicationResponseDTO> getMyApplications();

    ApplicationResponseDTO getApplicationById(Long id);

    ApplicationResponseDTO updateStatus(Long id, String status);

    List<ApplicationStatusHistoryDTO> getStatusHistory(Long applicationId);

    DashboardStatsDTO getMyApplicationStats();
}
