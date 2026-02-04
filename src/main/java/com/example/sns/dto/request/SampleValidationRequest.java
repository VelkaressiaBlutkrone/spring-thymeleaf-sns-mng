package com.example.sns.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Valid 검증 예시 DTO.
 *        RULE 1.3.1: Controller 단에서 형식 검증(@Valid 등) 적용.
 */
@Getter
@Setter
@NoArgsConstructor
public class SampleValidationRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 1, max = 100)
    private String name;

    @Size(max = 500)
    private String description;
}
