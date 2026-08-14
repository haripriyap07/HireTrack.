package com.hiretrack.hiretrack.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hiretrack.hiretrack.dto.ApplicationResponseDTO;
import com.hiretrack.hiretrack.dto.ApplicationStatusHistoryDTO;
import com.hiretrack.hiretrack.dto.DashboardStatsDTO;
import com.hiretrack.hiretrack.entity.Application;
import com.hiretrack.hiretrack.service.ApplicationService;
import com.hiretrack.hiretrack.service.ResumeStorageService;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ResumeStorageService resumeStorageService;

    public ApplicationController(
            ApplicationService applicationService,
            ResumeStorageService resumeStorageService) {

        this.applicationService = applicationService;
        this.resumeStorageService = resumeStorageService;
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping(
            value = "/apply",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApplicationResponseDTO apply(
            @RequestParam("jobId") Long jobId,
            @RequestPart("resume") MultipartFile resume)
            throws IOException {

        String filename =
                resumeStorageService.save(resume);

        String resumeUrl =
                resumeStorageService.buildResumeUrl(filename);

        Application application =
                new Application();

        application.setJobId(jobId);
        application.setResumeUrl(resumeUrl);

        try {

            return applicationService
                    .applyForJob(application);

        } catch (RuntimeException ex) {

            resumeStorageService
                    .deleteQuietly(filename);

            throw ex;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ApplicationResponseDTO>
    getAllApplications() {

        return applicationService
                .getAllApplications();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CANDIDATE')")
    public List<ApplicationResponseDTO>
    getMyApplications() {

        return applicationService
                .getMyApplications();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('CANDIDATE')")
    public DashboardStatsDTO getMyStats() {

        return applicationService
                .getMyApplicationStats();
    }

    @GetMapping("/{id}")
    public ApplicationResponseDTO
    getApplicationById(@PathVariable Long id) {

        return applicationService
                .getApplicationById(id);
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public List<ApplicationResponseDTO>
    getApplicationsByJob(
            @PathVariable Long jobId) {

        return applicationService
                .getApplicationsByJob(jobId);
    }

    @GetMapping("/{id}/history")
    public List<ApplicationStatusHistoryDTO>
    getStatusHistory(@PathVariable Long id) {

        return applicationService
                .getStatusHistory(id);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ApplicationResponseDTO updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return applicationService
                .updateStatus(id, status);
    }
}