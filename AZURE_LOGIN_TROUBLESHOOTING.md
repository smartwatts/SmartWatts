# Azure Login Troubleshooting Guide

## Error: "Content is not a valid JSON object" (deploy-staging.yml)

**Workflow**: `deploy-staging.yml`  
**Secret**: `AZURE_CREDENTIALS_STAGING`  
**Action**: `azure/login@v1`

### Problem
The `AZURE_CREDENTIALS_STAGING` secret must be a valid JSON string.

### Solution

1. **Verify the secret format** in GitHub:
   - Go to: https://github.com/smartwatts/SmartWatts/settings/secrets/actions
   - Find `AZURE_CREDENTIALS_STAGING`
   - The value must be valid JSON (no extra spaces, proper quotes)

2. **Correct JSON format**:
```json
{
  "clientId": "your-client-id-here",
  "clientSecret": "your-client-secret-here",
  "subscriptionId": "your-subscription-id-here",
  "tenantId": "your-tenant-id-here"
}
```

3. **How to get these values**:
   - **Azure Portal** → **Azure Active Directory** → **App registrations**
   - Select your app (or create a new one)
   - **Overview**: Copy `Application (client) ID` → `clientId`
   - **Overview**: Copy `Directory (tenant) ID` → `tenantId`
   - **Certificates & secrets**: Create new secret → Copy value → `clientSecret`
   - **Subscriptions**: Copy Subscription ID → `subscriptionId`

4. **Important**: 
   - The JSON must be on a single line OR properly formatted
   - No trailing commas
   - All values must be in double quotes
   - No extra whitespace

---

## Error: "Tenant '***' not found" (main_smartwatts.yml)

**Workflow**: `main_smartwatts.yml`  
**Secrets**: 
- `AZUREAPPSERVICE_CLIENTID_577CA716950C4C849FCD8534DB68F666`
- `AZUREAPPSERVICE_TENANTID_E0A871202B5249189A79EA035FBEB0E8`
- `AZUREAPPSERVICE_SUBSCRIPTIONID_07BBF5665D464E728201D26286D736CD`

### Problem
The tenant ID value is incorrect or the secret contains the wrong value.

### Solution

1. **Verify each secret value**:
   - Go to: https://github.com/smartwatts/SmartWatts/settings/secrets/actions
   - Check each secret:
     - `AZUREAPPSERVICE_CLIENTID_*` → Should be a GUID (e.g., `12345678-1234-1234-1234-123456789abc`)
     - `AZUREAPPSERVICE_TENANTID_*` → Should be a GUID (e.g., `87654321-4321-4321-4321-cba987654321`)
     - `AZUREAPPSERVICE_SUBSCRIPTIONID_*` → Should be a GUID

2. **Get correct values from Azure Portal**:
   ```bash
   # Using Azure CLI (if you have it installed):
   az account show --query "{subscriptionId:id, tenantId:tenantId}" -o json
   ```

   Or manually:
   - **Azure Portal** → **Azure Active Directory** → **Overview**
   - Copy **Tenant ID** (should be a GUID like `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)
   - **Subscriptions** → Select subscription → Copy **Subscription ID**

3. **Verify the secret contains the VALUE, not the NAME**:
   - ❌ Wrong: Secret value = `AZUREAPPSERVICE_TENANTID_E0A871202B5249189A79EA035FBEB0E8`
   - ✅ Correct: Secret value = `e0a87120-2b52-4918-9a79-ea035fbeb0e8` (actual tenant ID GUID)

4. **Common mistakes**:
   - Copying the secret NAME instead of the secret VALUE
   - Missing hyphens in GUID
   - Extra spaces or characters
   - Using the wrong tenant ID (different Azure AD tenant)

---

## Quick Fix Checklist

### For deploy-staging.yml:
- [ ] `AZURE_CREDENTIALS_STAGING` is valid JSON
- [ ] JSON has all 4 fields: `clientId`, `clientSecret`, `subscriptionId`, `tenantId`
- [ ] All values are actual GUIDs/values, not placeholder text
- [ ] No extra spaces or formatting issues

### For main_smartwatts.yml:
- [ ] `AZUREAPPSERVICE_CLIENTID_*` contains actual Client ID GUID
- [ ] `AZUREAPPSERVICE_TENANTID_*` contains actual Tenant ID GUID
- [ ] `AZUREAPPSERVICE_SUBSCRIPTIONID_*` contains actual Subscription ID GUID
- [ ] All values are GUIDs (format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)
- [ ] No secret names used as values

---

## How to Get Azure Credentials

### Method 1: Azure Portal (Recommended)

1. **Get Tenant ID**:
   - Azure Portal → Azure Active Directory → Overview
   - Copy "Tenant ID"

2. **Get Subscription ID**:
   - Azure Portal → Subscriptions
   - Copy "Subscription ID"

3. **Create Service Principal** (for `AZURE_CREDENTIALS_STAGING`):
   - Azure Portal → Azure Active Directory → App registrations
   - Click "New registration"
   - Name: `SmartWatts-GitHub-Actions`
   - Click "Register"
   - Copy "Application (client) ID" → This is `clientId`
   - Copy "Directory (tenant) ID" → This is `tenantId`
   - Go to "Certificates & secrets" → "New client secret"
   - Copy the secret value → This is `clientSecret` (save immediately, can't view again!)
   - Go to "Subscriptions" → Copy Subscription ID → This is `subscriptionId`

4. **Grant Permissions**:
   - Go to "API permissions" → "Add a permission" → "Azure Service Management"
   - Select "user_impersonation" → "Add permissions"
   - Click "Grant admin consent"

### Method 2: Azure CLI

```bash
# Login to Azure
az login

# Get tenant and subscription info
az account show --query "{subscriptionId:id, tenantId:tenantId}" -o json

# Create service principal
az ad sp create-for-rbac --name "SmartWatts-GitHub-Actions" \
  --role contributor \
  --scopes /subscriptions/{subscription-id} \
  --sdk-auth

# This outputs JSON that can be used directly for AZURE_CREDENTIALS_STAGING
```

---

## Verification Steps

After updating secrets:

1. **Test the JSON format** (for `AZURE_CREDENTIALS_STAGING`):
   ```bash
   # Copy the secret value and test it
   echo '{"clientId":"...","clientSecret":"...","subscriptionId":"...","tenantId":"..."}' | python3 -m json.tool
   # Should output formatted JSON without errors
   ```

2. **Verify GUID format**:
   - All IDs should be in format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`
   - 8-4-4-4-12 characters separated by hyphens

3. **Re-run the workflow**:
   - Go to Actions tab
   - Click "Re-run failed jobs" or trigger manually

---

## Still Having Issues?

1. **Double-check secret values** match Azure Portal exactly
2. **Verify service principal has correct permissions**
3. **Check if using correct Azure AD tenant** (if you have multiple)
4. **Ensure subscription is active** and not suspended
5. **Try regenerating service principal** if credentials are old

---

**Last Updated**: 2025-01-16

