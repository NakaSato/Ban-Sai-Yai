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

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Stop and remove existing containers
echo "🛑 Stopping existing containers..."
docker-compose down --remove-orphans

# Build and start the application
echo "🔨 Building and starting application..."
docker-compose up --build -d

# Wait for the database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 30

# Check if the application is running
echo "🔍 Checking application status..."
if docker-compose ps | grep -q "Up"; then
    echo "✅ Application is running successfully!"
    echo ""
    echo "📋 Access Information:"
    echo "   - Frontend: http://localhost:8080"
    echo "   - Backend API: http://localhost:8080/api"
    echo "   - Database: localhost:3306"
    echo ""
    echo "🔐 Default Database Credentials:"
    echo "   - Database: ban_sai_yai"
    echo "   - Username: admin"
    echo "   - Password: admin123"
    echo ""
    echo "📝 To view logs: docker-compose logs -f"
    echo "🛑 To stop: docker-compose down"
else
    echo "❌ Application failed to start. Check logs with: docker-compose logs"
    exit 1
fi

echo "🎉 Deployment completed successfully!"
