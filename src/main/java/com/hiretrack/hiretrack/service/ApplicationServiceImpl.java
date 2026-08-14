package com.hiretrack.hiretrack.service;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hiretrack.hiretrack.dto.ApplicationResponseDTO;
import com.hiretrack.hiretrack.dto.ApplicationStatusHistoryDTO;
import com.hiretrack.hiretrack.dto.DashboardStatsDTO;
import com.hiretrack.hiretrack.entity.Application;
import com.hiretrack.hiretrack.entity.ApplicationStatus;
import com.hiretrack.hiretrack.entity.ApplicationStatusHistory;
import com.hiretrack.hiretrack.entity.Job;
import com.hiretrack.hiretrack.entity.User;
import com.hiretrack.hiretrack.exception.DuplicateResourceException;
import com.hiretrack.hiretrack.exception.ForbiddenException;
import com.hiretrack.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.hiretrack.repository.ApplicationRepository;
import com.hiretrack.hiretrack.repository.ApplicationStatusHistoryRepository;
import com.hiretrack.hiretrack.repository.JobRepository;
import com.hiretrack.hiretrack.repository.UserRepository;
import com.hiretrack.hiretrack.util.SecurityUtils;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeMatchingService resumeMatchingService;
    private final ResumeStorageService resumeStorageService;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  ApplicationStatusHistoryRepository historyRepository,
                                  JobRepository jobRepository,
                                  UserRepository userRepository,
                                  ResumeMatchingService resumeMatchingService,
                                  ResumeStorageService resumeStorageService) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.resumeMatchingService = resumeMatchingService;
        this.resumeStorageService = resumeStorageService;
    }

    @Override
    @Transactional
    public ApplicationResponseDTO applyForJob(Application application) {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        if (currentEmail == null) {
            throw new ForbiddenException("Authentication required.");
        }

        application.setApplicantEmail(currentEmail);
        if (application.getJobId() == null) {
            throw new IllegalArgumentException("Job id is required.");
        }

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + application.getJobId()));

        if (!job.isActive()) {
            throw new IllegalArgumentException("This job is no longer accepting applications.");
        }

        if (applicationRepository.findByApplicantEmailAndJobId(currentEmail, application.getJobId()).isPresent()) {
            throw new DuplicateResourceException("You have already applied for this job.");
        }

        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedDate(java.time.LocalDateTime.now());

        // Calculate a transparent skill match when the resume is uploaded.
        if (application.getResumeUrl() != null) {
            String filename = application.getResumeUrl().substring(application.getResumeUrl().lastIndexOf('/') + 1);
            ResumeMatchingService.MatchResult match = resumeMatchingService.calculate(
                    resumeStorageService.getPath(filename), job.getSkills(), job.getDescription());
            application.setMatchScore(match.score());
            application.setMatchedSkills(String.join(", ", match.matchedSkills()));
            application.setMissingSkills(String.join(", ", match.missingSkills()));
        }

        try {
            Application saved = applicationRepository.save(application);
            addHistory(saved, ApplicationStatus.APPLIED, getDisplayName(currentEmail));
            return toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("You have already applied for this job.");
        }
    }

    @Override
    public List<ApplicationResponseDTO> getAllApplications() {
        assertAdmin();
        return applicationRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<ApplicationResponseDTO> getApplicationsByJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + jobId));
        assertRecruiterForJob(job);
        return applicationRepository.findByJobId(jobId).stream().map(this::toDto).toList();
    }

    @Override
    public List<ApplicationResponseDTO> getMyApplications() {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        return applicationRepository.findByApplicantEmail(currentEmail).stream().map(this::toDto).toList();
    }

    @Override
    public ApplicationResponseDTO getApplicationById(Long id) {
        Application application = getApplicationOrThrow(id);
        assertCanViewApplication(application);
        return toDto(application);
    }

    @Override
    @Transactional
    public ApplicationResponseDTO updateStatus(Long id, String status) {
        Application application = getApplicationOrThrow(id);
        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found."));
        assertRecruiterForJob(job);

        ApplicationStatus newStatus = parseStatus(status);
        if (application.getStatus() == newStatus) {
            return toDto(application);
        }

        application.setStatus(newStatus);
        Application saved = applicationRepository.save(application);
        addHistory(saved, newStatus, getDisplayName(SecurityUtils.getCurrentUserEmail()));
        return toDto(saved);
    }

    @Override
    public List<ApplicationStatusHistoryDTO> getStatusHistory(Long applicationId) {
        Application application = getApplicationOrThrow(applicationId);
        assertCanViewApplication(application);
        return historyRepository.findByApplicationIdOrderByChangedAtAsc(applicationId).stream()
                .map(h -> new ApplicationStatusHistoryDTO(h.getStatus(), h.getChangedAt(), h.getChangedBy()))
                .toList();
    }

    @Override
    public DashboardStatsDTO getMyApplicationStats() {
        List<Application> applications = applicationRepository.findByApplicantEmail(SecurityUtils.getCurrentUserEmail());
        Map<String, Long> counts = new java.util.HashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            counts.put(status.name(), 0L);
        }
        counts.put("TOTAL", (long) applications.size());

        for (Application application : applications) {
            ApplicationStatus status = normalizeStatus(application.getStatus());
            if (status != null) {
                counts.put(status.name(), counts.get(status.name()) + 1);
            }
        }
        return new DashboardStatsDTO(counts);
    }

    private void addHistory(Application application, ApplicationStatus status, String changedBy) {
        ApplicationStatusHistory history = new ApplicationStatusHistory(application, status, changedBy);
        historyRepository.save(history);
    }

    private Application getApplicationOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));
    }

    private void assertCanViewApplication(Application application) {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        String role = SecurityUtils.normalizeRole(currentUser.getRole());

        if ("ADMIN".equals(role)) {
            return;
        }
        if ("CANDIDATE".equals(role) && application.getApplicantEmail().equals(currentEmail)) {
            return;
        }
        if ("RECRUITER".equals(role)) {
            Job job = jobRepository.findById(application.getJobId()).orElse(null);
            if (job != null && currentEmail.equals(job.getRecruiterEmail())) {
                return;
            }
        }
        throw new ForbiddenException("You do not have permission to view this application.");
    }

    private void assertRecruiterForJob(Job job) {
        User currentUser = getCurrentUser();
        String role = SecurityUtils.normalizeRole(currentUser.getRole());
        if ("ADMIN".equals(role)) {
            return;
        }
        if ("RECRUITER".equals(role) && currentUser.getEmail().equals(job.getRecruiterEmail())) {
            return;
        }
        throw new ForbiddenException("You do not have permission to view these applications.");
    }

    private void assertAdmin() {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(SecurityUtils.normalizeRole(currentUser.getRole()))) {
            throw new ForbiddenException("Admin access required.");
        }
    }

    private User getCurrentUser() {
        return userRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private String getDisplayName(String email) {
        return userRepository.findByEmail(email).map(User::getName).orElse(email);
    }

    private ApplicationStatus parseStatus(String status) {
        String normalized = status == null ? "" : status.toUpperCase().replaceAll("\\s+", "_");
        try {
            return ApplicationStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    private ApplicationStatus normalizeStatus(ApplicationStatus status) {
        return status;
    }

    private ApplicationResponseDTO toDto(Application application) {
        ApplicationResponseDTO dto = new ApplicationResponseDTO();
        dto.setId(application.getId());
        dto.setApplicantEmail(application.getApplicantEmail());
        dto.setJobId(application.getJobId());
        dto.setStatus(application.getStatus());
        dto.setResumeUrl(application.getResumeUrl());
        dto.setMatchScore(application.getMatchScore());
        dto.setMatchedSkills(application.getMatchedSkills());
        dto.setMissingSkills(application.getMissingSkills());
        dto.setAppliedDate(application.getAppliedDate());

        userRepository.findByEmail(application.getApplicantEmail())
                .ifPresent(user -> dto.setApplicantName(user.getName()));

        jobRepository.findById(application.getJobId()).ifPresent(job -> {
            dto.setJobTitle(job.getTitle());
            dto.setJobCompany(job.getCompany());
            dto.setJobLocation(job.getLocation());
        });

        return dto;
    }
}
