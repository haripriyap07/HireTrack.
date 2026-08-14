package com.hiretrack.hiretrack.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hiretrack.hiretrack.dto.DashboardStatsDTO;
import com.hiretrack.hiretrack.dto.JobResponseDTO;
import com.hiretrack.hiretrack.entity.Job;
import com.hiretrack.hiretrack.service.JobService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public Page<JobResponseDTO> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return jobService.getJobs(page, size);
    }

    @GetMapping("/all")
    public List<JobResponseDTO> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public List<JobResponseDTO> getMyJobs() {
        return jobService.getMyJobs();
    }

    @GetMapping("/stats/recruiter")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public DashboardStatsDTO getRecruiterStats() {
        return jobService.getRecruiterStats();
    }

    @GetMapping("/stats/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardStatsDTO getAdminStats() {
        return jobService.getAdminStats();
    }

    @GetMapping("/{id}")
    public JobResponseDTO getJobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    @PostMapping
    public JobResponseDTO createJob(@Valid @RequestBody Job job) {
        logger.info("Create job request received: title='{}' by user='{}'", job.getTitle(), job.getRecruiterEmail());
        return jobService.createJob(job);
    }

    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    @PutMapping("/{id}")
    public JobResponseDTO updateJob(@PathVariable Long id, @Valid @RequestBody Job job) {
        return jobService.updateJob(id, job);
    }

    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    @PutMapping("/{id}/close")
    public JobResponseDTO closeJob(@PathVariable Long id) {
        return jobService.closeJob(id);
    }

    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    @PutMapping("/{id}/reopen")
    public JobResponseDTO reopenJob(@PathVariable Long id) {
        return jobService.reopenJob(id);
    }

    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
