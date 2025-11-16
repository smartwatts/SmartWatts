# GitHub Actions Secrets Configuration Checklist

**Repository**: https://github.com/smartwatts/SmartWatts

**Navigation**: Settings > Secrets and variables > Actions > New repository secret

---

## Azure Deployment Secrets (Required for GitHub Actions)

### 1. Azure App Service Credentials
Navigate to: Azure Portal > App Services > SmartWatts > Deployment Center

#### Secret: `AZUREAPPSERVICE_CLIENTID_577CA716950C4C849FCD8534DB68F666`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

#### Secret: `AZUREAPPSERVICE_TENANTID_E0A871202B5249189A79EA035FBEB0E8`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

#### Secret: `AZUREAPPSERVICE_SUBSCRIPTIONID_07BBF5665D464E728201D26286D736CD`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

**How to get these values:**
```bash
# If you have Azure CLI configured:
az account show --query "{subscriptionId:id, tenantId:tenantId}" -o json

# For Client ID, check Azure Portal > Azure Active Directory > App registrations
# Or regenerate in: Azure Portal > App Service > Deployment Center > Settings
```

---

### 2. Azure Staging VM Credentials

#### Secret: `AZURE_CREDENTIALS_STAGING`
- [ ] Value: JSON format:
```json
{
  "clientId": "...",
  "clientSecret": "...",
  "subscriptionId": "...",
  "tenantId": "..."
}
```
- [ ] Status: ⬜ Not Set | ✅ Configured

**How to get:** Azure Portal > Azure Active Directory > App registrations > Certificates & secrets

---

#### Secret: `VM_SSH_PRIVATE_KEY_STAGING`
- [ ] Value: `-----BEGIN OPENSSH PRIVATE KEY-----` (full private key)
- [ ] Status: ⬜ Not Set | ✅ Configured

**Important:** 
- Must be the complete private key including headers
- Should match the public key configured on the VM
- Keep a backup in secure location

---

#### Secret: `VM_ADMIN_PASSWORD_STAGING`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

**Note:** This is the admin password for the staging VM

---

### 3. Azure Static Web Apps (Optional)

#### Secret: `AZURE_STATIC_WEB_APPS_API_TOKEN_STAGING`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ⬜ Not Used | ✅ Configured

**How to get:** Azure Portal > Static Web Apps > sw-staging-dashboard > Manage deployment token

---

## Application Secrets (Required for Runtime)

### 4. Database Configuration

#### Secret: `POSTGRES_PASSWORD`
- [ ] Value: `_______________________________` (minimum 16 characters)
- [ ] Status: ⬜ Not Set | ✅ Configured

**Recommendation:** Generate strong password: `openssl rand -base64 24`

---

#### Secret: `REDIS_PASSWORD`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

---

### 5. JWT Configuration

#### Secret: `JWT_SECRET`
- [ ] Value: `_______________________________` (minimum 32 characters)
- [ ] Status: ⬜ Not Set | ✅ Configured

**Generate new secret:**
```bash
openssl rand -base64 32
```

**Important:** Generating a new JWT secret will invalidate all existing user sessions.

---

### 6. Email Service (SendGrid)

#### Secret: `SENDGRID_API_KEY`
- [ ] Value: `SG.___________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

**How to get:** SendGrid Dashboard > Settings > API Keys > Create API Key

---

### 7. SMS Service (Twilio)

#### Secret: `TWILIO_ACCOUNT_SID`
- [ ] Value: `AC___________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

#### Secret: `TWILIO_AUTH_TOKEN`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

#### Secret: `TWILIO_FROM_NUMBER`
- [ ] Value: `+1234567890` (E.164 format)
- [ ] Status: ⬜ Not Set | ✅ Configured

**How to get:** Twilio Console > Account > Account Info

---

### 8. Push Notifications (Firebase Cloud Messaging)

#### Secret: `FCM_SERVER_KEY`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

**How to get:** Firebase Console > Project Settings > Cloud Messaging > Server Key

---

### 9. Security Configuration

#### Secret: `SSL_KEYSTORE_PASSWORD`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

#### Secret: `SSL_TRUSTSTORE_PASSWORD`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

---

### 10. Monitoring (Grafana)

#### Secret: `GRAFANA_ADMIN_PASSWORD`
- [ ] Value: `_______________________________`
- [ ] Status: ⬜ Not Set | ✅ Configured

---

## Environment Variables (not secrets)

Navigate to: Settings > Secrets and variables > Actions > Variables tab

#### Variable: `VM_IP`
- [ ] Value: `52.182.155.148` (or your staging VM IP)
- [ ] Status: ⬜ Not Set | ✅ Configured

#### Variable: `CORS_ALLOWED_ORIGINS`
- [ ] Value: `https://smartwatts-app.azurewebsites.net,http://localhost:3000`
- [ ] Status: ⬜ Not Set | ✅ Configured

---

## Verification Steps

After configuring all secrets:

1. **Count Secrets**: Go to Settings > Secrets and variables > Actions
   - Expected: ~17 secrets minimum
   - Current: _____ secrets

2. **Test Workflow Syntax**: 
   ```bash
   cd /Users/r/mySmartWatts
   # GitHub Actions will validate on push
   ```

3. **Review Secret Names**: All secret names must match exactly in workflow files
   - `.github/workflows/main_smartwatts.yml`
   - `.github/workflows/deploy-staging.yml`

---

## Security Best Practices

✅ **DO:**
- Store backup of critical secrets in password manager (1Password, LastPass, etc.)
- Use strong, unique passwords for each service
- Rotate secrets regularly (every 90 days)
- Use Azure Key Vault for production secrets
- Limit secret access to necessary workflows only

❌ **DON'T:**
- Commit secrets to git
- Share secrets via email or chat
- Reuse passwords across services
- Store secrets in plaintext files

---

## Troubleshooting

### Secret Not Found Error
If workflow fails with "secret not found":
1. Check exact secret name matches workflow file
2. Verify secret is in correct repository
3. Check secret hasn't expired (Azure secrets)

### Invalid Secret Format
- Azure credentials: Must be valid JSON
- SSH keys: Must include complete key with headers
- API tokens: Check for extra spaces or line breaks

### Authentication Failures
- Verify secrets are current and not revoked
- Check Azure service principal permissions
- Test SSH key separately before configuring

---

## Quick Access Links

- **New Repository Secrets**: https://github.com/smartwatts/SmartWatts/settings/secrets/actions
- **Azure Portal**: https://portal.azure.com
- **SendGrid Dashboard**: https://app.sendgrid.com
- **Twilio Console**: https://console.twilio.com
- **Firebase Console**: https://console.firebase.google.com

---

**Last Updated:** 2025-01-16
**Status:** 🚧 Configuration in Progress

