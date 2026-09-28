package com.example.edtech.service;

import com.example.edtech.config.AppConfig;
import com.example.edtech.domain.CourseOpsDecision;
import com.example.edtech.domain.CourseOpsPolicy;
import com.example.edtech.domain.CourseSessionSnapshot;
import com.example.edtech.infrai.InfraiClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class LiveCourseOpsService {
    private final AppConfig config;
    private final InfraiClient infrai;
    private final CourseOpsPolicy policy;

    public LiveCourseOpsService(AppConfig config, InfraiClient infrai, CourseOpsPolicy policy) {
        this.config = config;
        this.infrai = infrai;
        this.policy = policy;
    }

    public static LiveCourseOpsService createDefault(AppConfig config) {
        return new LiveCourseOpsService(config, new InfraiClient(config.baseUrl(), config.apiKey()), new CourseOpsPolicy());
    }

    @SuppressWarnings("unchecked")
    public LiveCourseOpsResult stream(CourseSessionSnapshot snapshot) {
        String roomName = snapshot.courseId() + "-" + snapshot.cohortName().replace(' ', '-').replace('/', '-');
        String channel = config.dashboardChannel();

        infrai.realtimeChannelCreate(channel, "broadcast", "native");
        infrai.rtcRoomCreate(roomName, snapshot.enrolledLearners() + 5, 1800, snapshot.region());

        CourseOpsDecision decision = policy.evaluate(snapshot, Instant.now());

        List<Map<String, Object>> series = List.of(
                Map.of("metric", "edtech.course.attendance_ratio", "value", decision.attendanceRatio(), "course_id", snapshot.courseId()),
                Map.of("metric", "edtech.course.submission_ratio", "value", decision.submissionRatio(), "course_id", snapshot.courseId()),
                Map.of("metric", "edtech.course.deadline_risk", "value", numericRisk(decision.deadlineRisk()), "course_id", snapshot.courseId())
        );
        infrai.metricsBatch(series);

        Map<String, Object> dashboardPayload = Map.of(
                "courseId", snapshot.courseId(),
                "cohortName", snapshot.cohortName(),
                "attendanceRatio", decision.attendanceRatio(),
                "submissionRatio", decision.submissionRatio(),
                "deadlineRisk", decision.deadlineRisk(),
                "reportingState", decision.reportingState(),
                "activeRtcParticipants", snapshot.activeRtcParticipants(),
                "nextDeadlineAt", snapshot.nextDeadlineAt().toString()
        );
        infrai.realtimePublish(channel, "course.ops.updated", dashboardPayload, config.accountId());

        Map<String, Object> realtimeTokenEnv = infrai.realtimeTokenIssue(
                config.dashboardClientId(),
                List.of(channel),
                List.of("subscribe"),
                3600
        );
        Map<String, Object> rtcTokenEnv = infrai.rtcTokenIssue(
                roomName,
                config.dashboardClientId(),
                "Educator Dashboard",
                3600,
                false,
                true
        );

        Map<String, Object> realtimeData = (Map<String, Object>) realtimeTokenEnv.get("data");
        Map<String, Object> rtcData = (Map<String, Object>) rtcTokenEnv.get("data");

        return new LiveCourseOpsResult(
                roomName,
                channel,
                decision.attendanceRatio(),
                decision.submissionRatio(),
                decision.deadlineRisk(),
                decision.reportingState(),
                String.valueOf(realtimeData),
                String.valueOf(rtcData)
        );
    }

    private double numericRisk(String risk) {
        return switch (risk) {
            case "RED" -> 2.0;
            case "AMBER" -> 1.0;
            default -> 0.0;
        };
    }
}
