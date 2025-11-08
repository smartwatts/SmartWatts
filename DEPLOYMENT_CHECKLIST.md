# SmartWatts Production Deployment Checklist

## Pre-Deployment Checklist

### 🔒 Security Validation
- [ ] **Environment Variables**: Run `./scripts/validate-env.sh .env.production`
- [ ] **No Hardcoded Credentials**: Verify no hardcoded passwords in codebase
- [ ] **SSL Certificates**: Ensure valid SSL certificates are in place
- [ ] **Secrets Management**: All secrets stored in environment variables
- [ ] **Database Security**: Strong passwords for all database users
- [ ] **Redis Security**: Redis password configured and secure

### 🏗️ Infrastructure Validation
- [ ] **Docker Environment**: Docker and Docker Compose installed and running
- [ ] **System Resources**: Sufficient memory (8GB+) and CPU (4+ cores)
- [ ] **Disk Space**: At least 50GB free space for logs and data
- [ ] **Network Configuration**: All required ports accessible
- [ ] **Firewall Rules**: Proper firewall configuration for production

### 📦 Application Validation
- [ ] **Frontend Build**: `cd frontend && npm run build` succeeds
- [ ] **Backend Services**: All 13 microservices compile successfully
- [ ] **Database Migrations**: All Flyway migrations ready
- [ ] **Configuration Files**: All service configurations validated
- [ ] **Dependencies**: All dependencies installed and up to date

### 🧪 Testing Validation
- [ ] **Unit Tests**: `npm test` passes with >70% coverage
- [ ] **E2E Tests**: `npm run test:e2e` passes
- [ ] **Integration Tests**: Backend integration tests pass
- [ ] **Security Tests**: Security scan completed with no critical issues
- [ ] **Performance Tests**: Load testing completed successfully

### 📱 PWA Validation
- [ ] **PWA Manifest**: `manifest.json` configured correctly
- [ ] **Service Worker**: Service worker registered and functional
- [ ] **Offline Support**: Application works offline
- [ ] **Mobile Responsiveness**: All pages responsive on mobile devices
- [ ] **Install Prompt**: PWA install prompt working

## Deployment Steps

### 1. Pre-Deployment Validation
```bash
# Run comprehensive pre-deployment check
./scripts/pre-deployment-check.sh .env.production

# Validate environment variables
./scripts/validate-env.sh .env.production

# Check system resources
df -h
free -h
```

### 2. Backup Current State
```bash
# Create backup of current deployment
sudo ./scripts/rollback.sh backup

# Backup database
docker exec smartwatts-postgres pg_dump -U postgres smartwatts > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 3. Deploy Services
```bash
# Stop existing services
docker-compose down

# Pull latest images (if using registry)
docker-compose pull

# Start services
docker-compose up -d

# Wait for services to start
sleep 60
```

### 4. Post-Deployment Validation
```bash
# Run comprehensive health check
./scripts/health-check-all.sh

# Check service logs
docker-compose logs -f

# Verify all endpoints
curl -f http://localhost:8080/actuator/health
curl -f http://localhost:3000
```

### 5. Database Migration
```bash
# Run database migrations
docker-compose exec api-gateway java -jar /app.jar --spring.profiles.active=production

# Verify database schema
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "\dt"
```

### 6. SSL Configuration
```bash
# Generate SSL certificates (if not already done)
./ssl/generate-certificates.sh

# Update nginx configuration
sudo cp nginx/nginx.conf /etc/nginx/nginx.conf
sudo nginx -t
sudo systemctl reload nginx
```

## Post-Deployment Checklist

### 🏥 Health Checks
- [ ] **All Services Running**: All 13 microservices healthy
- [ ] **Database Connectivity**: PostgreSQL accessible from all services
- [ ] **Redis Connectivity**: Redis accessible from all services
- [ ] **Service Discovery**: Eureka registering all services
- [ ] **API Gateway**: All routes working correctly
- [ ] **Frontend**: React app loading and functional

### 🔗 Integration Checks
- [ ] **User Authentication**: Login/logout working
- [ ] **Energy Data**: Energy service returning data
- [ ] **Device Management**: Device registration working
- [ ] **Analytics**: Analytics service processing data
- [ ] **Billing**: Billing calculations working
- [ ] **PWA Features**: Offline functionality working

### 📊 Performance Checks
- [ ] **Response Times**: API responses < 200ms
- [ ] **Memory Usage**: Services using reasonable memory
- [ ] **CPU Usage**: CPU usage within acceptable limits
- [ ] **Database Performance**: Query times < 100ms
- [ ] **Frontend Performance**: Page load times < 3s

### 🔒 Security Checks
- [ ] **HTTPS**: All traffic encrypted
- [ ] **Authentication**: JWT tokens working correctly
- [ ] **Authorization**: Role-based access control working
- [ ] **Input Validation**: All inputs properly validated
- [ ] **SQL Injection**: No SQL injection vulnerabilities

### 📱 Mobile & PWA Checks
- [ ] **Mobile Responsiveness**: All pages work on mobile
- [ ] **PWA Installation**: App can be installed on mobile/desktop
- [ ] **Offline Functionality**: Core features work offline
- [ ] **Service Worker**: Caching working correctly
- [ ] **Push Notifications**: Notifications working (if implemented)

## Monitoring Setup

### 📈 Metrics Collection
- [ ] **Prometheus**: Metrics collection configured
- [ ] **Grafana**: Dashboards created and functional
- [ ] **Loki**: Log aggregation working
- [ ] **Alerting**: Alert rules configured
- [ ] **Health Checks**: Automated health monitoring

### 📝 Logging
- [ ] **Structured Logging**: JSON logs configured
- [ ] **Log Levels**: Appropriate log levels set
- [ ] **Log Rotation**: Log rotation configured
- [ ] **Error Tracking**: Error tracking integrated
- [ ] **Performance Monitoring**: APM tools configured

## Rollback Plan

### 🚨 Emergency Rollback
```bash
# Quick rollback to previous version
sudo ./scripts/rollback.sh

# Restore database from backup
docker exec -i smartwatts-postgres psql -U postgres -d smartwatts < backup.sql

# Restart services
docker-compose restart
```

### 🔄 Gradual Rollback
1. **Stop New Services**: Stop problematic services
2. **Restore Previous Version**: Deploy previous working version
3. **Verify Functionality**: Test critical features
4. **Monitor Performance**: Watch for issues
5. **Full Rollback**: Complete rollback if needed

## Success Criteria

### ✅ Technical Metrics
- [ ] **Uptime**: 99.5% uptime target
- [ ] **Response Time**: < 200ms API response times
- [ ] **Error Rate**: < 1% error rate
- [ ] **Test Coverage**: > 70% test coverage
- [ ] **Security Score**: No critical vulnerabilities

### ✅ Business Metrics
- [ ] **User Experience**: Smooth user experience
- [ ] **Feature Completeness**: All features working
- [ ] **Performance**: Fast page loads and interactions
- [ ] **Reliability**: Stable operation
- [ ] **Scalability**: Can handle expected load

## Troubleshooting

### 🔧 Common Issues
1. **Service Startup Failures**: Check logs and resource usage
2. **Database Connection Issues**: Verify database configuration
3. **Redis Connection Issues**: Check Redis configuration
4. **API Gateway Issues**: Verify routing configuration
5. **Frontend Build Issues**: Check Node.js and dependencies

### 📞 Emergency Contacts
- **System Administrator**: [Contact Info]
- **Database Administrator**: [Contact Info]
- **Security Team**: [Contact Info]
- **Development Team**: [Contact Info]

### 📚 Documentation
- **Runbook**: [Link to runbook]
- **Architecture Diagrams**: [Link to diagrams]
- **API Documentation**: [Link to API docs]
- **User Manual**: [Link to user manual]

## Post-Deployment Tasks

### 📋 Immediate Tasks (First 24 hours)
- [ ] Monitor all services for issues
- [ ] Check error logs for any problems
- [ ] Verify all integrations working
- [ ] Test critical user flows
- [ ] Monitor performance metrics

### 📋 Short-term Tasks (First week)
- [ ] Complete user acceptance testing
- [ ] Monitor system performance
- [ ] Gather user feedback
- [ ] Optimize based on usage patterns
- [ ] Plan next iteration

### 📋 Long-term Tasks (First month)
- [ ] Performance optimization
- [ ] Security audit
- [ ] Capacity planning
- [ ] Feature enhancements
- [ ] Documentation updates

---

**Deployment Date**: _______________
**Deployed By**: _______________
**Approved By**: _______________
**Rollback Plan**: _______________

**Notes**:







