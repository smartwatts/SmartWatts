package com.smartwatts.energyservice.repository;

import com.smartwatts.energyservice.model.EnergySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EnergySourceRepository extends JpaRepository<EnergySource, UUID> {
    
    Page<EnergySource> findByUserId(UUID userId, Pageable pageable);
    
    Page<EnergySource> findByUserIdAndSourceType(UUID userId, EnergySource.SourceType sourceType, Pageable pageable);
    
    Page<EnergySource> findByUserIdAndStatus(UUID userId, EnergySource.Status status, Pageable pageable);
    
    @Query("SELECT es FROM EnergySource es WHERE es.userId = :userId AND es.status = :status")
    List<EnergySource> findActiveSourcesByUserId(UUID userId, EnergySource.Status status);
    
    @Query("SELECT es FROM EnergySource es WHERE es.nextMaintenanceDate <= :date")
    List<EnergySource> findSourcesNeedingMaintenance(LocalDateTime date);
    
    @Query("SELECT COUNT(es) FROM EnergySource es WHERE es.userId = :userId AND es.status = :status")
    long countByUserIdAndStatus(UUID userId, EnergySource.Status status);
    
    @Query("SELECT COUNT(es) FROM EnergySource es WHERE es.userId = :userId AND es.sourceType = :sourceType")
    long countByUserIdAndSourceType(UUID userId, EnergySource.SourceType sourceType);
} 