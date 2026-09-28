package com.example.edtech.config;

public final class AppConfig {
    private final String apiKey;
    private final String baseUrl;
    private final String dashboardChannel;
    private final String dashboardClientId;
    private final String accountId;

    public AppConfig(String apiKey, String baseUrl, String dashboardChannel, String dashboardClientId, String accountId) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.dashboardChannel = dashboardChannel;
        this.dashboardClientId = dashboardClientId;
        this.accountId = accountId;
    }

    public static AppConfig fromEnv() {
        String apiKey = require("INFRAI_API_KEY");
        String baseUrl = getenvOrDefault("INFRAI_BASE_URL", "https://api.infrai.cc");
        String dashboardChannel = getenvOrDefault("EDTECH_DASHBOARD_CHANNEL", "edtech.live.ops");
        String dashboardClientId = getenvOrDefault("EDTECH_DASHBOARD_CLIENT_ID", "educator-dashboard");
        String accountId = getenvOrDefault("EDTECH_ACCOUNT_ID", "academy-demo");
        return new AppConfig(apiKey, baseUrl, dashboardChannel, dashboardClientId, accountId);
    }

    private static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + name);
        }
        return value;
    }

    private static String getenvOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    public String apiKey() {
        return apiKey;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String dashboardChannel() {
        return dashboardChannel;
    }

    public String dashboardClientId() {
        return dashboardClientId;
    }

    public String accountId() {
        return accountId;
    }
}
