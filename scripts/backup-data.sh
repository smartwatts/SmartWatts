#!/bin/bash

# SmartWatts Data Backup Script
# Creates timestamped backup of all SmartWatts data

set -e

BACKUP_DIR="data/backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_NAME="smartwatts_backup_$TIMESTAMP"
BACKUP_PATH="$BACKUP_DIR/$BACKUP_NAME"

echo "🔄 Creating SmartWatts data backup..."
echo "Backup name: $BACKUP_NAME"

# Create backup directory
mkdir -p "$BACKUP_PATH"

# Stop services to ensure data consistency
echo "Stopping services for backup..."
docker-compose -f docker-compose.yml down

# Backup database data
if [ -d "data/postgres" ]; then
    echo "Backing up PostgreSQL data..."
    cp -r data/postgres "$BACKUP_PATH/"
fi

# Backup Redis data
if [ -d "data/redis" ]; then
    echo "Backing up Redis data..."
    cp -r data/redis "$BACKUP_PATH/"
fi

# Backup Edge Gateway data
if [ -d "data/edge-gateway" ]; then
    echo "Backing up Edge Gateway data..."
    cp -r data/edge-gateway "$BACKUP_PATH/"
fi

# Backup configuration files
echo "Backing up configuration files..."
cp .env "$BACKUP_PATH/" 2>/dev/null || echo "No .env file found"
cp -r config "$BACKUP_PATH/" 2>/dev/null || echo "No config directory found"

# Create backup info file
cat > "$BACKUP_PATH/backup_info.txt" << EOF
SmartWatts Backup Information
============================
Backup Date: $(date)
Backup Name: $BACKUP_NAME
SmartWatts Version: 1.0.0
Platform: $(uname -s) $(uname -m)
Docker Version: $(docker --version)
Docker Compose Version: $(docker-compose --version)

Contents:
- PostgreSQL data
- Redis data
- Edge Gateway data
- Configuration files

To restore this backup:
1. Stop SmartWatts: docker-compose down
2. Copy backup data: cp -r $BACKUP_NAME/* data/
3. Start SmartWatts: docker-compose up -d
EOF

# Create compressed backup
echo "Creating compressed backup..."
cd "$BACKUP_DIR"
tar -czf "${BACKUP_NAME}.tar.gz" "$BACKUP_NAME"
rm -rf "$BACKUP_NAME"
cd - > /dev/null

# Restart services
echo "Restarting services..."
docker-compose -f docker-compose.yml up -d

echo "✅ Backup completed successfully!"
echo "Backup location: $BACKUP_PATH.tar.gz"
echo "Backup size: $(du -h "$BACKUP_DIR/${BACKUP_NAME}.tar.gz" | cut -f1)"

# List all backups
echo ""
echo "Available backups:"
ls -la "$BACKUP_DIR"/*.tar.gz 2>/dev/null || echo "No backups found"


