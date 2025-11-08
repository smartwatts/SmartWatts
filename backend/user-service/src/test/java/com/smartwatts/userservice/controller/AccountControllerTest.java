package com.smartwatts.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.userservice.dto.AccountDto;
import com.smartwatts.userservice.service.AccountService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.smartwatts.userservice.config.TestSecurityConfig;
import com.smartwatts.userservice.TestApplication;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AccountController.class, 
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
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountDto testAccountDto;
    private UUID testAccountId;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        
        testAccountDto = new AccountDto();
        testAccountDto.setId(testAccountId);
        testAccountDto.setName("Test Account");
        testAccountDto.setEmail("account@example.com");
        testAccountDto.setContactPerson("John Doe");
        testAccountDto.setPhone("+2341234567890");
        testAccountDto.setType(AccountDto.AccountType.ENTERPRISE);
        testAccountDto.setStatus(AccountDto.AccountStatus.ACTIVE);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllAccounts_Success_ReturnsPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccountDto> page = new PageImpl<>(Arrays.asList(testAccountDto), pageable, 1);
        when(accountService.getAllAccounts(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Account"));

        verify(accountService).getAllAccounts(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAccountById_Success_ReturnsAccountDto() throws Exception {
        // Given
        when(accountService.getAccountById(testAccountId)).thenReturn(testAccountDto);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/{accountId}", testAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAccountId.toString()))
                .andExpect(jsonPath("$.name").value("Test Account"));

        verify(accountService).getAccountById(testAccountId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createAccount_Success_ReturnsCreated() throws Exception {
        // Given
        when(accountService.createAccount(any(AccountDto.class))).thenReturn(testAccountDto);

        // When & Then
        mockMvc.perform(post("/api/v1/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccountDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Account"));

        verify(accountService).createAccount(any(AccountDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateAccount_Success_ReturnsUpdatedAccount() throws Exception {
        // Given
        testAccountDto.setName("Updated Account");
        when(accountService.updateAccount(eq(testAccountId), any(AccountDto.class))).thenReturn(testAccountDto);

        // When & Then
        mockMvc.perform(put("/api/v1/accounts/{accountId}", testAccountId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccountDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Account"));

        verify(accountService).updateAccount(eq(testAccountId), any(AccountDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteAccount_Success_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(accountService).deleteAccount(testAccountId);

        // When & Then
        mockMvc.perform(delete("/api/v1/accounts/{accountId}", testAccountId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(accountService).deleteAccount(testAccountId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAccountStats_Success_ReturnsStats() throws Exception {
        // Given
        AccountController.AccountStats stats = new AccountController.AccountStats();
        stats.setTotalAccounts(10L);
        when(accountService.getAccountStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(10));

        verify(accountService).getAccountStats();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void searchAccounts_Success_ReturnsPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccountDto> page = new PageImpl<>(Arrays.asList(testAccountDto), pageable, 1);
        when(accountService.searchAccounts(anyString(), any(), any(), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/search")
                .param("query", "Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(accountService).searchAccounts(anyString(), any(), any(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void getAllAccounts_WithoutAdminRole_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isForbidden());

        verify(accountService, never()).getAllAccounts(any(Pageable.class));
    }
}

