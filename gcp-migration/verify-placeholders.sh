#!/bin/bash

###############################################################################
# Verify Placeholders Script
# 
# Purpose: Check if any placeholders remain in configuration files
#
# Usage: ./verify-placeholders.sh
#
###############################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PLACEHOLDERS=("PROJECT_ID" "REGION" "INSTANCE_NAME" "DATABASE_NAME" "REPOSITORY")

echo -e "${BLUE}Checking for remaining placeholders...${NC}"
echo ""

FOUND_PLACEHOLDERS=0

# Check Cloud Run configs
echo -e "${YELLOW}Checking Cloud Run configuration files...${NC}"
for config_file in gcp-migration/cloud-run-configs/*.yaml; do
    if [ -f "$config_file" ]; then
        for placeholder in "${PLACEHOLDERS[@]}"; do
            if grep -q "$placeholder" "$config_file" 2>/dev/null; then
                echo -e "${RED}✗ Found ${placeholder} in ${config_file}${NC}"
                FOUND_PLACEHOLDERS=$((FOUND_PLACEHOLDERS + 1))
            fi
        done
    fi
done

# Check Cloud Build configs
echo -e "${YELLOW}Checking Cloud Build configuration files...${NC}"
for config_file in gcp-migration/ci-cd/cloudbuild-*.yaml; do
    if [ -f "$config_file" ]; then
        # Cloud Build uses ${PROJECT_ID} which is a variable, so check for literal PROJECT_ID
        if grep -q "PROJECT_ID[^}]" "$config_file" 2>/dev/null || grep -q "\${PROJECT_ID}" "$config_file" 2>/dev/null; then
            echo -e "${GREEN}✓ ${config_file} uses PROJECT_ID variable (correct)${NC}"
        fi
    fi
done

echo ""

if [ $FOUND_PLACEHOLDERS -eq 0 ]; then
    echo -e "${GREEN}✓ No placeholders found - all files are ready!${NC}"
    exit 0
else
    echo -e "${RED}✗ Found ${FOUND_PLACEHOLDERS} placeholder(s) remaining${NC}"
    echo -e "${YELLOW}Run ./replace-placeholders.sh to fix them${NC}"
    exit 1
fi

