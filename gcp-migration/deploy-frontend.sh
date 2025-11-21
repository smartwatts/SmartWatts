#!/bin/bash
# Deploy Frontend to Cloud Run

set -e

PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
REGION="${GCP_REGION:-europe-west1}"
SERVICE_NAME="frontend"
IMAGE_NAME="frontend"

echo "=========================================="
echo "Deploying Frontend to Cloud Run"
echo "=========================================="
echo "Project: $PROJECT_ID"
echo "Region: $REGION"
echo "Service: $SERVICE_NAME"
echo ""

# Set project
gcloud config set project "$PROJECT_ID"

# Get API Gateway URL
API_GATEWAY_URL=$(gcloud run services describe api-gateway \
  --region="$REGION" \
  --project="$PROJECT_ID" \
  --format="value(status.url)" 2>/dev/null || echo "https://api-gateway-3daykcsw5a-ew.a.run.app")

echo "API Gateway URL: $API_GATEWAY_URL"
echo ""

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

# Build Docker image
echo "Building Docker image..."
cd "$FRONTEND_DIR"

docker build \
  --platform linux/amd64 \
  -f Dockerfile.cloudrun \
  -t "$REGION-docker.pkg.dev/$PROJECT_ID/$IMAGE_NAME/$IMAGE_NAME:latest" \
  .

echo "✓ Image built"
echo ""

# Push to Artifact Registry
echo "Pushing image to Artifact Registry..."
docker push "$REGION-docker.pkg.dev/$PROJECT_ID/$IMAGE_NAME/$IMAGE_NAME:latest"

echo "✓ Image pushed"
echo ""

# Update Cloud Run config with API Gateway URL
cd "$SCRIPT_DIR"
CONFIG_FILE="$SCRIPT_DIR/cloud-run-configs/frontend.yaml"

# Update API Gateway URL in config
WS_URL="${API_GATEWAY_URL/https/wss}"
if [[ "$OSTYPE" == "darwin"* ]]; then
  # macOS
  sed -i '' "s|value: https://api-gateway.*|value: $API_GATEWAY_URL|" "$CONFIG_FILE"
  sed -i '' "s|value: wss://api-gateway.*|value: $WS_URL|" "$CONFIG_FILE"
else
  # Linux
  sed -i "s|value: https://api-gateway.*|value: $API_GATEWAY_URL|" "$CONFIG_FILE"
  sed -i "s|value: wss://api-gateway.*|value: $WS_URL|" "$CONFIG_FILE"
fi

# Deploy to Cloud Run
echo "Deploying to Cloud Run..."
gcloud run services replace "$CONFIG_FILE" \
  --region="$REGION" \
  --project="$PROJECT_ID"

echo "✓ Deployed to Cloud Run"
echo ""

# Get service URL
SERVICE_URL=$(gcloud run services describe "$SERVICE_NAME" \
  --region="$REGION" \
  --project="$PROJECT_ID" \
  --format="value(status.url)")

echo "=========================================="
echo "Frontend Deployment Complete!"
echo "=========================================="
echo ""
echo "🌐 Frontend URL: $SERVICE_URL"
echo "🔗 API Gateway: $API_GATEWAY_URL"
echo ""
echo "You can now access SmartWatts at:"
echo "  $SERVICE_URL"
echo ""
echo "Next steps:"
echo "1. Open the URL in your browser"
echo "2. Test login and dashboard"
echo "3. Configure custom domain (optional)"
echo ""

