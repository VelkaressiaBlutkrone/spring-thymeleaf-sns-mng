package com.example.sns.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Pin 수정 요청.
 *
 * API 명세 6.4 PinUpdateRequest.
 */
public record PinUpdateRequest(
        @Size(max = 500)
        String description,

        Double latitude,
        Double longitude
) {
}
