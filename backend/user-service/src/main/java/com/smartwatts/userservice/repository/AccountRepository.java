package com.smartwatts.userservice.repository;

import com.smartwatts.userservice.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, java.util.UUID> {
    
    long countByStatus(Account.AccountStatus status);
    
    @Query("SELECT COALESCE(SUM(a.monthlyRevenue), 0) FROM Account a")
    Double sumMonthlyRevenue();
    
    @Query("SELECT COALESCE(SUM(a.devices), 0) FROM Account a")
    Long sumDevices();
    
    @Query("SELECT COALESCE(AVG(a.energySavings), 0) FROM Account a")
    Double averageEnergySavings();
    
    @Query("SELECT a FROM Account a WHERE " +
           "(:query IS NULL OR " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.contactPerson) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:type IS NULL OR a.type = :type) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Account> searchAccounts(@Param("query") String query, 
                                @Param("type") Account.AccountType type, 
                                @Param("status") Account.AccountStatus status, 
                                Pageable pageable);
}

