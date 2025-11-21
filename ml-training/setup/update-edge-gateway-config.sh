#!/bin/bash
# Update edge gateway configuration with model repository URLs

set -e

BUCKET_NAME="${MODEL_BUCKET_NAME:-smartwatts-models}"
EDGE_GATEWAY_DIR="${EDGE_GATEWAY_DIR:-../../backend/edge-gateway}"
BASE_URL="https://storage.googleapis.com/$BUCKET_NAME/models/tflite"

echo "=========================================="
echo "Updating Edge Gateway Configuration"
echo "=========================================="
echo "Model Repository: $BASE_URL"
echo "Edge Gateway: $EDGE_GATEWAY_DIR"
echo ""

# Check if edge gateway directory exists
if [ ! -d "$EDGE_GATEWAY_DIR" ]; then
    echo "Error: Edge gateway directory not found: $EDGE_GATEWAY_DIR"
    exit 1
fi

# Update application.yml
CONFIG_FILE="$EDGE_GATEWAY_DIR/src/main/resources/application.yml"
if [ -f "$CONFIG_FILE" ]; then
    echo "Updating $CONFIG_FILE..."
    
    # Create backup
    cp "$CONFIG_FILE" "${CONFIG_FILE}.backup"
    
    # Update model URLs (if not already set)
    if ! grep -q "model-repository-url" "$CONFIG_FILE"; then
        # Add model repository configuration
        cat >> "$CONFIG_FILE" << EOF

# Model Repository Configuration
edge:
  ml:
    models:
      repository-url: $BASE_URL
      auto-download: true
      sync-interval: 3600000  # 1 hour
EOF
        echo "✓ Added model repository configuration"
    else
        echo "✓ Model repository already configured"
    fi
else
    echo "⚠ Configuration file not found: $CONFIG_FILE"
fi

# Update application-cloudrun.yml
CLOUDRUN_CONFIG="$EDGE_GATEWAY_DIR/src/main/resources/application-cloudrun.yml"
if [ -f "$CLOUDRUN_CONFIG" ]; then
    echo "Updating $CLOUDRUN_CONFIG..."
    
    # Create backup
    cp "$CLOUDRUN_CONFIG" "${CLOUDRUN_CONFIG}.backup"
    
    # Update model URLs
    if ! grep -q "repository-url" "$CLOUDRUN_CONFIG"; then
        # Add model repository configuration
        cat >> "$CLOUDRUN_CONFIG" << EOF

# Model Repository Configuration
edge:
  ml:
    models:
      repository-url: $BASE_URL
      auto-download: true
EOF
        echo "✓ Added model repository configuration to Cloud Run config"
    else
        echo "✓ Model repository already configured in Cloud Run config"
    fi
fi

echo ""
echo "=========================================="
echo "Configuration Update Complete!"
echo "=========================================="
echo ""
echo "Edge gateway will now:"
echo "  - Download models from: $BASE_URL"
echo "  - Auto-update every hour"
echo "  - Fall back to local models if download fails"
echo ""
echo "Model URLs:"
echo "  - Energy Forecast: $BASE_URL/energy_forecast.tflite"
echo "  - Anomaly Detection: $BASE_URL/anomaly_detection.tflite"
echo "  - Load Prediction: $BASE_URL/load_prediction.tflite"
echo "  - Optimization: $BASE_URL/optimization.tflite"
echo ""

