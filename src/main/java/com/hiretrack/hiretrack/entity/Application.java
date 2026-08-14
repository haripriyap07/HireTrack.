package com.hiretrack.hiretrack.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"applicant_email", "job_id"})
})
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicant_email")
    private String applicantEmail;

    @Column(name = "job_id")
    private Long jobId;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private String resumeUrl;

    @Column(name = "match_score")
    private Double matchScore;

    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills;

    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;

    private LocalDateTime appliedDate;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<ApplicationStatusHistory> statusHistory = new ArrayList<>();

    public Application() {}

    @PrePersist
    protected void onCreate() {
        if (appliedDate == null) {
            appliedDate = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public Long getJobId() {
        return jobId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public Double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }

    public String getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(String matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(String missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public LocalDateTime getAppliedDate() {
        return appliedDate;
    }

    public List<ApplicationStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public void setAppliedDate(LocalDateTime appliedDate) {
        this.appliedDate = appliedDate;
    }

    public void setStatusHistory(List<ApplicationStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }
}
