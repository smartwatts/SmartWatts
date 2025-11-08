package com.smartwatts.userservice.service;

import com.smartwatts.userservice.controller.AccountController.AccountStats;
import com.smartwatts.userservice.dto.AccountDto;
import com.smartwatts.userservice.model.Account;
import com.smartwatts.userservice.repository.AccountRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private AccountDto testAccountDto;
    private UUID testAccountId;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();

        testAccount = Account.builder()
                .id(testAccountId)
                .name("Test Account")
                .type(Account.AccountType.ENTERPRISE)
                .status(Account.AccountStatus.ACTIVE)
                .contactPerson("John Doe")
                .email("account@example.com")
                .phone("+2341234567890")
                .address("123 Test St")
                .city("Lagos")
                .state("Lagos")
                .subscriptionPlan("Premium")
                .monthlyRevenue(1000.0)
                .devices(10)
                .energySavings(500.0)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testAccountDto = AccountDto.builder()
                .id(testAccountId)
                .name("Test Account")
                .type(AccountDto.AccountType.ENTERPRISE)
                .status(AccountDto.AccountStatus.ACTIVE)
                .contactPerson("John Doe")
                .email("account@example.com")
                .phone("+2341234567890")
                .address("123 Test St")
                .city("Lagos")
                .state("Lagos")
                .subscriptionPlan("Premium")
                .monthlyRevenue(1000.0)
                .devices(10)
                .energySavings(500.0)
                .build();
    }

    @Test
    void getAllAccounts_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Arrays.asList(testAccount), pageable, 1);
        when(accountRepository.findAll(pageable)).thenReturn(accountPage);

        // When
        Page<AccountDto> result = accountService.getAllAccounts(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(accountRepository).findAll(pageable);
    }

    @Test
    void getAccountById_Success_ReturnsAccountDto() {
        // Given
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        // When
        AccountDto result = accountService.getAccountById(testAccountId);

        // Then
        assertNotNull(result);
        assertEquals(testAccountId, result.getId());
        assertEquals("Test Account", result.getName());
        verify(accountRepository).findById(testAccountId);
    }

    @Test
    void getAccountById_NotFound_ThrowsException() {
        // Given
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            accountService.getAccountById(testAccountId);
        });
    }

    @Test
    void createAccount_Success_CreatesAccount() {
        // Given
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountDto result = accountService.createAccount(testAccountDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Account", result.getName());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_Success_UpdatesAccount() {
        // Given
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        testAccountDto.setName("Updated Account");
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountDto result = accountService.updateAccount(testAccountId, testAccountDto);

        // Then
        assertNotNull(result);
        verify(accountRepository).findById(testAccountId);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_NotFound_ThrowsException() {
        // Given
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            accountService.updateAccount(testAccountId, testAccountDto);
        });
    }

    @Test
    void deleteAccount_Success_DeletesAccount() {
        // Given
        when(accountRepository.existsById(testAccountId)).thenReturn(true);
        doNothing().when(accountRepository).deleteById(testAccountId);

        // When
        accountService.deleteAccount(testAccountId);

        // Then
        verify(accountRepository).existsById(testAccountId);
        verify(accountRepository).deleteById(testAccountId);
    }

    @Test
    void deleteAccount_NotFound_ThrowsException() {
        // Given
        when(accountRepository.existsById(testAccountId)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            accountService.deleteAccount(testAccountId);
        });
    }

    @Test
    void getAccountStats_Success_ReturnsStats() {
        // Given
        when(accountRepository.count()).thenReturn(10L);
        when(accountRepository.countByStatus(Account.AccountStatus.ACTIVE)).thenReturn(8L);
        when(accountRepository.sumMonthlyRevenue()).thenReturn(10000.0);
        when(accountRepository.sumDevices()).thenReturn(100L);
        when(accountRepository.averageEnergySavings()).thenReturn(500.0);

        // When
        AccountStats result = accountService.getAccountStats();

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getTotalAccounts());
        assertEquals(8L, result.getActiveAccounts());
        assertEquals(10000.0, result.getTotalRevenue());
        assertEquals(100, result.getTotalDevices());
        assertEquals(500.0, result.getAverageSavings());
        verify(accountRepository).count();
        verify(accountRepository).countByStatus(Account.AccountStatus.ACTIVE);
    }

    @Test
    void getAccountStats_WithNullValues_ReturnsZero() {
        // Given
        when(accountRepository.count()).thenReturn(0L);
        when(accountRepository.countByStatus(Account.AccountStatus.ACTIVE)).thenReturn(0L);
        when(accountRepository.sumMonthlyRevenue()).thenReturn(null);
        when(accountRepository.sumDevices()).thenReturn(null);
        when(accountRepository.averageEnergySavings()).thenReturn(null);

        // When
        AccountStats result = accountService.getAccountStats();

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getTotalRevenue());
        assertEquals(0L, result.getTotalDevices());
        assertEquals(0.0, result.getAverageSavings());
    }

    @Test
    void searchAccounts_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Arrays.asList(testAccount), pageable, 1);
        when(accountRepository.searchAccounts(anyString(), any(), any(), eq(pageable))).thenReturn(accountPage);

        // When
        Page<AccountDto> result = accountService.searchAccounts("Test", "ENTERPRISE", "ACTIVE", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(accountRepository).searchAccounts(anyString(), any(), any(), eq(pageable));
    }

    @Test
    void searchAccounts_WithAllType_IgnoresType() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Arrays.asList(testAccount), pageable, 1);
        when(accountRepository.searchAccounts(anyString(), isNull(), any(), eq(pageable))).thenReturn(accountPage);

        // When
        Page<AccountDto> result = accountService.searchAccounts("Test", "All", "ACTIVE", pageable);

        // Then
        assertNotNull(result);
        verify(accountRepository).searchAccounts(anyString(), isNull(), any(), eq(pageable));
    }

    @Test
    void searchAccounts_WithInvalidType_IgnoresType() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Arrays.asList(testAccount), pageable, 1);
        when(accountRepository.searchAccounts(anyString(), isNull(), any(), eq(pageable))).thenReturn(accountPage);

        // When
        Page<AccountDto> result = accountService.searchAccounts("Test", "INVALID", "ACTIVE", pageable);

        // Then
        assertNotNull(result);
        verify(accountRepository).searchAccounts(anyString(), isNull(), any(), eq(pageable));
    }
}

