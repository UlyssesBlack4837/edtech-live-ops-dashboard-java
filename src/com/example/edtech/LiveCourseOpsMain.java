package com.example.edtech;

import com.example.edtech.config.AppConfig;
import com.example.edtech.domain.CourseSessionSnapshot;
import com.example.edtech.service.LiveCourseOpsResult;
import com.example.edtech.service.LiveCourseOpsService;

import java.time.Instant;

public class LiveCourseOpsMain {
    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnv();
        LiveCourseOpsService service = LiveCourseOpsService.createDefault(config);

        CourseSessionSnapshot snapshot = new CourseSessionSnapshot(
                "course-finance-101",
                "Cohort 7 / Week 3",
                18,
                12,
                4,
                Instant.now().plusSeconds(20 * 3600),
                "eu-west"
        );

        LiveCourseOpsResult result = service.stream(snapshot);
        System.out.println(result.toJson());
    }
}
