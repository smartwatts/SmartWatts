#!/bin/bash
set -e

echo "Setting up Sentry for SmartWatts..."

# Check if SENTRY_DSN is set
if [ -z "$SENTRY_DSN" ]; then
  echo "Error: SENTRY_DSN environment variable not set"
  echo "Please sign up at https://sentry.io and get your DSN"
  exit 1
fi

# Update backend services
for service in backend/*/; do
  if [ -f "$service/src/main/resources/application.yml" ]; then
    echo "Configuring Sentry for $service"
    # Add SENTRY_DSN to environment variables
  fi
done

# Update frontend
echo "Configuring Sentry for frontend"
echo "NEXT_PUBLIC_SENTRY_DSN=$SENTRY_DSN" >> frontend/.env.local

echo "Sentry setup completed!"
echo "Don't forget to add SENTRY_DSN to your production environment variables"







