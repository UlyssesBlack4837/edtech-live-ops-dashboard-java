package com.example.edtech.domain;

public record CourseOpsDecision(
        double attendanceRatio,
        double submissionRatio,
        String deadlineRisk,
        String reportingState
) {
}
