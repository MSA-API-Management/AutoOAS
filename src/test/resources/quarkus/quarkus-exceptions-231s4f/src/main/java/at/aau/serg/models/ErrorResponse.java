package at.aau.serg.models;

import jakarta.json.bind.annotation.JsonbProperty;

public class ErrorResponse {
    @JsonbProperty("error")
    private String error;

    @JsonbProperty("message")
    private String message;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    // No-arg constructor needed for JSON-B
    public ErrorResponse() {}

    // Getters and setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}