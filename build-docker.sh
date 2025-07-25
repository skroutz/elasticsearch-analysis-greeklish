#!/bin/bash

# Build script for Elasticsearch plugin using Docker

set -e  # Exit on any error

echo "Building Elasticsearch plugin in Docker..."

# Build the Docker image
echo "Step 1: Building Docker image..."
docker build -t elasticsearch-analysis-greeklish-builder .

# Create a container to extract the built artifacts
echo "Step 2: Extracting built artifacts..."
CONTAINER_ID=$(docker create elasticsearch-analysis-greeklish-builder)

# Create output directory if it doesn't exist
mkdir -p ./docker-build-target

# Copy the built files from container to host
docker cp $CONTAINER_ID:/app/target/releases/. ./docker-build-target/

# Clean up the temporary container
docker rm $CONTAINER_ID

echo "Build completed successfully!"
echo "Built artifacts are available in: ./docker-build-target/"
echo ""
echo "Contents:"
ls -la ./docker-build-target/