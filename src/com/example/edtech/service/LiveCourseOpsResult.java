package com.example.edtech.service;

import com.example.edtech.infrai.Json;

import java.util.Map;

public record LiveCourseOpsResult(
        String roomName,
        String channel,
        double attendanceRatio,
        double submissionRatio,
        String deadlineRisk,
        String reportingState,
        String realtimeViewerToken,
        String rtcViewerToken
) {
    public String toJson() {
        return Json.stringify(Map.of(
                "roomName", roomName,
                "channel", channel,
                "attendanceRatio", attendanceRatio,
                "submissionRatio", submissionRatio,
                "deadlineRisk", deadlineRisk,
                "reportingState", reportingState,
                "realtimeViewerToken", realtimeViewerToken,
                "rtcViewerToken", rtcViewerToken
        ));
    }
}
