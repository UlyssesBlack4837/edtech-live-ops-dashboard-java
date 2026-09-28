package com.example.edtech.domain;

import java.time.Duration;
import java.time.Instant;

public final class CourseOpsPolicy {
    public CourseOpsDecision evaluate(CourseSessionSnapshot snapshot, Instant now) {
        double attendanceRatio = ratio(snapshot.activeRtcParticipants(), snapshot.enrolledLearners());
        double submissionRatio = ratio(snapshot.submittedAssignments(), snapshot.enrolledLearners());
        long hoursToDeadline = Duration.between(now, snapshot.nextDeadlineAt()).toHours();

        String deadlineRisk;
        if (hoursToDeadline <= 12 || submissionRatio < 0.50) {
            deadlineRisk = "RED";
        } else if (hoursToDeadline <= 24 || submissionRatio < 0.75) {
            deadlineRisk = "AMBER";
        } else {
            deadlineRisk = "GREEN";
        }

        String reportingState = ("RED".equals(deadlineRisk) || attendanceRatio < 0.35)
                ? "NEEDS_EDUCATOR_ATTENTION"
                : "ON_TRACK";

        return new CourseOpsDecision(
                round(attendanceRatio),
                round(submissionRatio),
                deadlineRisk,
                reportingState
        );
    }

    private double ratio(int part, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return (double) part / (double) total;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
