package com.smartwatts.billingservice.service;

import com.smartwatts.billingservice.dto.BillDto;
import com.smartwatts.billingservice.dto.BillItemDto;
import com.smartwatts.billingservice.model.Bill;
import com.smartwatts.billingservice.model.BillItem;
import com.smartwatts.billingservice.model.Tariff;
import com.smartwatts.billingservice.repository.BillRepository;
import com.smartwatts.billingservice.repository.BillItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillGenerationService {
    
    private final BillRepository billRepository; // Used for bill persistence
    private final BillItemRepository billItemRepository; // Used for bill item persistence
    
    /**
     * Check if repositories are available
     */
    public boolean areRepositoriesAvailable() {
        return billRepository != null && billItemRepository != null;
    }
    
    /**
     * Convert Bill entity to BillDto
     */
    private BillDto convertToDto(Bill bill) {
        BillDto dto = new BillDto();
        dto.setId(bill.getId());
        dto.setUserId(bill.getUserId());
        dto.setBillNumber(bill.getBillNumber());
        dto.setBillType(bill.getBillType());
        dto.setBillingPeriodStart(bill.getBillingPeriodStart());
        dto.setBillingPeriodEnd(bill.getBillingPeriodEnd());
        dto.setDueDate(bill.getDueDate());
        dto.setTotalAmount(bill.getTotalAmount());
        dto.setStatus(bill.getStatus());
        dto.setCreatedAt(bill.getCreatedAt());
        dto.setUpdatedAt(bill.getUpdatedAt());
        return dto;
    }
    private final TariffCalculationService tariffCalculationService;
    
    /**
     * Generate electricity bill for a user based on consumption data
     */
    @Transactional
    public BillDto generateElectricityBill(
            UUID userId,
            BigDecimal totalConsumptionKwh,
            BigDecimal peakConsumptionKwh,
            BigDecimal offPeakConsumptionKwh,
            BigDecimal nightConsumptionKwh,
            LocalDateTime billingPeriodStart,
            LocalDateTime billingPeriodEnd,
            String meterNumber,
            String accountNumber,
            String customerName,
            String customerAddress,
            String customerPhone,
            String customerEmail) {
        
        log.info("Generating electricity bill for user: {}, consumption: {} kWh", userId, totalConsumptionKwh);
        
        // Get applicable tariff (this would typically be based on user's location and customer category)
        Tariff tariff = getDefaultTariff(); // In real implementation, get from user profile
        
        // Calculate bill using tariff
        TariffCalculationService.TariffCalculationResult calculation = tariffCalculationService.calculateElectricityBill(
                totalConsumptionKwh, peakConsumptionKwh, offPeakConsumptionKwh, nightConsumptionKwh,
                tariff.getId(), LocalDateTime.now());
        
        // Create bill
        BillDto billDto = BillDto.builder()
                .userId(userId)
                .billType(Bill.BillType.GRID_ELECTRICITY)
                .status(Bill.BillStatus.ISSUED)
                .billingPeriodStart(billingPeriodStart)
                .billingPeriodEnd(billingPeriodEnd)
                .dueDate(billingPeriodEnd.plusDays(15)) // 15 days grace period
                .issuedDate(LocalDateTime.now())
                .totalConsumptionKwh(totalConsumptionKwh)
                .totalAmount(calculation.getSubtotal())
                .taxAmount(calculation.getTaxAmount())
                .finalAmount(calculation.getFinalAmount())
                .balanceDue(calculation.getFinalAmount())
                .currency("NGN")
                .meterNumber(meterNumber)
                .accountNumber(accountNumber)
                .customerName(customerName)
                .customerAddress(customerAddress)
                .customerPhone(customerPhone)
                .customerEmail(customerEmail)
                .build();
        
        // Create bill directly using repository
        Bill bill = new Bill();
        bill.setUserId(billDto.getUserId());
        bill.setBillNumber(billDto.getBillNumber());
        bill.setBillType(billDto.getBillType());
        bill.setBillingPeriodStart(billDto.getBillingPeriodStart());
        bill.setBillingPeriodEnd(billDto.getBillingPeriodEnd());
        bill.setDueDate(billDto.getDueDate());
        bill.setTotalAmount(billDto.getTotalAmount());
        bill.setStatus(billDto.getStatus());
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        
        Bill savedBill = billRepository.save(bill);
        BillDto createdBill = convertToDto(savedBill);
        
        // Create bill items
        createBillItems(createdBill.getId(), calculation, tariff);
        
        log.info("Electricity bill generated successfully. Bill ID: {}", createdBill.getId());
        return createdBill;
    }
    
    /**
     * Generate solar generation bill
     */
    @Transactional
    public BillDto generateSolarBill(
            UUID userId,
            BigDecimal generationKwh,
            LocalDateTime billingPeriodStart,
            LocalDateTime billingPeriodEnd) {
        
        log.info("Generating solar bill for user: {}, generation: {} kWh", userId, generationKwh);
        
        // Create bill for solar generation (typically for feed-in tariff or net metering)
        BillDto billDto = BillDto.builder()
                .userId(userId)
                .billType(Bill.BillType.SOLAR_GENERATION)
                .status(Bill.BillStatus.ISSUED)
                .billingPeriodStart(billingPeriodStart)
                .billingPeriodEnd(billingPeriodEnd)
                .dueDate(billingPeriodEnd.plusDays(15))
                .issuedDate(LocalDateTime.now())
                .totalConsumptionKwh(generationKwh)
                .totalAmount(BigDecimal.ZERO) // Solar generation typically doesn't incur charges
                .finalAmount(BigDecimal.ZERO)
                .balanceDue(BigDecimal.ZERO)
                .currency("NGN")
                .build();
        
        // Create bill directly using repository
        Bill bill = new Bill();
        bill.setUserId(billDto.getUserId());
        bill.setBillNumber(billDto.getBillNumber());
        bill.setBillType(billDto.getBillType());
        bill.setBillingPeriodStart(billDto.getBillingPeriodStart());
        bill.setBillingPeriodEnd(billDto.getBillingPeriodEnd());
        bill.setDueDate(billDto.getDueDate());
        bill.setTotalAmount(billDto.getTotalAmount());
        bill.setStatus(billDto.getStatus());
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        
        Bill savedBill = billRepository.save(bill);
        BillDto createdBill = convertToDto(savedBill);
        
        // Create bill item for solar generation
        BillItemDto itemDto = BillItemDto.builder()
                .billId(createdBill.getId())
                .itemName("Solar Generation")
                .itemDescription("Solar energy generation for billing period")
                .itemType(BillItem.ItemType.ELECTRICITY_CONSUMPTION)
                .quantity(generationKwh)
                .unit("kWh")
                .unitPrice(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .consumptionKwh(generationKwh)
                .build();
        
        // Create bill item directly using repository
        BillItem item = new BillItem();
        item.setBillId(itemDto.getBillId());
        item.setItemType(itemDto.getItemType());
        item.setItemDescription(itemDto.getItemDescription());
        item.setQuantity(itemDto.getQuantity());
        item.setUnitPrice(itemDto.getUnitPrice());
        item.setTotalAmount(itemDto.getTotalAmount());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        billItemRepository.save(item);
        
        log.info("Solar bill generated successfully. Bill ID: {}", createdBill.getId());
        return createdBill;
    }
    
    /**
     * Generate generator fuel bill
     */
    @Transactional
    public BillDto generateGeneratorBill(
            UUID userId,
            BigDecimal fuelConsumptionLiters,
            BigDecimal fuelPricePerLiter,
            LocalDateTime billingPeriodStart,
            LocalDateTime billingPeriodEnd) {
        
        log.info("Generating generator bill for user: {}, fuel consumption: {} liters", userId, fuelConsumptionLiters);
        
        BigDecimal totalAmount = fuelConsumptionLiters.multiply(fuelPricePerLiter);
        
        BillDto billDto = BillDto.builder()
                .userId(userId)
                .billType(Bill.BillType.GENERATOR_FUEL)
                .status(Bill.BillStatus.ISSUED)
                .billingPeriodStart(billingPeriodStart)
                .billingPeriodEnd(billingPeriodEnd)
                .dueDate(billingPeriodEnd.plusDays(15))
                .issuedDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .finalAmount(totalAmount)
                .balanceDue(totalAmount)
                .currency("NGN")
                .build();
        
        // Create bill directly using repository
        Bill bill = new Bill();
        bill.setUserId(billDto.getUserId());
        bill.setBillNumber(billDto.getBillNumber());
        bill.setBillType(billDto.getBillType());
        bill.setBillingPeriodStart(billDto.getBillingPeriodStart());
        bill.setBillingPeriodEnd(billDto.getBillingPeriodEnd());
        bill.setDueDate(billDto.getDueDate());
        bill.setTotalAmount(billDto.getTotalAmount());
        bill.setStatus(billDto.getStatus());
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        
        Bill savedBill = billRepository.save(bill);
        BillDto createdBill = convertToDto(savedBill);
        
        // Create bill item for fuel consumption
        BillItemDto itemDto = BillItemDto.builder()
                .billId(createdBill.getId())
                .itemName("Generator Fuel")
                .itemDescription("Diesel/Petrol consumption for generator")
                .itemType(BillItem.ItemType.OTHER_CHARGE)
                .quantity(fuelConsumptionLiters)
                .unit("Liters")
                .unitPrice(fuelPricePerLiter)
                .subtotal(totalAmount)
                .totalAmount(totalAmount)
                .build();
        
        // Create bill item directly using repository
        BillItem item = new BillItem();
        item.setBillId(itemDto.getBillId());
        item.setItemType(itemDto.getItemType());
        item.setItemDescription(itemDto.getItemDescription());
        item.setQuantity(itemDto.getQuantity());
        item.setUnitPrice(itemDto.getUnitPrice());
        item.setTotalAmount(itemDto.getTotalAmount());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        billItemRepository.save(item);
        
        log.info("Generator bill generated successfully. Bill ID: {}", createdBill.getId());
        return createdBill;
    }
    
    /**
     * Generate hybrid system bill (combination of grid and solar)
     */
    @Transactional
    public BillDto generateHybridBill(
            UUID userId,
            BigDecimal gridConsumptionKwh,
            BigDecimal solarGenerationKwh,
            BigDecimal netConsumptionKwh,
            LocalDateTime billingPeriodStart,
            LocalDateTime billingPeriodEnd) {
        
        log.info("Generating hybrid bill for user: {}, net consumption: {} kWh", userId, netConsumptionKwh);
        
        // Get applicable tariff
        Tariff tariff = getDefaultTariff();
        
        // Calculate bill for net consumption
        TariffCalculationService.TariffCalculationResult calculation = tariffCalculationService.calculateElectricityBill(
                netConsumptionKwh, BigDecimal.ZERO, netConsumptionKwh, BigDecimal.ZERO,
                tariff.getId(), LocalDateTime.now());
        
        BillDto billDto = BillDto.builder()
                .userId(userId)
                .billType(Bill.BillType.HYBRID_SYSTEM)
                .status(Bill.BillStatus.ISSUED)
                .billingPeriodStart(billingPeriodStart)
                .billingPeriodEnd(billingPeriodEnd)
                .dueDate(billingPeriodEnd.plusDays(15))
                .issuedDate(LocalDateTime.now())
                .totalConsumptionKwh(netConsumptionKwh)
                .totalAmount(calculation.getSubtotal())
                .taxAmount(calculation.getTaxAmount())
                .finalAmount(calculation.getFinalAmount())
                .balanceDue(calculation.getFinalAmount())
                .currency("NGN")
                .build();
        
        // Create bill directly using repository
        Bill bill = new Bill();
        bill.setUserId(billDto.getUserId());
        bill.setBillNumber(billDto.getBillNumber());
        bill.setBillType(billDto.getBillType());
        bill.setBillingPeriodStart(billDto.getBillingPeriodStart());
        bill.setBillingPeriodEnd(billDto.getBillingPeriodEnd());
        bill.setDueDate(billDto.getDueDate());
        bill.setTotalAmount(billDto.getTotalAmount());
        bill.setStatus(billDto.getStatus());
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        
        Bill savedBill = billRepository.save(bill);
        BillDto createdBill = convertToDto(savedBill);
        
        // Create bill items for hybrid system
        createHybridBillItems(createdBill.getId(), calculation, gridConsumptionKwh, solarGenerationKwh);
        
        log.info("Hybrid bill generated successfully. Bill ID: {}", createdBill.getId());
        return createdBill;
    }
    
    /**
     * Generate recurring bills for users
     */
    @Transactional
    public void generateRecurringBills() {
        log.info("Starting recurring bill generation process");
        
        // This would typically involve:
        // 1. Finding users with recurring billing enabled
        // 2. Getting their consumption data for the billing period
        // 3. Generating bills based on their tariff and consumption
        // 4. Sending notifications
        
        log.info("Recurring bill generation process completed");
    }
    
    /**
     * Generate estimated bills when meter readings are not available
     */
    @Transactional
    public BillDto generateEstimatedBill(
            UUID userId,
            BigDecimal estimatedConsumptionKwh,
            String estimationReason,
            LocalDateTime billingPeriodStart,
            LocalDateTime billingPeriodEnd) {
        
        log.info("Generating estimated bill for user: {}, estimated consumption: {} kWh", userId, estimatedConsumptionKwh);
        
        Tariff tariff = getDefaultTariff();
        
        TariffCalculationService.TariffCalculationResult calculation = tariffCalculationService.calculateElectricityBill(
                estimatedConsumptionKwh, BigDecimal.ZERO, estimatedConsumptionKwh, BigDecimal.ZERO,
                tariff.getId(), LocalDateTime.now());
        
        BillDto billDto = BillDto.builder()
                .userId(userId)
                .billType(Bill.BillType.GRID_ELECTRICITY)
                .status(Bill.BillStatus.ISSUED)
                .billingPeriodStart(billingPeriodStart)
                .billingPeriodEnd(billingPeriodEnd)
                .dueDate(billingPeriodEnd.plusDays(15))
                .issuedDate(LocalDateTime.now())
                .totalConsumptionKwh(estimatedConsumptionKwh)
                .totalAmount(calculation.getSubtotal())
                .taxAmount(calculation.getTaxAmount())
                .finalAmount(calculation.getFinalAmount())
                .balanceDue(calculation.getFinalAmount())
                .currency("NGN")
                .isEstimated(true)
                .estimationReason(estimationReason)
                .build();
        
        // Create bill directly using repository
        Bill bill = new Bill();
        bill.setUserId(billDto.getUserId());
        bill.setBillNumber(billDto.getBillNumber());
        bill.setBillType(billDto.getBillType());
        bill.setBillingPeriodStart(billDto.getBillingPeriodStart());
        bill.setBillingPeriodEnd(billDto.getBillingPeriodEnd());
        bill.setDueDate(billDto.getDueDate());
        bill.setTotalAmount(billDto.getTotalAmount());
        bill.setStatus(billDto.getStatus());
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        
        Bill savedBill = billRepository.save(bill);
        BillDto createdBill = convertToDto(savedBill);
        
        // Create estimated bill items
        createEstimatedBillItems(createdBill.getId(), calculation, estimationReason);
        
        log.info("Estimated bill generated successfully. Bill ID: {}", createdBill.getId());
        return createdBill;
    }
    
    // Private helper methods
    private void createBillItems(UUID billId, TariffCalculationService.TariffCalculationResult calculation, Tariff tariff) {
        // Create consumption item
        if (calculation.getPeakConsumptionKwh() != null && calculation.getPeakConsumptionKwh().compareTo(BigDecimal.ZERO) > 0) {
            BillItemDto peakItem = BillItemDto.builder()
                    .billId(billId)
                    .itemName("Peak Consumption")
                    .itemDescription("Electricity consumption during peak hours")
                    .itemType(BillItem.ItemType.PEAK_CONSUMPTION)
                    .quantity(calculation.getPeakConsumptionKwh())
                    .unit("kWh")
                    .unitPrice(tariff.getPeakRate())
                    .subtotal(calculation.getPeakAmount())
                    .totalAmount(calculation.getPeakAmount())
                    .consumptionKwh(calculation.getPeakConsumptionKwh())
                    .peakConsumptionKwh(calculation.getPeakConsumptionKwh())
                    .peakRate(tariff.getPeakRate())
                    .peakAmount(calculation.getPeakAmount())
                    .build();
            
            // Create bill item directly using repository
            BillItem item = new BillItem();
            item.setBillId(peakItem.getBillId());
            item.setItemType(peakItem.getItemType());
            item.setItemName(peakItem.getItemName());
            item.setQuantity(peakItem.getQuantity());
            item.setUnitPrice(peakItem.getUnitPrice());
            item.setTotalAmount(peakItem.getTotalAmount());
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            billItemRepository.save(item);
        }
        
        // Create off-peak consumption item
        if (calculation.getOffPeakConsumptionKwh() != null && calculation.getOffPeakConsumptionKwh().compareTo(BigDecimal.ZERO) > 0) {
            BillItemDto offPeakItem = BillItemDto.builder()
                    .billId(billId)
                    .itemName("Off-Peak Consumption")
                    .itemDescription("Electricity consumption during off-peak hours")
                    .itemType(BillItem.ItemType.OFF_PEAK_CONSUMPTION)
                    .quantity(calculation.getOffPeakConsumptionKwh())
                    .unit("kWh")
                    .unitPrice(tariff.getOffPeakRate())
                    .subtotal(calculation.getOffPeakAmount())
                    .totalAmount(calculation.getOffPeakAmount())
                    .consumptionKwh(calculation.getOffPeakConsumptionKwh())
                    .offPeakConsumptionKwh(calculation.getOffPeakConsumptionKwh())
                    .offPeakRate(tariff.getOffPeakRate())
                    .offPeakAmount(calculation.getOffPeakAmount())
                    .build();
            
            // Create bill item directly using repository
            BillItem item = new BillItem();
            item.setBillId(offPeakItem.getBillId());
            item.setItemType(offPeakItem.getItemType());
            item.setItemName(offPeakItem.getItemName());
            item.setQuantity(offPeakItem.getQuantity());
            item.setUnitPrice(offPeakItem.getUnitPrice());
            item.setTotalAmount(offPeakItem.getTotalAmount());
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            billItemRepository.save(item);
        }
        
        // Create service charge item
        if (calculation.getServiceCharge() != null && calculation.getServiceCharge().compareTo(BigDecimal.ZERO) > 0) {
            BillItemDto serviceItem = BillItemDto.builder()
                    .billId(billId)
                    .itemName("Service Charge")
                    .itemDescription("Monthly service charge")
                    .itemType(BillItem.ItemType.SERVICE_CHARGE)
                    .quantity(BigDecimal.ONE)
                    .unit("Monthly")
                    .unitPrice(calculation.getServiceCharge())
                    .subtotal(calculation.getServiceCharge())
                    .totalAmount(calculation.getServiceCharge())
                    .serviceCharge(calculation.getServiceCharge())
                    .build();
            
            // Create bill item directly using repository
            BillItem item = new BillItem();
            item.setBillId(serviceItem.getBillId());
            item.setItemType(serviceItem.getItemType());
            item.setItemName(serviceItem.getItemName());
            item.setQuantity(serviceItem.getQuantity());
            item.setUnitPrice(serviceItem.getUnitPrice());
            item.setTotalAmount(serviceItem.getTotalAmount());
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            billItemRepository.save(item);
        }
        
        // Create tax item
        if (calculation.getTaxAmount() != null && calculation.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            BillItemDto taxItem = BillItemDto.builder()
                    .billId(billId)
                    .itemName("Value Added Tax (VAT)")
                    .itemDescription("VAT on electricity consumption")
                    .itemType(BillItem.ItemType.TAX)
                    .quantity(BigDecimal.ONE)
                    .unit("Tax")
                    .unitPrice(calculation.getTaxAmount())
                    .subtotal(calculation.getTaxAmount())
                    .totalAmount(calculation.getTaxAmount())
                    .taxAmount(calculation.getTaxAmount())
                    .build();
            
            // Create bill item directly using repository
            BillItem item = new BillItem();
            item.setBillId(taxItem.getBillId());
            item.setItemType(taxItem.getItemType());
            item.setItemName(taxItem.getItemName());
            item.setQuantity(taxItem.getQuantity());
            item.setUnitPrice(taxItem.getUnitPrice());
            item.setTotalAmount(taxItem.getTotalAmount());
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            billItemRepository.save(item);
        }
    }
    
    private void createHybridBillItems(UUID billId, TariffCalculationService.TariffCalculationResult calculation, 
                                     BigDecimal gridConsumption, BigDecimal solarGeneration) {
        // Create grid consumption item
        if (gridConsumption != null && gridConsumption.compareTo(BigDecimal.ZERO) > 0) {
            BillItemDto gridItem = BillItemDto.builder()
                    .billId(billId)
                    .itemName("Grid Consumption")
                    .itemDescription("Electricity consumed from grid")
                    .itemType(BillItem.ItemType.ELECTRICITY_CONSUMPTION)
                    .quantity(gridConsumption)
                    .unit("kWh")
                    .unitPrice(BigDecimal.ZERO) // Will be calculated based on tariff
                    .subtotal(calculation.getSubtotal())
                    .totalAmount(calculation.getSubtotal())
                    .consumptionKwh(gridConsumption)
                    .build();
            
            // Create bill item directly using repository
            BillItem item = new BillItem();
            item.setBillId(gridItem.getBillId());
            item.setItemType(gridItem.getItemType());
            item.setItemName(gridItem.getItemName());
            item.setQuantity(gridItem.getQuantity());
            item.setUnitPrice(gridItem.getUnitPrice());
            item.setTotalAmount(gridItem.getTotalAmount());
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            billItemRepository.save(item);
        }
        
        // Create solar generation item
        if (solarGeneration != null && solarGeneration.compareTo(BigDecimal.ZERO) > 0) {
            BillItemDto solarItem = BillItemDto.builder()
                    .billId(billId)
                    .itemName("Solar Generation")
                    .itemDescription("Solar energy generated")
                    .itemType(BillItem.ItemType.ELECTRICITY_CONSUMPTION)
                    .quantity(solarGeneration)
                    .unit("kWh")
                    .unitPrice(BigDecimal.ZERO)
                    .subtotal(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.ZERO)
                    .consumptionKwh(solarGeneration)
                    .build();
            
            // Create bill item directly using repository
            BillItem item = new BillItem();
            item.setBillId(solarItem.getBillId());
            item.setItemType(solarItem.getItemType());
            item.setItemName(solarItem.getItemName());
            item.setQuantity(solarItem.getQuantity());
            item.setUnitPrice(solarItem.getUnitPrice());
            item.setTotalAmount(solarItem.getTotalAmount());
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            billItemRepository.save(item);
        }
    }
    
    private void createEstimatedBillItems(UUID billId, TariffCalculationService.TariffCalculationResult calculation, String estimationReason) {
        BillItemDto estimatedItem = BillItemDto.builder()
                .billId(billId)
                .itemName("Estimated Consumption")
                .itemDescription("Estimated electricity consumption - " + estimationReason)
                .itemType(BillItem.ItemType.ELECTRICITY_CONSUMPTION)
                .quantity(calculation.getSubtotal())
                .unit("kWh")
                .unitPrice(BigDecimal.ZERO)
                .subtotal(calculation.getSubtotal())
                .totalAmount(calculation.getSubtotal())
                .consumptionKwh(calculation.getSubtotal())
                .notes("This is an estimated bill based on historical consumption patterns")
                .build();
        
        // Create bill item directly using repository
        BillItem item = new BillItem();
        item.setBillId(estimatedItem.getBillId());
        item.setItemType(estimatedItem.getItemType());
        item.setItemName(estimatedItem.getItemName());
        item.setQuantity(estimatedItem.getQuantity());
        item.setUnitPrice(estimatedItem.getUnitPrice());
        item.setTotalAmount(estimatedItem.getTotalAmount());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        billItemRepository.save(item);
    }
    
    private Tariff getDefaultTariff() {
        // In a real implementation, this would get the appropriate tariff based on user location
        // For now, return a default tariff
        return new Tariff(); // This would need to be implemented properly
    }
} 