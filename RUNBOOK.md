# SmartWatts Production Runbook

## Overview
This runbook provides step-by-step procedures for common production scenarios, troubleshooting, and emergency responses for the SmartWatts platform.

## Table of Contents
1. [Emergency Procedures](#emergency-procedures)
2. [Service Management](#service-management)
3. [Database Operations](#database-operations)
4. [Monitoring & Alerting](#monitoring--alerting)
5. [Troubleshooting Guide](#troubleshooting-guide)
6. [Performance Optimization](#performance-optimization)
7. [Security Procedures](#security-procedures)
8. [Backup & Recovery](#backup--recovery)

---

## Emergency Procedures

### 🚨 Service Down
**Symptoms**: Users cannot access the application, API returns 503 errors

**Immediate Actions**:
1. Check service status: `docker-compose ps`
2. Check logs: `docker-compose logs -f [service-name]`
3. Restart affected service: `docker-compose restart [service-name]`
4. If multiple services down, restart all: `docker-compose restart`

**Escalation**:
- If services don't restart: Check system resources (memory, disk)
- If database issues: Check PostgreSQL logs and connectivity
- If Redis issues: Check Redis logs and memory usage

### 🔥 Database Issues
**Symptoms**: Database connection errors, slow queries, data corruption

**Immediate Actions**:
1. Check database status: `docker-compose logs postgres`
2. Check database connectivity: `docker exec smartwatts-postgres pg_isready`
3. Check database size: `docker exec smartwatts-postgres psql -U postgres -c "SELECT pg_size_pretty(pg_database_size('smartwatts'));"`
4. Check active connections: `docker exec smartwatts-postgres psql -U postgres -c "SELECT count(*) FROM pg_stat_activity;"`

**Recovery Procedures**:
```bash
# Restart PostgreSQL
docker-compose restart postgres

# Check database integrity
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "VACUUM ANALYZE;"

# If corruption detected, restore from backup
docker exec -i smartwatts-postgres psql -U postgres -d smartwatts < backup.sql
```

### 💾 Memory Issues
**Symptoms**: Services being killed, OutOfMemoryError, slow performance

**Immediate Actions**:
1. Check memory usage: `free -h`
2. Check Docker memory: `docker stats`
3. Check service memory limits: `docker-compose config`

**Recovery Procedures**:
```bash
# Increase memory limits in docker-compose.yml
# Restart services with more memory
docker-compose down
docker-compose up -d

# If system memory low, restart Docker
sudo systemctl restart docker
```

### 🔒 Security Incidents
**Symptoms**: Unauthorized access, suspicious activity, data breaches

**Immediate Actions**:
1. **DO NOT PANIC** - Document everything
2. Isolate affected systems if necessary
3. Check access logs: `docker-compose logs api-gateway | grep -i "unauthorized\|forbidden"`
4. Check authentication logs: `docker-compose logs user-service | grep -i "login\|auth"`

**Response Procedures**:
```bash
# Block suspicious IPs
sudo iptables -A INPUT -s [suspicious-ip] -j DROP

# Rotate JWT secrets
# Update JWT_SECRET in .env file
# Restart all services

# Check for compromised accounts
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "SELECT * FROM users WHERE last_login > NOW() - INTERVAL '1 hour';"
```

---

## Service Management

### 🔄 Starting Services
```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d [service-name]

# Start with logs
docker-compose up -d && docker-compose logs -f
```

### 🛑 Stopping Services
```bash
# Stop all services
docker-compose down

# Stop specific service
docker-compose stop [service-name]

# Stop and remove volumes (DANGER: Data loss)
docker-compose down -v
```

### 🔍 Checking Service Status
```bash
# Check all services
docker-compose ps

# Check specific service
docker-compose ps [service-name]

# Check service health
curl -f http://localhost:8080/actuator/health
```

### 📊 Service Logs
```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs [service-name]

# Follow logs in real-time
docker-compose logs -f [service-name]

# View last 100 lines
docker-compose logs --tail=100 [service-name]
```

### 🔧 Service Configuration
```bash
# Validate configuration
docker-compose config

# Check environment variables
docker-compose exec [service-name] env

# Update service configuration
# Edit docker-compose.yml or .env file
# Restart service: docker-compose restart [service-name]
```

---

## Database Operations

### 🗄️ Database Backup
```bash
# Full database backup
docker exec smartwatts-postgres pg_dump -U postgres smartwatts > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup specific tables
docker exec smartwatts-postgres pg_dump -U postgres -t users smartwatts > users_backup.sql

# Compressed backup
docker exec smartwatts-postgres pg_dump -U postgres smartwatts | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

### 🔄 Database Restore
```bash
# Restore from backup
docker exec -i smartwatts-postgres psql -U postgres -d smartwatts < backup.sql

# Restore compressed backup
gunzip -c backup.sql.gz | docker exec -i smartwatts-postgres psql -U postgres -d smartwatts
```

### 🧹 Database Maintenance
```bash
# Vacuum database
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "VACUUM ANALYZE;"

# Check database size
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "SELECT pg_size_pretty(pg_database_size('smartwatts'));"

# Check table sizes
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "SELECT schemaname,tablename,pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size FROM pg_tables WHERE schemaname='public' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;"
```

### 🔍 Database Queries
```bash
# Connect to database
docker exec -it smartwatts-postgres psql -U postgres -d smartwatts

# Check active connections
SELECT count(*) FROM pg_stat_activity;

# Check slow queries
SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;

# Check database locks
SELECT * FROM pg_locks WHERE NOT granted;
```

---

## Monitoring & Alerting

### 📊 Health Checks
```bash
# Run comprehensive health check
./scripts/health-check-all.sh

# Check specific service
curl -f http://localhost:8080/api/[service]/actuator/health

# Check database health
docker exec smartwatts-postgres pg_isready -U postgres

# Check Redis health
docker exec smartwatts-redis redis-cli ping
```

### 📈 Performance Monitoring
```bash
# Check system resources
htop
df -h
free -h

# Check Docker resource usage
docker stats

# Check service response times
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/actuator/health
```

### 🚨 Alerting
```bash
# Check alert rules
# Prometheus: http://localhost:9090/alerts
# Grafana: http://localhost:3000

# Manual alert testing
curl -X POST http://localhost:9093/api/v1/alerts -d '[{"labels":{"alertname":"TestAlert"}}]'
```

---

## Troubleshooting Guide

### 🔍 Common Issues

#### Service Won't Start
**Symptoms**: Service fails to start, exits with error code

**Diagnosis**:
```bash
# Check service logs
docker-compose logs [service-name]

# Check service configuration
docker-compose config

# Check system resources
free -h
df -h
```

**Solutions**:
1. **Memory issues**: Increase memory limits
2. **Configuration errors**: Fix configuration files
3. **Dependency issues**: Check service dependencies
4. **Port conflicts**: Check for port conflicts

#### Database Connection Issues
**Symptoms**: Services cannot connect to database

**Diagnosis**:
```bash
# Check database status
docker-compose logs postgres

# Check database connectivity
docker exec smartwatts-postgres pg_isready -U postgres

# Check database configuration
docker-compose exec postgres env | grep POSTGRES
```

**Solutions**:
1. **Database down**: Restart PostgreSQL
2. **Connection limits**: Check max_connections
3. **Authentication issues**: Check user credentials
4. **Network issues**: Check Docker networking

#### API Gateway Issues
**Symptoms**: API requests failing, routing errors

**Diagnosis**:
```bash
# Check API Gateway logs
docker-compose logs api-gateway

# Check routing configuration
curl -f http://localhost:8080/actuator/gateway/routes

# Check service discovery
curl -f http://localhost:8761/eureka/apps
```

**Solutions**:
1. **Service discovery issues**: Restart Eureka
2. **Routing configuration**: Check route definitions
3. **Load balancer issues**: Check upstream services
4. **Authentication issues**: Check JWT configuration

#### Frontend Issues
**Symptoms**: Frontend not loading, build errors

**Diagnosis**:
```bash
# Check frontend build
cd frontend && npm run build

# Check frontend logs
docker-compose logs frontend

# Check browser console
# Open browser dev tools and check console errors
```

**Solutions**:
1. **Build errors**: Fix build issues
2. **Dependency issues**: Update dependencies
3. **Configuration issues**: Check environment variables
4. **Network issues**: Check API connectivity

### 🔧 Performance Issues

#### Slow Response Times
**Diagnosis**:
```bash
# Check service response times
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/actuator/health

# Check database performance
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"

# Check system resources
htop
```

**Solutions**:
1. **Database optimization**: Optimize queries, add indexes
2. **Caching**: Implement Redis caching
3. **Load balancing**: Distribute load across instances
4. **Resource scaling**: Increase memory/CPU

#### High Memory Usage
**Diagnosis**:
```bash
# Check memory usage
docker stats

# Check specific service memory
docker-compose exec [service-name] ps aux

# Check system memory
free -h
```

**Solutions**:
1. **Memory leaks**: Restart services regularly
2. **Resource limits**: Increase memory limits
3. **Garbage collection**: Tune JVM settings
4. **Caching**: Implement memory-efficient caching

---

## Performance Optimization

### 🚀 Database Optimization
```bash
# Analyze query performance
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"

# Create indexes for slow queries
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "CREATE INDEX CONCURRENTLY idx_users_email ON users(email);"

# Update table statistics
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "ANALYZE;"
```

### 🔄 Caching Optimization
```bash
# Check Redis performance
docker exec smartwatts-redis redis-cli info memory

# Check cache hit rates
docker exec smartwatts-redis redis-cli info stats

# Optimize Redis configuration
# Edit redis.conf for better performance
```

### 📊 Service Optimization
```bash
# Check service metrics
curl -f http://localhost:8080/actuator/metrics

# Check JVM settings
docker-compose exec [service-name] java -XX:+PrintFlagsFinal -version

# Optimize JVM settings
# Add JVM options to docker-compose.yml
```

---

## Security Procedures

### 🔒 Security Monitoring
```bash
# Check authentication logs
docker-compose logs user-service | grep -i "login\|auth\|failed"

# Check access logs
docker-compose logs api-gateway | grep -i "unauthorized\|forbidden"

# Check for suspicious activity
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "SELECT * FROM users WHERE last_login > NOW() - INTERVAL '1 hour';"
```

### 🛡️ Security Updates
```bash
# Update Docker images
docker-compose pull

# Update system packages
sudo apt update && sudo apt upgrade -y

# Update application dependencies
cd frontend && npm update
```

### 🔐 Access Control
```bash
# Check user permissions
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "SELECT username, role FROM users;"

# Check API access logs
docker-compose logs api-gateway | grep -i "access"

# Monitor failed login attempts
docker-compose logs user-service | grep -i "failed login"
```

---

## Backup & Recovery

### 💾 Backup Procedures
```bash
# Full system backup
./scripts/backup-full.sh

# Database backup
./scripts/backup-database.sh

# Configuration backup
./scripts/backup-config.sh
```

### 🔄 Recovery Procedures
```bash
# Full system recovery
./scripts/rollback.sh

# Database recovery
./scripts/restore-database.sh backup.sql

# Configuration recovery
./scripts/restore-config.sh
```

### 📋 Backup Verification
```bash
# Verify backup integrity
./scripts/verify-backup.sh

# Test recovery procedures
./scripts/test-recovery.sh
```

---

## Emergency Contacts

### 📞 On-Call Team
- **Primary**: [Contact Info]
- **Secondary**: [Contact Info]
- **Escalation**: [Contact Info]

### 🏥 External Support
- **Cloud Provider**: [Contact Info]
- **Database Support**: [Contact Info]
- **Security Team**: [Contact Info]

### 📚 Documentation
- **Architecture**: [Link to architecture docs]
- **API Documentation**: [Link to API docs]
- **User Manual**: [Link to user manual]

---

## Quick Reference

### 🚀 Common Commands
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Check service status
docker-compose ps

# View logs
docker-compose logs -f

# Restart service
docker-compose restart [service-name]

# Health check
./scripts/health-check-all.sh
```

### 🔧 Emergency Commands
```bash
# Quick rollback
./scripts/rollback.sh

# Emergency restart
docker-compose down && docker-compose up -d

# Database backup
docker exec smartwatts-postgres pg_dump -U postgres smartwatts > emergency_backup.sql

# System restart
sudo reboot
```

### 📊 Monitoring URLs
- **Application**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Service Discovery**: http://localhost:8761
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001

---

**Last Updated**: [Date]
**Version**: 1.0
**Maintained By**: [Team Name]










