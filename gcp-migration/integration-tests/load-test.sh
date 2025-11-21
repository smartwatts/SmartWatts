#!/bin/bash

# Load Testing Script for SmartWatts Services
# Tests services under various load conditions

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
REGION="${GCP_REGION:-europe-west1}"

# Load test parameters
CONCURRENT_USERS="${CONCURRENT_USERS:-10}"
REQUESTS_PER_USER="${REQUESTS_PER_USER:-100}"
TEST_DURATION="${TEST_DURATION:-60}"  # seconds

# Results
RESULTS_DIR="load-test-results-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESULTS_DIR"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Load Testing - SmartWatts Services${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Configuration:"
echo -e "  Concurrent Users: ${CONCURRENT_USERS}"
echo -e "  Requests per User: ${REQUESTS_PER_USER}"
echo -e "  Test Duration: ${TEST_DURATION}s"
echo -e "  Results Directory: ${RESULTS_DIR}"
echo ""

# Get service URLs
echo -e "${YELLOW}Fetching service URLs...${NC}"
API_GATEWAY_URL=$(gcloud run services describe "api-gateway" --region="${REGION}" --project="${PROJECT_ID}" --format="value(status.url)" 2>/dev/null)
USER_SERVICE_URL=$(gcloud run services describe "user-service" --region="${REGION}" --project="${PROJECT_ID}" --format="value(status.url)" 2>/dev/null)

if [ -z "$API_GATEWAY_URL" ] || [ -z "$USER_SERVICE_URL" ]; then
    echo -e "${RED}Error: Could not retrieve service URLs${NC}"
    exit 1
fi

echo -e "${GREEN}API Gateway: ${API_GATEWAY_URL}${NC}"
echo -e "${GREEN}User Service: ${USER_SERVICE_URL}${NC}"
echo ""

# Function to run load test
run_load_test() {
    local name=$1
    local url=$2
    local method=${3:-GET}
    local headers=${4:-}
    local data=${5:-}
    local output_file="$RESULTS_DIR/${name// /_}.txt"
    
    echo -e "${BLUE}Testing: ${name}${NC}"
    echo -e "  URL: ${url}"
    echo -e "  Method: ${method}"
    echo -e "  Concurrent: ${CONCURRENT_USERS}"
    echo -e "  Requests: $((CONCURRENT_USERS * REQUESTS_PER_USER))"
    echo ""
    
    # Check if ab (Apache Bench) is available
    if command -v ab &> /dev/null; then
        echo -e "${YELLOW}Using Apache Bench (ab)${NC}"
        
        local ab_headers=""
        if [ -n "$headers" ]; then
            ab_headers="-H '${headers}'"
        fi
        
        if [ "$method" = "GET" ]; then
            # ab requires headers in format: -H "Header: Value"
            if [ -n "$headers" ]; then
                # Split header string and format for ab
                IFS=':' read -r header_name header_value <<< "$headers"
                ab -n $((CONCURRENT_USERS * REQUESTS_PER_USER)) \
                   -c ${CONCURRENT_USERS} \
                   -g "${output_file%.txt}.gnuplot" \
                   -e "${output_file%.txt}.csv" \
                   -H "${header_name}:${header_value}" \
                   "${url}" > "$output_file" 2>&1 || {
                    echo -e "${YELLOW}ab failed, falling back to curl-based test${NC}"
                    run_curl_load_test "$name" "$url" "$method" "$headers" "$data" "$output_file"
                }
            else
                ab -n $((CONCURRENT_USERS * REQUESTS_PER_USER)) \
                   -c ${CONCURRENT_USERS} \
                   -g "${output_file%.txt}.gnuplot" \
                   -e "${output_file%.txt}.csv" \
                   "${url}" > "$output_file" 2>&1 || {
                    echo -e "${YELLOW}ab failed, falling back to curl-based test${NC}"
                    run_curl_load_test "$name" "$url" "$method" "$headers" "$data" "$output_file"
                }
            fi
        else
            echo -e "${YELLOW}Note: ab doesn't support POST with body, using curl-based test${NC}"
            run_curl_load_test "$name" "$url" "$method" "$headers" "$data" "$output_file"
        fi
    elif command -v wrk &> /dev/null; then
        echo -e "${YELLOW}Using wrk${NC}"
        
        local wrk_headers=""
        if [ -n "$headers" ]; then
            wrk_headers="-H '${headers}'"
        fi
        
        wrk -t${CONCURRENT_USERS} \
            -c${CONCURRENT_USERS} \
            -d${TEST_DURATION}s \
            ${wrk_headers} \
            "${url}" > "$output_file" 2>&1
    else
        echo -e "${YELLOW}Using curl-based load test (ab/wrk not available)${NC}"
        run_curl_load_test "$name" "$url" "$method" "$headers" "$data" "$output_file"
    fi
    
    # Parse results
    parse_results "$name" "$output_file"
    echo ""
}

# Function to run curl-based load test
run_curl_load_test() {
    local name=$1
    local url=$2
    local method=$3
    local headers=$4
    local data=$5
    local output_file=$6
    
    local total_requests=$((CONCURRENT_USERS * REQUESTS_PER_USER))
    local start_time=$(date +%s)
    local success_count=0
    local error_count=0
    local total_time=0
    local min_time=999999
    local max_time=0
    
    echo "Running ${total_requests} requests..." > "$output_file"
    
    # Create a function to make a single request
    make_request() {
        local req_start=$(date +%s%N)
        local http_code=$(curl -s -w "%{http_code}" --max-time 10 \
            ${headers:+-H "$headers"} \
            ${data:+-d "$data"} \
            -X "$method" \
            "${url}" -o /dev/null 2>&1)
        local req_end=$(date +%s%N)
        local req_time=$(( (req_end - req_start) / 1000000 ))  # Convert to milliseconds
        
        echo "$http_code $req_time"
    }
    
    # Export function and variables for parallel execution
    export -f make_request
    export url method headers data
    
    # Run requests in parallel
    local pids=()
    local results_file=$(mktemp)
    
    for ((i=1; i<=total_requests; i++)); do
        make_request >> "$results_file" &
        pids+=($!)
        
        # Limit concurrent processes
        if (( i % CONCURRENT_USERS == 0 )); then
            wait "${pids[@]}"
            pids=()
        fi
    done
    
    # Wait for remaining processes
    wait "${pids[@]}"
    
    # Process results
    while IFS=' ' read -r http_code req_time; do
        if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
            ((success_count++))
        else
            ((error_count++))
        fi
        
        total_time=$((total_time + req_time))
        if [ "$req_time" -lt "$min_time" ]; then
            min_time=$req_time
        fi
        if [ "$req_time" -gt "$max_time" ]; then
            max_time=$req_time
        fi
    done < "$results_file"
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    local avg_time=$((total_time / total_requests))
    local rps=$((total_requests / duration))
    
    # Write summary to output file
    {
        echo "Load Test Results: ${name}"
        echo "=========================================="
        echo "Total Requests: ${total_requests}"
        echo "Successful: ${success_count}"
        echo "Failed: ${error_count}"
        echo "Success Rate: $(( success_count * 100 / total_requests ))%"
        echo "Duration: ${duration}s"
        echo "Requests per Second: ${rps}"
        echo "Average Response Time: ${avg_time}ms"
        echo "Min Response Time: ${min_time}ms"
        echo "Max Response Time: ${max_time}ms"
    } >> "$output_file"
    
    rm -f "$results_file"
}

# Function to parse results
parse_results() {
    local name=$1
    local output_file=$2
    
    if [ ! -f "$output_file" ]; then
        echo -e "${RED}  Results file not found${NC}"
        return
    fi
    
    # Try to extract key metrics
    if grep -q "Requests per second" "$output_file"; then
        # Apache Bench format
        local rps=$(grep "Requests per second" "$output_file" | awk '{print $4}')
        local time_per_request=$(grep "Time per request" "$output_file" | head -1 | awk '{print $4}')
        local failed=$(grep "Failed requests" "$output_file" | awk '{print $3}')
        
        echo -e "${GREEN}  Results:${NC}"
        echo -e "    Requests/sec: ${rps}"
        echo -e "    Time/request: ${time_per_request}ms"
        echo -e "    Failed: ${failed}"
    elif grep -q "Requests per second" "$output_file" || grep -q "Success Rate" "$output_file"; then
        # Custom format
        local rps=$(grep -i "requests per second" "$output_file" | awk -F: '{print $2}' | tr -d ' ')
        local success_rate=$(grep -i "success rate" "$output_file" | awk -F: '{print $2}' | tr -d ' ' | sed 's/%//')
        local avg_time=$(grep -i "average response time" "$output_file" | awk -F: '{print $2}' | tr -d ' ' | sed 's/ms//')
        
        echo -e "${GREEN}  Results:${NC}"
        [ -n "$rps" ] && echo -e "    Requests/sec: ${rps}"
        [ -n "$success_rate" ] && echo -e "    Success Rate: ${success_rate}%"
        [ -n "$avg_time" ] && echo -e "    Avg Response Time: ${avg_time}ms"
    else
        echo -e "${YELLOW}  Results saved to: ${output_file}${NC}"
    fi
}

# Get JWT token for authenticated tests
echo -e "${YELLOW}Getting JWT token for authenticated tests...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" \
    -d '{"usernameOrEmail":"testuser1763653954","password":"Test123!@#Password"}' \
    "${API_GATEWAY_URL}/api/v1/users/login" 2>&1)

JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$JWT_TOKEN" ] || [ "$JWT_TOKEN" = "null" ]; then
    echo -e "${YELLOW}Could not get JWT token, will skip authenticated tests${NC}"
    JWT_TOKEN=""
else
    echo -e "${GREEN}JWT token obtained${NC}"
fi

echo ""

# Test 1: API Gateway Health Check
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test 1: API Gateway Health Check${NC}"
echo -e "${BLUE}========================================${NC}"
run_load_test "API Gateway Health" "${API_GATEWAY_URL}/actuator/health" "GET"

# Test 2: User Service Health Check
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test 2: User Service Health Check${NC}"
echo -e "${BLUE}========================================${NC}"
run_load_test "User Service Health" "${USER_SERVICE_URL}/actuator/health" "GET"

# Test 3: API Gateway - Public Endpoint
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test 3: API Gateway - Public Endpoint${NC}"
echo -e "${BLUE}========================================${NC}"
run_load_test "API Gateway Public" "${API_GATEWAY_URL}/actuator/info" "GET"

# Test 4: API Gateway - Authenticated Endpoint (if token available)
if [ -n "$JWT_TOKEN" ]; then
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}Test 4: API Gateway - Authenticated Endpoint${NC}"
    echo -e "${BLUE}========================================${NC}"
    run_load_test "API Gateway Authenticated" \
        "${API_GATEWAY_URL}/api/v1/users/profile" \
        "GET" \
        "Authorization: Bearer ${JWT_TOKEN}"
else
    echo -e "${YELLOW}Skipping authenticated endpoint test (no token)${NC}"
fi

# Test 5: User Service - Direct Access
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test 5: User Service - Direct Access${NC}"
echo -e "${BLUE}========================================${NC}"
run_load_test "User Service Direct" "${USER_SERVICE_URL}/actuator/health" "GET"

# Generate summary report
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Load Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

SUMMARY_FILE="$RESULTS_DIR/summary.txt"
{
    echo "Load Test Summary"
    echo "=================="
    echo "Date: $(date)"
    echo "Configuration:"
    echo "  Concurrent Users: ${CONCURRENT_USERS}"
    echo "  Requests per User: ${REQUESTS_PER_USER}"
    echo "  Total Requests: $((CONCURRENT_USERS * REQUESTS_PER_USER))"
    echo "  Test Duration: ${TEST_DURATION}s"
    echo ""
    echo "Results:"
    echo "--------"
} > "$SUMMARY_FILE"

for result_file in "$RESULTS_DIR"/*.txt; do
    if [ -f "$result_file" ] && [ "$(basename "$result_file")" != "summary.txt" ]; then
        echo "" >> "$SUMMARY_FILE"
        echo "Test: $(basename "$result_file" .txt)" >> "$SUMMARY_FILE"
        tail -20 "$result_file" >> "$SUMMARY_FILE"
    fi
done

cat "$SUMMARY_FILE"
echo ""
echo -e "${GREEN}Detailed results saved to: ${RESULTS_DIR}${NC}"
echo ""

# Check if we should install ab or wrk for better testing
if ! command -v ab &> /dev/null && ! command -v wrk &> /dev/null; then
    echo -e "${YELLOW}Recommendation: Install Apache Bench or wrk for better load testing${NC}"
    echo -e "  macOS: brew install httpd (for ab) or brew install wrk"
    echo -e "  Linux: apt-get install apache2-utils (for ab) or apt-get install wrk"
    echo ""
fi

echo -e "${GREEN}Load testing complete!${NC}"

