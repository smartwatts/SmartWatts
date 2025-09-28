package com.smartwatts.energyservice.service;

import com.smartwatts.energyservice.model.Energy;
import com.smartwatts.energyservice.repository.EnergyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnergyService {
    @Autowired
    private EnergyRepository energyRepository;

    public List<Energy> findAll() {
        return energyRepository.findAll();
    }

    public Optional<Energy> findById(Long id) {
        return energyRepository.findById(id);
    }

    public Energy save(Energy energy) {
        return energyRepository.save(energy);
    }

    public void deleteById(Long id) {
        energyRepository.deleteById(id);
    }
} 