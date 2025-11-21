#!/bin/bash

# Monitor Cold Start Frequency
# Analyzes Cloud Run logs to identify cold starts

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
REGION="${GCP_REGION:-europe-west1}"
LOOKBACK_HOURS="${LOOKBACK_HOURS:-24}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Cold Start Monitoring${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Project: ${PROJECT_ID}"
echo -e "Region: ${REGION}"
echo -e "Lookback: ${LOOKBACK_HOURS} hours"
echo ""

# Get all Cloud Run services
SERVICES=$(gcloud run services list \
    --region="${REGION}" \
    --project="${PROJECT_ID}" \
    --format="value(metadata.name)" 2>/dev/null)

if [ -z "$SERVICES" ]; then
    echo -e "${RED}No services found${NC}"
    exit 1
fi

echo -e "${BLUE}Analyzing cold starts for services...${NC}"
echo ""

COLD_START_COUNT=0
TOTAL_REQUESTS=0

for service in $SERVICES; do
    echo -e "${YELLOW}Service: ${service}${NC}"
    
    # Query logs for cold start indicators
    # Cold starts typically show "Container starting" or high initial response times
    COLD_STARTS=$(gcloud logging read \
        "resource.type=cloud_run_revision AND \
         resource.labels.service_name=${service} AND \
         (textPayload=~'Container starting' OR \
          textPayload=~'Started.*Application' OR \
          jsonPayload.message=~'Container starting' OR \
          jsonPayload.message=~'Started.*Application')" \
        --limit=100 \
        --format="value(timestamp)" \
        --project="${PROJECT_ID}" \
        --freshness="${LOOKBACK_HOURS}h" 2>/dev/null | wc -l | tr -d ' ')
    
    # Count total requests
    REQUESTS=$(gcloud logging read \
        "resource.type=cloud_run_revision AND \
         resource.labels.service_name=${service} AND \
         httpRequest.requestMethod=GET" \
        --limit=1000 \
        --format="value(timestamp)" \
        --project="${PROJECT_ID}" \
        --freshness="${LOOKBACK_HOURS}h" 2>/dev/null | wc -l | tr -d ' ')
    
    if [ -z "$COLD_STARTS" ]; then
        COLD_STARTS=0
    fi
    
    if [ -z "$REQUESTS" ]; then
        REQUESTS=0
    fi
    
    COLD_START_COUNT=$((COLD_START_COUNT + COLD_STARTS))
    TOTAL_REQUESTS=$((TOTAL_REQUESTS + REQUESTS))
    
    if [ "$REQUESTS" -gt 0 ]; then
        COLD_START_PERCENT=$(awk "BEGIN {printf \"%.2f\", ($COLD_STARTS / $REQUESTS) * 100}")
    else
        COLD_START_PERCENT=0
    fi
    
    if [ "$COLD_STARTS" -gt 0 ]; then
        echo -e "  Cold Starts: ${RED}${COLD_STARTS}${NC}"
    else
        echo -e "  Cold Starts: ${GREEN}${COLD_STARTS}${NC}"
    fi
    
    echo -e "  Total Requests: ${REQUESTS}"
    echo -e "  Cold Start Rate: ${COLD_START_PERCENT}%"
    
    # Check minScale setting
    MIN_SCALE=$(gcloud run services describe "${service}" \
        --region="${REGION}" \
        --project="${PROJECT_ID}" \
        --format="value(spec.template.metadata.annotations['autoscaling.knative.dev/minScale'])" 2>/dev/null || echo "0")
    
    if [ "$MIN_SCALE" = "0" ] || [ -z "$MIN_SCALE" ]; then
        echo -e "  Min Scale: ${YELLOW}0 (can scale to zero)${NC}"
    else
        echo -e "  Min Scale: ${GREEN}${MIN_SCALE}${NC}"
    fi
    
    echo ""
done

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "Total Cold Starts: ${COLD_START_COUNT}"
echo -e "Total Requests: ${TOTAL_REQUESTS}"

if [ "$TOTAL_REQUESTS" -gt 0 ]; then
    OVERALL_RATE=$(awk "BEGIN {printf \"%.2f\", ($COLD_START_COUNT / $TOTAL_REQUESTS) * 100}")
    echo -e "Overall Cold Start Rate: ${OVERALL_RATE}%"
    
    if (( $(echo "$OVERALL_RATE > 5" | bc -l) )); then
        echo -e "${RED}⚠ High cold start rate detected (>5%)${NC}"
        echo -e "${YELLOW}Recommendation: Increase minScale for frequently accessed services${NC}"
    elif (( $(echo "$OVERALL_RATE > 1" | bc -l) )); then
        echo -e "${YELLOW}⚠ Moderate cold start rate (1-5%)${NC}"
        echo -e "${YELLOW}Consider increasing minScale for critical services${NC}"
    else
        echo -e "${GREEN}✓ Low cold start rate (<1%)${NC}"
    fi
fi

echo ""
echo -e "${BLUE}To reduce cold starts:${NC}"
echo "  1. Increase minScale: ./optimize-min-instances.sh"
echo "  2. Optimize container startup time"
echo "  3. Use Cloud Run warm-up requests"

