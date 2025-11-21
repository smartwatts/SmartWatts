#!/bin/bash
# Upload trained models to GCP Cloud Storage repository

set -e

BUCKET_NAME="${MODEL_BUCKET_NAME:-smartwatts-models}"
MODELS_DIR="${MODELS_DIR:-../models/tflite}"
VERSION="${MODEL_VERSION:-$(date +%Y%m%d_%H%M%S)}"

echo "=========================================="
echo "Uploading Models to Repository"
echo "=========================================="
echo "Bucket: gs://$BUCKET_NAME"
echo "Source: $MODELS_DIR"
echo "Version: $VERSION"
echo ""

# Check if gsutil is installed
if ! command -v gsutil &> /dev/null; then
    echo "Error: gsutil not found. Please install Google Cloud SDK."
    exit 1
fi

# Check if models directory exists
if [ ! -d "$MODELS_DIR" ]; then
    echo "Error: Models directory not found: $MODELS_DIR"
    echo "Please train models first using: python scripts/train_all_models.py"
    exit 1
fi

# Upload models
echo "Uploading models..."
for model in energy_forecast anomaly_detection load_prediction optimization; do
    model_file="$MODELS_DIR/${model}.tflite"
    if [ -f "$model_file" ]; then
        echo "  Uploading $model..."
        
        # Upload to current version
        gsutil cp "$model_file" "gs://$BUCKET_NAME/models/tflite/${model}.tflite"
        
        # Also save versioned copy
        gsutil cp "$model_file" "gs://$BUCKET_NAME/models/versions/${model}_v${VERSION}.tflite"
        
        # Get file size
        SIZE=$(du -h "$model_file" | cut -f1)
        echo "    ✓ Uploaded ($SIZE)"
    else
        echo "  ⚠ $model.tflite not found, skipping"
    fi
done

# Update version file
echo "Updating version file..."
cat > /tmp/versions.json << EOF
{
  "energy_forecast": "$VERSION",
  "anomaly_detection": "$VERSION",
  "load_prediction": "$VERSION",
  "optimization": "$VERSION",
  "last_updated": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
}
EOF
gsutil cp /tmp/versions.json "gs://$BUCKET_NAME/models/versions.json"
rm /tmp/versions.json
echo "✓ Version file updated"

# Set public read permissions
echo "Setting public read permissions..."
gsutil -m acl ch -u AllUsers:R "gs://$BUCKET_NAME/models/tflite/*.tflite"
echo "✓ Permissions set"

echo ""
echo "=========================================="
echo "Upload Complete!"
echo "=========================================="
echo "Models are now available at:"
echo "  https://storage.googleapis.com/$BUCKET_NAME/models/tflite/"
echo ""
echo "Edge gateways will automatically download new models on next sync."
echo ""

