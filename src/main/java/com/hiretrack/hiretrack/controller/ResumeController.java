package com.hiretrack.hiretrack.controller;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hiretrack.hiretrack.entity.Application;
import com.hiretrack.hiretrack.entity.User;
import com.hiretrack.hiretrack.repository.ApplicationRepository;
import com.hiretrack.hiretrack.repository.JobRepository;
import com.hiretrack.hiretrack.repository.UserRepository;
import com.hiretrack.hiretrack.service.ResumeStorageService;
import com.hiretrack.hiretrack.util.SecurityUtils;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeStorageService storageService;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public ResumeController(
            ResumeStorageService storageService,
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            JobRepository jobRepository) {

        this.storageService = storageService;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<String> uploadResume(
            @RequestParam("file") MultipartFile file) {

        try {
            String filename = storageService.save(file);

            return ResponseEntity.ok(
                    storageService.buildResumeUrl(filename)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IOException e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Failed to save resume.");
        }
    }

    @GetMapping("/download/{filename:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable String filename,
            Authentication authentication) throws IOException {

        String requestedUrl =
                storageService.buildResumeUrl(filename);

        Application application =
                applicationRepository
                        .findByResumeUrl(requestedUrl)
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (application == null) {
            return ResponseEntity.notFound().build();
        }

        if (!canViewResume(
                application,
                authentication.getName())) {

            return ResponseEntity
                    .status(403)
                    .build();
        }

        Path path = storageService.getPath(filename);

        Resource resource =
                new FileSystemResource(path);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                storageService.contentType(path)
        );

        headers.setContentDisposition(
                ContentDisposition
                        .inline()
                        .filename("resume" + getExtension(filename))
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(resource);
    }

    private boolean canViewResume(
            Application application,
            String email) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user == null) {
            return false;
        }

        String role =
                SecurityUtils.normalizeRole(user.getRole());

        if ("ADMIN".equals(role)) {
            return true;
        }

        if ("CANDIDATE".equals(role)) {
            return email.equals(
                    application.getApplicantEmail()
            );
        }

        if ("RECRUITER".equals(role)) {

            return jobRepository
                    .findById(application.getJobId())
                    .map(job ->
                            email.equals(
                                    job.getRecruiterEmail()
                            ))
                    .orElse(false);
        }

        return false;
    }

    private String getExtension(String filename) {

        int dot = filename.lastIndexOf('.');

        return dot >= 0
                ? filename.substring(dot)
                : "";
    }
}