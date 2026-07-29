package com.lms.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Kolkata")
    private ZonedDateTime timestamp;

    private Map<String, String> validationErrors;

    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")))
                .build();
    }

    public static ErrorResponse withValidations(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")))
                .validationErrors(validationErrors)
                .build();
    }
}
