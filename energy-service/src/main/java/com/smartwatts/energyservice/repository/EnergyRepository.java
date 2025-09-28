package com.smartwatts.energyservice.repository;

import com.smartwatts.energyservice.model.Energy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnergyRepository extends JpaRepository<Energy, Long> {
} 