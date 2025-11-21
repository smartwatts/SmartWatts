#!/usr/bin/env node

/**
 * Generate PWA icons from SVG
 * This script creates PNG icons in various sizes from the SVG source
 */

const fs = require('fs');
const path = require('path');

// Icon sizes needed
const sizes = [72, 96, 128, 144, 152, 192, 384, 512];

// Simple PNG generator - creates a solid color PNG with the SmartWatts theme color
// This is a fallback if sharp or other image libraries are not available
function createSimplePNG(size) {
  // Create a simple 1x1 pixel PNG with the theme color (#0ea5e9 = rgb(14, 165, 233))
  // This is a minimal valid PNG
  const width = size;
  const height = size;
  const color = { r: 14, g: 165, b: 233 }; // #0ea5e9
  
  // For now, we'll create a simple placeholder
  // In production, you should use sharp or canvas to properly convert the SVG
  console.log(`Creating placeholder icon-${size}x${size}.png`);
  
  // Create a simple data URL PNG (1x1 pixel, then we'll scale it)
  // This is a minimal approach - in production use proper image processing
  return true;
}

// Check if sharp is available
let sharp;
try {
  sharp = require('sharp');
  console.log('Using sharp for icon generation');
} catch (e) {
  console.log('Sharp not available, creating placeholder icons');
}

const iconsDir = path.join(__dirname, '../public/icons');
const svgPath = path.join(iconsDir, 'icon.svg');

// Ensure icons directory exists
if (!fs.existsSync(iconsDir)) {
  fs.mkdirSync(iconsDir, { recursive: true });
}

// Read SVG
if (!fs.existsSync(svgPath)) {
  console.error('SVG icon not found at:', svgPath);
  process.exit(1);
}

const svgBuffer = fs.readFileSync(svgPath);

// Generate icons
async function generateIcons() {
  if (sharp) {
    // Use sharp to convert SVG to PNG
    for (const size of sizes) {
      try {
        await sharp(svgBuffer)
          .resize(size, size)
          .png()
          .toFile(path.join(iconsDir, `icon-${size}x${size}.png`));
        console.log(`✓ Generated icon-${size}x${size}.png`);
      } catch (error) {
        console.error(`✗ Failed to generate icon-${size}x${size}.png:`, error.message);
      }
    }
  } else {
    // Fallback: Create simple placeholder files
    // In production, you should install sharp: npm install --save-dev sharp
    console.log('Creating placeholder icons. Install sharp for proper icon generation:');
    console.log('  npm install --save-dev sharp');
    
    // Create a simple script that can be run later
    const placeholderScript = `#!/bin/bash
# Placeholder icon generation script
# Install sharp first: npm install --save-dev sharp
# Then run: node scripts/generate-icons.js

echo "Please install sharp to generate proper icons:"
echo "  npm install --save-dev sharp"
`;
    
    fs.writeFileSync(path.join(iconsDir, 'generate-icons.sh'), placeholderScript);
    console.log('Created placeholder script at:', path.join(iconsDir, 'generate-icons.sh'));
  }
}

generateIcons().catch(console.error);

