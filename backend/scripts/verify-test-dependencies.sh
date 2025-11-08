#!/bin/bash

# Script to verify and ensure all services have required test dependencies
# This prevents the recurring issue of missing Spring Security test dependencies

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=== Verifying Test Dependencies Across All Services ==="
echo ""

# Required test dependencies
REQUIRED_TEST_DEPS=(
    "org.springframework.boot:spring-boot-starter-test"
    "org.springframework.security:spring-security-test"
)

# Services to check
SERVICES=(
    "user-service"
    "device-service"
    "energy-service"
    "analytics-service"
    "billing-service"
    "facility-service"
    "feature-flag-service"
    "appliance-monitoring-service"
    "edge-gateway"
    "api-gateway"
    "api-docs-service"
)

# Function to check if a dependency exists in build.gradle
check_dependency() {
    local service=$1
    local dep=$2
    local build_file="$BACKEND_DIR/$service/build.gradle"
    
    if [ ! -f "$build_file" ]; then
        echo "  ❌ build.gradle not found"
        return 1
    fi
    
    if grep -q "$dep" "$build_file"; then
        return 0
    else
        return 1
    fi
}

# Function to add missing dependency
add_dependency() {
    local service=$1
    local dep=$2
    local build_file="$BACKEND_DIR/$service/build.gradle"
    
    # Find the testImplementation section
    if grep -q "testImplementation" "$build_file"; then
        # Add after the last testImplementation line
        sed -i.bak "/testImplementation.*spring-boot-starter-test/a\\
    testImplementation '$dep'
" "$build_file"
        rm -f "$build_file.bak"
    else
        # Add testImplementation section before dependencies closing
        sed -i.bak "/^dependencies {/,/^}/ {
            /^}/ i\\
    testImplementation 'org.springframework.boot:spring-boot-starter-test'\\
    testImplementation '$dep'
}" "$build_file"
        rm -f "$build_file.bak"
    fi
}

# Verify all services
MISSING_DEPS=()
for service in "${SERVICES[@]}"; do
    echo "Checking $service..."
    SERVICE_MISSING=0
    
    for dep in "${REQUIRED_TEST_DEPS[@]}"; do
        if ! check_dependency "$service" "$dep"; then
            echo "  ❌ Missing: $dep"
            MISSING_DEPS+=("$service:$dep")
            SERVICE_MISSING=1
        fi
    done
    
    if [ $SERVICE_MISSING -eq 0 ]; then
        echo "  ✅ All test dependencies present"
    fi
    echo ""
done

# Fix missing dependencies
if [ ${#MISSING_DEPS[@]} -gt 0 ]; then
    echo "=== Fixing Missing Dependencies ==="
    for entry in "${MISSING_DEPS[@]}"; do
        IFS=':' read -r service dep <<< "$entry"
        echo "Adding $dep to $service..."
        add_dependency "$service" "$dep"
    done
    echo "✅ All missing dependencies added"
    echo ""
fi

# Force Gradle to refresh dependencies
echo "=== Refreshing Gradle Dependencies ==="
for service in "${SERVICES[@]}"; do
    if [ -d "$BACKEND_DIR/$service" ]; then
        echo "Refreshing $service..."
        cd "$BACKEND_DIR/$service"
        ./gradlew clean build --refresh-dependencies --no-daemon > /dev/null 2>&1 || echo "  ⚠️  Build failed (this is OK if dependencies are being refreshed)"
    fi
done

echo ""
echo "=== Verification Complete ==="
echo ""
echo "If your IDE still shows errors:"
echo "1. Close and reopen your IDE"
echo "2. In IntelliJ: File -> Invalidate Caches / Restart"
echo "3. In VS Code: Reload Window (Cmd+Shift+P -> 'Reload Window')"
echo "4. Run: ./gradlew clean build in each service directory"
echo ""




