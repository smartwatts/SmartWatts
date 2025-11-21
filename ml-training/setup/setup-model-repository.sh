#!/bin/bash
# Setup Model Repository on GCP Cloud Storage
# This creates a bucket for storing trained ML models

set -e

PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
BUCKET_NAME="${MODEL_BUCKET_NAME:-smartwatts-models}"
REGION="${GCP_REGION:-europe-west1}"

echo "=========================================="
echo "Setting up Model Repository"
echo "=========================================="
echo "Project: $PROJECT_ID"
echo "Bucket: $BUCKET_NAME"
echo "Region: $REGION"
echo ""

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "Error: gcloud CLI not found. Please install Google Cloud SDK."
    exit 1
fi

# Set project
echo "Setting GCP project..."
gcloud config set project "$PROJECT_ID"

# Check if bucket exists
if gsutil ls -b "gs://$BUCKET_NAME" &> /dev/null; then
    echo "✓ Bucket $BUCKET_NAME already exists"
else
    echo "Creating bucket $BUCKET_NAME..."
    gsutil mb -p "$PROJECT_ID" -c STANDARD -l "$REGION" "gs://$BUCKET_NAME"
    echo "✓ Bucket created"
fi

# Set bucket permissions (public read for models)
echo "Setting bucket permissions..."
gsutil iam ch allUsers:objectViewer "gs://$BUCKET_NAME"
echo "✓ Public read access enabled"

# Note: GCS doesn't require folders - objects are created with paths
# The folder structure will be created automatically when files are uploaded
echo "✓ Folder structure will be created automatically when models are uploaded"

# Set CORS for web access (if needed)
echo "Setting CORS configuration..."
cat > /tmp/cors.json << EOF
[
  {
    "origin": ["*"],
    "method": ["GET", "HEAD"],
    "responseHeader": ["Content-Type"],
    "maxAgeSeconds": 3600
  }
]
EOF
gsutil cors set /tmp/cors.json "gs://$BUCKET_NAME"
rm /tmp/cors.json
echo "✓ CORS configured"

# Create version file
echo "Creating version file..."
cat > /tmp/versions.json << EOF
{
  "energy_forecast": "1.0.0",
  "anomaly_detection": "1.0.0",
  "load_prediction": "1.0.0",
  "optimization": "1.0.0",
  "last_updated": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
}
EOF
gsutil cp /tmp/versions.json "gs://$BUCKET_NAME/models/versions.json"
rm /tmp/versions.json
echo "✓ Version file created"

# Get public URL
PUBLIC_URL="https://storage.googleapis.com/$BUCKET_NAME"
echo ""
echo "=========================================="
echo "Model Repository Setup Complete!"
echo "=========================================="
echo "Bucket: gs://$BUCKET_NAME"
echo "Public URL: $PUBLIC_URL"
echo ""
echo "Model URLs:"
echo "  Energy Forecast: $PUBLIC_URL/models/tflite/energy_forecast.tflite"
echo "  Anomaly Detection: $PUBLIC_URL/models/tflite/anomaly_detection.tflite"
echo "  Load Prediction: $PUBLIC_URL/models/tflite/load_prediction.tflite"
echo "  Optimization: $PUBLIC_URL/models/tflite/optimization.tflite"
echo ""
echo "Next steps:"
echo "1. Update edge-gateway config with model URLs"
echo "2. Upload trained models using: ./upload-models.sh"
echo ""

