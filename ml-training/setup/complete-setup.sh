#!/bin/bash
# Complete ML Training Pipeline Setup
# This script sets up everything needed for ML training

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ML_TRAINING_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=========================================="
echo "SmartWatts ML Training Pipeline Setup"
echo "=========================================="
echo ""

# Check prerequisites
echo "Checking prerequisites..."

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "✗ Python 3 not found. Please install Python 3.8+"
    exit 1
fi
PYTHON_VERSION=$(python3 --version | cut -d' ' -f2)
echo "✓ Python $PYTHON_VERSION found"

# Check pip
if ! command -v pip3 &> /dev/null; then
    echo "✗ pip3 not found. Please install pip"
    exit 1
fi
echo "✓ pip3 found"

# Check gcloud (optional, for model repository)
if command -v gcloud &> /dev/null; then
    echo "✓ gcloud CLI found"
    GCLOUD_AVAILABLE=true
else
    echo "⚠ gcloud CLI not found (optional, needed for model repository)"
    GCLOUD_AVAILABLE=false
fi

echo ""

# Step 1: Install Python dependencies
echo "=========================================="
echo "Step 1: Installing Python Dependencies"
echo "=========================================="
cd "$ML_TRAINING_DIR"
pip3 install -r requirements.txt
echo "✓ Dependencies installed"
echo ""

# Step 2: Create directory structure
echo "=========================================="
echo "Step 2: Creating Directory Structure"
echo "=========================================="
mkdir -p data/raw
mkdir -p data/processed
mkdir -p models/tflite
mkdir -p models/quantized
mkdir -p logs
echo "✓ Directory structure created"
echo ""

# Step 3: Make scripts executable
echo "=========================================="
echo "Step 3: Making Scripts Executable"
echo "=========================================="
chmod +x scripts/*.py
chmod +x setup/*.sh
chmod +x utils/*.py
echo "✓ Scripts made executable"
echo ""

# Step 4: Setup model repository (if gcloud available)
if [ "$GCLOUD_AVAILABLE" = true ]; then
    echo "=========================================="
    echo "Step 4: Setting up Model Repository"
    echo "=========================================="
    read -p "Do you want to set up GCP Cloud Storage model repository? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cd setup
        ./setup-model-repository.sh
        echo "✓ Model repository setup complete"
    else
        echo "⚠ Skipping model repository setup"
    fi
    echo ""
else
    echo "=========================================="
    echo "Step 4: Model Repository Setup"
    echo "=========================================="
    echo "⚠ gcloud not available. Skipping model repository setup."
    echo "  You can set it up later using: ./setup/setup-model-repository.sh"
    echo ""
fi

# Step 5: Setup automated training
echo "=========================================="
echo "Step 5: Setting up Automated Training"
echo "=========================================="
read -p "Do you want to set up GitHub Actions for automated training? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd setup
    ./setup-automated-training.sh
    echo "✓ Automated training setup complete"
    echo ""
    echo "⚠ IMPORTANT: Add GitHub secrets before using automated training:"
    echo "  - GCP_SA_KEY"
    echo "  - DB_HOST, DB_PORT, DB_USER, DB_PASSWORD"
    echo "  See: ml-training/setup/SETUP_INSTRUCTIONS.md"
else
    echo "⚠ Skipping automated training setup"
fi
echo ""

# Step 6: Generate sample data (optional)
echo "=========================================="
echo "Step 6: Generate Sample Data (Optional)"
echo "=========================================="
read -p "Do you want to generate sample training data for testing? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd "$ML_TRAINING_DIR"
    python3 utils/generate_sample_data.py \
        --output data/raw/sample_energy_data.csv \
        --days 30 \
        --devices 3
    echo "✓ Sample data generated"
else
    echo "⚠ Skipping sample data generation"
fi
echo ""

# Summary
echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
echo "✅ Installed dependencies"
echo "✅ Created directory structure"
echo "✅ Made scripts executable"
if [ "$GCLOUD_AVAILABLE" = true ]; then
    echo "✅ Model repository ready (if configured)"
fi
echo "✅ Automated training ready (if configured)"
echo ""
echo "Next steps:"
echo ""
echo "1. Prepare your training data:"
echo "   - Place CSV files in: ml-training/data/raw/"
echo "   - Or extract from database: ./setup/extract-training-data.sh"
echo ""
echo "2. Train models:"
echo "   python scripts/train_all_models.py --data-dir data/raw --epochs 100 --quantize"
echo ""
echo "3. Upload models (if repository set up):"
echo "   ./setup/upload-models.sh"
echo ""
echo "4. Deploy to edge gateways:"
echo "   - Models will auto-download from repository"
echo "   - Or manually copy: cp models/tflite/*.tflite backend/edge-gateway/src/main/resources/models/"
echo ""
echo "Documentation:"
echo "  - Quick Start: ml-training/QUICK_START.md"
echo "  - Full Guide: ml-training/README.md"
echo "  - Architecture: ml-training/TRAINING_ARCHITECTURE.md"
echo ""

