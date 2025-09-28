#!/bin/bash

# SmartWatts Database Restore Script
# This script restores PostgreSQL databases from backup files

set -e

# Configuration
BACKUP_DIR="/var/backups/smartwatts"
LOG_FILE="/var/log/smartwatts/restore.log"

# Database configuration
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
DB_NAME="${POSTGRES_DB:-smartwatts}"
DB_USER="${POSTGRES_USER:-postgres}"
DB_PASSWORD="${POSTGRES_PASSWORD:-postgres}"

# Logging function
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Error handling
error_exit() {
    log "ERROR: $1"
    exit 1
}

# Function to list available backups
list_backups() {
    log "Available backups:"
    find "$BACKUP_DIR" -name "*.sql" -type f | sort -r | while read -r backup_file; do
        local filename=$(basename "$backup_file")
        local size=$(du -h "$backup_file" | cut -f1)
        local date=$(echo "$filename" | grep -o '[0-9]\{8\}_[0-9]\{6\}')
        log "  $filename (Size: $size, Date: $date)"
    done
}

# Function to verify backup before restore
verify_backup() {
    local backup_file=$1
    local checksum_file="${backup_file}.sha256"
    
    if [ ! -f "$backup_file" ]; then
        error_exit "Backup file not found: $backup_file"
    fi
    
    if [ -f "$checksum_file" ]; then
        log "Verifying backup integrity..."
        if sha256sum -c "$checksum_file" >> "$LOG_FILE" 2>&1; then
            log "Backup integrity verified: $backup_file"
            return 0
        else
            error_exit "Backup integrity check failed: $backup_file"
        fi
    else
        log "WARNING: No checksum file found for: $backup_file"
        read -p "Continue without verification? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            error_exit "Restore cancelled by user"
        fi
    fi
}

# Function to create database if it doesn't exist
create_database() {
    local db_name=$1
    
    log "Creating database if it doesn't exist: $db_name"
    
    export PGPASSWORD="$DB_PASSWORD"
    
    if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres \
        -c "CREATE DATABASE $db_name;" 2>>"$LOG_FILE"; then
        log "Database created successfully: $db_name"
    else
        log "Database may already exist or creation failed: $db_name"
    fi
}

# Function to restore database
restore_database() {
    local backup_file=$1
    local db_name=$2
    
    log "Starting restore for database: $db_name"
    log "Using backup file: $backup_file"
    
    # Verify backup first
    verify_backup "$backup_file"
    
    # Create database if it doesn't exist
    create_database "$db_name"
    
    # Set PGPASSWORD environment variable
    export PGPASSWORD="$DB_PASSWORD"
    
    # Drop existing connections to the database
    log "Terminating existing connections to database: $db_name"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres \
        -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$db_name' AND pid <> pg_backend_pid();" \
        >> "$LOG_FILE" 2>&1 || true
    
    # Restore database
    log "Restoring database from backup..."
    if pg_restore -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$db_name" \
        --verbose --no-password --clean --if-exists \
        "$backup_file" >> "$LOG_FILE" 2>&1; then
        
        log "Database restored successfully: $db_name"
        
        # Verify restore by checking table count
        local table_count=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$db_name" \
            -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null | tr -d ' ')
        
        log "Restore verification: $table_count tables found in database"
        
        return 0
    else
        error_exit "Failed to restore database: $db_name"
    fi
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS] [BACKUP_FILE]"
    echo ""
    echo "Options:"
    echo "  -l, --list          List available backups"
    echo "  -h, --help          Show this help message"
    echo ""
    echo "Arguments:"
    echo "  BACKUP_FILE         Path to backup file to restore"
    echo ""
    echo "Examples:"
    echo "  $0 --list"
    echo "  $0 /var/backups/smartwatts/user-service_smartwatts_20240115_120000.sql"
    echo "  $0 user-service_smartwatts_20240115_120000.sql"
}

# Main function
main() {
    # Parse command line arguments
    case "${1:-}" in
        -l|--list)
            list_backups
            exit 0
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        "")
            show_usage
            exit 1
            ;;
        *)
            local backup_file="$1"
            
            # If backup file doesn't start with /, assume it's in BACKUP_DIR
            if [[ "$backup_file" != /* ]]; then
                backup_file="$BACKUP_DIR/$backup_file"
            fi
            
            # Check if backup file exists
            if [ ! -f "$backup_file" ]; then
                error_exit "Backup file not found: $backup_file"
            fi
            
            log "Starting SmartWatts database restore process"
            log "Backup file: $backup_file"
            
            # Confirm restore operation
            echo "WARNING: This will replace the current database with the backup!"
            echo "Database: $DB_NAME"
            echo "Backup file: $backup_file"
            read -p "Are you sure you want to continue? (y/N): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                log "Restore cancelled by user"
                exit 0
            fi
            
            # Restore database
            restore_database "$backup_file" "$DB_NAME"
            
            log "Database restore process completed successfully"
            ;;
    esac
}

# Run main function
main "$@"
