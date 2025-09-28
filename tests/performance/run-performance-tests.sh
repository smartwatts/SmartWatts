#!/bin/bash

# SmartWatts Performance Testing Script
# This script runs comprehensive performance tests using JMeter

set -e

# Configuration
JMETER_HOME="${JMETER_HOME:-/opt/apache-jmeter-5.5}"
TARGET_URL="${TARGET_URL:-https://api.smartwatts.com}"
REPORT_DIR="./performance-reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_PREFIX="smartwatts_performance_${TIMESTAMP}"

# Test configurations
LOAD_TEST_THREADS=100
LOAD_TEST_DURATION=300
STRESS_TEST_THREADS=500
STRESS_TEST_DURATION=600
SPIKE_TEST_THREADS=1000
SPIKE_TEST_DURATION=60

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

# Function to check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check if JMeter is installed
    if [ ! -d "$JMETER_HOME" ]; then
        error "JMeter not found at $JMETER_HOME. Please install JMeter first."
    fi
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        error "Java is not installed. Please install Java first."
    fi
    
    # Check if target URL is accessible
    if ! curl -s --head "$TARGET_URL" > /dev/null; then
        error "Target URL $TARGET_URL is not accessible."
    fi
    
    log "Prerequisites check completed"
}

# Function to create report directory
create_report_directory() {
    log "Creating report directory..."
    mkdir -p "$REPORT_DIR"
    log "Report directory created: $REPORT_DIR"
}

# Function to run load test
run_load_test() {
    log "Starting load test..."
    log "Configuration: $LOAD_TEST_THREADS threads, $LOAD_TEST_DURATION seconds"
    
    local test_file="$REPORT_DIR/${REPORT_PREFIX}_load_test.jtl"
    local report_dir="$REPORT_DIR/${REPORT_PREFIX}_load_test_report"
    
    "$JMETER_HOME/bin/jmeter" -n -t tests/performance/smartwatts-load-test.jmx \
        -JTHREADS=$LOAD_TEST_THREADS \
        -JDURATION=$LOAD_TEST_DURATION \
        -JHOST=$(echo $TARGET_URL | cut -d'/' -f3 | cut -d':' -f1) \
        -JPORT=$(echo $TARGET_URL | cut -d'/' -f3 | cut -d':' -f2 | cut -d'/' -f1) \
        -JPROTOCOL=$(echo $TARGET_URL | cut -d':' -f1) \
        -l "$test_file" \
        -e -o "$report_dir"
    
    log "Load test completed. Report saved to: $report_dir"
}

# Function to run stress test
run_stress_test() {
    log "Starting stress test..."
    log "Configuration: $STRESS_TEST_THREADS threads, $STRESS_TEST_DURATION seconds"
    
    local test_file="$REPORT_DIR/${REPORT_PREFIX}_stress_test.jtl"
    local report_dir="$REPORT_DIR/${REPORT_PREFIX}_stress_test_report"
    
    "$JMETER_HOME/bin/jmeter" -n -t tests/performance/smartwatts-load-test.jmx \
        -JTHREADS=$STRESS_TEST_THREADS \
        -JDURATION=$STRESS_TEST_DURATION \
        -JHOST=$(echo $TARGET_URL | cut -d'/' -f3 | cut -d':' -f1) \
        -JPORT=$(echo $TARGET_URL | cut -d'/' -f3 | cut -d':' -f2 | cut -d'/' -f1) \
        -JPROTOCOL=$(echo $TARGET_URL | cut -d':' -f1) \
        -l "$test_file" \
        -e -o "$report_dir"
    
    log "Stress test completed. Report saved to: $report_dir"
}

# Function to run spike test
run_spike_test() {
    log "Starting spike test..."
    log "Configuration: $SPIKE_TEST_THREADS threads, $SPIKE_TEST_DURATION seconds"
    
    local test_file="$REPORT_DIR/${REPORT_PREFIX}_spike_test.jtl"
    local report_dir="$REPORT_DIR/${REPORT_PREFIX}_spike_test_report"
    
    "$JMETER_HOME/bin/jmeter" -n -t tests/performance/smartwatts-load-test.jmx \
        -JTHREADS=$SPIKE_TEST_THREADS \
        -JDURATION=$SPIKE_TEST_DURATION \
        -JHOST=$(echo $TARGET_URL | cut -d'/' -f3 | cut -d':' -f1) \
        -JPORT=$(echo $TARGET_URL | cut -d'/' -f3 | cut -d':' -f2 | cut -d'/' -f1) \
        -JPROTOCOL=$(echo $TARGET_URL | cut -d':' -f1) \
        -l "$test_file" \
        -e -o "$report_dir"
    
    log "Spike test completed. Report saved to: $report_dir"
}

# Function to run endurance test
run_endurance_test() {
    log "Starting endurance test..."
    log "Configuration: 50 threads, 3600 seconds (1 hour)"
    
    local test_file="$REPORT_DIR/${REPORT_PREFIX}_endurance_test.jtl"
    local report_dir="$REPORT_DIR/${REPORT_PREFIX}_endurance_test_report"
    
    "$JMETER_HOME/bin/jmeter" -n -t tests/performance/smartwatts-load-test.jmx \
        -JTHREADS=50 \
        -JDURATION=3600 \
        -JHOST=$(echo $TARGET_URL | cut -d'/' -f3 | cut -d':' -f1) \
        -JPORT=$(echo $TARGET_URL | cut -d'/' -f3 | cut -d':' -f2 | cut -d'/' -f1) \
        -JPROTOCOL=$(echo $TARGET_URL | cut -d':' -f1) \
        -l "$test_file" \
        -e -o "$report_dir"
    
    log "Endurance test completed. Report saved to: $report_dir"
}

# Function to run API-specific tests
run_api_tests() {
    log "Starting API-specific performance tests..."
    
    # Test each service individually
    local services=("user-service" "energy-service" "device-service" "analytics-service" "billing-service")
    
    for service in "${services[@]}"; do
        log "Testing $service..."
        
        local service_url="$TARGET_URL/api/v1/${service#*-}"
        local test_file="$REPORT_DIR/${REPORT_PREFIX}_${service}_test.jtl"
        local report_dir="$REPORT_DIR/${REPORT_PREFIX}_${service}_report"
        
        # Create a simple JMeter test for each service
        cat > "/tmp/${service}_test.jmx" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.5">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="$service Test" enabled="true">
      <stringProp name="TestPlan.comments">Performance test for $service</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Thread Group" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControllerGui" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">-1</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">50</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.duration">300</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.same_user_on_next_iteration">true</boolProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="HTTP Request" enabled="true">
          <boolProp name="HTTPSampler.postBodyRaw">false</boolProp>
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments"/>
          </elementProp>
          <stringProp name="HTTPSampler.domain">$(echo $TARGET_URL | cut -d'/' -f3 | cut -d':' -f1)</stringProp>
          <stringProp name="HTTPSampler.port">$(echo $TARGET_URL | cut -d'/' -f3 | cut -d':' -f2 | cut -d'/' -f1)</stringProp>
          <stringProp name="HTTPSampler.protocol">$(echo $TARGET_URL | cut -d':' -f1)</stringProp>
          <stringProp name="HTTPSampler.contentEncoding"></stringProp>
          <stringProp name="HTTPSampler.path">/api/v1/${service#*-}/health</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
          <stringProp name="HTTPSampler.embedded_url_re"></stringProp>
          <stringProp name="HTTPSampler.connect_timeout"></stringProp>
          <stringProp name="HTTPSampler.response_timeout"></stringProp>
        </HTTPSamplerProxy>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
EOF
        
        "$JMETER_HOME/bin/jmeter" -n -t "/tmp/${service}_test.jmx" \
            -l "$test_file" \
            -e -o "$report_dir"
        
        log "$service test completed. Report saved to: $report_dir"
    done
    
    log "API-specific tests completed"
}

# Function to analyze results
analyze_results() {
    log "Analyzing performance test results..."
    
    local analysis_file="$REPORT_DIR/${REPORT_PREFIX}_analysis.txt"
    
    cat > "$analysis_file" << EOF
SmartWatts Performance Test Analysis
===================================

Test Date: $(date)
Target URL: $TARGET_URL
Report Directory: $REPORT_DIR

Test Results Summary:
EOF
    
    # Analyze each test result
    for jtl_file in "$REPORT_DIR"/*.jtl; do
        if [ -f "$jtl_file" ]; then
            local test_name=$(basename "$jtl_file" .jtl)
            echo "" >> "$analysis_file"
            echo "Test: $test_name" >> "$analysis_file"
            echo "----------------------------------------" >> "$analysis_file"
            
            # Extract key metrics
            local total_samples=$(tail -n +2 "$jtl_file" | wc -l)
            local success_count=$(tail -n +2 "$jtl_file" | cut -d',' -f8 | grep -c "true" || echo "0")
            local error_count=$((total_samples - success_count))
            local success_rate=$((success_count * 100 / total_samples))
            
            echo "Total Samples: $total_samples" >> "$analysis_file"
            echo "Successful: $success_count" >> "$analysis_file"
            echo "Errors: $error_count" >> "$analysis_file"
            echo "Success Rate: $success_rate%" >> "$analysis_file"
            
            # Calculate average response time
            local avg_response_time=$(tail -n +2 "$jtl_file" | cut -d',' -f2 | awk '{sum+=$1} END {print sum/NR}' 2>/dev/null || echo "N/A")
            echo "Average Response Time: ${avg_response_time}ms" >> "$analysis_file"
            
            # Calculate 95th percentile response time
            local p95_response_time=$(tail -n +2 "$jtl_file" | cut -d',' -f2 | sort -n | awk 'BEGIN{i=0} {s[i]=$1; i++} END{print s[int(NR*0.95)]}' 2>/dev/null || echo "N/A")
            echo "95th Percentile Response Time: ${p95_response_time}ms" >> "$analysis_file"
        fi
    done
    
    log "Analysis completed. Results saved to: $analysis_file"
}

# Function to generate performance report
generate_performance_report() {
    log "Generating performance report..."
    
    local report_file="$REPORT_DIR/${REPORT_PREFIX}_performance_report.html"
    
    cat > "$report_file" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>SmartWatts Performance Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f0f0f0; padding: 20px; border-radius: 5px; }
        .section { margin: 20px 0; }
        .metric { display: inline-block; margin: 10px; padding: 10px; background-color: #e8f4f8; border-radius: 3px; }
        .success { color: green; }
        .warning { color: orange; }
        .error { color: red; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="header">
        <h1>SmartWatts Performance Test Report</h1>
        <p><strong>Test Date:</strong> $(date)</p>
        <p><strong>Target URL:</strong> $TARGET_URL</p>
        <p><strong>Report Directory:</strong> $REPORT_DIR</p>
    </div>
    
    <div class="section">
        <h2>Test Configuration</h2>
        <div class="metric"><strong>Load Test:</strong> $LOAD_TEST_THREADS threads, $LOAD_TEST_DURATION seconds</div>
        <div class="metric"><strong>Stress Test:</strong> $STRESS_TEST_THREADS threads, $STRESS_TEST_DURATION seconds</div>
        <div class="metric"><strong>Spike Test:</strong> $SPIKE_TEST_THREADS threads, $SPIKE_TEST_DURATION seconds</div>
    </div>
    
    <div class="section">
        <h2>Test Results</h2>
        <p>Detailed test results are available in the individual report directories:</p>
        <ul>
EOF
    
    # List all test results
    for report_dir in "$REPORT_DIR"/*_report; do
        if [ -d "$report_dir" ]; then
            local test_name=$(basename "$report_dir" _report)
            echo "            <li><a href=\"$(basename "$report_dir")/index.html\">$test_name</a></li>" >> "$report_file"
        fi
    done
    
    cat >> "$report_file" << EOF
        </ul>
    </div>
    
    <div class="section">
        <h2>Performance Metrics</h2>
        <p>Key performance indicators and thresholds:</p>
        <ul>
            <li><strong>Response Time:</strong> &lt; 200ms (target), &lt; 500ms (acceptable)</li>
            <li><strong>Throughput:</strong> &gt; 1000 requests/second (target)</li>
            <li><strong>Error Rate:</strong> &lt; 1% (target), &lt; 5% (acceptable)</li>
            <li><strong>Availability:</strong> &gt; 99.9% (target)</li>
        </ul>
    </div>
    
    <div class="section">
        <h2>Recommendations</h2>
        <p>Based on the test results, consider the following optimizations:</p>
        <ul>
            <li>Database query optimization</li>
            <li>Caching strategy implementation</li>
            <li>Load balancer configuration</li>
            <li>Auto-scaling policy adjustment</li>
            <li>Resource allocation optimization</li>
        </ul>
    </div>
</body>
</html>
EOF
    
    log "Performance report generated: $report_file"
}

# Function to run all tests
run_all_tests() {
    log "Starting comprehensive performance testing..."
    
    check_prerequisites
    create_report_directory
    run_load_test
    run_stress_test
    run_spike_test
    run_endurance_test
    run_api_tests
    analyze_results
    generate_performance_report
    
    log "Performance testing completed successfully!"
    log "Reports saved in: $REPORT_DIR"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --target URL        Target URL to test (default: https://api.smartwatts.com)"
    echo "  --load              Run load test only"
    echo "  --stress            Run stress test only"
    echo "  --spike             Run spike test only"
    echo "  --endurance         Run endurance test only"
    echo "  --api               Run API-specific tests only"
    echo "  --all               Run all tests (default)"
    echo "  --help              Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  JMETER_HOME         JMeter installation directory (default: /opt/apache-jmeter-5.5)"
    echo "  TARGET_URL          Target URL for testing (default: https://api.smartwatts.com)"
    echo "  LOAD_TEST_THREADS   Number of threads for load test (default: 100)"
    echo "  LOAD_TEST_DURATION  Duration of load test in seconds (default: 300)"
    echo ""
    echo "Examples:"
    echo "  $0 --target https://staging-api.smartwatts.com"
    echo "  $0 --load --stress"
    echo "  $0 --all"
}

# Main function
main() {
    case "${1:-}" in
        --load)
            check_prerequisites
            create_report_directory
            run_load_test
            analyze_results
            generate_performance_report
            ;;
        --stress)
            check_prerequisites
            create_report_directory
            run_stress_test
            analyze_results
            generate_performance_report
            ;;
        --spike)
            check_prerequisites
            create_report_directory
            run_spike_test
            analyze_results
            generate_performance_report
            ;;
        --endurance)
            check_prerequisites
            create_report_directory
            run_endurance_test
            analyze_results
            generate_performance_report
            ;;
        --api)
            check_prerequisites
            create_report_directory
            run_api_tests
            analyze_results
            generate_performance_report
            ;;
        --all|"")
            run_all_tests
            ;;
        --help|-h)
            show_usage
            ;;
        *)
            show_usage
            ;;
    esac
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --target)
            TARGET_URL="$2"
            shift 2
            ;;
        *)
            main "$1"
            shift
            ;;
    esac
done

# Run main function if no arguments
if [ $# -eq 0 ]; then
    main --all
fi
