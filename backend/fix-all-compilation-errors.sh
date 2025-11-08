#!/bin/bash

set -e

echo "🔧 Fixing all compilation errors across all services..."

# Analytics Service Fixes
echo "📝 Fixing analytics-service..."
cd analytics-service

# Fix getUsagePatterns method name and return type
sed -i '' 's/getUsagePatterns(/getUsagePatternsByUserId(/g' src/test/java/com/smartwatts/analyticsservice/controller/AnalyticsControllerTest.java
sed -i '' 's/List<UsagePatternDto> patterns = Arrays\.asList(patternDto);/Page<UsagePatternDto> patterns = new PageImpl<>(Arrays.asList(patternDto));/g' src/test/java/com/smartwatts/analyticsservice/controller/AnalyticsControllerTest.java
sed -i '' 's/when(analyticsService\.getUsagePatternsByUserId(eq(testUserId), any())\.thenReturn(patterns);/when(analyticsService.getUsagePatternsByUserId(eq(testUserId), any(Pageable.class))).thenReturn(patterns);/g' src/test/java/com/smartwatts/analyticsservice/controller/AnalyticsControllerTest.java
sed -i '' 's/verify(analyticsService)\.getUsagePatterns(eq(testUserId), any());/verify(analyticsService).getUsagePatternsByUserId(eq(testUserId), any(Pageable.class));/g' src/test/java/com/smartwatts/analyticsservice/controller/AnalyticsControllerTest.java
sed -i '' 's/\.andExpect(jsonPath("\$")\.isArray());/\.andExpect(jsonPath("$.content").isArray());/g' src/test/java/com/smartwatts/analyticsservice/controller/AnalyticsControllerTest.java

# Add missing import for PageImpl
if ! grep -q "import org.springframework.data.domain.PageImpl;" src/test/java/com/smartwatts/analyticsservice/controller/AnalyticsControllerTest.java; then
    sed -i '' '/import org.springframework.data.domain.Page;/a\
import org.springframework.data.domain.PageImpl;
' src/test/java/com/smartwatts/analyticsservice/controller/AnalyticsControllerTest.java
fi

# Add missing import for Pageable
if ! grep -q "import org.springframework.data.domain.Pageable;" src/test/java/com/smartwatts/analyticsservice/controller/AnalyticsControllerTest.java; then
    sed -i '' '/import org.springframework.data.domain.PageImpl;/a\
import org.springframework.data.domain.Pageable;
' src/test/java/com/smartwatts/analyticsservice/controller/AnalyticsControllerTest.java
fi

cd ..

# Billing Service Fixes
echo "📝 Fixing billing-service..."
cd billing-service

# Fix payBill method name
sed -i '' 's/verify(billingService)\.payBill(/verify(billingService).markBillAsPaid(/g' src/test/java/com/smartwatts/billingservice/controller/BillingControllerTest.java
sed -i '' 's/markBillAsPaid(eq(testBillId), any())/markBillAsPaid(eq(testBillId), anyString(), anyString())/g' src/test/java/com/smartwatts/billingservice/controller/BillingControllerTest.java

cd ..

# Facility Service Fixes
echo "📝 Fixing facility-service..."
cd facility-service

# Fix FleetStatus.ACTIVE to OPERATIONAL
sed -i '' 's/FleetStatus\.ACTIVE/FleetStatus.OPERATIONAL/g' src/test/java/com/smartwatts/facilityservice/controller/FleetControllerTest.java

# Fix WorkOrderStatus.OPEN to DRAFT
sed -i '' 's/WorkOrderStatus\.OPEN/WorkOrderStatus.DRAFT/g' src/test/java/com/smartwatts/facilityservice/controller/WorkOrderControllerTest.java

cd ..

# Feature Flag Service Fixes
echo "📝 Fixing feature-flag-service..."
cd feature-flag-service

# Fix UserFeatureAccessDto.userId to use toString()
sed -i '' 's/accessDto\.setUserId(testUserId);/accessDto.setUserId(testUserId.toString());/g' src/test/java/com/smartwatts/featureflagservice/controller/FeatureFlagControllerTest.java

# Fix FeatureFlagServiceTest.java - remove builder pattern remnants
sed -i '' '/\.id(testFeatureFlag\.getId())/,/\.isPaidFeature(false)/d' src/test/java/com/smartwatts/featureflagservice/service/FeatureFlagServiceTest.java

cd ..

# Appliance Monitoring Service Fixes
echo "📝 Fixing appliance-monitoring-service..."
cd appliance-monitoring-service

# Fix ApplianceEfficiencyStats to use qualified name
sed -i '' 's/new ApplianceEfficiencyStats()/new ApplianceMonitoringService.ApplianceEfficiencyStats()/g' src/test/java/com/smartwatts/appliancemonitoringservice/controller/ApplianceMonitoringControllerTest.java

# Fix getApplianceConsumption to getEfficiencyStats
sed -i '' 's/getApplianceConsumption(/getEfficiencyStats(/g' src/test/java/com/smartwatts/appliancemonitoringservice/controller/ApplianceMonitoringControllerTest.java

# Fix the test to use proper stats object instead of Map
sed -i '' '/Map<String, Object> consumption = Map\.of(/,/);/c\
        ApplianceMonitoringService.ApplianceEfficiencyStats stats = ApplianceMonitoringService.ApplianceEfficiencyStats.builder()\
                .totalConsumption(new BigDecimal("1000.5"))\
                .averageEfficiency(new BigDecimal("85.5"))\
                .readingsCount(10)\
                .build();\
        when(applianceMonitoringService.getEfficiencyStats(eq(testApplianceId), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(stats);
' src/test/java/com/smartwatts/appliancemonitoringservice/controller/ApplianceMonitoringControllerTest.java

# Fix JSON path expectations
sed -i '' 's/\.andExpect(jsonPath("\$\.applianceId")\.value(testApplianceId\.toString()));/\.andExpect(jsonPath("$.totalConsumption").value(1000.5));/g' src/test/java/com/smartwatts/appliancemonitoringservice/controller/ApplianceMonitoringControllerTest.java

cd ..

echo "✅ All compilation error fixes applied!"
echo "🧪 Running compilation check..."

# Test each service
for service in analytics-service billing-service facility-service feature-flag-service appliance-monitoring-service; do
    echo "Testing $service..."
    cd $service
    if ./gradlew compileTestJava --no-daemon > /dev/null 2>&1; then
        echo "✅ $service: Compilation successful"
    else
        echo "❌ $service: Compilation failed"
        ./gradlew compileTestJava --no-daemon 2>&1 | grep -A 2 "error:" | head -5
    fi
    cd ..
done

echo "🎉 Done!"

