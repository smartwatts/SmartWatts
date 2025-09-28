#!/bin/bash

# SmartWatts Backup Cron Setup Script
# This script sets up automated database backups using cron

set -e

# Configuration
SCRIPT_DIR="/opt/smartwatts/scripts"
BACKUP_SCRIPT="$SCRIPT_DIR/backup-database.sh"
CRON_LOG="/var/log/smartwatts/cron-backup.log"

# Logging function
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Function to create necessary directories
setup_directories() {
    log "Creating necessary directories..."
    
    # Create script directory
    sudo mkdir -p "$SCRIPT_DIR"
    
    # Create log directory
    sudo mkdir -p "/var/log/smartwatts"
    
    # Create backup directory
    sudo mkdir -p "/var/backups/smartwatts"
    
    # Set proper permissions
    sudo chown -R $(whoami):$(whoami) "/var/log/smartwatts"
    sudo chown -R $(whoami):$(whoami) "/var/backups/smartwatts"
    
    log "Directories created successfully"
}

# Function to install backup script
install_backup_script() {
    log "Installing backup script..."
    
    # Copy backup script to script directory
    sudo cp "$(dirname "$0")/backup-database.sh" "$BACKUP_SCRIPT"
    sudo chmod +x "$BACKUP_SCRIPT"
    
    # Create wrapper script for cron
    sudo tee "$SCRIPT_DIR/backup-wrapper.sh" > /dev/null << 'EOF'
#!/bin/bash
# Wrapper script for database backup with proper environment

# Set environment variables
export PATH="/usr/local/bin:/usr/bin:/bin"
export PGPASSWORD="${POSTGRES_PASSWORD:-postgres}"

# Load environment file if it exists
if [ -f "/opt/smartwatts/.env" ]; then
    source "/opt/smartwatts/.env"
fi

# Run backup script
exec /opt/smartwatts/scripts/backup-database.sh >> /var/log/smartwatts/cron-backup.log 2>&1
EOF

    sudo chmod +x "$SCRIPT_DIR/backup-wrapper.sh"
    
    log "Backup script installed successfully"
}

# Function to setup cron job
setup_cron_job() {
    log "Setting up cron job for automated backups..."
    
    # Create cron job entry
    local cron_entry="0 2 * * * $SCRIPT_DIR/backup-wrapper.sh"
    
    # Check if cron job already exists
    if crontab -l 2>/dev/null | grep -q "backup-wrapper.sh"; then
        log "Cron job already exists, skipping..."
    else
        # Add cron job
        (crontab -l 2>/dev/null; echo "$cron_entry") | crontab -
        log "Cron job added successfully: $cron_entry"
    fi
    
    # Show current crontab
    log "Current crontab:"
    crontab -l 2>/dev/null | grep -v "^#" | grep -v "^$" || log "No cron jobs found"
}

# Function to create environment file template
create_env_template() {
    log "Creating environment file template..."
    
    sudo tee "/opt/smartwatts/.env.template" > /dev/null << 'EOF'
# SmartWatts Database Backup Configuration
# Copy this file to /opt/smartwatts/.env and update with your values

# Database Configuration
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=smartwatts
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Backup Configuration
BACKUP_RETENTION_DAYS=30
BACKUP_DIR=/var/backups/smartwatts

# Optional: AWS S3 Configuration for cloud backup
# AWS_ACCESS_KEY_ID=your_access_key
# AWS_SECRET_ACCESS_KEY=your_secret_key
# AWS_S3_BUCKET=your-backup-bucket
# AWS_DEFAULT_REGION=us-east-1
EOF

    log "Environment template created: /opt/smartwatts/.env.template"
}

# Function to create backup monitoring script
create_monitoring_script() {
    log "Creating backup monitoring script..."
    
    sudo tee "$SCRIPT_DIR/check-backup-status.sh" > /dev/null << 'EOF'
#!/bin/bash

# SmartWatts Backup Status Check Script

BACKUP_DIR="/var/backups/smartwatts"
LOG_FILE="/var/log/smartwatts/backup.log"
ALERT_EMAIL="${BACKUP_ALERT_EMAIL:-admin@smartwatts.com}"

# Check if backup directory exists
if [ ! -d "$BACKUP_DIR" ]; then
    echo "ERROR: Backup directory does not exist: $BACKUP_DIR"
    exit 1
fi

# Check for recent backups (within last 25 hours)
recent_backups=$(find "$BACKUP_DIR" -name "*.sql" -type f -mtime -1 | wc -l)

if [ "$recent_backups" -eq 0 ]; then
    echo "WARNING: No recent backups found in the last 24 hours"
    echo "Backup directory: $BACKUP_DIR"
    echo "Last backup check: $(date)"
    
    # Send alert email if configured
    if command -v mail >/dev/null 2>&1; then
        echo "No recent backups found in the last 24 hours" | mail -s "SmartWatts Backup Alert" "$ALERT_EMAIL"
    fi
    
    exit 1
else
    echo "OK: $recent_backups recent backup(s) found"
    exit 0
fi
EOF

    sudo chmod +x "$SCRIPT_DIR/check-backup-status.sh"
    
    log "Backup monitoring script created successfully"
}

# Function to show status
show_status() {
    log "SmartWatts Backup Setup Status:"
    echo ""
    
    # Check if backup script exists
    if [ -f "$BACKUP_SCRIPT" ]; then
        echo "✓ Backup script installed: $BACKUP_SCRIPT"
    else
        echo "✗ Backup script not found: $BACKUP_SCRIPT"
    fi
    
    # Check if cron job exists
    if crontab -l 2>/dev/null | grep -q "backup-wrapper.sh"; then
        echo "✓ Cron job configured"
        crontab -l 2>/dev/null | grep "backup-wrapper.sh"
    else
        echo "✗ Cron job not configured"
    fi
    
    # Check backup directory
    if [ -d "/var/backups/smartwatts" ]; then
        echo "✓ Backup directory exists: /var/backups/smartwatts"
        local backup_count=$(find "/var/backups/smartwatts" -name "*.sql" -type f | wc -l)
        echo "  - Backup files: $backup_count"
    else
        echo "✗ Backup directory not found: /var/backups/smartwatts"
    fi
    
    # Check log directory
    if [ -d "/var/log/smartwatts" ]; then
        echo "✓ Log directory exists: /var/log/smartwatts"
    else
        echo "✗ Log directory not found: /var/log/smartwatts"
    fi
    
    echo ""
    echo "Next steps:"
    echo "1. Copy /opt/smartwatts/.env.template to /opt/smartwatts/.env"
    echo "2. Update environment variables in /opt/smartwatts/.env"
    echo "3. Test backup manually: $BACKUP_SCRIPT"
    echo "4. Monitor backup logs: tail -f /var/log/smartwatts/backup.log"
}

# Main function
main() {
    case "${1:-}" in
        --status)
            show_status
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --status    Show current setup status"
            echo "  --help      Show this help message"
            echo ""
            echo "This script sets up automated database backups for SmartWatts."
            ;;
        *)
            log "Setting up SmartWatts automated backup system..."
            
            setup_directories
            install_backup_script
            setup_cron_job
            create_env_template
            create_monitoring_script
            
            log "Setup completed successfully!"
            echo ""
            show_status
            ;;
    esac
}

# Run main function
main "$@"
