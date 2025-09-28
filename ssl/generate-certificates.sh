#!/bin/bash

# SmartWatts SSL Certificate Generation Script
# This script generates self-signed certificates for development and production

set -e

# Configuration
SSL_DIR="/opt/smartwatts/ssl"
CERT_DIR="$SSL_DIR/certs"
KEY_DIR="$SSL_DIR/private"
CONF_DIR="$SSL_DIR/conf"

# Certificate configuration
CERT_COUNTRY="NG"
CERT_STATE="Lagos"
CERT_CITY="Lagos"
CERT_ORG="SmartWatts"
CERT_OU="IT Department"
CERT_EMAIL="admin@smartwatts.com"
CERT_DOMAIN="smartwatts.com"
CERT_ALT_DOMAINS="api.smartwatts.com,monitoring.smartwatts.com,admin.smartwatts.com"

# Logging function
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Error handling
error_exit() {
    log "ERROR: $1"
    exit 1
}

# Function to create directories
create_directories() {
    log "Creating SSL directories..."
    
    sudo mkdir -p "$CERT_DIR"
    sudo mkdir -p "$KEY_DIR"
    sudo mkdir -p "$CONF_DIR"
    
    # Set proper permissions
    sudo chmod 755 "$CERT_DIR"
    sudo chmod 700 "$KEY_DIR"
    sudo chmod 755 "$CONF_DIR"
    
    log "SSL directories created successfully"
}

# Function to generate CA certificate
generate_ca_certificate() {
    log "Generating CA certificate..."
    
    # Generate CA private key
    sudo openssl genrsa -out "$KEY_DIR/ca-key.pem" 4096
    sudo chmod 600 "$KEY_DIR/ca-key.pem"
    
    # Generate CA certificate
    sudo openssl req -new -x509 -days 3650 -key "$KEY_DIR/ca-key.pem" \
        -out "$CERT_DIR/ca-cert.pem" \
        -subj "/C=$CERT_COUNTRY/ST=$CERT_STATE/L=$CERT_CITY/O=$CERT_ORG/OU=$CERT_OU/CN=SmartWatts CA/emailAddress=$CERT_EMAIL"
    
    log "CA certificate generated successfully"
}

# Function to generate server certificate
generate_server_certificate() {
    local service_name=$1
    local common_name=$2
    local san_domains=$3
    
    log "Generating server certificate for $service_name..."
    
    # Create certificate configuration
    local conf_file="$CONF_DIR/${service_name}.conf"
    sudo tee "$conf_file" > /dev/null << EOF
[req]
default_bits = 2048
prompt = no
distinguished_name = req_distinguished_name
req_extensions = v3_req

[req_distinguished_name]
C = $CERT_COUNTRY
ST = $CERT_STATE
L = $CERT_CITY
O = $CERT_ORG
OU = $CERT_OU
CN = $common_name
emailAddress = $CERT_EMAIL

[v3_req]
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = $common_name
EOF

    # Add SAN domains
    local domain_count=2
    for domain in $(echo "$san_domains" | tr ',' ' '); do
        echo "DNS.$domain_count = $domain" | sudo tee -a "$conf_file" > /dev/null
        ((domain_count++))
    done
    
    # Generate private key
    sudo openssl genrsa -out "$KEY_DIR/${service_name}-key.pem" 2048
    sudo chmod 600 "$KEY_DIR/${service_name}-key.pem"
    
    # Generate certificate signing request
    sudo openssl req -new -key "$KEY_DIR/${service_name}-key.pem" \
        -out "$CONF_DIR/${service_name}.csr" \
        -config "$conf_file"
    
    # Sign certificate with CA
    sudo openssl x509 -req -in "$CONF_DIR/${service_name}.csr" \
        -CA "$CERT_DIR/ca-cert.pem" \
        -CAkey "$KEY_DIR/ca-key.pem" \
        -CAcreateserial \
        -out "$CERT_DIR/${service_name}-cert.pem" \
        -days 365 \
        -extensions v3_req \
        -extfile "$conf_file"
    
    # Create PKCS12 certificate for Java services
    sudo openssl pkcs12 -export -in "$CERT_DIR/${service_name}-cert.pem" \
        -inkey "$KEY_DIR/${service_name}-key.pem" \
        -out "$CERT_DIR/${service_name}.p12" \
        -name "$service_name" \
        -password pass:smartwatts123
    
    log "Server certificate generated for $service_name"
}

# Function to generate client certificate
generate_client_certificate() {
    local client_name=$1
    
    log "Generating client certificate for $client_name..."
    
    # Generate client private key
    sudo openssl genrsa -out "$KEY_DIR/${client_name}-client-key.pem" 2048
    sudo chmod 600 "$KEY_DIR/${client_name}-client-key.pem"
    
    # Generate client certificate signing request
    sudo openssl req -new -key "$KEY_DIR/${client_name}-client-key.pem" \
        -out "$CONF_DIR/${client_name}-client.csr" \
        -subj "/C=$CERT_COUNTRY/ST=$CERT_STATE/L=$CERT_CITY/O=$CERT_ORG/OU=$CERT_OU/CN=$client_name/emailAddress=$CERT_EMAIL"
    
    # Sign client certificate with CA
    sudo openssl x509 -req -in "$CONF_DIR/${client_name}-client.csr" \
        -CA "$CERT_DIR/ca-cert.pem" \
        -CAkey "$KEY_DIR/ca-key.pem" \
        -CAcreateserial \
        -out "$CERT_DIR/${client_name}-client-cert.pem" \
        -days 365
    
    # Create PKCS12 client certificate
    sudo openssl pkcs12 -export -in "$CERT_DIR/${client_name}-client-cert.pem" \
        -inkey "$KEY_DIR/${client_name}-client-key.pem" \
        -out "$CERT_DIR/${client_name}-client.p12" \
        -name "$client_name" \
        -password pass:smartwatts123
    
    log "Client certificate generated for $client_name"
}

# Function to create truststore for Java services
create_truststore() {
    log "Creating Java truststore..."
    
    # Create truststore with CA certificate
    sudo keytool -import -alias smartwatts-ca \
        -file "$CERT_DIR/ca-cert.pem" \
        -keystore "$CERT_DIR/truststore.jks" \
        -storepass smartwatts123 \
        -noprompt
    
    log "Java truststore created successfully"
}

# Function to generate all certificates
generate_all_certificates() {
    log "Generating all SSL certificates..."
    
    # Generate CA certificate
    generate_ca_certificate
    
    # Generate server certificates for each service
    generate_server_certificate "api-gateway" "api.smartwatts.com" "api.smartwatts.com,localhost"
    generate_server_certificate "user-service" "user.smartwatts.com" "user.smartwatts.com,localhost"
    generate_server_certificate "energy-service" "energy.smartwatts.com" "energy.smartwatts.com,localhost"
    generate_server_certificate "device-service" "device.smartwatts.com" "device.smartwatts.com,localhost"
    generate_server_certificate "analytics-service" "analytics.smartwatts.com" "analytics.smartwatts.com,localhost"
    generate_server_certificate "billing-service" "billing.smartwatts.com" "billing.smartwatts.com,localhost"
    generate_server_certificate "notification-service" "notification.smartwatts.com" "notification.smartwatts.com,localhost"
    generate_server_certificate "edge-gateway-service" "edge.smartwatts.com" "edge.smartwatts.com,localhost"
    
    # Generate monitoring certificates
    generate_server_certificate "prometheus" "monitoring.smartwatts.com" "monitoring.smartwatts.com,localhost"
    generate_server_certificate "grafana" "admin.smartwatts.com" "admin.smartwatts.com,localhost"
    
    # Generate client certificates
    generate_client_certificate "admin"
    generate_client_certificate "mobile-app"
    generate_client_certificate "web-app"
    
    # Create Java truststore
    create_truststore
    
    log "All certificates generated successfully"
}

# Function to show certificate information
show_certificate_info() {
    local cert_file=$1
    
    if [ -f "$cert_file" ]; then
        log "Certificate information for $cert_file:"
        sudo openssl x509 -in "$cert_file" -text -noout | grep -E "(Subject:|Issuer:|Not Before:|Not After:|DNS:)"
    else
        log "Certificate file not found: $cert_file"
    fi
}

# Function to list all certificates
list_certificates() {
    log "Generated certificates:"
    echo ""
    
    echo "CA Certificate:"
    ls -la "$CERT_DIR/ca-cert.pem" 2>/dev/null || echo "  Not found"
    
    echo ""
    echo "Server Certificates:"
    for cert in "$CERT_DIR"/*-cert.pem; do
        if [ -f "$cert" ]; then
            echo "  $(basename "$cert")"
        fi
    done
    
    echo ""
    echo "Client Certificates:"
    for cert in "$CERT_DIR"/*-client-cert.pem; do
        if [ -f "$cert" ]; then
            echo "  $(basename "$cert")"
        fi
    done
    
    echo ""
    echo "PKCS12 Certificates:"
    for cert in "$CERT_DIR"/*.p12; do
        if [ -f "$cert" ]; then
            echo "  $(basename "$cert")"
        fi
    done
    
    echo ""
    echo "Java Truststore:"
    ls -la "$CERT_DIR/truststore.jks" 2>/dev/null || echo "  Not found"
}

# Function to create certificate bundle
create_certificate_bundle() {
    log "Creating certificate bundle..."
    
    # Create certificate bundle for web browsers
    sudo cat "$CERT_DIR/ca-cert.pem" > "$CERT_DIR/smartwatts-ca-bundle.pem"
    
    # Create certificate bundle for all server certificates
    sudo cat "$CERT_DIR"/*-cert.pem > "$CERT_DIR/smartwatts-server-bundle.pem"
    
    log "Certificate bundles created successfully"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --generate-all     Generate all certificates"
    echo "  --list             List all generated certificates"
    echo "  --info CERT_FILE   Show certificate information"
    echo "  --bundle           Create certificate bundles"
    echo "  --help             Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --generate-all"
    echo "  $0 --list"
    echo "  $0 --info /opt/smartwatts/ssl/certs/api-gateway-cert.pem"
}

# Main function
main() {
    case "${1:-}" in
        --generate-all)
            create_directories
            generate_all_certificates
            create_certificate_bundle
            list_certificates
            ;;
        --list)
            list_certificates
            ;;
        --info)
            if [ -n "${2:-}" ]; then
                show_certificate_info "$2"
            else
                error_exit "Certificate file path required"
            fi
            ;;
        --bundle)
            create_certificate_bundle
            ;;
        --help|-h)
            show_usage
            ;;
        *)
            show_usage
            ;;
    esac
}

# Run main function
main "$@"
