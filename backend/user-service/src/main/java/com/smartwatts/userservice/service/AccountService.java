package com.smartwatts.userservice.service;

import com.smartwatts.userservice.controller.AccountController.AccountStats;
import com.smartwatts.userservice.dto.AccountDto;
import com.smartwatts.userservice.model.Account;
import com.smartwatts.userservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    public Page<AccountDto> getAllAccounts(Pageable pageable) {
        log.info("Fetching all accounts with pagination");
        Page<Account> accounts = accountRepository.findAll(pageable);
        return accounts.map(this::convertToDto);
    }
    
    public AccountDto getAccountById(UUID accountId) {
        log.info("Fetching account with ID: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        return convertToDto(account);
    }
    
    public AccountDto createAccount(AccountDto accountDto) {
        log.info("Creating new account: {}", accountDto.getName());
        Account account = convertToEntity(accountDto);
        account.setCreatedAt(LocalDate.now());
        account.setUpdatedAt(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);
        return convertToDto(savedAccount);
    }
    
    public AccountDto updateAccount(UUID accountId, AccountDto accountDto) {
        log.info("Updating account with ID: {}", accountId);
        Account existingAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        
        updateAccountFromDto(existingAccount, accountDto);
        existingAccount.setUpdatedAt(LocalDateTime.now());
        Account updatedAccount = accountRepository.save(existingAccount);
        return convertToDto(updatedAccount);
    }
    
    public void deleteAccount(UUID accountId) {
        log.info("Deleting account with ID: {}", accountId);
        if (!accountRepository.existsById(accountId)) {
            throw new RuntimeException("Account not found with ID: " + accountId);
        }
        accountRepository.deleteById(accountId);
    }
    
    public AccountStats getAccountStats() {
        log.info("Calculating account statistics");
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.countByStatus(Account.AccountStatus.ACTIVE);
        Double totalRevenue = accountRepository.sumMonthlyRevenue();
        Long totalDevices = accountRepository.sumDevices();
        Double averageSavings = accountRepository.averageEnergySavings();
        
        return new AccountStats(
            totalAccounts,
            activeAccounts,
            totalRevenue != null ? totalRevenue : 0.0,
            totalDevices != null ? totalDevices : 0L,
            averageSavings != null ? averageSavings : 0.0
        );
    }
    
    public Page<AccountDto> searchAccounts(String query, String type, String status, Pageable pageable) {
        log.info("Searching accounts with query: {}, type: {}, status: {}", query, type, status);
        
        Account.AccountType accountType = null;
        if (type != null && !type.equals("All")) {
            try {
                accountType = Account.AccountType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid account type: {}", type);
            }
        }
        
        Account.AccountStatus accountStatus = null;
        if (status != null && !status.equals("All")) {
            try {
                accountStatus = Account.AccountStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid account status: {}", status);
            }
        }
        
        Page<Account> accounts = accountRepository.searchAccounts(query, accountType, accountStatus, pageable);
        return accounts.map(this::convertToDto);
    }
    
    private AccountDto convertToDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .name(account.getName())
                .type(AccountDto.AccountType.valueOf(account.getType().name()))
                .status(AccountDto.AccountStatus.valueOf(account.getStatus().name()))
                .contactPerson(account.getContactPerson())
                .email(account.getEmail())
                .phone(account.getPhone())
                .address(account.getAddress())
                .city(account.getCity())
                .state(account.getState())
                .subscriptionPlan(account.getSubscriptionPlan())
                .monthlyRevenue(account.getMonthlyRevenue())
                .lastPayment(account.getLastPayment())
                .nextBilling(account.getNextBilling())
                .devices(account.getDevices())
                .energySavings(account.getEnergySavings())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
    
    private Account convertToEntity(AccountDto accountDto) {
        Account account = new Account();
        account.setName(accountDto.getName());
        account.setType(Account.AccountType.valueOf(accountDto.getType().name()));
        account.setStatus(Account.AccountStatus.valueOf(accountDto.getStatus().name()));
        account.setContactPerson(accountDto.getContactPerson());
        account.setEmail(accountDto.getEmail());
        account.setPhone(accountDto.getPhone());
        account.setAddress(accountDto.getAddress());
        account.setCity(accountDto.getCity());
        account.setState(accountDto.getState());
        account.setSubscriptionPlan(accountDto.getSubscriptionPlan());
        account.setMonthlyRevenue(accountDto.getMonthlyRevenue());
        account.setLastPayment(accountDto.getLastPayment());
        account.setNextBilling(accountDto.getNextBilling());
        account.setDevices(accountDto.getDevices());
        account.setEnergySavings(accountDto.getEnergySavings());
        return account;
    }
    
    private void updateAccountFromDto(Account account, AccountDto accountDto) {
        if (accountDto.getName() != null) account.setName(accountDto.getName());
        if (accountDto.getType() != null) account.setType(Account.AccountType.valueOf(accountDto.getType().name()));
        if (accountDto.getStatus() != null) account.setStatus(Account.AccountStatus.valueOf(accountDto.getStatus().name()));
        if (accountDto.getContactPerson() != null) account.setContactPerson(accountDto.getContactPerson());
        if (accountDto.getEmail() != null) account.setEmail(accountDto.getEmail());
        if (accountDto.getPhone() != null) account.setPhone(accountDto.getPhone());
        if (accountDto.getAddress() != null) account.setAddress(accountDto.getAddress());
        if (accountDto.getCity() != null) account.setCity(accountDto.getCity());
        if (accountDto.getState() != null) account.setState(accountDto.getState());
        if (accountDto.getSubscriptionPlan() != null) account.setSubscriptionPlan(accountDto.getSubscriptionPlan());
        if (accountDto.getMonthlyRevenue() != null) account.setMonthlyRevenue(accountDto.getMonthlyRevenue());
        if (accountDto.getLastPayment() != null) account.setLastPayment(accountDto.getLastPayment());
        if (accountDto.getNextBilling() != null) account.setNextBilling(accountDto.getNextBilling());
        if (accountDto.getDevices() != null) account.setDevices(accountDto.getDevices());
        if (accountDto.getEnergySavings() != null) account.setEnergySavings(accountDto.getEnergySavings());
    }
}

