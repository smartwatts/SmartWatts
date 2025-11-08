#!/bin/bash

# SmartWatts Log Archival to Azure Blob Storage Script
# This script archives application logs to Azure Blob Storage
# REQUIRED for Azure deployment

set -e

# Configuration
LOG_DIR="${LOG_DIR:-/var/log/smartwatts}"
ARCHIVE_DIR="${ARCHIVE_DIR:-/tmp/smartwatts-log-archive}"
CONTAINER_NAME="${AZURE_BLOB_LOG_CONTAINER:-logs}"
RETENTION_DAYS="${LOG_RETENTION_DAYS:-90}"
LOG_FILE="${LOG_FILE:-/var/log/smartwatts/log-archive.log}"

# Azure Blob Storage configuration (from environment variables)
STORAGE_ACCOUNT="${AZURE_STORAGE_ACCOUNT}"
STORAGE_KEY="${AZURE_STORAGE_KEY}"
STORAGE_CONNECTION_STRING="${AZURE_STORAGE_CONNECTION_STRING}"

# Create directories if they don't exist
mkdir -p "$ARCHIVE_DIR"
mkdir -p "$(dirname "$LOG_FILE")"

# Logging function
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Error handling
error_exit() {
    log "ERROR: $1"
    exit 1
}

# Check if Azure CLI is installed
check_azure_cli() {
    if ! command -v az &> /dev/null; then
        error_exit "Azure CLI is not installed. Install it from https://docs.microsoft.com/cli/azure/install-azure-cli"
    fi
}

# Check Azure Blob Storage configuration
check_config() {
    if [ -z "$STORAGE_CONNECTION_STRING" ] && [ -z "$STORAGE_ACCOUNT" ] || [ -z "$STORAGE_KEY" ]; then
        error_exit "Azure Blob Storage configuration is missing. Set AZURE_STORAGE_CONNECTION_STRING or AZURE_STORAGE_ACCOUNT and AZURE_STORAGE_KEY"
    fi
}

# Archive and compress log files
archive_logs() {
    local archive_file="$ARCHIVE_DIR/logs-$(date +%Y%m%d_%H%M%S).tar.gz"
    
    log "Archiving logs from $LOG_DIR"
    
    # Find log files older than retention period
    find "$LOG_DIR" -type f -name "*.log" -mtime +$RETENTION_DAYS | while read -r log_file; do
        log "Archiving: $log_file"
    done
    
    # Create archive of all log files
    if tar -czf "$archive_file" -C "$LOG_DIR" . 2>/dev/null; then
        log "✅ Created archive: $archive_file"
        echo "$archive_file"
    else
        log "❌ Failed to create archive"
        return 1
    fi
}

# Upload archive to Azure Blob Storage
upload_archive() {
    local archive_file="$1"
    
    if [ ! -f "$archive_file" ]; then
        log "WARNING: Archive file not found: $archive_file"
        return 1
    fi
    
    local filename=$(basename "$archive_file")
    local blob_name="$(date +%Y/%m)/$filename"
    
    log "Uploading $archive_file to Azure Blob Storage as $blob_name"
    
    if [ -n "$STORAGE_CONNECTION_STRING" ]; then
        # Use connection string
        az storage blob upload \
            --connection-string "$STORAGE_CONNECTION_STRING" \
            --container-name "$CONTAINER_NAME" \
            --name "$blob_name" \
            --file "$archive_file" \
            --output none
        
        if [ $? -eq 0 ]; then
            log "✅ Successfully uploaded $blob_name"
            # Remove local archive after successful upload
            rm -f "$archive_file"
            return 0
        else
            log "❌ Failed to upload $blob_name"
            return 1
        fi
    else
        # Use account name and key
        az storage blob upload \
            --account-name "$STORAGE_ACCOUNT" \
            --account-key "$STORAGE_KEY" \
            --container-name "$CONTAINER_NAME" \
            --name "$blob_name" \
            --file "$archive_file" \
            --output none
        
        if [ $? -eq 0 ]; then
            log "✅ Successfully uploaded $blob_name"
            # Remove local archive after successful upload
            rm -f "$archive_file"
            return 0
        else
            log "❌ Failed to upload $blob_name"
            return 1
        fi
    fi
}

# Clean up old log files (after archival)
cleanup_old_logs() {
    log "Cleaning up log files older than $RETENTION_DAYS days"
    
    find "$LOG_DIR" -type f -name "*.log" -mtime +$RETENTION_DAYS -delete
    
    log "✅ Cleanup completed"
}

# Main execution
main() {
    log "🚀 Starting log archival to Azure Blob Storage"
    
    # Check prerequisites
    check_azure_cli
    check_config
    
    # Archive logs
    archive_file=$(archive_logs)
    
    if [ -n "$archive_file" ]; then
        # Upload archive
        if upload_archive "$archive_file"; then
            # Clean up old logs after successful upload
            cleanup_old_logs
        fi
    else
        log "No logs to archive"
    fi
    
    log "✅ Log archival process completed"
}

# Run main function
main "$@"

