# Azure DevOps Setup for SmartWatts

This directory contains the Azure DevOps pipeline configuration for the SmartWatts Energy Monitoring Platform.

## Prerequisites

Before setting up the Azure DevOps pipeline, ensure you have:

1. **Azure DevOps Organization** with appropriate permissions
2. **Azure Container Registry (ACR)** for storing Docker images
3. **Azure Kubernetes Service (AKS)** cluster for deployment
4. **SonarCloud** account for code quality analysis
5. **Slack** workspace for notifications (optional)

## Setup Instructions

### 1. Create Service Connections

#### Container Registry Connection
1. Go to Project Settings > Service connections
2. Create new connection: Docker Registry
3. Name: `smartwatts-acr-connection`
4. Registry URL: `https://smartwatts.azurecr.io`
5. Username: Your ACR username
6. Password: Your ACR password

#### Kubernetes Service Connection
1. Go to Project Settings > Service connections
2. Create new connection: Kubernetes
3. Name: `smartwatts-aks-connection`
4. Server URL: Your AKS cluster URL
5. Secret: Your AKS cluster secret

### 2. Configure Environments

#### Staging Environment
1. Go to Pipelines > Environments
2. Create environment: `staging`
3. Add approval checks if needed
4. Configure deployment history retention

#### Production Environment
1. Go to Pipelines > Environments
2. Create environment: `production`
3. Add approval checks (recommended)
4. Configure deployment history retention

### 3. Configure Variables

#### Pipeline Variables
1. Go to Pipelines > Library
2. Create variable group: `smartwatts-variables`
3. Add the following variables:

```
# Container Registry
containerRegistry: smartwatts.azurecr.io
imageRepository: smartwatts
containerRegistryServiceConnection: smartwatts-acr-connection

# Kubernetes
kubernetesServiceConnection: smartwatts-aks-connection
namespace: smartwatts
helmReleaseName: smartwatts

# SonarCloud
sonarCloudOrganization: smartwatts
sonarCloudProjectKey: smartwatts
```

#### Secret Variables
Add these as secret variables in the variable group:

```
# Database
POSTGRES_PASSWORD: your-postgres-password
POSTGRES_STAGING_PASSWORD: your-staging-postgres-password
POSTGRES_PRODUCTION_PASSWORD: your-production-postgres-password

# Redis
REDIS_PASSWORD: your-redis-password

# Monitoring
GRAFANA_ADMIN_PASSWORD: your-grafana-password

# Notifications (Optional)
SLACK_WEBHOOK_URL: your-slack-webhook-url
```

### 4. Create the Pipeline

1. Go to Pipelines > Pipelines
2. Create new pipeline
3. Select "Azure Repos Git" or "GitHub"
4. Choose your repository
5. Select "Existing Azure Pipelines YAML file"
6. Path: `azure-pipelines/azure-pipelines.yml`
7. Save and run

## Pipeline Stages

### 1. Build Stage
- **Code Quality**: SonarCloud analysis, Trivy security scan
- **Build & Test**: Gradle build, unit tests, integration tests
- **Code Coverage**: JaCoCo coverage reports

### 2. Build Images Stage
- **Docker Build**: Build and push Docker images to ACR
- **Security Scan**: Trivy vulnerability scanning
- **Image Security**: Publish security analysis results

### 3. Deploy Staging Stage
- **Helm Deploy**: Deploy to staging AKS cluster
- **Health Checks**: Wait for deployment readiness
- **Smoke Tests**: Basic functionality tests

### 4. Deploy Production Stage
- **Helm Deploy**: Deploy to production AKS cluster
- **Health Checks**: Wait for deployment readiness
- **Health Tests**: Production health verification
- **Notifications**: Slack deployment notifications

### 5. Performance Testing Stage
- **JMeter Tests**: Load testing with JMeter
- **Performance Reports**: Generate performance reports
- **Artifact Publishing**: Publish test results

### 6. Security Testing Stage
- **OWASP Dependency Check**: Dependency vulnerability scan
- **OWASP ZAP**: Web application security testing
- **Security Reports**: Generate security reports

### 7. Cleanup Stage
- **Image Cleanup**: Remove old container images
- **Resource Cleanup**: Clean up temporary resources

## Branch Strategy

- **main**: Production deployments
- **develop**: Staging deployments
- **feature/***: Development branches (build only)

## Monitoring and Alerts

### Built-in Monitoring
- Pipeline execution status
- Deployment success/failure
- Test results and coverage
- Security scan results

### Custom Alerts
- Failed deployments
- Security vulnerabilities
- Performance degradation
- Resource utilization

## Troubleshooting

### Common Issues

#### Build Failures
- Check Java version compatibility
- Verify Gradle wrapper permissions
- Review test failures in logs

#### Deployment Failures
- Verify service connections
- Check Kubernetes cluster status
- Review Helm chart values

#### Security Scan Failures
- Update vulnerable dependencies
- Review security scan results
- Configure security policies

### Debug Steps

1. **Check Pipeline Logs**: Review detailed execution logs
2. **Verify Service Connections**: Test connectivity to external services
3. **Review Environment Status**: Check AKS cluster and ACR status
4. **Validate Configuration**: Verify Helm values and environment variables

## Best Practices

### Security
- Use Azure Key Vault for sensitive secrets
- Enable RBAC for service connections
- Regular security scanning and updates
- Network policies for Kubernetes

### Performance
- Optimize Docker images (multi-stage builds)
- Use appropriate resource limits
- Implement horizontal pod autoscaling
- Monitor resource utilization

### Reliability
- Implement proper health checks
- Use rolling deployments
- Set up monitoring and alerting
- Regular backup and disaster recovery

## Support

For issues or questions:
- Check Azure DevOps documentation
- Review pipeline logs and error messages
- Contact the SmartWatts development team
- Create an issue in the project repository
