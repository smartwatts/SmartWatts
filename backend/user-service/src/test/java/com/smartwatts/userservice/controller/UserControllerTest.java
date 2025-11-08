package com.smartwatts.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.userservice.dto.AuthRequest;
import com.smartwatts.userservice.dto.AuthResponse;
import com.smartwatts.userservice.dto.UserDto;
import com.smartwatts.userservice.model.Role;
import com.smartwatts.userservice.service.UserService;
import com.smartwatts.userservice.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.smartwatts.userservice.config.TestSecurityConfig;
import com.smartwatts.userservice.TestApplication;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UserController.class, 
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class, 
        HibernateJpaAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        SecurityAutoConfiguration.class
    }
)
@ContextConfiguration(classes = {TestApplication.class, TestSecurityConfig.class})
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto testUserDto;
    private AuthRequest testAuthRequest;
    private AuthResponse testAuthResponse;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testUserDto = new UserDto();
        testUserDto.setId(testUserId);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setFirstName("Test");
        testUserDto.setLastName("User");
        testUserDto.setPhoneNumber("+2341234567890");
        testUserDto.setRole(Role.RoleName.ROLE_USER);

        testAuthRequest = new AuthRequest();
        testAuthRequest.setUsernameOrEmail("test@example.com");
        testAuthRequest.setPassword("password123");

        testAuthResponse = AuthResponse.builder()
                .userId(testUserId)
                .username("testuser")
                .email("test@example.com")
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .role("ROLE_USER")
                .isActive(true)
                .build();
    }

    @Test
    void registerUser_Success_ReturnsCreated() throws Exception {
        // Given
        when(userService.createUser(any(UserDto.class))).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    void registerUser_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        UserDto invalidDto = new UserDto();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserDto.class));
    }

    @Test
    void login_Success_ReturnsAuthResponse() throws Exception {
        // Given
        when(userService.authenticate(any(AuthRequest.class))).thenReturn(testAuthResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).authenticate(any(AuthRequest.class));
    }

    @Test
    void login_InvalidCredentials_ReturnsBadRequest() throws Exception {
        // Given
        when(userService.authenticate(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Invalid password"));

        // When & Then
        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAuthRequest)))
                .andExpect(status().isBadRequest());

        verify(userService).authenticate(any(AuthRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUserProfile_Success_ReturnsUserDto() throws Exception {
        // Given
        when(userService.getCurrentUserProfile()).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getCurrentUserProfile();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void getUserById_Success_ReturnsUserDto() throws Exception {
        // Given
        when(userService.getUserById(testUserId)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).getUserById(testUserId);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserById_Unauthorized_ReturnsForbidden() throws Exception {
        // Given
        UUID otherUserId = UUID.randomUUID();
        // The @PreAuthorize expression checks: hasRole('ADMIN') or #userId == authentication.principal.username
        // Since username is "testuser" (String) and userId is UUID, they won't match
        // And user doesn't have ADMIN role, so should return forbidden
        // Note: The @PreAuthorize check happens before the service is called

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}", otherUserId))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserById(any(UUID.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void getUserByUsername_Success_ReturnsUserDto() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/api/v1/users/username/{username}", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).getUserByUsername("testuser");
    }

    @Test
    void registerUser_WithoutCsrf_ReturnsCreated() throws Exception {
        // Given - CSRF is disabled in SecurityConfig, so request should succeed
        when(userService.createUser(any(UserDto.class))).thenReturn(testUserDto);
        
        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isCreated());

        verify(userService).createUser(any(UserDto.class));
    }
}

