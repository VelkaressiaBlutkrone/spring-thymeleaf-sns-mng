package com.example.sns.dto.response;

import java.time.LocalDateTime;

import com.example.sns.domain.Pin;

/**
 * Pin 응답 DTO.
 *
 * RULE 3.3: 엔티티 직접 반환 금지.
 * API 명세 PinResponse.
 */
public record PinResponse(
        Long id,
        Long ownerId,
        String ownerNickname,
        Double latitude,
        Double longitude,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PinResponse from(Pin pin) {
        return new PinResponse(
                pin.getId(),
                pin.getOwner().getId(),
                pin.getOwner().getNickname(),
                pin.getLatitude(),
                pin.getLongitude(),
                pin.getDescription(),
                pin.getCreatedAt(),
                pin.getUpdatedAt()
        );
    }
}
