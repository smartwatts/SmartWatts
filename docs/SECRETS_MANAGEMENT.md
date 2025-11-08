# Secrets Management Guide - SmartWatts

## Overview

This guide provides instructions for setting up and using secrets management services for SmartWatts production deployment. All sensitive credentials should be stored in a secrets management service rather than hardcoded in configuration files.

## Supported Secrets Management Services

### Azure Key Vault (Recommended for Azure Deployments)

Azure Key Vault is the recommended secrets management solution for Azure deployments.

#### Setup Instructions

1. **Create Azure Key Vault**
   ```bash
   az keyvault create \
     --name mysmartwatts-kv \
     --resource-group mysmartwatts-rg \
     --location eastus
   ```

2. **Add Secrets to Key Vault**
   ```bash
   # Database credentials
   az keyvault secret set --vault-name mysmartwatts-kv --name "postgres-password" --value "<strong-password>"
   az keyvault secret set --vault-name mysmartwatts-kv --name "postgres-user" --value "mysmartwatts_user"
   
   # Redis password
   az keyvault secret set --vault-name mysmartwatts-kv --name "redis-password" --value "<strong-password>"
   
   # JWT secret
   az keyvault secret set --vault-name mysmartwatts-kv --name "jwt-secret" --value "<jwt-secret>"
   
   # API keys
   az keyvault secret set --vault-name mysmartwatts-kv --name "sendgrid-api-key" --value "<sendgrid-api-key>"
   az keyvault secret set --vault-name mysmartwatts-kv --name "twilio-account-sid" --value "<twilio-account-sid>"
   az keyvault secret set --vault-name mysmartwatts-kv --name "twilio-auth-token" --value "<twilio-auth-token>"
   az keyvault secret set --vault-name mysmartwatts-kv --name "openweather-api-key" --value "<openweather-api-key>"
   
   # SSL certificates
   az keyvault secret set --vault-name mysmartwatts-kv --name "ssl-keystore-password" --value "<keystore-password>"
   az keyvault secret set --vault-name mysmartwatts-kv --name "ssl-truststore-password" --value "<truststore-password>"
   
   # Monitoring
   az keyvault secret set --vault-name mysmartwatts-kv --name "grafana-admin-password" --value "<grafana-password>"
   ```

3. **Configure Service Access**
   - Grant access to Azure services (AKS, App Service, etc.)
   - Use Managed Identity for automatic authentication
   - Configure access policies for specific services

4. **Retrieve Secrets in Application**
   - Use Azure Key Vault SDK in Spring Boot applications
   - Configure `spring-cloud-azure-starter-keyvault-secrets` dependency
   - Reference secrets in `application.yml` using `@azure.keyvault.secret-name@`

### AWS Secrets Manager (For AWS Deployments)

AWS Secrets Manager is recommended for AWS deployments.

#### Setup Instructions

1. **Create Secrets in AWS Secrets Manager**
   ```bash
   # Database credentials
   aws secretsmanager create-secret \
     --name mysmartwatts/postgres-password \
     --secret-string "<strong-password>"
   
   # Redis password
   aws secretsmanager create-secret \
     --name mysmartwatts/redis-password \
     --secret-string "<strong-password>"
   
   # JWT secret
   aws secretsmanager create-secret \
     --name mysmartwatts/jwt-secret \
     --secret-string "<jwt-secret>"
   ```

2. **Configure IAM Permissions**
   - Grant `secretsmanager:GetSecretValue` permission to application roles
   - Use IAM roles for service-to-service access

3. **Retrieve Secrets in Application**
   - Use AWS SDK for Java in Spring Boot applications
   - Configure AWS credentials via IAM roles or environment variables
   - Reference secrets in `application.yml` using custom property sources

### Kubernetes Secrets (For Kubernetes Deployments)

For Kubernetes deployments, use Kubernetes Secrets with external secret management integration.

#### Setup Instructions

1. **Install External Secrets Operator**
   ```bash
   kubectl apply -f https://raw.githubusercontent.com/external-secrets/external-secrets/main/deploy/charts/external-secrets/templates/crds/secretstore.yaml
   kubectl apply -f https://raw.githubusercontent.com/external-secrets/external-secrets/main/deploy/charts/external-secrets/templates/crds/externalsecret.yaml
   ```

2. **Configure Secret Store**
   - Create SecretStore resource pointing to Azure Key Vault or AWS Secrets Manager
   - Configure authentication credentials

3. **Create External Secrets**
   - Create ExternalSecret resources that reference secrets in external stores
   - External Secrets Operator will sync secrets to Kubernetes Secrets

## Required Secrets

### Database Credentials
- `POSTGRES_PASSWORD` - Strong password (minimum 16 characters)
- `POSTGRES_USER` - Database username
- `POSTGRES_HOST` - Database host
- `POSTGRES_PORT` - Database port

### Redis Credentials
- `REDIS_PASSWORD` - Strong Redis password
- `REDIS_HOST` - Redis host
- `REDIS_PORT` - Redis port

### JWT Configuration
- `JWT_SECRET` - Secure JWT secret (minimum 256 bits / 32 characters)
- `JWT_EXPIRATION` - Token expiration time
- `JWT_REFRESH_EXPIRATION` - Refresh token expiration

### Third-Party API Keys
- `SENDGRID_API_KEY` - SendGrid API key for email notifications
- `TWILIO_ACCOUNT_SID` - Twilio account SID
- `TWILIO_AUTH_TOKEN` - Twilio authentication token
- `TWILIO_FROM_NUMBER` - Twilio phone number
- `OPENWEATHER_API_KEY` - OpenWeatherMap API key
- `FCM_SERVER_KEY` - Firebase Cloud Messaging server key

### SSL/TLS Certificates
- `SSL_KEYSTORE_PASSWORD` - Keystore password
- `SSL_TRUSTSTORE_PASSWORD` - Truststore password

### Monitoring
- `GRAFANA_ADMIN_PASSWORD` - Grafana admin password
- `SENTRY_DSN` - Sentry DSN for error tracking

## Migration Steps

### Step 1: Set Up Secrets Management Service
1. Choose secrets management service (Azure Key Vault, AWS Secrets Manager, etc.)
2. Create secrets management instance
3. Configure authentication and access policies

### Step 2: Migrate Secrets
1. Generate strong passwords for all credentials
2. Store secrets in secrets management service
3. Update application configuration to reference secrets from service

### Step 3: Update Application Configuration
1. Update `application.yml` files to use secrets management references
2. Configure Spring Boot to use secrets management SDK
3. Remove hardcoded secrets from configuration files

### Step 4: Update Deployment Scripts
1. Update deployment scripts to fetch secrets from secrets management
2. Configure environment variables to reference secrets
3. Update Kubernetes secrets to use External Secrets Operator

### Step 5: Verify and Test
1. Verify all secrets are accessible from application
2. Test application startup with secrets from management service
3. Verify no hardcoded secrets remain in codebase

## Best Practices

1. **Never Commit Secrets**
   - Use `.gitignore` to exclude `.env` files
   - Never commit secrets to version control
   - Use secrets management service for all sensitive data

2. **Rotate Secrets Regularly**
   - Implement secret rotation policies
   - Rotate passwords every 90 days
   - Rotate API keys every 180 days

3. **Use Strong Passwords**
   - Minimum 16 characters for database passwords
   - Minimum 32 characters for JWT secrets
   - Use mix of letters, numbers, and symbols

4. **Limit Access**
   - Grant access only to services that need secrets
   - Use least privilege principle
   - Audit secret access regularly

5. **Monitor Secret Access**
   - Enable logging for secret access
   - Monitor for unauthorized access attempts
   - Set up alerts for suspicious activity

## Troubleshooting

### Secrets Not Accessible
- Verify authentication credentials are correct
- Check IAM roles and access policies
- Verify secrets exist in secrets management service

### Application Startup Failures
- Check environment variables are set correctly
- Verify secrets management SDK is configured
- Check application logs for specific error messages

### Secret Rotation Issues
- Plan secret rotation during maintenance windows
- Update all services simultaneously
- Test secret rotation in staging environment first

## Additional Resources

- [Azure Key Vault Documentation](https://docs.microsoft.com/azure/key-vault/)
- [AWS Secrets Manager Documentation](https://docs.aws.amazon.com/secretsmanager/)
- [Kubernetes External Secrets Operator](https://external-secrets.io/)
- [Spring Cloud Azure Key Vault](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/spring-cloud-azure-starter-keyvault-secrets)

