package com.sharepay.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @JsonProperty("success")
    private final boolean success;

    private final String messageKey;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, null, message, data);
    }

    public static <T> ApiResponse<T> success(String messageKey, String message, T data) {
        return new ApiResponse<>(true, messageKey, message, data);
    }

    public static <T> ApiResponse<T> failure(String message, T data) {
        return new ApiResponse<>(false, null, message, data);
    }

    public static <T> ApiResponse<T> failure(String messageKey, String message, T data) {
        return new ApiResponse<>(false, messageKey, message, data);
    }
}

