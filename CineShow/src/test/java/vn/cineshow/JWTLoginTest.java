package vn.cineshow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.cineshow.dto.request.SignInRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("dev")
public class JWTLoginTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testJWTLoginFlow() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test data
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("user@test.com");
        signInRequest.setPassword("00000000");

        // Test login endpoint
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
    public void testJWTLoginWithInvalidCredentials() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test with invalid credentials
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("user@test.com");
        signInRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/log-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testJWTTokenValidation() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // First login to get token
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("user@test.com");
        signInRequest.setPassword("12345678");

        String response = mockMvc.perform(post("/auth/log-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract access token from response
        String accessToken = objectMapper.readTree(response)
                .get("data")
                .get("accessToken")
                .asText();

        // Test token validation by making a request with Authorization header
        mockMvc.perform(post("/auth/refresh-token")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }
}
