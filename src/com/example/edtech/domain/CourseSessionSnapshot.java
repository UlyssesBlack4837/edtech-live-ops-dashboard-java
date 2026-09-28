package com.example.edtech.domain;

import java.time.Instant;

public record CourseSessionSnapshot(
        String courseId,
        String cohortName,
        int enrolledLearners,
        int submittedAssignments,
        int activeRtcParticipants,
        Instant nextDeadlineAt,
        String region
) {
}
