package com.example.edtech;

import com.example.edtech.domain.CourseOpsDecision;
import com.example.edtech.domain.CourseOpsPolicy;
import com.example.edtech.domain.CourseSessionSnapshot;

import java.time.Instant;

public class CourseOpsPolicyTest {
    public static void main(String[] args) {
        CourseOpsPolicy policy = new CourseOpsPolicy();
        Instant now = Instant.parse("2026-01-10T10:00:00Z");
        CourseSessionSnapshot snapshot = new CourseSessionSnapshot(
                "course-finance-101",
                "Cohort 7 / Week 3",
                18,
                12,
                4,
                now.plusSeconds(20 * 3600),
                "eu-west"
        );

        CourseOpsDecision decision = policy.evaluate(snapshot, now);

        assertEquals(0.22, decision.attendanceRatio(), "attendanceRatio");
        assertEquals(0.67, decision.submissionRatio(), "submissionRatio");
        assertEquals("AMBER", decision.deadlineRisk(), "deadlineRisk");
        assertEquals("NEEDS_EDUCATOR_ATTENTION", decision.reportingState(), "reportingState");
        System.out.println("OK");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected=" + expected + " actual=" + actual);
        }
    }
}
