#!/bin/bash

# Script to create .docx files from markdown documentation
# This script uses pandoc to convert markdown to .docx format

set -e

echo "Creating .docx documentation files..."

# Check if pandoc is installed
if ! command -v pandoc &> /dev/null; then
    echo "Error: pandoc is not installed"
    echo "Please install pandoc first:"
    echo "  Ubuntu/Debian: sudo apt install pandoc"
    echo "  macOS: brew install pandoc"
    echo "  Windows: Download from https://pandoc.org/installing.html"
    exit 1
fi

# Create docs directory if it doesn't exist
mkdir -p docs

# Convert Hardware Integration Guide
echo "Converting Hardware Integration Guide..."
pandoc -f markdown -t docx \
    --reference-doc=docs/template.docx \
    -o docs/SmartWatts_Hardware_Integration_Guide.docx \
    docs/SmartWatts_Hardware_Integration_Guide.docx

# Convert Quick Start Guide
echo "Converting Quick Start Guide..."
pandoc -f markdown -t docx \
    --reference-doc=docs/template.docx \
    -o docs/SmartWatts_Quick_Start_Guide.docx \
    docs/SmartWatts_Quick_Start_Guide.docx

# Convert Portable Installation Guide
echo "Converting Portable Installation Guide..."
pandoc -f markdown -t docx \
    --reference-doc=docs/template.docx \
    -o docs/SmartWatts_Portable_Installation_Guide.docx \
    docs/SmartWatts_Portable_Installation_Guide.docx

echo "Documentation files created successfully!"
echo "Files created:"
echo "  - docs/SmartWatts_Hardware_Integration_Guide.docx"
echo "  - docs/SmartWatts_Quick_Start_Guide.docx"
echo "  - docs/SmartWatts_Portable_Installation_Guide.docx"


