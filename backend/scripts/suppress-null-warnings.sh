#!/bin/bash

# Script to add @SuppressWarnings("null") to methods with Map.of(), List.of(), Set.of()
# These are known-safe patterns that cause false positive warnings

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=== Adding @SuppressWarnings(\"null\") to safe patterns ==="
echo ""

# Find all Java files with Map.of, List.of, Set.of
find "$BACKEND_DIR" -name "*.java" -type f | while read file; do
    if grep -q "Map\.of\|List\.of\|Set\.of" "$file"; then
        echo "Processing: $file"
        
        # Add @SuppressWarnings("null") import if not present
        if ! grep -q "import.*SuppressWarnings" "$file"; then
            # Find the last import line
            last_import_line=$(grep -n "^import " "$file" | tail -1 | cut -d: -f1)
            if [ -n "$last_import_line" ]; then
                sed -i.bak "${last_import_line}a\\
import java.lang.SuppressWarnings;
" "$file"
                rm -f "$file.bak"
            fi
        fi
        
        # Add @SuppressWarnings("null") to methods containing Map.of, List.of, Set.of
        # This is a complex operation - we'll use a Python script for this
    fi
done

echo ""
echo "Note: This script identifies files. For actual fixes, use a more sophisticated approach."
echo "Consider using IDE's 'Suppress Warning' feature or manually adding annotations."
echo ""


