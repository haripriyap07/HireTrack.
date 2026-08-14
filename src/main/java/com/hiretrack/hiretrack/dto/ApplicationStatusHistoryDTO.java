package com.hiretrack.hiretrack.dto;

import java.time.LocalDateTime;

import com.hiretrack.hiretrack.entity.ApplicationStatus;

public class ApplicationStatusHistoryDTO {

    private ApplicationStatus status;
    private LocalDateTime changedAt;
    private String changedBy;

    public ApplicationStatusHistoryDTO() {}

    public ApplicationStatusHistoryDTO(ApplicationStatus status, LocalDateTime changedAt, String changedBy) {
        this.status = status;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }
}
