package com.smartwatts.userservice.service;

import com.smartwatts.userservice.dto.AuthRequest;
import com.smartwatts.userservice.dto.AuthResponse;
import com.smartwatts.userservice.dto.UserDto;
import com.smartwatts.userservice.model.Role;
import com.smartwatts.userservice.model.User;
import com.smartwatts.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhoneNumber("1234567890");
        testUser.setRole(Role.RoleName.ROLE_USER);
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(false);
        testUser.setIsPhoneVerified(false);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testUserDto = new UserDto();
        testUserDto.setId(testUserId);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setPassword("password123");
        testUserDto.setFirstName("Test");
        testUserDto.setLastName("User");
        testUserDto.setPhoneNumber("1234567890");
        testUserDto.setRole(Role.RoleName.ROLE_USER);
    }

    @Test
    void loadUserByUsername_WithUsername_ReturnsUserDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_WithEmail_ReturnsUserDetails() {
        // Given
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        verify(userRepository).findByUsername("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void createUser_Success_CreatesUser() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.createUser(testUserDto);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.createUser(testUserDto);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.createUser(testUserDto);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_PhoneNumberExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("1234567890")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.createUser(testUserDto);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getCurrentUserProfile_Success_ReturnsUserDto() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getCurrentUserProfile();

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getCurrentUserProfile_NoAuthentication_ThrowsException() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.getCurrentUserProfile();
        });
    }

    @Test
    void getCurrentUserProfile_AnonymousUser_ThrowsException() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("anonymousUser");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.getCurrentUserProfile();
        });
    }

    @Test
    void getUserById_Success_ReturnsUserDto() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserById(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        verify(userRepository).findById(testUserId);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.getUserById(testUserId);
        });
    }

    @Test
    void getUserByUsername_Success_ReturnsUserDto() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getUserByEmail_Success_ReturnsUserDto() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getAllUsers_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserDto> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getUsersByRole_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findByRole(Role.RoleName.ROLE_USER, pageable)).thenReturn(userPage);

        // When
        Page<UserDto> result = userService.getUsersByRole(Role.RoleName.ROLE_USER, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByRole(Role.RoleName.ROLE_USER, pageable);
    }

    @Test
    void updateUser_Success_UpdatesUser() {
        // Given
        UserDto updatedDto = new UserDto();
        updatedDto.setFirstName("Updated");
        updatedDto.setLastName("Name");
        updatedDto.setEmail("updated@example.com");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.updateUser(testUserId, updatedDto);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserRole_Success_UpdatesRole() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.updateUserRole(testUserId, Role.RoleName.ROLE_ADMIN);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void activateUser_Success_ActivatesUser() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.activateUser(testUserId);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deactivateUser_Success_DeactivatesUser() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.deactivateUser(testUserId);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticate_Success_ReturnsAuthResponse() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsernameOrEmail("testuser");
        authRequest.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("refreshToken");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        AuthResponse result = userService.authenticate(authRequest);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtService).generateToken(any(UserDetails.class));
        verify(jwtService).generateRefreshToken(any(UserDetails.class));
    }

    @Test
    void authenticate_InvalidPassword_ThrowsException() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsernameOrEmail("testuser");
        authRequest.setPassword("wrongPassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.authenticate(authRequest);
        });
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void getUserCountByRole_Success_ReturnsCount() {
        // Given
        when(userRepository.countByRole(Role.RoleName.ROLE_USER)).thenReturn(10L);

        // When
        long result = userService.getUserCountByRole(Role.RoleName.ROLE_USER);

        // Then
        assertEquals(10L, result);
        verify(userRepository).countByRole(Role.RoleName.ROLE_USER);
    }

    @Test
    void getActiveUserCount_Success_ReturnsCount() {
        // Given
        when(userRepository.countByActiveStatus(true)).thenReturn(5L);

        // When
        long result = userService.getActiveUserCount();

        // Then
        assertEquals(5L, result);
        verify(userRepository).countByActiveStatus(true);
    }

    @Test
    void refreshToken_Success_ReturnsNewTokens() {
        // Given
        String refreshToken = "validRefreshToken";

        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("newRefreshToken");

        // When
        AuthResponse result = userService.refreshToken(refreshToken);

        // Then
        assertNotNull(result);
        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("newRefreshToken", result.getRefreshToken());
        verify(jwtService).isTokenValid(refreshToken);
        verify(jwtService).generateToken(any(UserDetails.class));
        verify(jwtService).generateRefreshToken(any(UserDetails.class));
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        // Given
        String refreshToken = "invalidRefreshToken";
        when(jwtService.isTokenValid(refreshToken)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.refreshToken(refreshToken);
        });
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void forgotPassword_Success_SendsEmail() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generatePasswordResetToken("test@example.com")).thenReturn("resetToken");
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        // When
        userService.forgotPassword("test@example.com");

        // Then
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generatePasswordResetToken("test@example.com");
        verify(emailService).sendPasswordResetEmail("test@example.com", "resetToken", "testuser");
    }

    @Test
    void forgotPassword_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.forgotPassword("nonexistent@example.com");
        });
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resetPassword_Success_UpdatesPassword() {
        // Given
        String resetToken = "validResetToken";
        String newPassword = "newPassword123";

        when(jwtService.isPasswordResetTokenValid(resetToken)).thenReturn(true);
        when(jwtService.extractEmailFromResetToken(resetToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.resetPassword(resetToken, newPassword);

        // Then
        verify(jwtService).isPasswordResetTokenValid(resetToken);
        verify(jwtService).extractEmailFromResetToken(resetToken);
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_InvalidToken_ThrowsException() {
        // Given
        String resetToken = "invalidResetToken";
        String newPassword = "newPassword123";

        when(jwtService.isPasswordResetTokenValid(resetToken)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.resetPassword(resetToken, newPassword);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_UserNotFound_ThrowsException() {
        // Given
        String resetToken = "validResetToken";
        String newPassword = "newPassword123";

        when(jwtService.isPasswordResetTokenValid(resetToken)).thenReturn(true);
        when(jwtService.extractEmailFromResetToken(resetToken)).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.resetPassword(resetToken, newPassword);
        });
        verify(userRepository, never()).save(any(User.class));
    }
}

