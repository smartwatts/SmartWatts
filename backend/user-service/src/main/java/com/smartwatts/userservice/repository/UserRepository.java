package com.smartwatts.userservice.repository;

import com.smartwatts.userservice.model.Role;
import com.smartwatts.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    Optional<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.phoneNumber = :phoneNumber")
    Optional<User> findByEmailOrPhoneNumber(@Param("email") String email, @Param("phoneNumber") String phoneNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByUsername(String username);
    
    Page<User> findByRole(Role.RoleName role, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isActive = :isActive")
    Page<User> findByActiveStatus(@Param("isActive") boolean isActive, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role.RoleName role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = :isActive")
    long countByActiveStatus(@Param("isActive") boolean isActive);
} 