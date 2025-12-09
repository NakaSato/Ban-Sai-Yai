# Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the Ban Sai Yai Savings Group Financial Accounting System in production, staging, and development environments.

## System Requirements

### Hardware Requirements

#### Minimum Requirements (Small Deployment - < 100 members)
- **CPU**: 2 cores, 2.4 GHz
- **RAM**: 4 GB
- **Storage**: 50 GB SSD
- **Network**: 100 Mbps

#### Recommended Requirements (Medium Deployment - 100-500 members)
- **CPU**: 4 cores, 2.8 GHz
- **RAM**: 8 GB
- **Storage**: 200 GB SSD
- **Network**: 1 Gbps

#### Enterprise Requirements (Large Deployment - 500+ members)
- **CPU**: 8 cores, 3.0+ GHz
- **RAM**: 16 GB+
- **Storage**: 500+ GB SSD with backup
- **Network**: 1 Gbps+ with redundancy

### Software Requirements

#### Operating System
- **Linux**: Ubuntu 20.04 LTS / 22.04 LTS (Recommended)
- **Linux**: CentOS 8 / RHEL 8
- **Windows**: Windows Server 2019 / 2022 (Not recommended)

#### Java Runtime
- **Java**: OpenJDK 17 or Oracle JDK 17
- **JAVA_HOME**: Properly configured
- **Memory**: Minimum 2GB heap allocation

#### Database
- **MariaDB**: Version 10.6+ or MySQL 8.0+
- **Storage Engine**: InnoDB
- **Charset**: utf8mb4
- **Collation**: utf8mb4_unicode_ci

#### Web Server
- **Apache HTTP Server**: 2.4+ (Recommended for production)
- **Nginx**: 1.18+ (Alternative option)

## Pre-Deployment Setup

### 1. Environment Preparation

#### Create Application User
```bash
# Create dedicated user for the application
sudo useradd -m -s /bin/bash bansaiyai
sudo usermod -aG sudo bansaiyai

# Switch to application user
sudo su - bansaiyai
```

#### Directory Structure
```bash
# Create application directories
mkdir -p /opt/bansaiyai/{app,logs,config,backup,temp}
mkdir -p /opt/bansaiyai/logs/{app,access,error}
mkdir -p /opt/bansaiyai/backup/{database,files}
mkdir -p /opt/bansaiyai/temp/{uploads,exports}

# Set permissions
chmod 755 /opt/bansaiyai
chmod -R 755 /opt/bansaiyai/{logs,config,backup,temp}
```

### 2. Java Installation

#### Ubuntu/Debian
```bash
# Update package index
sudo apt update

# Install OpenJDK 17
sudo apt install -y openjdk-17-jdk

# Set JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
source ~/.bashrc

# Verify installation
java -version
javac -version
```

#### CentOS/RHEL
```bash
# Install OpenJDK 17
sudo yum install -y java-17-openjdk java-17-openjdk-devel

# Set JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk' >> ~/.bashrc
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
source ~/.bashrc

# Verify installation
java -version
```

### 3. Database Setup

#### MariaDB Installation
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y mariadb-server mariadb-client

# Secure installation
sudo mysql_secure_installation

# Start and enable service
sudo systemctl start mariadb
sudo systemctl enable mariadb
```

#### Database Creation
```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database
CREATE DATABASE bansaiyai_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create application user
CREATE USER 'bansaiyai_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT ALL PRIVILEGES ON bansaiyai_db.* TO 'bansaiyai_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify user creation
SHOW GRANTS FOR 'bansaiyai_user'@'localhost';
```

#### Database Configuration
```ini
# /etc/mysql/mariadb.conf.d/99-custom.cnf
[mysqld]
# General settings
datadir = /var/lib/mysql
tmpdir = /tmp
socket = /var/run/mysqld/mysqld.sock
port = 3306

# Character set
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# Performance settings
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 1
innodb_flush_method = O_DIRECT

# Connection settings
max_connections = 200
max_allowed_packet = 64M

# Query cache
query_cache_type = 1
query_cache_size = 64M

# Slow query log
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

## Application Deployment

### 1. Build Application

#### Build from Source
```bash
# Clone repository (if using Git)
git clone https://github.com/organization/bansaiyai-system.git
cd bansaiyai-system

# Build using Maven
./mvnw clean package -DskipTests

# Or using Gradle
./gradlew build -x test
```

#### Verify Build
```bash
# Check if JAR file is created
ls -la target/bansaiyai-*.jar

# Test application startup
java -jar target/bansaiyai-*.jar --spring.profiles.active=test
```

### 2. Configuration

#### Application Properties
```properties
# /opt/bansaiyai/config/application-prod.properties

# Server Configuration
server.port=8080
server.servlet.context-path=/api
server.tomcat.max-threads=200
server.tomcat.connection-timeout=20000

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/bansaiyai_db?useSSL=false&serverTimezone=Asia/Bangkok&allowPublicKeyRetrieval=true
spring.datasource.username=bansaiyai_user
spring.datasource.password=StrongPassword123!
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000

# File Storage
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.upload.location=/opt/bansaiyai/temp/uploads
app.export.location=/opt/bansaiyai/temp/exports

# Logging
logging.config=file:/opt/bansaiyai/config/logback-spring.xml
logging.level.com.bansaiyai.bansaiyai=INFO
logging.level.org.springframework.security=WARN

# Security
app.jwt.secret=YourSecretKeyHere-256BitsLong
app.jwt.expiration=86400000

# Email Configuration (for notifications)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### Logging Configuration
```xml
<!-- /opt/bansaiyai/config/logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATH" value="/opt/bansaiyai/logs"/>
    <property name="APP_NAME" value="bansaiyai"/>
    
    <appender name="FILE-ERROR" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/error/${APP_NAME}-error.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/error/${APP_NAME}-error.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
    
    <appender name="FILE-APP" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/app/${APP_NAME}.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/app/${APP_NAME}.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE-APP"/>
        <appender-ref ref="FILE-ERROR"/>
    </root>
</configuration>
```

### 3. System Service Setup

#### Create Systemd Service
```ini
# /etc/systemd/system/bansaiyai.service
[Unit]
Description=Ban Sai Yai Financial Accounting System
After=network.target mysql.service

[Service]
Type=simple
User=bansaiyai
Group=bansaiyai
WorkingDirectory=/opt/bansaiyai/app
ExecStart=/usr/bin/java -jar -Xmx2g -Xms1g \
    -Dspring.profiles.active=prod \
    -Dspring.config.location=file:/opt/bansaiyai/config/application-prod.properties \
    -Dlogging.config=file:/opt/bansaiyai/config/logback-spring.xml \
    /opt/bansaiyai/app/bansaiyai-1.0.0.jar

SuccessExitStatus=143
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=bansaiyai

[Install]
WantedBy=multi-user.target
```

#### Enable and Start Service
```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable bansaiyai

# Start service
sudo systemctl start bansaiyai

# Check service status
sudo systemctl status bansaiyai

# View logs
sudo journalctl -u bansaiyai -f
```

## Web Server Configuration

### 1. Apache HTTP Server Setup

#### Installation
```bash
# Ubuntu/Debian
sudo apt install -y apache2

# Enable required modules
sudo a2enmod proxy
sudo a2enmod proxy_http
sudo a2enmod proxy_balancer
sudo a2enmod lbmethod_byrequests
sudo a2enmod rewrite
sudo a2enmod headers
sudo a2enmod ssl
```

#### Virtual Host Configuration
```apache
# /etc/apache2/sites-available/bansaiyai.conf
<VirtualHost *:80>
    ServerName bansaiyai.example.com
    ServerAlias www.bansaiyai.example.com
    
    # Redirect HTTP to HTTPS
    Redirect permanent / https://bansaiyai.example.com/
</VirtualHost>

<VirtualHost *:443>
    ServerName bansaiyai.example.com
    ServerAlias www.bansaiyai.example.com
    
    # SSL Configuration
    SSLEngine on
    SSLCertificateFile /etc/ssl/certs/bansaiyai.crt
    SSLCertificateKeyFile /etc/ssl/private/bansaiyai.key
    SSLCertificateChainFile /etc/ssl/certs/bansaiyai-chain.crt
    
    # Security Headers
    Header always set X-Frame-Options DENY
    Header always set X-Content-Type-Options nosniff
    Header always set X-XSS-Protection "1; mode=block"
    Header always set Strict-Transport-Security "max-age=31536000; includeSubDomains"
    Header always set Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
    
    # Proxy Configuration
    ProxyPreserveHost On
    ProxyRequests Off
    
    # API Proxy
    ProxyPass /api http://localhost:8080/api retry=1
    ProxyPassReverse /api http://localhost:8080/api
    
    # Static Files (if serving frontend)
    DocumentRoot /var/www/bansaiyai/html
    <Directory /var/www/bansaiyai/html>
        AllowOverride All
        Require all granted
    </Directory>
    
    # Logging
    ErrorLog ${APACHE_LOG_DIR}/bansaiyai_error.log
    CustomLog ${APACHE_LOG_DIR}/bansaiyai_access.log combined
</VirtualHost>
```

#### Enable Site
```bash
# Enable site
sudo a2ensite bansaiyai

# Disable default site
sudo a2dissite 000-default

# Test configuration
sudo apache2ctl configtest

# Restart Apache
sudo systemctl restart apache2
```

### 2. Nginx Alternative Configuration

#### Installation
```bash
# Ubuntu/Debian
sudo apt install -y nginx

# Remove default site
sudo rm /etc/nginx/sites-enabled/default
```

#### Nginx Configuration
```nginx
# /etc/nginx/sites-available/bansaiyai
server {
    listen 80;
    server_name bansaiyai.example.com www.bansaiyai.example.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name bansaiyai.example.com www.bansaiyai.example.com;
    
    # SSL Configuration
    ssl_certificate /etc/ssl/certs/bansaiyai.crt;
    ssl_certificate_key /etc/ssl/private/bansaiyai.key;
    ssl_certificate_chain /etc/ssl/certs/bansaiyai-chain.crt;
    
    # SSL Settings
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    
    # Security Headers
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    
    # API Proxy
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }
    
    # Static Files
    location / {
        root /var/www/bansaiyai/html;
        index index.html;
        try_files $uri $uri/ =404;
    }
    
    # Logging
    access_log /var/log/nginx/bansaiyai_access.log;
    error_log /var/log/nginx/bansaiyai_error.log;
}
```

#### Enable Site
```bash
# Create symbolic link
sudo ln -s /etc/nginx/sites-available/bansaiyai /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Restart Nginx
sudo systemctl restart nginx
```

## Database Migration

### 1. Schema Migration

#### Using Flyway
```bash
# Run database migrations
java -jar bansaiyai-1.0.0.jar \
    --spring.profiles.active=migration \
    --spring.flyway.enabled=true \
    --spring.flyway.locations=classpath:db/migration
```

#### Manual SQL Migration
```sql
-- Create tables and initial data
-- Run provided SQL scripts in order:
-- 01_create_tables.sql
-- 02_create_indexes.sql
-- 03_insert_initial_data.sql
-- 04_create_procedures.sql
-- 05_create_triggers.sql

-- Verify migration
SELECT COUNT(*) as total_tables FROM information_schema.tables 
WHERE table_schema = 'bansaiyai_db';
```

### 2. Data Import (if migrating from existing system)

#### Data Validation
```sql
-- Validate data integrity before import
SELECT 
    COUNT(*) as total_members,
    COUNT(CASE WHEN id_card IS NULL OR LENGTH(id_card) != 13 THEN 1 END) as invalid_id_cards
FROM member;

SELECT 
    COUNT(*) as total_loans,
    COUNT(CASE WHEN member_id IS NULL THEN 1 END) as orphaned_loans
FROM loan;
```

## Security Configuration

### 1. Firewall Setup

#### UFW (Ubuntu)
```bash
# Enable firewall
sudo ufw enable

# Allow SSH
sudo ufw allow ssh

# Allow HTTP/HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Allow database access (if needed from specific IPs)
sudo ufw allow from 192.168.1.0/24 to any port 3306

# Check status
sudo ufw status
```

#### firewalld (CentOS/RHEL)
```bash
# Enable firewall
sudo systemctl enable firewalld
sudo systemctl start firewalld

# Allow services
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https

# Reload rules
sudo firewall-cmd --reload
```

### 2. SSL Certificate Setup

#### Self-Signed Certificate (Development)
```bash
# Generate private key
sudo openssl genrsa -out /etc/ssl/private/bansaiyai.key 2048

# Generate certificate signing request
sudo openssl req -new -key /etc/ssl/private/bansaiyai.key -out /etc/ssl/certs/bansaiyai.csr

# Generate self-signed certificate
sudo openssl x509 -req -days 365 -in /etc/ssl/certs/bansaiyai.csr \
    -signkey /etc/ssl/private/bansaiyai.key -out /etc/ssl/certs/bansaiyai.crt
```

#### Let's Encrypt Certificate (Production)
```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-apache

# Obtain certificate
sudo certbot --apache -d bansaiyai.example.com -d www.bansaiyai.example.com

# Set up auto-renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

## Monitoring and Maintenance

### 1. Application Monitoring

#### Health Check Endpoint
```bash
# Configure health check in application.properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.health.probes.enabled=true

# Test health endpoint
curl http://localhost:8080/api/actuator/health
```

#### Monitoring Script
```bash
#!/bin/bash
# /opt/bansaiyai/scripts/health-check.sh

APP_URL="http://localhost:8080/api/actuator/health"
LOG_FILE="/opt/bansaiyai/logs/health-check.log"

response=$(curl -s -o /dev/null -w "%{http_code}" $APP_URL)

if [ $response != "200" ]; then
    echo "$(date): Application health check failed (HTTP $response)" >> $LOG_FILE
    # Send alert notification
    # systemctl restart bansaiyai
else
    echo "$(date): Application health check passed" >> $LOG_FILE
fi
```

### 2. Database Maintenance

#### Backup Script
```bash
#!/bin/bash
# /opt/bansaiyai/scripts/backup-database.sh

DB_NAME="bansaiyai_db"
DB_USER="bansaiyai_user"
DB_PASS="StrongPassword123!"
BACKUP_DIR="/opt/bansaiyai/backup/database"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME | gzip > $BACKUP_DIR/backup_$DATE.sql.gz

# Remove backups older than 30 days
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete

echo "Database backup completed: backup_$DATE.sql.gz"
```

#### Schedule Backups
```bash
# Add to crontab
crontab -e

# Daily backup at 2 AM
0 2 * * * /opt/bansaiyai/scripts/backup-database.sh

# Health check every 5 minutes
*/5 * * * * /opt/bansaiyai/scripts/health-check.sh
```

### 3. Log Rotation

#### Logrotate Configuration
```bash
# /etc/logrotate.d/bansaiyai
/opt/bansaiyai/logs/app/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 bansaiyai bansaiyai
    postrotate
        systemctl reload bansaiyai
    endscript
}

/opt/bansaiyai/logs/error/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 bansaiyai bansaiyai
    postrotate
        systemctl reload bansaiyai
    endscript
}
```

## Performance Tuning

### 1. JVM Tuning

#### Production JVM Settings
```bash
# Adjust JVM parameters based on system resources
JAVA_OPTS="-Xmx4g -Xms2g \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=prod"
```

### 2. Database Optimization

#### MySQL Performance Tuning
```sql
-- Optimize InnoDB settings for production
SET GLOBAL innodb_buffer_pool_size = 2147483648; -- 2GB
SET GLOBAL innodb_log_file_size = 268435456; -- 256MB
SET GLOBAL innodb_flush_log_at_trx_commit = 1;
SET GLOBAL innodb_flush_method = 'O_DIRECT';
SET GLOBAL innodb_file_per_table = 1;
```

### 3. Application Performance

#### Connection Pool Optimization
```properties
# Optimize HikariCP settings
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.leak-detection-threshold=60000
```

## Troubleshooting

### Common Issues and Solutions

#### Application Won't Start
1. Check Java version: `java -version`
2. Check port availability: `netstat -tlnp | grep 8080`
3. Check logs: `tail -f /opt/bansaiyai/logs/app/bansaiyai.log`
4. Check database connection: `telnet localhost 3306`

#### Database Connection Issues
1. Verify database is running: `systemctl status mysql`
2. Check connectivity: `mysql -u bansaiyai_user -p bansaiyai_db`
3. Verify credentials in configuration
4. Check firewall settings

#### Performance Issues
1. Monitor system resources: `top`, `htop`
2. Check database performance: `SHOW PROCESSLIST;`
3. Analyze slow query log
4. Monitor JVM memory usage: `jstat -gc`

### Log Analysis

#### Application Logs
```bash
# View recent application logs
tail -f /opt/bansaiyai/logs/app/bansaiyai.log

# Search for errors
grep -i error /opt/bansaiyai/logs/app/bansaiyai.log

# View database logs
tail -f /var/log/mysql/error.log
```

#### System Logs
```bash
# View systemd service logs
journalctl -u bansaiyai -f

# View Apache logs
tail -f /var/log/apache2/bansaiyai_access.log
tail -f /var/log/apache2/bansaiyai_error.log
```

---

**Related Documentation**:
- [System Architecture](../architecture/system-design.md) - System design overview
- [Database Schema](../architecture/database-schema.md) - Database design
- [Security Configuration](../security/authentication-authorization.md) - Security setup
