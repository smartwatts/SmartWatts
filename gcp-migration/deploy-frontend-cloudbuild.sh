#!/bin/bash
# Deploy Frontend to Cloud Run using Cloud Build

set -e

PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
REGION="${GCP_REGION:-europe-west1}"
SERVICE_NAME="frontend"
IMAGE_NAME="frontend"

echo "=========================================="
echo "Deploying Frontend to Cloud Run (Cloud Build)"
echo "=========================================="
echo "Project: $PROJECT_ID"
echo "Region: $REGION"
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

# Build and deploy using Cloud Build
echo "Building and deploying using Cloud Build..."
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

cd "$FRONTEND_DIR"

gcloud builds submit \
  --config=cloudbuild.yaml \
  --project="$PROJECT_ID" \
  --substitutions=_REGION=$REGION,_PROJECT_ID=$PROJECT_ID

echo "✓ Image built and pushed"
echo ""

# Deploy to Cloud Run
echo "Deploying to Cloud Run..."
cd "$(dirname "$0")"

gcloud run deploy "$SERVICE_NAME" \
  --image="$REGION-docker.pkg.dev/$PROJECT_ID/$IMAGE_NAME/$IMAGE_NAME:latest" \
  --region="$REGION" \
  --project="$PROJECT_ID" \
  --platform=managed \
  --allow-unauthenticated \
  --service-account="cloud-run-sa@$PROJECT_ID.iam.gserviceaccount.com" \
  --set-env-vars="NODE_ENV=production,HOSTNAME=0.0.0.0,NEXT_PUBLIC_API_URL=$API_GATEWAY_URL,NEXT_PUBLIC_WS_URL=${API_GATEWAY_URL/https/wss}" \
  --memory=1Gi \
  --cpu=1 \
  --timeout=300 \
  --max-instances=10 \
  --min-instances=1 \
  --port=3000 \
  --execution-environment=gen2

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

