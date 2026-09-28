package com.example.edtech.infrai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InfraiClient {
    private final String baseUrl;
    private final String apiKey;
    private final HttpClient httpClient;

    public InfraiClient(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
    }

    public InfraiClient(String baseUrl, String apiKey, HttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    public Map<String, Object> realtimeChannelCreate(String channel, String type, String vendor) {
        return post("/v1/realtime/channel/create", Map.of(
                "channel", channel,
                "type", type,
                "vendor", vendor
        ));
    }

    public Map<String, Object> realtimePublish(String channel, String event, Object data, String accountId) {
        return post("/v1/realtime/publish", Map.of(
                "channel", channel,
                "event", event,
                "data", data,
                "account_id", accountId
        ));
    }

    public Map<String, Object> realtimeTokenIssue(String clientId, List<String> channels, List<String> capabilities, int ttlSeconds) {
        return post("/v1/realtime/token/issue", Map.of(
                "client_id", clientId,
                "channels", channels,
                "capabilities", capabilities,
                "ttl_seconds", ttlSeconds
        ));
    }

    public Map<String, Object> rtcRoomCreate(String name, int maxParticipants, int emptyTimeoutS, String region) {
        return post("/v1/rtc/room/create", Map.of(
                "name", name,
                "max_participants", maxParticipants,
                "empty_timeout_s", emptyTimeoutS,
                "region", region
        ));
    }

    public Map<String, Object> rtcTokenIssue(String room, String identity, String displayName, int ttlS, boolean canPublish, boolean canSubscribe) {
        return post("/v1/rtc/token/issue", Map.of(
                "room", room,
                "identity", identity,
                "display_name", displayName,
                "ttl_s", ttlS,
                "can_publish", canPublish,
                "can_subscribe", canSubscribe
        ));
    }

    public Map<String, Object> metricsBatch(List<Map<String, Object>> series) {
        return post("/v1/metrics/batch", Map.of("points", series));
    }

    private Map<String, Object> post(String path, Map<String, Object> payload) {
        String body = Json.stringify(payload);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return sendWithRetry(request, body);
    }

    private Map<String, Object> sendWithRetry(HttpRequest request, String body) {
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Map<String, Object> envelope = parseEnvelope(response.body(), response.statusCode());
                if (response.statusCode() == 429 && attempts < 4) {
                    sleep(retryDelayMillis(response.headers(), attempts));
                    continue;
                }
                if (response.statusCode() >= 500) {
                    throw new IllegalStateException("Server returned status " + response.statusCode());
                }
                return envelope;
            } catch (IOException | InterruptedException e) {
                if (attempts >= 4) {
                    throw new IllegalStateException("Transport failure", e);
                }
                sleep(200L * attempts * attempts);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseEnvelope(String responseBody, int statusCode) {
        Object parsed = SimpleJsonParser.parse(responseBody);
        if (!(parsed instanceof Map<?, ?> rawMap)) {
            throw new IllegalStateException("Expected JSON object envelope");
        }
        Map<String, Object> envelope = (Map<String, Object>) rawMap;
        Object ok = envelope.get("ok");
        if (Boolean.FALSE.equals(ok)) {
            Object error = envelope.get("error");
            String code = "INFRAI_ERROR";
            String details = String.valueOf(error);
            if (error instanceof Map<?, ?> errorMap) {
                Object maybeCode = errorMap.get("code");
                if (maybeCode != null) {
                    code = String.valueOf(maybeCode);
                }
                details = Json.stringify((Map<?, ?>) errorMap);
            }
            throw new InfraiError(code, details, statusCode);
        }
        return envelope;
    }

    private long retryDelayMillis(HttpHeaders headers, int attempts) {
        Optional<String> retryAfter = headers.firstValue("Retry-After");
        if (retryAfter.isPresent()) {
            try {
                return Long.parseLong(retryAfter.get()) * 1000L;
            } catch (NumberFormatException ignored) {
                return 300L * attempts * attempts;
            }
        }
        return 300L * attempts * attempts;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        }
    }
}
