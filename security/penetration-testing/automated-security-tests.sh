#!/bin/bash

# SmartWatts Automated Security Testing Script
# This script runs comprehensive security tests against the SmartWatts platform

set -e

# Configuration
TARGET_URL="${TARGET_URL:-https://api.smartwatts.com}"
REPORT_DIR="./security-reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_PREFIX="smartwatts_security_${TIMESTAMP}"

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
    
    # Check if required tools are installed
    local tools=("nmap" "nikto" "sqlmap" "dirb" "gobuster" "whatweb" "curl" "jq")
    
    for tool in "${tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            error "$tool is not installed. Please install it first."
        fi
    done
    
    log "Prerequisites check completed"
}

# Function to create report directory
create_report_directory() {
    log "Creating report directory..."
    mkdir -p "$REPORT_DIR"
    log "Report directory created: $REPORT_DIR"
}

# Function to run reconnaissance
run_reconnaissance() {
    log "Starting reconnaissance phase..."
    
    # DNS enumeration
    log "Running DNS enumeration..."
    dig @8.8.8.8 smartwatts.com ANY > "$REPORT_DIR/${REPORT_PREFIX}_dns_enumeration.txt" 2>&1
    dig @8.8.8.8 api.smartwatts.com ANY >> "$REPORT_DIR/${REPORT_PREFIX}_dns_enumeration.txt" 2>&1
    
    # Port scanning
    log "Running port scan..."
    nmap -sS -sV -O -A -p- "$TARGET_URL" -oN "$REPORT_DIR/${REPORT_PREFIX}_nmap_scan.txt"
    
    # Service enumeration
    log "Running service enumeration..."
    whatweb "$TARGET_URL" > "$REPORT_DIR/${REPORT_PREFIX}_whatweb_scan.txt" 2>&1
    
    log "Reconnaissance phase completed"
}

# Function to run web application scanning
run_web_scanning() {
    log "Starting web application scanning..."
    
    # Directory enumeration
    log "Running directory enumeration..."
    dirb "$TARGET_URL" /usr/share/wordlists/dirb/common.txt -o "$REPORT_DIR/${REPORT_PREFIX}_dirb_scan.txt"
    
    # Gobuster directory enumeration
    log "Running Gobuster directory enumeration..."
    gobuster dir -u "$TARGET_URL" -w /usr/share/wordlists/dirb/common.txt -o "$REPORT_DIR/${REPORT_PREFIX}_gobuster_scan.txt"
    
    # Nikto web vulnerability scan
    log "Running Nikto vulnerability scan..."
    nikto -h "$TARGET_URL" -output "$REPORT_DIR/${REPORT_PREFIX}_nikto_scan.txt" -Format txt
    
    # SQLMap scan
    log "Running SQLMap scan..."
    sqlmap -u "$TARGET_URL" --batch --crawl=2 --level=3 --risk=3 --output-dir="$REPORT_DIR/${REPORT_PREFIX}_sqlmap_scan"
    
    log "Web application scanning completed"
}

# Function to run API security testing
run_api_testing() {
    log "Starting API security testing..."
    
    # API endpoint discovery
    log "Discovering API endpoints..."
    curl -s "$TARGET_URL/swagger-ui.html" > "$REPORT_DIR/${REPORT_PREFIX}_swagger_discovery.txt" 2>&1
    curl -s "$TARGET_URL/v3/api-docs" > "$REPORT_DIR/${REPORT_PREFIX}_openapi_spec.json" 2>&1
    
    # API authentication testing
    log "Testing API authentication..."
    curl -s -X POST "$TARGET_URL/api/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"admin"}' \
        > "$REPORT_DIR/${REPORT_PREFIX}_auth_test.txt" 2>&1
    
    # API rate limiting testing
    log "Testing API rate limiting..."
    for i in {1..100}; do
        curl -s "$TARGET_URL/api/v1/health" >> "$REPORT_DIR/${REPORT_PREFIX}_rate_limit_test.txt" 2>&1
        sleep 0.1
    done
    
    # API parameter fuzzing
    log "Running API parameter fuzzing..."
    curl -s "$TARGET_URL/api/v1/users?page=1&size=10&sort=id" > "$REPORT_DIR/${REPORT_PREFIX}_api_params_test.txt" 2>&1
    
    log "API security testing completed"
}

# Function to run SSL/TLS testing
run_ssl_testing() {
    log "Starting SSL/TLS testing..."
    
    # SSL certificate analysis
    log "Analyzing SSL certificate..."
    echo | openssl s_client -servername "$TARGET_URL" -connect "$TARGET_URL:443" 2>/dev/null | openssl x509 -noout -text > "$REPORT_DIR/${REPORT_PREFIX}_ssl_certificate.txt"
    
    # SSL cipher testing
    log "Testing SSL ciphers..."
    nmap --script ssl-enum-ciphers -p 443 "$TARGET_URL" > "$REPORT_DIR/${REPORT_PREFIX}_ssl_ciphers.txt"
    
    # SSL vulnerability testing
    log "Testing SSL vulnerabilities..."
    nmap --script ssl-* -p 443 "$TARGET_URL" > "$REPORT_DIR/${REPORT_PREFIX}_ssl_vulnerabilities.txt"
    
    log "SSL/TLS testing completed"
}

# Function to run infrastructure testing
run_infrastructure_testing() {
    log "Starting infrastructure testing..."
    
    # Kubernetes API testing
    log "Testing Kubernetes API..."
    curl -s -k "https://$TARGET_URL:6443/api/v1" > "$REPORT_DIR/${REPORT_PREFIX}_k8s_api_test.txt" 2>&1
    
    # Database connectivity testing
    log "Testing database connectivity..."
    nmap -p 5432,6379 "$TARGET_URL" > "$REPORT_DIR/${REPORT_PREFIX}_database_ports.txt"
    
    # Service discovery
    log "Running service discovery..."
    nmap -sS -sV -p 1-65535 "$TARGET_URL" > "$REPORT_DIR/${REPORT_PREFIX}_full_port_scan.txt"
    
    log "Infrastructure testing completed"
}

# Function to run vulnerability scanning
run_vulnerability_scanning() {
    log "Starting vulnerability scanning..."
    
    # Nmap vulnerability scripts
    log "Running Nmap vulnerability scripts..."
    nmap --script vuln "$TARGET_URL" > "$REPORT_DIR/${REPORT_PREFIX}_nmap_vuln_scan.txt"
    
    # Common vulnerabilities
    log "Testing for common vulnerabilities..."
    
    # Test for SQL injection
    curl -s "$TARGET_URL/api/v1/users?id=1' OR '1'='1" > "$REPORT_DIR/${REPORT_PREFIX}_sql_injection_test.txt" 2>&1
    
    # Test for XSS
    curl -s "$TARGET_URL/api/v1/search?q=<script>alert('XSS')</script>" > "$REPORT_DIR/${REPORT_PREFIX}_xss_test.txt" 2>&1
    
    # Test for directory traversal
    curl -s "$TARGET_URL/api/v1/files/../../../etc/passwd" > "$REPORT_DIR/${REPORT_PREFIX}_dir_traversal_test.txt" 2>&1
    
    log "Vulnerability scanning completed"
}

# Function to run performance testing
run_performance_testing() {
    log "Starting performance testing..."
    
    # Load testing with curl
    log "Running basic load test..."
    for i in {1..50}; do
        curl -s -w "%{time_total},%{http_code}\n" -o /dev/null "$TARGET_URL/api/v1/health" >> "$REPORT_DIR/${REPORT_PREFIX}_load_test.txt"
    done
    
    # Stress testing
    log "Running stress test..."
    for i in {1..100}; do
        curl -s "$TARGET_URL/api/v1/health" &
    done
    wait
    
    log "Performance testing completed"
}

# Function to generate summary report
generate_summary_report() {
    log "Generating summary report..."
    
    local summary_file="$REPORT_DIR/${REPORT_PREFIX}_summary.txt"
    
    cat > "$summary_file" << EOF
SmartWatts Security Testing Summary
===================================

Test Date: $(date)
Target URL: $TARGET_URL
Report Directory: $REPORT_DIR

Files Generated:
EOF

    # List all generated files
    find "$REPORT_DIR" -name "${REPORT_PREFIX}_*" -type f | sort >> "$summary_file"
    
    # Count vulnerabilities found
    local vuln_count=0
    
    if [ -f "$REPORT_DIR/${REPORT_PREFIX}_nikto_scan.txt" ]; then
        local nikto_vulns=$(grep -c "OSVDB-" "$REPORT_DIR/${REPORT_PREFIX}_nikto_scan.txt" || echo "0")
        vuln_count=$((vuln_count + nikto_vulns))
        echo "Nikto vulnerabilities found: $nikto_vulns" >> "$summary_file"
    fi
    
    if [ -f "$REPORT_DIR/${REPORT_PREFIX}_nmap_vuln_scan.txt" ]; then
        local nmap_vulns=$(grep -c "VULNERABLE" "$REPORT_DIR/${REPORT_PREFIX}_nmap_vuln_scan.txt" || echo "0")
        vuln_count=$((vuln_count + nmap_vulns))
        echo "Nmap vulnerabilities found: $nmap_vulns" >> "$summary_file"
    fi
    
    echo "Total vulnerabilities found: $vuln_count" >> "$summary_file"
    
    log "Summary report generated: $summary_file"
}

# Function to run all tests
run_all_tests() {
    log "Starting comprehensive security testing..."
    
    check_prerequisites
    create_report_directory
    run_reconnaissance
    run_web_scanning
    run_api_testing
    run_ssl_testing
    run_infrastructure_testing
    run_vulnerability_scanning
    run_performance_testing
    generate_summary_report
    
    log "Security testing completed successfully!"
    log "Reports saved in: $REPORT_DIR"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --target URL        Target URL to test (default: https://api.smartwatts.com)"
    echo "  --recon            Run reconnaissance only"
    echo "  --web              Run web application scanning only"
    echo "  --api              Run API security testing only"
    echo "  --ssl              Run SSL/TLS testing only"
    echo "  --infra            Run infrastructure testing only"
    echo "  --vuln             Run vulnerability scanning only"
    echo "  --perf             Run performance testing only"
    echo "  --all              Run all tests (default)"
    echo "  --help             Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --target https://staging-api.smartwatts.com"
    echo "  $0 --web --api"
    echo "  $0 --all"
}

# Main function
main() {
    case "${1:-}" in
        --recon)
            check_prerequisites
            create_report_directory
            run_reconnaissance
            generate_summary_report
            ;;
        --web)
            check_prerequisites
            create_report_directory
            run_web_scanning
            generate_summary_report
            ;;
        --api)
            check_prerequisites
            create_report_directory
            run_api_testing
            generate_summary_report
            ;;
        --ssl)
            check_prerequisites
            create_report_directory
            run_ssl_testing
            generate_summary_report
            ;;
        --infra)
            check_prerequisites
            create_report_directory
            run_infrastructure_testing
            generate_summary_report
            ;;
        --vuln)
            check_prerequisites
            create_report_directory
            run_vulnerability_scanning
            generate_summary_report
            ;;
        --perf)
            check_prerequisites
            create_report_directory
            run_performance_testing
            generate_summary_report
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
