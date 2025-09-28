package com.smartwatts.billingservice.service;

import com.smartwatts.billingservice.model.Tariff;
import com.smartwatts.billingservice.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TariffCalculationService {
    
    private final TariffRepository tariffRepository;
    
    /**
     * Calculate electricity bill based on consumption and tariff
     */
    public TariffCalculationResult calculateElectricityBill(
            BigDecimal totalConsumptionKwh,
            BigDecimal peakConsumptionKwh,
            BigDecimal offPeakConsumptionKwh,
            BigDecimal nightConsumptionKwh,
            UUID tariffId,
            LocalDateTime billingDate) {
        
        log.info("Calculating electricity bill for tariff: {}, total consumption: {} kWh", tariffId, totalConsumptionKwh);
        
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new RuntimeException("Tariff not found with ID: " + tariffId));
        
        TariffCalculationResult result = new TariffCalculationResult();
        result.setTariffId(tariffId);
        result.setTariffCode(tariff.getTariffCode());
        
        // Calculate consumption charges
        BigDecimal peakAmount = calculatePeakAmount(peakConsumptionKwh, tariff);
        BigDecimal offPeakAmount = calculateOffPeakAmount(offPeakConsumptionKwh, tariff);
        BigDecimal nightAmount = calculateNightAmount(nightConsumptionKwh, tariff);
        
        result.setPeakConsumptionKwh(peakConsumptionKwh);
        result.setOffPeakConsumptionKwh(offPeakConsumptionKwh);
        result.setNightConsumptionKwh(nightConsumptionKwh);
        result.setPeakAmount(peakAmount);
        result.setOffPeakAmount(offPeakAmount);
        result.setNightAmount(nightAmount);
        
        // Calculate fixed charges
        BigDecimal serviceCharge = tariff.getServiceCharge() != null ? tariff.getServiceCharge() : BigDecimal.ZERO;
        BigDecimal meterRental = tariff.getMeterRental() != null ? tariff.getMeterRental() : BigDecimal.ZERO;
        BigDecimal demandCharge = tariff.getDemandCharge() != null ? tariff.getDemandCharge() : BigDecimal.ZERO;
        
        result.setServiceCharge(serviceCharge);
        result.setMeterRental(meterRental);
        result.setDemandCharge(demandCharge);
        
        // Calculate variable charges
        BigDecimal capacityCharge = calculateCapacityCharge(totalConsumptionKwh, tariff);
        BigDecimal transmissionCharge = calculateTransmissionCharge(totalConsumptionKwh, tariff);
        BigDecimal distributionCharge = calculateDistributionCharge(totalConsumptionKwh, tariff);
        BigDecimal regulatoryCharge = calculateRegulatoryCharge(totalConsumptionKwh, tariff);
        BigDecimal environmentalCharge = calculateEnvironmentalCharge(totalConsumptionKwh, tariff);
        
        result.setCapacityCharge(capacityCharge);
        result.setTransmissionCharge(transmissionCharge);
        result.setDistributionCharge(distributionCharge);
        result.setRegulatoryCharge(regulatoryCharge);
        result.setEnvironmentalCharge(environmentalCharge);
        
        // Calculate fuel adjustment
        BigDecimal fuelAdjustment = calculateFuelAdjustment(totalConsumptionKwh, tariff);
        result.setFuelAdjustment(fuelAdjustment);
        
        // Calculate subtotal
        BigDecimal consumptionSubtotal = peakAmount.add(offPeakAmount).add(nightAmount);
        BigDecimal fixedChargesSubtotal = serviceCharge.add(meterRental).add(demandCharge);
        BigDecimal variableChargesSubtotal = capacityCharge.add(transmissionCharge)
                .add(distributionCharge).add(regulatoryCharge).add(environmentalCharge);
        
        BigDecimal subtotal = consumptionSubtotal.add(fixedChargesSubtotal)
                .add(variableChargesSubtotal).add(fuelAdjustment);
        
        result.setSubtotal(subtotal);
        
        // Calculate tax
        BigDecimal taxAmount = calculateTaxAmount(subtotal, tariff);
        result.setTaxAmount(taxAmount);
        
        // Calculate final amount
        BigDecimal finalAmount = subtotal.add(taxAmount);
        result.setFinalAmount(finalAmount);
        
        // Apply minimum charge if applicable
        if (tariff.getMinimumCharge() != null && finalAmount.compareTo(tariff.getMinimumCharge()) < 0) {
            finalAmount = tariff.getMinimumCharge();
            result.setFinalAmount(finalAmount);
            result.setMinimumChargeApplied(true);
        }
        
        // Apply maximum charge if applicable
        if (tariff.getMaximumCharge() != null && finalAmount.compareTo(tariff.getMaximumCharge()) > 0) {
            finalAmount = tariff.getMaximumCharge();
            result.setFinalAmount(finalAmount);
            result.setMaximumChargeApplied(true);
        }
        
        log.info("Electricity bill calculation completed. Final amount: {}", finalAmount);
        return result;
    }
    
    /**
     * Calculate tiered billing based on consumption
     */
    public TariffCalculationResult calculateTieredBill(
            BigDecimal totalConsumptionKwh,
            UUID tariffId,
            LocalDateTime billingDate) {
        
        log.info("Calculating tiered bill for tariff: {}, consumption: {} kWh", tariffId, totalConsumptionKwh);
        
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new RuntimeException("Tariff not found with ID: " + tariffId));
        
        TariffCalculationResult result = new TariffCalculationResult();
        result.setTariffId(tariffId);
        result.setTariffCode(tariff.getTariffCode());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal remainingConsumption = totalConsumptionKwh;
        
        // Tier 1 calculation
        if (tariff.getTier1Limit() != null && remainingConsumption.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier1Consumption = remainingConsumption.min(tariff.getTier1Limit());
            BigDecimal tier1Amount = tier1Consumption.multiply(tariff.getTier1Rate());
            totalAmount = totalAmount.add(tier1Amount);
            remainingConsumption = remainingConsumption.subtract(tier1Consumption);
            
            result.setTier1Consumption(tier1Consumption);
            result.setTier1Amount(tier1Amount);
        }
        
        // Tier 2 calculation
        if (tariff.getTier2Limit() != null && remainingConsumption.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier2Consumption = remainingConsumption.min(tariff.getTier2Limit().subtract(tariff.getTier1Limit()));
            BigDecimal tier2Amount = tier2Consumption.multiply(tariff.getTier2Rate());
            totalAmount = totalAmount.add(tier2Amount);
            remainingConsumption = remainingConsumption.subtract(tier2Consumption);
            
            result.setTier2Consumption(tier2Consumption);
            result.setTier2Amount(tier2Amount);
        }
        
        // Tier 3 calculation
        if (tariff.getTier3Limit() != null && remainingConsumption.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier3Consumption = remainingConsumption.min(tariff.getTier3Limit().subtract(tariff.getTier2Limit()));
            BigDecimal tier3Amount = tier3Consumption.multiply(tariff.getTier3Rate());
            totalAmount = totalAmount.add(tier3Amount);
            remainingConsumption = remainingConsumption.subtract(tier3Consumption);
            
            result.setTier3Consumption(tier3Consumption);
            result.setTier3Amount(tier3Amount);
        }
        
        // Tier 4 calculation (unlimited)
        if (remainingConsumption.compareTo(BigDecimal.ZERO) > 0 && tariff.getTier4Rate() != null) {
            BigDecimal tier4Amount = remainingConsumption.multiply(tariff.getTier4Rate());
            totalAmount = totalAmount.add(tier4Amount);
            
            result.setTier4Consumption(remainingConsumption);
            result.setTier4Amount(tier4Amount);
        }
        
        result.setSubtotal(totalAmount);
        
        // Calculate tax
        BigDecimal taxAmount = calculateTaxAmount(totalAmount, tariff);
        result.setTaxAmount(taxAmount);
        
        // Calculate final amount
        BigDecimal finalAmount = totalAmount.add(taxAmount);
        result.setFinalAmount(finalAmount);
        
        log.info("Tiered bill calculation completed. Final amount: {}", finalAmount);
        return result;
    }
    
    /**
     * Get applicable tariff for customer and location
     */
    public Tariff getApplicableTariff(
            Tariff.CustomerCategory customerCategory,
            String discoCode,
            String region,
            LocalDateTime billingDate) {
        
        log.info("Finding applicable tariff for category: {}, disco: {}, region: {}", customerCategory, discoCode, region);
        
        List<Tariff> activeTariffs = tariffRepository.findActiveTariffs(billingDate);
        
        // Filter by customer category
        List<Tariff> categoryTariffs = activeTariffs.stream()
                .filter(t -> t.getCustomerCategory() == customerCategory)
                .toList();
        
        if (categoryTariffs.isEmpty()) {
            throw new RuntimeException("No active tariff found for customer category: " + customerCategory);
        }
        
        // Filter by disco code if provided
        if (discoCode != null && !discoCode.isEmpty()) {
            List<Tariff> discoTariffs = categoryTariffs.stream()
                    .filter(t -> discoCode.equals(t.getDiscoCode()))
                    .toList();
            
            if (!discoTariffs.isEmpty()) {
                return discoTariffs.get(0); // Return first matching tariff
            }
        }
        
        // Filter by region if provided
        if (region != null && !region.isEmpty()) {
            List<Tariff> regionTariffs = categoryTariffs.stream()
                    .filter(t -> region.equals(t.getRegion()))
                    .toList();
            
            if (!regionTariffs.isEmpty()) {
                return regionTariffs.get(0); // Return first matching tariff
            }
        }
        
        // Return first available tariff for the category
        return categoryTariffs.get(0);
    }
    
    // Private calculation methods
    private BigDecimal calculatePeakAmount(BigDecimal peakConsumption, Tariff tariff) {
        if (peakConsumption == null || tariff.getPeakRate() == null) {
            return BigDecimal.ZERO;
        }
        return peakConsumption.multiply(tariff.getPeakRate()).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateOffPeakAmount(BigDecimal offPeakConsumption, Tariff tariff) {
        if (offPeakConsumption == null || tariff.getOffPeakRate() == null) {
            return BigDecimal.ZERO;
        }
        return offPeakConsumption.multiply(tariff.getOffPeakRate()).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateNightAmount(BigDecimal nightConsumption, Tariff tariff) {
        if (nightConsumption == null || tariff.getNightRate() == null) {
            return BigDecimal.ZERO;
        }
        return nightConsumption.multiply(tariff.getNightRate()).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateCapacityCharge(BigDecimal consumption, Tariff tariff) {
        if (consumption == null || tariff.getCapacityCharge() == null) {
            return BigDecimal.ZERO;
        }
        return tariff.getCapacityCharge().setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateTransmissionCharge(BigDecimal consumption, Tariff tariff) {
        if (consumption == null || tariff.getTransmissionCharge() == null) {
            return BigDecimal.ZERO;
        }
        return tariff.getTransmissionCharge().setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateDistributionCharge(BigDecimal consumption, Tariff tariff) {
        if (consumption == null || tariff.getDistributionCharge() == null) {
            return BigDecimal.ZERO;
        }
        return tariff.getDistributionCharge().setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateRegulatoryCharge(BigDecimal consumption, Tariff tariff) {
        if (consumption == null || tariff.getRegulatoryCharge() == null) {
            return BigDecimal.ZERO;
        }
        return tariff.getRegulatoryCharge().setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateEnvironmentalCharge(BigDecimal consumption, Tariff tariff) {
        if (consumption == null || tariff.getEnvironmentalCharge() == null) {
            return BigDecimal.ZERO;
        }
        return tariff.getEnvironmentalCharge().setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateFuelAdjustment(BigDecimal consumption, Tariff tariff) {
        if (consumption == null || tariff.getFuelAdjustmentRate() == null) {
            return BigDecimal.ZERO;
        }
        return consumption.multiply(tariff.getFuelAdjustmentRate()).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateTaxAmount(BigDecimal subtotal, Tariff tariff) {
        if (subtotal == null || tariff.getTaxRate() == null) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(tariff.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
    
    // Result class for tariff calculations
    public static class TariffCalculationResult {
        private UUID tariffId;
        private String tariffCode;
        private BigDecimal peakConsumptionKwh;
        private BigDecimal offPeakConsumptionKwh;
        private BigDecimal nightConsumptionKwh;
        private BigDecimal peakAmount;
        private BigDecimal offPeakAmount;
        private BigDecimal nightAmount;
        private BigDecimal serviceCharge;
        private BigDecimal meterRental;
        private BigDecimal demandCharge;
        private BigDecimal capacityCharge;
        private BigDecimal transmissionCharge;
        private BigDecimal distributionCharge;
        private BigDecimal regulatoryCharge;
        private BigDecimal environmentalCharge;
        private BigDecimal fuelAdjustment;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal finalAmount;
        private BigDecimal tier1Consumption;
        private BigDecimal tier1Amount;
        private BigDecimal tier2Consumption;
        private BigDecimal tier2Amount;
        private BigDecimal tier3Consumption;
        private BigDecimal tier3Amount;
        private BigDecimal tier4Consumption;
        private BigDecimal tier4Amount;
        private boolean minimumChargeApplied;
        private boolean maximumChargeApplied;
        
        // Getters and setters
        public UUID getTariffId() { return tariffId; }
        public void setTariffId(UUID tariffId) { this.tariffId = tariffId; }
        
        public String getTariffCode() { return tariffCode; }
        public void setTariffCode(String tariffCode) { this.tariffCode = tariffCode; }
        
        public BigDecimal getPeakConsumptionKwh() { return peakConsumptionKwh; }
        public void setPeakConsumptionKwh(BigDecimal peakConsumptionKwh) { this.peakConsumptionKwh = peakConsumptionKwh; }
        
        public BigDecimal getOffPeakConsumptionKwh() { return offPeakConsumptionKwh; }
        public void setOffPeakConsumptionKwh(BigDecimal offPeakConsumptionKwh) { this.offPeakConsumptionKwh = offPeakConsumptionKwh; }
        
        public BigDecimal getNightConsumptionKwh() { return nightConsumptionKwh; }
        public void setNightConsumptionKwh(BigDecimal nightConsumptionKwh) { this.nightConsumptionKwh = nightConsumptionKwh; }
        
        public BigDecimal getPeakAmount() { return peakAmount; }
        public void setPeakAmount(BigDecimal peakAmount) { this.peakAmount = peakAmount; }
        
        public BigDecimal getOffPeakAmount() { return offPeakAmount; }
        public void setOffPeakAmount(BigDecimal offPeakAmount) { this.offPeakAmount = offPeakAmount; }
        
        public BigDecimal getNightAmount() { return nightAmount; }
        public void setNightAmount(BigDecimal nightAmount) { this.nightAmount = nightAmount; }
        
        public BigDecimal getServiceCharge() { return serviceCharge; }
        public void setServiceCharge(BigDecimal serviceCharge) { this.serviceCharge = serviceCharge; }
        
        public BigDecimal getMeterRental() { return meterRental; }
        public void setMeterRental(BigDecimal meterRental) { this.meterRental = meterRental; }
        
        public BigDecimal getDemandCharge() { return demandCharge; }
        public void setDemandCharge(BigDecimal demandCharge) { this.demandCharge = demandCharge; }
        
        public BigDecimal getCapacityCharge() { return capacityCharge; }
        public void setCapacityCharge(BigDecimal capacityCharge) { this.capacityCharge = capacityCharge; }
        
        public BigDecimal getTransmissionCharge() { return transmissionCharge; }
        public void setTransmissionCharge(BigDecimal transmissionCharge) { this.transmissionCharge = transmissionCharge; }
        
        public BigDecimal getDistributionCharge() { return distributionCharge; }
        public void setDistributionCharge(BigDecimal distributionCharge) { this.distributionCharge = distributionCharge; }
        
        public BigDecimal getRegulatoryCharge() { return regulatoryCharge; }
        public void setRegulatoryCharge(BigDecimal regulatoryCharge) { this.regulatoryCharge = regulatoryCharge; }
        
        public BigDecimal getEnvironmentalCharge() { return environmentalCharge; }
        public void setEnvironmentalCharge(BigDecimal environmentalCharge) { this.environmentalCharge = environmentalCharge; }
        
        public BigDecimal getFuelAdjustment() { return fuelAdjustment; }
        public void setFuelAdjustment(BigDecimal fuelAdjustment) { this.fuelAdjustment = fuelAdjustment; }
        
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        
        public BigDecimal getTaxAmount() { return taxAmount; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
        
        public BigDecimal getFinalAmount() { return finalAmount; }
        public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
        
        public BigDecimal getTier1Consumption() { return tier1Consumption; }
        public void setTier1Consumption(BigDecimal tier1Consumption) { this.tier1Consumption = tier1Consumption; }
        
        public BigDecimal getTier1Amount() { return tier1Amount; }
        public void setTier1Amount(BigDecimal tier1Amount) { this.tier1Amount = tier1Amount; }
        
        public BigDecimal getTier2Consumption() { return tier2Consumption; }
        public void setTier2Consumption(BigDecimal tier2Consumption) { this.tier2Consumption = tier2Consumption; }
        
        public BigDecimal getTier2Amount() { return tier2Amount; }
        public void setTier2Amount(BigDecimal tier2Amount) { this.tier2Amount = tier2Amount; }
        
        public BigDecimal getTier3Consumption() { return tier3Consumption; }
        public void setTier3Consumption(BigDecimal tier3Consumption) { this.tier3Consumption = tier3Consumption; }
        
        public BigDecimal getTier3Amount() { return tier3Amount; }
        public void setTier3Amount(BigDecimal tier3Amount) { this.tier3Amount = tier3Amount; }
        
        public BigDecimal getTier4Consumption() { return tier4Consumption; }
        public void setTier4Consumption(BigDecimal tier4Consumption) { this.tier4Consumption = tier4Consumption; }
        
        public BigDecimal getTier4Amount() { return tier4Amount; }
        public void setTier4Amount(BigDecimal tier4Amount) { this.tier4Amount = tier4Amount; }
        
        public boolean isMinimumChargeApplied() { return minimumChargeApplied; }
        public void setMinimumChargeApplied(boolean minimumChargeApplied) { this.minimumChargeApplied = minimumChargeApplied; }
        
        public boolean isMaximumChargeApplied() { return maximumChargeApplied; }
        public void setMaximumChargeApplied(boolean maximumChargeApplied) { this.maximumChargeApplied = maximumChargeApplied; }
    }
} 