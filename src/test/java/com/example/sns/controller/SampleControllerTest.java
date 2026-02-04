package com.example.sns.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("@Valid 검증 성공 시 200 반환")
    void validate_success() throws Exception {
        String body = "{\"name\":\"테스트\",\"description\":\"설명\"}";
        mockMvc.perform(post("/api/sample/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.name").value("테스트"));
    }

    @Test
    @DisplayName("@Valid 검증 실패 시 ErrorResponse 반환")
    void validate_fail_returnsErrorResponse() throws Exception {
        String body = "{\"name\":\"\",\"description\":\"설명\"}";
        mockMvc.perform(post("/api/sample/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("E005"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("BusinessException 시 ErrorResponse 반환")
    void businessException_returnsErrorResponse() throws Exception {
        mockMvc.perform(post("/api/sample/error/business")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("E001"))
                .andExpect(jsonPath("$.message").exists());
    }
}
