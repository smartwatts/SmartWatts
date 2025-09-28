package com.smartwatts.userservice.service;

import com.smartwatts.userservice.dto.AuthRequest;
import com.smartwatts.userservice.dto.AuthResponse;
import com.smartwatts.userservice.dto.UserDto;
import com.smartwatts.userservice.model.Role;
import com.smartwatts.userservice.model.User;
import com.smartwatts.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username)));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name().replace("ROLE_", ""))
                .disabled(!user.getIsActive())
                .build();
    }
    
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Creating user with username: {}", userDto.getUsername());
        
        // Check if user already exists
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(userDto.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }
        
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        
        // Set default values
        if (user.getRole() == null) {
            user.setRole(Role.RoleName.ROLE_USER);
        }
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        user.setIsPhoneVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        
        return convertToDto(savedUser);
    }
    
    @Transactional(readOnly = true)
    public UserDto getCurrentUserProfile() {
        log.info("Fetching current user profile");
        
        // Get the current authenticated user's username
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Current authentication: {}", authentication);
        log.info("Authentication name: {}", authentication != null ? authentication.getName() : "null");
        log.info("Authentication principal: {}", authentication != null ? authentication.getPrincipal() : "null");
        log.info("Authentication authorities: {}", authentication != null ? authentication.getAuthorities() : "null");
        
        if (authentication == null) {
            log.error("No authentication found in security context");
            throw new RuntimeException("Authentication required. Please log in again.");
        }
        
        String username = authentication.getName();
        
        // Check if the user is anonymous (not authenticated)
        if ("anonymousUser".equals(username)) {
            log.error("User is not authenticated - anonymousUser detected");
            throw new RuntimeException("Authentication required. Please log in again.");
        }
        
        log.info("Looking up user with username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("User not found with username or email: " + username)));
        
        return convertToDto(user);
    }
    
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID userId) {
        log.info("Fetching user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return convertToDto(user);
    }
    
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return convertToDto(user);
    }
    
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return convertToDto(user);
    }
    
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.info("Fetching all users");
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByRole(Role.RoleName role, Pageable pageable) {
        log.info("Fetching users with role: {}", role);
        Page<User> users = userRepository.findByRole(role, pageable);
        return users.map(this::convertToDto);
    }
    
    @Transactional
    public UserDto updateUser(UUID userId, UserDto userDto) {
        log.info("Updating user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Update fields
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        user.setCity(userDto.getCity());
        user.setState(userDto.getState());
        user.setPostalCode(userDto.getPostalCode());
        user.setCountry(userDto.getCountry());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    @Transactional
    public UserDto updateUserRole(UUID userId, Role.RoleName role) {
        log.info("Updating user role to: {} for user: {}", role, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    @Transactional
    public UserDto activateUser(UUID userId) {
        log.info("Activating user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    @Transactional
    public UserDto deactivateUser(UUID userId) {
        log.info("Deactivating user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    @Transactional
    public AuthResponse authenticate(AuthRequest authRequest) {
        log.info("Authenticating user: {}", authRequest.getUsernameOrEmail());
        
        // Load user details
        UserDetails userDetails = loadUserByUsername(authRequest.getUsernameOrEmail());
        
        // Verify password manually
        if (!passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        // Generate tokens
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        // Update last login
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseGet(() -> userRepository.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found")));
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .build();
    }
    
    @Transactional(readOnly = true)
    public long getUserCountByRole(Role.RoleName role) {
        log.info("Counting users with role: {}", role);
        return userRepository.countByRole(role);
    }
    
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        log.info("Counting active users");
        return userRepository.countByActiveStatus(true);
    }
    
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing access token");
        
        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        // Extract username from token
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = loadUserByUsername(username);
        
        // Generate new tokens
        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);
        
        // Get user details
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("User not found")));
        
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .build();
    }
    
    @Transactional
    public void forgotPassword(String email) {
        log.info("Processing forgot password for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Generate reset token
        String resetToken = jwtService.generatePasswordResetToken(user.getEmail());
        
        // Send email with reset token
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken, user.getUsername());
        log.info("Password reset token generated and email sent for user: {}", user.getUsername());
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Processing password reset");
        
        // Validate reset token
        if (!jwtService.isPasswordResetTokenValid(token)) {
            throw new RuntimeException("Invalid reset token");
        }
        
        // Extract email from token
        String email = jwtService.extractEmailFromResetToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("Password reset successful for user: {}", user.getUsername());
    }
    
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
} 