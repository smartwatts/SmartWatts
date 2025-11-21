#!/bin/bash

###############################################################################
# Cloud SQL Setup Script
# 
# Purpose: Create Cloud SQL PostgreSQL instances for staging and production
#          with multi-region setup (europe-west1 primary, europe-west4 replica)
#          and create all 9 databases
#
# Usage: ./setup-cloud-sql.sh
#
# Prerequisites:
#   - GCP projects created
#   - Service accounts created
#   - Google Cloud SDK (gcloud) installed and configured
#
###############################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
STAGING_PROJECT_ID="smartwatts-staging"
PRODUCTION_PROJECT_ID="smartwatts-production"
PRIMARY_REGION="europe-west1"
REPLICA_REGION="europe-west4"

# Database instance configuration
STAGING_INSTANCE_NAME="smartwatts-staging-db"
PRODUCTION_INSTANCE_NAME="smartwatts-production-db"
DATABASE_VERSION="POSTGRES_15"
TIER="db-f1-micro"  # Free tier for staging, can be upgraded for production
STORAGE_SIZE="20GB"
STORAGE_TYPE="SSD"

# Database list
DATABASES=(
    "smartwatts_users"
    "smartwatts_energy"
    "smartwatts_devices"
    "smartwatts_analytics"
    "smartwatts_billing"
    "smartwatts_facility360"
    "smartwatts_feature_flags"
    "smartwatts_device_verification"
    "smartwatts_appliance_monitoring"
)

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Cloud SQL Setup${NC}"
echo -e "${BLUE}Primary Region: ${PRIMARY_REGION}${NC}"
echo -e "${BLUE}Replica Region: ${REPLICA_REGION}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    if ! command -v gcloud &> /dev/null; then
        echo -e "${RED}Error: Google Cloud SDK (gcloud) not found${NC}"
        exit 1
    fi
    
    # Verify projects exist
    if ! gcloud projects describe "${STAGING_PROJECT_ID}" &>/dev/null; then
        echo -e "${RED}Error: Staging project not found${NC}"
        exit 1
    fi
    
    if ! gcloud projects describe "${PRODUCTION_PROJECT_ID}" &>/dev/null; then
        echo -e "${RED}Error: Production project not found${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Prerequisites checked${NC}"
    echo ""
}

# Function to create Cloud SQL instance
create_cloud_sql_instance() {
    local project_id=$1
    local instance_name=$2
    local tier=$3
    
    echo -e "${YELLOW}Creating Cloud SQL instance: ${instance_name} in ${project_id}...${NC}"
    
    # Set project
    gcloud config set project "${project_id}"
    
    # Check if instance already exists
    if gcloud sql instances describe "${instance_name}" --project="${project_id}" &>/dev/null; then
        echo -e "${YELLOW}Instance ${instance_name} already exists, skipping creation${NC}"
        return 0
    fi
    
    # Create instance
    gcloud sql instances create "${instance_name}" \
        --project="${project_id}" \
        --database-version="${DATABASE_VERSION}" \
        --tier="${tier}" \
        --region="${PRIMARY_REGION}" \
        --storage-type="${STORAGE_TYPE}" \
        --storage-size="${STORAGE_SIZE}" \
        --backup-start-time="02:00" \
        --maintenance-window-day=SUN \
        --maintenance-window-hour=3 \
        --maintenance-release-channel=production \
        --database-flags="max_connections=100" || {
        echo -e "${RED}Error: Failed to create Cloud SQL instance${NC}"
        return 1
    }
    
    echo -e "${GREEN}✓ Cloud SQL instance ${instance_name} created${NC}"
    echo ""
}

# Function to set root password
set_root_password() {
    local project_id=$1
    local instance_name=$2
    local password=$3
    
    echo -e "${YELLOW}Setting root password for ${instance_name}...${NC}"
    
    gcloud sql users set-password root \
        --instance="${instance_name}" \
        --project="${project_id}" \
        --password="${password}" || {
        echo -e "${YELLOW}Warning: Could not set root password${NC}"
        echo "You may need to set it manually"
    }
    
    echo -e "${GREEN}✓ Root password set${NC}"
    echo ""
}

# Function to create database
create_database() {
    local project_id=$1
    local instance_name=$2
    local database_name=$3
    
    echo -e "  Creating database: ${database_name}"
    
    # Check if database already exists
    if gcloud sql databases describe "${database_name}" \
        --instance="${instance_name}" \
        --project="${project_id}" &>/dev/null; then
        echo -e "    ${YELLOW}Database ${database_name} already exists, skipping${NC}"
        return 0
    fi
    
    # Create database
    gcloud sql databases create "${database_name}" \
        --instance="${instance_name}" \
        --project="${project_id}" || {
        echo -e "    ${RED}Error: Failed to create database ${database_name}${NC}"
        return 1
    }
    
    echo -e "    ${GREEN}✓ Database ${database_name} created${NC}"
}

# Function to create all databases
create_all_databases() {
    local project_id=$1
    local instance_name=$2
    
    echo -e "${YELLOW}Creating databases in ${instance_name}...${NC}"
    
    for database_name in "${DATABASES[@]}"; do
        create_database "${project_id}" "${instance_name}" "${database_name}"
    done
    
    echo -e "${GREEN}✓ All databases created${NC}"
    echo ""
}

# Function to configure connection
configure_connection() {
    local project_id=$1
    local instance_name=$2
    
    echo -e "${YELLOW}Configuring connection for ${instance_name}...${NC}"
    
    # Get connection name
    CONNECTION_NAME=$(gcloud sql instances describe "${instance_name}" \
        --project="${project_id}" \
        --format="value(connectionName)")
    
    echo -e "${GREEN}✓ Connection name: ${CONNECTION_NAME}${NC}"
    echo ""
}

# Function to create read replica (for production)
create_read_replica() {
    local project_id=$1
    local instance_name=$2
    local replica_name="${instance_name}-replica"
    
    echo -e "${YELLOW}Creating read replica: ${replica_name}...${NC}"
    
    # Check if replica already exists
    if gcloud sql instances describe "${replica_name}" --project="${project_id}" &>/dev/null; then
        echo -e "${YELLOW}Replica ${replica_name} already exists, skipping creation${NC}"
        return 0
    fi
    
    # Create read replica
    gcloud sql instances create "${replica_name}" \
        --project="${project_id}" \
        --master-instance-name="${instance_name}" \
        --region="${REPLICA_REGION}" \
        --tier="${TIER}" || {
        echo -e "${YELLOW}Warning: Could not create read replica${NC}"
        echo "This is optional and can be created later"
        return 1
    }
    
    echo -e "${GREEN}✓ Read replica ${replica_name} created${NC}"
    echo ""
}

# Function to setup environment databases
setup_environment_databases() {
    local project_id=$1
    local instance_name=$2
    local tier=$3
    local environment=$4
    
    echo -e "${BLUE}Setting up databases for ${environment}...${NC}"
    echo ""
    
    # Create instance
    create_cloud_sql_instance "${project_id}" "${instance_name}" "${tier}"
    
    # Set root password (prompt user)
    if [ "${environment}" = "production" ]; then
        read -sp "Enter root password for ${instance_name}: " ROOT_PASSWORD
        echo ""
        set_root_password "${project_id}" "${instance_name}" "${ROOT_PASSWORD}"
    fi
    
    # Create all databases
    create_all_databases "${project_id}" "${instance_name}"
    
    # Configure connection
    configure_connection "${project_id}" "${instance_name}"
    
    # Create read replica for production
    if [ "${environment}" = "production" ]; then
        create_read_replica "${project_id}" "${instance_name}"
    fi
    
    echo -e "${GREEN}✓ Database setup complete for ${environment}${NC}"
    echo ""
}

# Function to create database summary
create_database_summary() {
    echo -e "${YELLOW}Creating database summary...${NC}"
    
    local summary_file="gcp-migration/gcp-setup/cloud-sql-summary.json"
    
    # Get connection names
    STAGING_CONNECTION=$(gcloud sql instances describe "${STAGING_INSTANCE_NAME}" \
        --project="${STAGING_PROJECT_ID}" \
        --format="value(connectionName)" 2>/dev/null || echo "")
    
    PRODUCTION_CONNECTION=$(gcloud sql instances describe "${PRODUCTION_INSTANCE_NAME}" \
        --project="${PRODUCTION_PROJECT_ID}" \
        --format="value(connectionName)" 2>/dev/null || echo "")
    
    cat > "${summary_file}" <<EOF
{
  "creationDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "primaryRegion": "${PRIMARY_REGION}",
  "replicaRegion": "${REPLICA_REGION}",
  "instances": {
    "staging": {
      "projectId": "${STAGING_PROJECT_ID}",
      "instanceName": "${STAGING_INSTANCE_NAME}",
      "connectionName": "${STAGING_CONNECTION}",
      "region": "${PRIMARY_REGION}",
      "tier": "${TIER}",
      "databases": $(printf '%s\n' "${DATABASES[@]}" | jq -R . | jq -s .)
    },
    "production": {
      "projectId": "${PRODUCTION_PROJECT_ID}",
      "instanceName": "${PRODUCTION_INSTANCE_NAME}",
      "connectionName": "${PRODUCTION_CONNECTION}",
      "region": "${PRIMARY_REGION}",
      "tier": "${TIER}",
      "databases": $(printf '%s\n' "${DATABASES[@]}" | jq -R . | jq -s .)
    }
  }
}
EOF
    
    echo -e "${GREEN}✓ Database summary created: ${summary_file}${NC}"
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}Starting Cloud SQL setup...${NC}"
    echo ""
    
    check_prerequisites
    
    # Setup staging databases
    setup_environment_databases "${STAGING_PROJECT_ID}" "${STAGING_INSTANCE_NAME}" "${TIER}" "staging"
    
    # Setup production databases
    setup_environment_databases "${PRODUCTION_PROJECT_ID}" "${PRODUCTION_INSTANCE_NAME}" "${TIER}" "production"
    
    # Create summary
    create_database_summary
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Cloud SQL setup completed!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Note the connection names for Cloud Run configuration"
    echo "2. Run setup-artifact-registry.sh to create Docker repositories"
    echo "3. Run setup-secrets.sh to migrate secrets"
    echo "4. Update application configurations with Cloud SQL connection strings"
}

# Run main function
main "$@"

