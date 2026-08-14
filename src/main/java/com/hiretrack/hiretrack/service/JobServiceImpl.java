package com.hiretrack.hiretrack.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hiretrack.hiretrack.dto.DashboardStatsDTO;
import com.hiretrack.hiretrack.dto.JobResponseDTO;
import com.hiretrack.hiretrack.entity.Job;
import com.hiretrack.hiretrack.exception.ForbiddenException;
import com.hiretrack.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.hiretrack.repository.ApplicationRepository;
import com.hiretrack.hiretrack.repository.JobRepository;
import com.hiretrack.hiretrack.repository.UserRepository;
import com.hiretrack.hiretrack.util.SecurityUtils;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public JobServiceImpl(JobRepository jobRepository,
                          ApplicationRepository applicationRepository,
                          UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JobServiceImpl.class);

    @Override
    public Page<JobResponseDTO> getJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public List<JobResponseDTO> getAllJobs() {
        return jobRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public JobResponseDTO createJob(Job job) {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        if (currentEmail == null) {
            logger.warn("Attempt to create job without authenticated user");
            throw new com.hiretrack.hiretrack.exception.ForbiddenException("Authentication required.");
        }
        job.setRecruiterEmail(currentEmail);
        job.setActive(true);
        if (job.getPostedDate() == null) {
            job.setPostedDate(java.time.LocalDateTime.now());
        }
        return toDto(jobRepository.save(job));
    }

    @Override
    public JobResponseDTO updateJob(Long id, Job job) {
        Job existing = getOwnedJob(id);
        existing.setTitle(job.getTitle());
        existing.setCompany(job.getCompany());
        existing.setLocation(job.getLocation());
        existing.setDescription(job.getDescription());
        if (job.getJobType() != null) {
            existing.setJobType(job.getJobType());
        }
        if (job.getSkills() != null) {
            existing.setSkills(job.getSkills());
        }
        return toDto(jobRepository.save(existing));
    }

    @Override
    public JobResponseDTO closeJob(Long id) {
        Job existing = getOwnedJob(id);
        existing.setActive(false);
        return toDto(jobRepository.save(existing));
    }

    @Override
    public JobResponseDTO reopenJob(Long id) {
        Job existing = getOwnedJob(id);
        existing.setActive(true);
        return toDto(jobRepository.save(existing));
    }

    @Override
    public JobResponseDTO getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + id));
        return toDto(job);
    }

    @Override
    public void deleteJob(Long id) {
        Job existing = getOwnedJob(id);
        jobRepository.delete(existing);
    }

    @Override
    public List<JobResponseDTO> getMyJobs() {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        return jobRepository.findByRecruiterEmail(currentEmail).stream().map(this::toDto).toList();
    }

    @Override
    public DashboardStatsDTO getRecruiterStats() {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        List<Job> jobs = jobRepository.findByRecruiterEmail(currentEmail);
        long totalJobs = jobs.size();
        long activeJobs = jobs.stream().filter(Job::isActive).count();
        long totalApplications = jobs.stream()
                .mapToLong(job -> applicationRepository.findByJobId(job.getId()).size())
                .sum();
        long pendingReviews = applicationRepository.findByJobIdIn(
                jobs.stream().map(Job::getId).toList()
        ).stream().filter(app -> app.getStatus() == com.hiretrack.hiretrack.entity.ApplicationStatus.APPLIED).count();

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.put("TOTAL_JOBS", totalJobs);
        stats.put("ACTIVE_JOBS", activeJobs);
        stats.put("TOTAL_APPLICATIONS", totalApplications);
        stats.put("PENDING_REVIEWS", pendingReviews);
        return stats;
    }

    @Override
    public DashboardStatsDTO getAdminStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.put("TOTAL_USERS", (long) userRepository.count());
        stats.put("CANDIDATES", userRepository.countByRole("CANDIDATE")
                + userRepository.countByRole("ROLE_CANDIDATE")
                + userRepository.countByRole("USER")
                + userRepository.countByRole("ROLE_USER"));
        stats.put("RECRUITERS", userRepository.countByRole("RECRUITER")
                + userRepository.countByRole("ROLE_RECRUITER"));
        stats.put("TOTAL_JOBS", (long) jobRepository.count());
        stats.put("TOTAL_APPLICATIONS", (long) applicationRepository.count());
        return stats;
    }

    private Job getOwnedJob(Long id) {
        Job existing = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + id));
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        String role = SecurityUtils.normalizeRole(
                userRepository.findByEmail(currentEmail).map(u -> u.getRole()).orElse("")
        );
        if ("ADMIN".equals(role) || currentEmail.equals(existing.getRecruiterEmail())) {
            return existing;
        }
        throw new ForbiddenException("You do not have permission to manage this job.");
    }

    private JobResponseDTO toDto(Job job) {
        JobResponseDTO dto = new JobResponseDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setCompany(job.getCompany());
        dto.setLocation(job.getLocation());
        dto.setJobType(job.getJobType());
        dto.setDescription(job.getDescription());
        dto.setSkills(job.getSkills());
        dto.setActive(job.isActive());
        dto.setPostedDate(job.getPostedDate());
        dto.setRecruiterEmail(job.getRecruiterEmail());

        String currentEmail = SecurityUtils.getCurrentUserEmail();
        if (currentEmail != null) {
            applicationRepository.findByApplicantEmailAndJobId(currentEmail, job.getId())
                    .ifPresent(app -> {
                        dto.setApplied(true);
                        dto.setApplicationStatus(app.getStatus() != null ? app.getStatus().name() : null);
                    });
        }
        return dto;
    }
}
