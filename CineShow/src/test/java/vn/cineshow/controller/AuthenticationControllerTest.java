package vn.cineshow.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.cineshow.dto.request.SignInRequest;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /auth/log-in should return access token and set refresh token cookie")
    void login_shouldReturnAccessTokenAndCookie() throws Exception {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("user@test.com");
        signInRequest.setPassword("00000000");

        mockMvc.perform(post("/auth/log-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.roleName").exists())
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @DisplayName("POST /auth/log-in should fail with invalid credentials")
    void login_shouldFailWithInvalidCredentials() throws Exception {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("user@test.com");
        signInRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/log-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().is4xxClientError());
    }
}

