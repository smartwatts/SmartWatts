#!/bin/bash

# SmartWatts Database Backup Script
# This script creates automated backups of PostgreSQL databases with retention policies

set -e

# Configuration
BACKUP_DIR="/var/backups/smartwatts"
RETENTION_DAYS=30
DATE=$(date +%Y%m%d_%H%M%S)
LOG_FILE="/var/log/smartwatts/backup.log"

# Database configuration
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
DB_NAME="${POSTGRES_DB:-smartwatts}"
DB_USER="${POSTGRES_USER:-postgres}"
DB_PASSWORD="${POSTGRES_PASSWORD:-postgres}"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"
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

# Function to create database backup
create_backup() {
    local service_name=$1
    local db_name=$2
    local backup_file="$BACKUP_DIR/${service_name}_${db_name}_${DATE}.sql"
    
    log "Starting backup for $service_name ($db_name)"
    
    # Set PGPASSWORD environment variable
    export PGPASSWORD="$DB_PASSWORD"
    
    # Create backup using pg_dump
    if pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$db_name" \
        --verbose --no-password --format=custom --compress=9 \
        --file="$backup_file" 2>>"$LOG_FILE"; then
        
        # Get backup size
        local backup_size=$(du -h "$backup_file" | cut -f1)
        log "Backup completed successfully: $backup_file (Size: $backup_size)"
        
        # Create checksum
        sha256sum "$backup_file" > "${backup_file}.sha256"
        
        return 0
    else
        error_exit "Failed to create backup for $service_name ($db_name)"
    fi
}

# Function to clean old backups
cleanup_old_backups() {
    log "Cleaning up backups older than $RETENTION_DAYS days"
    
    # Find and remove old backup files
    find "$BACKUP_DIR" -name "*.sql" -type f -mtime +$RETENTION_DAYS -delete
    find "$BACKUP_DIR" -name "*.sha256" -type f -mtime +$RETENTION_DAYS -delete
    
    # Count remaining backups
    local remaining_backups=$(find "$BACKUP_DIR" -name "*.sql" -type f | wc -l)
    log "Cleanup completed. Remaining backups: $remaining_backups"
}

# Function to verify backup integrity
verify_backup() {
    local backup_file=$1
    local checksum_file="${backup_file}.sha256"
    
    if [ -f "$checksum_file" ]; then
        if sha256sum -c "$checksum_file" >> "$LOG_FILE" 2>&1; then
            log "Backup integrity verified: $backup_file"
            return 0
        else
            log "WARNING: Backup integrity check failed: $backup_file"
            return 1
        fi
    else
        log "WARNING: No checksum file found for: $backup_file"
        return 1
    fi
}

# Function to upload backup to cloud storage (optional)
upload_to_cloud() {
    local backup_file=$1
    
    # Check if Azure Blob Storage is configured (REQUIRED for Azure deployment)
    if [ -n "$AZURE_STORAGE_CONNECTION_STRING" ] || ([ -n "$AZURE_STORAGE_ACCOUNT" ] && [ -n "$AZURE_STORAGE_KEY" ]); then
        log "Uploading backup to Azure Blob Storage: $backup_file"
        
        # Use backup-to-blob-storage.sh script if available
        if [ -f "$(dirname "$0")/backup-to-blob-storage.sh" ]; then
            if bash "$(dirname "$0")/backup-to-blob-storage.sh" "$backup_file" >> "$LOG_FILE" 2>&1; then
                log "Backup uploaded successfully to Azure Blob Storage"
            else
                log "WARNING: Failed to upload backup to Azure Blob Storage"
            fi
        else
            log "WARNING: backup-to-blob-storage.sh script not found. Skipping Azure Blob Storage upload."
        fi
    # Check if AWS S3 is configured (legacy support)
    elif [ -n "$AWS_S3_BUCKET" ] && [ -n "$AWS_ACCESS_KEY_ID" ]; then
        log "Uploading backup to S3: $backup_file"
        
        if aws s3 cp "$backup_file" "s3://$AWS_S3_BUCKET/smartwatts/backups/" >> "$LOG_FILE" 2>&1; then
            log "Backup uploaded successfully to S3"
        else
            log "WARNING: Failed to upload backup to S3"
        fi
    fi
}

# Main backup process
main() {
    log "Starting SmartWatts database backup process"
    
    # List of services and their databases
    declare -A services=(
        ["user-service"]="smartwatts"
        ["energy-service"]="smartwatts"
        ["device-service"]="smartwatts"
        ["analytics-service"]="smartwatts"
        ["billing-service"]="smartwatts"
        ["notification-service"]="smartwatts"
    )
    
    # Create backups for each service
    for service in "${!services[@]}"; do
        create_backup "$service" "${services[$service]}"
    done
    
    # Verify all backups
    log "Verifying backup integrity"
    local failed_verifications=0
    for backup_file in "$BACKUP_DIR"/*.sql; do
        if [ -f "$backup_file" ]; then
            if ! verify_backup "$backup_file"; then
                ((failed_verifications++))
            fi
        fi
    done
    
    if [ $failed_verifications -eq 0 ]; then
        log "All backups verified successfully"
    else
        log "WARNING: $failed_verifications backup(s) failed verification"
    fi
    
    # Upload to cloud storage if configured
    for backup_file in "$BACKUP_DIR"/*.sql; do
        if [ -f "$backup_file" ]; then
            upload_to_cloud "$backup_file"
        fi
    done
    
    # Cleanup old backups
    cleanup_old_backups
    
    log "Backup process completed successfully"
}

# Run main function
main "$@"
