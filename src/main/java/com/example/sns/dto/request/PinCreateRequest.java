package com.example.sns.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Pin 생성 요청.
 *
 * API 명세 6.3 PinCreateRequest.
 */
public record PinCreateRequest(
        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @NotNull(message = "경도는 필수입니다.")
        Double longitude,

        @Size(max = 500)
        String description
) {
}
