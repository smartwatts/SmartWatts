# Generate Azure Credentials JSON (Correct Format)

## The Problem
GitHub secrets can have hidden characters or formatting issues. The best solution is to generate the JSON using Azure CLI, which ensures perfect formatting.

## Solution: Use Azure CLI to Generate JSON

### Step 1: Install Azure CLI (if not installed)
```bash
# macOS
brew install azure-cli

# Or download from: https://aka.ms/installazureclimac
```

### Step 2: Login to Azure
```bash
az login
```

### Step 3: Generate the JSON Credentials
```bash
# Replace with your actual subscription ID
SUBSCRIPTION_ID="2465e979-d52b-4a7b-af53-dd1fd2e456b5"

# Create service principal and get JSON output
az ad sp create-for-rbac \
  --name "SmartWatts-GitHub-Actions" \
  --role contributor \
  --scopes /subscriptions/$SUBSCRIPTION_ID \
  --sdk-auth
```

**This will output JSON in the correct format!** Copy the entire output.

### Step 4: Update GitHub Secrets

1. Go to: https://github.com/smartwatts/SmartWatts/settings/secrets/actions

2. **Update `AZURE_CREDENTIALS_STAGING`**:
   - Click "Update"
   - Paste the JSON output from Step 3 (it's already in correct format)
   - Click "Update secret"

3. **Update `AZURE_CREDENTIALS`**:
   - Click "Update" (or create if it doesn't exist)
   - Paste the SAME JSON output
   - Click "Update secret"

## Alternative: Manual JSON Format

If you can't use Azure CLI, use this EXACT format (single line, no spaces):

```
{"clientId":"2e16f0e2-afb4-4301-822e-71063e4f0265","clientSecret":"ElF8Q~oD1flUGaXy~cftPDhEPX_00HUrr4wpibUn","subscriptionId":"2465e979-d52b-4a7b-af53-dd1fd2e456b5","tenantId":"30a4d845-6951-461c-b834-7df07f824493"}
```

**Important:**
- All on ONE line
- No spaces after colons or commas
- All values in double quotes
- No trailing commas
- No newlines

## Validate JSON Before Adding to GitHub

Test your JSON locally:

```bash
# Test the JSON
echo '{"clientId":"2e16f0e2-afb4-4301-822e-71063e4f0265","clientSecret":"ElF8Q~oD1flUGaXy~cftPDhEPX_00HUrr4wpibUn","subscriptionId":"2465e979-d52b-4a7b-af53-dd1fd2e456b5","tenantId":"30a4d845-6951-461c-b834-7df07f824493"}' | python3 -m json.tool
```

If this works without errors, the JSON is valid!

## Common Issues

1. **Hidden characters**: Copy from Azure CLI output, not from formatted text
2. **Newlines**: JSON must be on one line for GitHub secrets
3. **Spaces**: Remove all spaces around colons and commas
4. **Quotes**: Use double quotes, not single quotes

## Quick Fix Command

If you have the values, use this command to generate the JSON:

```bash
cat > /tmp/azure_creds.json << 'EOF'
{
  "clientId": "2e16f0e2-afb4-4301-822e-71063e4f0265",
  "clientSecret": "ElF8Q~oD1flUGaXy~cftPDhEPX_00HUrr4wpibUn",
  "subscriptionId": "2465e979-d52b-4a7b-af53-dd1fd2e456b5",
  "tenantId": "30a4d845-6951-461c-b834-7df07f824493"
}
EOF

# Convert to single line (for GitHub secret)
cat /tmp/azure_creds.json | tr -d '\n' | sed 's/[[:space:]]//g'
```

Copy the output and paste into GitHub secret.

