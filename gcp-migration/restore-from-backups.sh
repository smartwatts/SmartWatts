#!/bin/bash

###############################################################################
# Restore From Backups Script
# 
# Purpose: Restore configuration files from .bak backup files
#          Useful if you need to revert placeholder replacements
#
# Usage: ./restore-from-backups.sh
#
###############################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}Restoring files from backups...${NC}"
echo ""

RESTORED=0

# Find all .bak files and restore them
for backup_file in gcp-migration/**/*.bak; do
    if [ -f "$backup_file" ]; then
        original_file="${backup_file%.bak}"
        echo -e "${YELLOW}Restoring: ${original_file}${NC}"
        cp "$backup_file" "$original_file"
        RESTORED=$((RESTORED + 1))
    fi
done

if [ $RESTORED -eq 0 ]; then
    echo -e "${YELLOW}No backup files found${NC}"
else
    echo -e "${GREEN}✓ Restored ${RESTORED} file(s) from backups${NC}"
fi

