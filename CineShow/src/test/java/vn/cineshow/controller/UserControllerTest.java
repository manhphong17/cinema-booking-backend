package vn.cineshow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import vn.cineshow.dto.request.UpdateUserRequest;
import vn.cineshow.dto.response.UserResponse;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.UserService;
import vn.cineshow.service.impl.AccountDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("user@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    @DisplayName("GET /users/me should return profile of authenticated user")
    void getProfile_shouldReturnCurrentUser() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("User Name")
                .email("user@example.com")
                .address("Hanoi")
                .loyalPoint(10)
                .build();

        when(userService.getProfile("user@example.com")).thenReturn(response);

        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Fetched user profile successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("User Name"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.address").value("Hanoi"))
                .andExpect(jsonPath("$.data.loyalPoint").value(10));

        verify(userService, times(1)).getProfile("user@example.com");
    }

    @Test
    @DisplayName("GET /users/me without principal should throw AuthenticatedException")
    void getProfile_withoutPrincipal_shouldThrowException() throws Exception {
        // When principal is null, resolveEmail() throws AuthenticatedException
        // Since @PreAuthorize is disabled with addFilters = false, we can test this directly
        mockMvc.perform(get("/users/me"))
                .andExpect(status().is5xxServerError());

        verify(userService, never()).getProfile(anyString());
    }

    @Test
    @DisplayName("PUT /users/me should update profile of authenticated user")
    void updateProfile_shouldUpdateCurrentUser() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("New Name")
                .address("Da Nang")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("New Name")
                .email("user@example.com")
                .address("Da Nang")
                .loyalPoint(15)
                .build();

        when(userService.updateProfile(eq("user@example.com"), eq(request))).thenReturn(response);

        mockMvc.perform(put("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Updated user profile successfully"))
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.address").value("Da Nang"));

        ArgumentCaptor<UpdateUserRequest> captor = ArgumentCaptor.forClass(UpdateUserRequest.class);
        verify(userService, times(1)).updateProfile(eq("user@example.com"), captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("New Name");
        assertThat(captor.getValue().getAddress()).isEqualTo("Da Nang");
    }

    @Test
    @DisplayName("PUT /users/me without principal should throw AuthenticatedException")
    void updateProfile_withoutPrincipal_shouldThrowException() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("New Name")
                .address("Da Nang")
                .build();

        // When principal is null, resolveEmail() throws AuthenticatedException
        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(userService, never()).updateProfile(anyString(), any(UpdateUserRequest.class));
    }
}
