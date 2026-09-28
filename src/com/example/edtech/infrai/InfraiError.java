package com.example.edtech.infrai;

public final class InfraiError extends RuntimeException {
    private final String code;
    private final int httpStatus;
    private final String details;

    public InfraiError(String code, String details, int httpStatus) {
        super(code + ": " + details);
        this.code = code;
        this.details = details;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public String details() {
        return details;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
