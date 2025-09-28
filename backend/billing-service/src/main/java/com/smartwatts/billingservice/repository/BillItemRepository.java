package com.smartwatts.billingservice.repository;

import com.smartwatts.billingservice.model.BillItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface BillItemRepository extends JpaRepository<BillItem, UUID> {
    
    List<BillItem> findByBillId(UUID billId);
    
    Page<BillItem> findByBillId(UUID billId, Pageable pageable);
    
    List<BillItem> findByBillIdAndItemType(UUID billId, BillItem.ItemType itemType);
    
    @Query("SELECT bi FROM BillItem bi WHERE bi.billId = :billId ORDER BY bi.createdAt ASC")
    List<BillItem> findItemsByBillIdOrdered(@Param("billId") UUID billId);
    
    @Query("SELECT bi FROM BillItem bi WHERE bi.billId = :billId AND bi.itemType = 'ELECTRICITY_CONSUMPTION'")
    List<BillItem> findConsumptionItemsByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT bi FROM BillItem bi WHERE bi.billId = :billId AND bi.itemType IN ('PEAK_CONSUMPTION', 'OFF_PEAK_CONSUMPTION', 'NIGHT_CONSUMPTION')")
    List<BillItem> findTimeBasedItemsByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT bi FROM BillItem bi WHERE bi.billId = :billId AND bi.itemType IN ('SERVICE_CHARGE', 'METER_RENTAL', 'DEMAND_CHARGE', 'CAPACITY_CHARGE', 'TRANSMISSION_CHARGE', 'DISTRIBUTION_CHARGE', 'REGULATORY_CHARGE', 'ENVIRONMENTAL_CHARGE')")
    List<BillItem> findChargeItemsByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT bi FROM BillItem bi WHERE bi.billId = :billId AND bi.itemType = 'TAX'")
    List<BillItem> findTaxItemsByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT bi FROM BillItem bi WHERE bi.billId = :billId AND bi.itemType = 'DISCOUNT'")
    List<BillItem> findDiscountItemsByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.totalAmount) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalAmountByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.taxAmount) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalTaxAmountByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.discountAmount) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalDiscountAmountByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.consumptionKwh) FROM BillItem bi WHERE bi.billId = :billId AND bi.itemType = 'ELECTRICITY_CONSUMPTION'")
    BigDecimal findTotalConsumptionByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.peakConsumptionKwh) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalPeakConsumptionByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.offPeakConsumptionKwh) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalOffPeakConsumptionByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.nightConsumptionKwh) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalNightConsumptionByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.peakAmount) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalPeakAmountByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.offPeakAmount) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalOffPeakAmountByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.nightAmount) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalNightAmountByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.serviceCharge) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalServiceChargeByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.meterRental) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalMeterRentalByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.demandCharge) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalDemandChargeByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT SUM(bi.fuelAdjustment) FROM BillItem bi WHERE bi.billId = :billId")
    BigDecimal findTotalFuelAdjustmentByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT COUNT(bi) FROM BillItem bi WHERE bi.billId = :billId")
    long countItemsByBillId(@Param("billId") UUID billId);
    
    @Query("SELECT bi FROM BillItem bi WHERE bi.billId = :billId AND bi.totalAmount >= :minAmount ORDER BY bi.totalAmount DESC")
    List<BillItem> findHighValueItemsByBillId(@Param("billId") UUID billId, @Param("minAmount") BigDecimal minAmount);
} 