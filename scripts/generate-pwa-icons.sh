#!/bin/bash

# SmartWatts PWA Icons Generation Script
# This script generates placeholder PWA icons for development

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

ICONS_DIR="frontend/public/icons"

echo -e "${BLUE}🎨 SmartWatts PWA Icons Generator${NC}"
echo "=============================================="
echo ""

# Check if ImageMagick is available
if ! command -v convert &> /dev/null; then
    echo -e "${YELLOW}⚠️  ImageMagick not found. Creating placeholder files instead.${NC}"
    echo ""
    
    # Create placeholder files
    mkdir -p "$ICONS_DIR"
    
    # Create a simple SVG icon
    cat > "$ICONS_DIR/icon.svg" << 'EOF'
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <rect width="512" height="512" fill="#0ea5e9"/>
  <text x="256" y="300" font-family="Arial, sans-serif" font-size="200" font-weight="bold" text-anchor="middle" fill="white">⚡</text>
</svg>
EOF
    
    echo -e "${GREEN}✅ Created SVG icon template${NC}"
    echo ""
    echo -e "${YELLOW}📝 Manual Steps Required:${NC}"
    echo "1. Replace the SVG icon with your actual SmartWatts logo"
    echo "2. Generate PNG icons from the SVG using an online tool or ImageMagick:"
    echo "   - 16x16, 32x32, 72x72, 96x96, 128x128, 144x144, 152x152, 192x192, 384x384, 512x512"
    echo "3. Place the PNG files in $ICONS_DIR/"
    echo ""
    echo -e "${BLUE}🔧 ImageMagick Commands (if available):${NC}"
    echo "convert icon.svg -resize 16x16 icon-16x16.png"
    echo "convert icon.svg -resize 32x32 icon-32x32.png"
    echo "convert icon.svg -resize 72x72 icon-72x72.png"
    echo "convert icon.svg -resize 96x96 icon-96x96.png"
    echo "convert icon.svg -resize 128x128 icon-128x128.png"
    echo "convert icon.svg -resize 144x144 icon-144x144.png"
    echo "convert icon.svg -resize 152x152 icon-152x152.png"
    echo "convert icon.svg -resize 192x192 icon-192x192.png"
    echo "convert icon.svg -resize 384x384 icon-384x384.png"
    echo "convert icon.svg -resize 512x512 icon-512x512.png"
    echo ""
    exit 0
fi

echo -e "${BLUE}🔧 Generating PWA icons with ImageMagick...${NC}"

# Create icons directory
mkdir -p "$ICONS_DIR"

# Create a simple SVG icon first
cat > "$ICONS_DIR/icon.svg" << 'EOF'
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <rect width="512" height="512" fill="#0ea5e9"/>
  <text x="256" y="300" font-family="Arial, sans-serif" font-size="200" font-weight="bold" text-anchor="middle" fill="white">⚡</text>
</svg>
EOF

# Generate PNG icons
echo -e "${BLUE}📱 Generating icon sizes...${NC}"

convert "$ICONS_DIR/icon.svg" -resize 16x16 "$ICONS_DIR/icon-16x16.png"
convert "$ICONS_DIR/icon.svg" -resize 32x32 "$ICONS_DIR/icon-32x32.png"
convert "$ICONS_DIR/icon.svg" -resize 72x72 "$ICONS_DIR/icon-72x72.png"
convert "$ICONS_DIR/icon.svg" -resize 96x96 "$ICONS_DIR/icon-96x96.png"
convert "$ICONS_DIR/icon.svg" -resize 128x128 "$ICONS_DIR/icon-128x128.png"
convert "$ICONS_DIR/icon.svg" -resize 144x144 "$ICONS_DIR/icon-144x144.png"
convert "$ICONS_DIR/icon.svg" -resize 152x152 "$ICONS_DIR/icon-152x152.png"
convert "$ICONS_DIR/icon.svg" -resize 192x192 "$ICONS_DIR/icon-192x192.png"
convert "$ICONS_DIR/icon.svg" -resize 384x384 "$ICONS_DIR/icon-384x384.png"
convert "$ICONS_DIR/icon.svg" -resize 512x512 "$ICONS_DIR/icon-512x512.png"

echo -e "${GREEN}✅ Generated all PWA icons${NC}"
echo ""

# Create favicon
convert "$ICONS_DIR/icon.svg" -resize 32x32 "$ICONS_DIR/favicon.ico"

# Create apple-touch-icon
cp "$ICONS_DIR/icon-152x152.png" "$ICONS_DIR/apple-touch-icon.png"

echo -e "${GREEN}✅ PWA icons generation completed${NC}"
echo ""
echo -e "${BLUE}📁 Generated files:${NC}"
ls -la "$ICONS_DIR/"
echo ""
echo -e "${YELLOW}📝 Next steps:${NC}"
echo "1. Replace the SVG icon with your actual SmartWatts logo"
echo "2. Regenerate PNG icons if you update the SVG"
echo "3. Test the PWA functionality in your browser"
echo "4. Use Lighthouse to audit PWA score"










