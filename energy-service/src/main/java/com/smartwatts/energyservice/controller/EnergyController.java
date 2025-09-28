package com.smartwatts.energyservice.controller;

import com.smartwatts.energyservice.model.Energy;
import com.smartwatts.energyservice.service.EnergyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/energy")
public class EnergyController {
    @Autowired
    private EnergyService energyService;

    @GetMapping
    public List<Energy> getAll() {
        return energyService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Energy> getById(@PathVariable Long id) {
        Optional<Energy> energy = energyService.findById(id);
        return energy.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Energy create(@RequestBody Energy energy) {
        return energyService.save(energy);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Energy> update(@PathVariable Long id, @RequestBody Energy updated) {
        return energyService.findById(id)
                .map(existing -> {
                    updated.setId(id);
                    return ResponseEntity.ok(energyService.save(updated));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (energyService.findById(id).isPresent()) {
            energyService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
} 