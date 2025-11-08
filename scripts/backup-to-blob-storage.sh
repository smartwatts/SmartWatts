#!/bin/bash

# SmartWatts Backup to Azure Blob Storage Script
# This script uploads database backups to Azure Blob Storage
# REQUIRED for Azure deployment

set -e

# Configuration
BACKUP_DIR="${BACKUP_DIR:-/var/backups/smartwatts}"
CONTAINER_NAME="${AZURE_BLOB_CONTAINER:-backups}"
LOG_FILE="${LOG_FILE:-/var/log/smartwatts/blob-backup.log}"

# Azure Blob Storage configuration (from environment variables)
STORAGE_ACCOUNT="${AZURE_STORAGE_ACCOUNT}"
STORAGE_KEY="${AZURE_STORAGE_KEY}"
STORAGE_CONNECTION_STRING="${AZURE_STORAGE_CONNECTION_STRING}"

# Create log directory if it doesn't exist
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

# Upload file to Azure Blob Storage
upload_to_blob() {
    local file_path="$1"
    local blob_name="$2"
    
    if [ ! -f "$file_path" ]; then
        log "WARNING: File not found: $file_path"
        return 1
    fi
    
    log "Uploading $file_path to Azure Blob Storage as $blob_name"
    
    if [ -n "$STORAGE_CONNECTION_STRING" ]; then
        # Use connection string
        az storage blob upload \
            --connection-string "$STORAGE_CONNECTION_STRING" \
            --container-name "$CONTAINER_NAME" \
            --name "$blob_name" \
            --file "$file_path" \
            --output none
        
        if [ $? -eq 0 ]; then
            log "✅ Successfully uploaded $blob_name"
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
            --file "$file_path" \
            --output none
        
        if [ $? -eq 0 ]; then
            log "✅ Successfully uploaded $blob_name"
            return 0
        else
            log "❌ Failed to upload $blob_name"
            return 1
        fi
    fi
}

# Upload all backups in backup directory
upload_backups() {
    local uploaded=0
    local failed=0
    
    if [ ! -d "$BACKUP_DIR" ]; then
        log "WARNING: Backup directory does not exist: $BACKUP_DIR"
        return 1
    fi
    
    log "Scanning backup directory: $BACKUP_DIR"
    
    # Find all backup files (SQL, custom format, tar.gz)
    find "$BACKUP_DIR" -type f \( -name "*.sql" -o -name "*.dump" -o -name "*.tar.gz" -o -name "*.gz" \) | while read -r backup_file; do
        # Get relative path from backup directory
        local relative_path="${backup_file#$BACKUP_DIR/}"
        local blob_name="$(date +%Y/%m/%d)/$relative_path"
        
        # Upload to blob storage
        if upload_to_blob "$backup_file" "$blob_name"; then
            uploaded=$((uploaded + 1))
        else
            failed=$((failed + 1))
        fi
    done
    
    log "Upload complete: $uploaded uploaded, $failed failed"
}

# Upload specific backup file
upload_specific_backup() {
    local backup_file="$1"
    
    if [ -z "$backup_file" ]; then
        error_exit "No backup file specified"
    fi
    
    local filename=$(basename "$backup_file")
    local blob_name="$(date +%Y/%m/%d)/$filename"
    
    upload_to_blob "$backup_file" "$blob_name"
}

# Main execution
main() {
    log "🚀 Starting backup upload to Azure Blob Storage"
    
    # Check prerequisites
    check_azure_cli
    check_config
    
    # If specific file provided, upload it
    if [ -n "$1" ]; then
        upload_specific_backup "$1"
    else
        # Upload all backups
        upload_backups
    fi
    
    log "✅ Backup upload process completed"
}

# Run main function
main "$@"

