# SmartWatts Migration - Remaining Manual Steps

**Status**: Core migration complete ✅  
**New Repository**: https://github.com/smartwatts/SmartWatts  
**Old Repository**: https://github.com/bintinray/SmartWatts

---

## ✅ Completed Steps

- [x] Repository created and mirrored with full history
- [x] Local git remotes updated
- [x] GitHub Actions workflows fixed and updated
- [x] Documentation URLs updated
- [x] Critical Azure secrets configured (workflow is running)

---

## 🔧 Remaining Manual Steps

### Step 1: Disable Workflows in Old Repository

**Purpose**: Prevent accidental runs and minute consumption on old account

**Steps:**
1. Go to: https://github.com/bintinray/SmartWatts/actions
2. Click on each workflow:
   - `main_smartwatts.yml`
   - `deploy-staging.yml`
3. For each workflow:
   - Click the "..." menu (three dots) in the top right
   - Select "Disable workflow"
   - Confirm the action

**Alternative Method (Disable All):**
1. Go to: https://github.com/bintinray/SmartWatts/settings/actions
2. Under "Workflow permissions", you can disable all workflows
3. Or set "Allow all actions and reusable workflows" to "Disabled"

**Verification:**
- Check Actions tab - workflows should show as disabled
- No new workflow runs should trigger

---

### Step 2: Configure Remaining Application Secrets (Optional but Recommended)

**Location**: https://github.com/smartwatts/SmartWatts/settings/secrets/actions

**Critical for Production:**
- [ ] `POSTGRES_PASSWORD` - Database password
- [ ] `REDIS_PASSWORD` - Redis password  
- [ ] `JWT_SECRET` - JWT signing key (generate new: `openssl rand -base64 32`)
- [ ] `CORS_ALLOWED_ORIGINS` - CORS configuration

**For Email Notifications:**
- [ ] `SENDGRID_API_KEY` - SendGrid API key

**For SMS Notifications:**
- [ ] `TWILIO_ACCOUNT_SID`
- [ ] `TWILIO_AUTH_TOKEN`
- [ ] `TWILIO_FROM_NUMBER`

**For Push Notifications:**
- [ ] `FCM_SERVER_KEY` - Firebase Cloud Messaging

**For SSL/HTTPS:**
- [ ] `SSL_KEYSTORE_PASSWORD`
- [ ] `SSL_TRUSTSTORE_PASSWORD`

**For Monitoring:**
- [ ] `GRAFANA_ADMIN_PASSWORD`

**Reference**: See `MIGRATION_SECRETS_CHECKLIST.md` for detailed instructions

---

### Step 3: Update Azure Service Connections

#### 3.1 Azure App Service Deployment

**Location**: Azure Portal > App Services > SmartWatts > Deployment Center

**Steps:**
1. Navigate to Azure Portal: https://portal.azure.com
2. Go to: App Services > SmartWatts
3. Click "Deployment Center" in left menu
4. Click "Settings" tab
5. Under "Source":
   - If using GitHub Actions: Verify it points to `smartwatts/SmartWatts`
   - If using GitHub App: Click "Disconnect" then "Connect" and select `smartwatts/SmartWatts`
6. Re-authenticate if prompted
7. Save changes

**Verification:**
- Check that deployment source shows: `smartwatts/SmartWatts`
- Test by triggering a deployment

---

#### 3.2 Azure VM Deployment

**Status**: ✅ Already configured via GitHub Actions secrets

**No changes needed** - The VM deployment uses:
- `AZURE_CREDENTIALS_STAGING` secret
- `VM_SSH_PRIVATE_KEY_STAGING` secret
- `VM_ADMIN_PASSWORD_STAGING` secret

These are already configured in the new repository.

**Verification:**
- Check GitHub Actions workflow: `.github/workflows/deploy-staging.yml`
- Verify secrets are set in new repository

---

#### 3.3 Azure Static Web Apps (if applicable)

**Location**: Azure Portal > Static Web Apps > sw-staging-dashboard

**Steps:**
1. Navigate to: Azure Portal > Static Web Apps > sw-staging-dashboard
2. Click "Manage deployment token"
3. Copy the deployment token
4. Go to: https://github.com/smartwatts/SmartWatts/settings/secrets/actions
5. Update secret: `AZURE_STATIC_WEB_APPS_API_TOKEN_STAGING`
6. Paste the new token

**Alternative**: If using GitHub Actions integration:
1. In Azure Portal, go to Static Web App > Deployment
2. Click "Disconnect" then "Connect"
3. Select `smartwatts/SmartWatts` repository
4. Re-authenticate

---

### Step 4: Configure Branch Protection Rules

**Location**: https://github.com/smartwatts/SmartWatts/settings/branches

**For `main` branch:**
1. Click "Add rule" or edit existing rule for `main`
2. Configure:
   - ✅ Require a pull request before merging
   - ✅ Require approvals: 1 (or more as needed)
   - ✅ Require status checks to pass before merging
   - ✅ Require branches to be up to date before merging
   - ✅ Include administrators
   - ✅ Do not allow bypassing the above settings

**For `develop` branch (if exists):**
1. Add similar rule for `develop`
2. Can be less strict than `main` (e.g., no required approvals)

**Verification:**
- Try to push directly to `main` - should be blocked
- Create a test PR - should require approval

---

### Step 5: Test GitHub Actions Workflows

**Status**: ✅ Workflow is already running (you saw the build attempt)

**Next Steps:**
1. Monitor the current workflow run:
   - Go to: https://github.com/smartwatts/SmartWatts/actions
   - Check if the Next.js build succeeds with the fixes we applied

2. If build succeeds:
   - ✅ Workflow is working correctly
   - Deployment should proceed automatically

3. If build still fails:
   - Check the error logs
   - May need additional fixes or configuration

**Manual Test:**
1. Make a small change (e.g., update a comment)
2. Commit and push:
   ```bash
   git commit --allow-empty -m "test: verify GitHub Actions workflow"
   git push origin main
   ```
3. Monitor Actions tab for successful run

---

### Step 6: Verify Azure Deployments

**After workflow completes successfully:**

#### 6.1 Frontend (Azure App Service)
- **URL**: Check your Azure App Service URL
- **Test**: Visit the URL and verify frontend loads
- **Health Check**: `curl https://your-app.azurewebsites.net`

#### 6.2 Backend (Azure VM)
- **IP**: Check VM IP from deployment logs (default: 52.182.155.148)
- **Test**: `curl http://VM_IP:8080/actuator/health`
- **Frontend**: `curl http://VM_IP:3000`

#### 6.3 Static Web App (if applicable)
- **URL**: Check Static Web App URL from Azure Portal
- **Test**: Visit URL and verify deployment

---

### Step 7: Monitor Both Repositories (1-2 Weeks)

**During Transition Period:**

1. **Monitor New Repository:**
   - Check GitHub Actions runs daily
   - Verify deployments are working
   - Monitor for any errors or issues

2. **Old Repository:**
   - Keep workflows disabled
   - Repository remains accessible for reference
   - Can be used for rollback if needed

3. **After 1-2 Weeks:**
   - If no issues, proceed to Step 8

---

### Step 8: Archive Old Repository (Future - After Verification)

**When confident in new setup:**

1. **Add Notice to Old Repository:**
   - Edit README.md in `bintinray/SmartWatts`
   - Add prominent notice at top:
     ```markdown
     # ⚠️ Repository Moved
     
     This repository has been moved to: https://github.com/smartwatts/SmartWatts
     
     Please update your remotes and use the new repository.
     ```

2. **Archive Repository:**
   - Go to: https://github.com/bintinray/SmartWatts/settings
   - Scroll to "Danger Zone"
   - Click "Archive this repository"
   - Confirm the action

**Note**: Archived repositories are read-only but remain accessible.

---

## 🔍 Verification Checklist

Before considering migration complete:

- [ ] Old repository workflows are disabled
- [ ] New repository workflows are running successfully
- [ ] All critical secrets are configured
- [ ] Azure App Service deployment is working
- [ ] Azure VM deployment is working
- [ ] Frontend is accessible
- [ ] Backend API is responding
- [ ] Branch protection rules are configured
- [ ] Documentation URLs are updated
- [ ] Local git remotes are configured correctly

---

## 🆘 Troubleshooting

### Workflow Not Running
- Check repository secrets are configured
- Verify workflow file syntax is correct
- Check Actions tab for error messages

### Deployment Failing
- Verify Azure credentials are correct
- Check Azure service status
- Review deployment logs in Azure Portal

### Build Errors
- Check Next.js build logs
- Verify Node.js version compatibility
- Clear caches and retry

### Authentication Issues
- Regenerate GitHub Personal Access Token
- Re-authenticate Azure connections
- Verify service principal permissions

---

## 📞 Support

If you encounter issues:
1. Check GitHub Actions logs
2. Check Azure Portal deployment logs
3. Review this migration guide
4. Check `MIGRATION_SECRETS_CHECKLIST.md` for secret configuration

---

**Last Updated**: 2025-01-16  
**Migration Status**: 🟡 In Progress (Core Complete, Manual Steps Remaining)

