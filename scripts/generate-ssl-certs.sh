#!/bin/bash

# SmartWatts SSL Certificate Generation Script
# This script generates self-signed certificates for development/testing

set -e

echo "🔐 Generating SSL certificates for SmartWatts..."

# Create SSL directory
mkdir -p nginx/ssl

# Generate private key
echo "📝 Generating private key..."
openssl genrsa -out nginx/ssl/smartwatts.key 2048

# Generate certificate signing request
echo "📝 Generating certificate signing request..."
openssl req -new -key nginx/ssl/smartwatts.key -out nginx/ssl/smartwatts.csr -subj "/C=NG/ST=Lagos/L=Lagos/O=SmartWatts/OU=IT Department/CN=mysmartwatts.com"

# Generate self-signed certificate
echo "📝 Generating self-signed certificate..."
openssl x509 -req -days 365 -in nginx/ssl/smartwatts.csr -signkey nginx/ssl/smartwatts.key -out nginx/ssl/smartwatts.crt

# Set proper permissions
chmod 600 nginx/ssl/smartwatts.key
chmod 644 nginx/ssl/smartwatts.crt

echo "✅ SSL certificates generated successfully!"
echo "📁 Certificate files:"
echo "   - Private Key: nginx/ssl/smartwatts.key"
echo "   - Certificate: nginx/ssl/smartwatts.crt"
echo ""
echo "🚀 To use with Nginx, update nginx.conf to use:"
echo "   ssl_certificate /etc/nginx/ssl/smartwatts.crt;"
echo "   ssl_certificate_key /etc/nginx/ssl/smartwatts.key;"
echo ""
echo "⚠️  Note: These are self-signed certificates for development only."
echo "   For production, use Let's Encrypt or a trusted CA."
