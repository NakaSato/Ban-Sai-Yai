#!/bin/bash

# Bansaiyai Financial System Deployment Script
# This script builds and deploys the application using Docker Compose

set -e

echo "🚀 Starting Bansaiyai Financial System Deployment..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Stop and remove existing containers
echo "🛑 Stopping existing containers..."
docker compose down --remove-orphans

# Build and start the application
echo "🔨 Building and starting application (Java 21 + Postgres)..."
docker compose up --build -d

# Wait for the application to be ready (Health Check)
echo "⏳ Waiting for application to be healthy (max 60s)..."
MAX_RETRIES=12
count=0
while [ $count -lt $MAX_RETRIES ]; do
    if curl -s http://localhost:9090/actuator/health | grep -q "UP"; then
        echo "✅ Application is HEALTHY!"
        break
    fi
    echo "   ... waiting ($count/$MAX_RETRIES)"
    sleep 5
    count=$((count + 1))
done

if [ $count -eq $MAX_RETRIES ]; then
     echo "⚠️ Application did not become healthy in time. Check logs."
fi

# Check if the application is running via Docker
echo "🔍 Checking container status..."
if docker compose ps | grep -q "Up"; then
    echo "🎉 Deployment completed successfully!"
    echo ""
    echo "📋 Access Information:"
    echo "   - Backend API: http://localhost:9090"
    echo "   - Swagger UI:  http://localhost:9090/swagger-ui.html"
    echo "   - Database:    localhost:5432 (PostgreSQL)"
    echo ""
    echo "📝 To view logs: docker compose logs -f"
    echo "🛑 To stop: docker compose down"
else
    echo "❌ Application failed to start. Check logs with: docker compose logs"
    exit 1
fi
