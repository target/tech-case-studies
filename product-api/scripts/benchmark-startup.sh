#!/bin/bash

# ============================================================================
# Product API - Docker Startup Time Benchmark Script
# ============================================================================
# Measures Docker container startup time by building the image locally and
# running multiple test iterations.
#
# Usage:
#   ./scripts/benchmark-startup.sh [iterations]
#
# Example:
#   ./scripts/benchmark-startup.sh 3
# ============================================================================

set -e

# Configuration
IMAGE_NAME="product-api:benchmark"
CONTAINER_PREFIX="product-api-benchmark"
HEALTH_ENDPOINT="http://localhost:8080/v1/health"
DEFAULT_ITERATIONS=3
HEALTH_TIMEOUT=60
WAIT_BETWEEN_ITERATIONS=2

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get iterations from argument or use default
ITERATIONS=${1:-$DEFAULT_ITERATIONS}

# Arrays to store results
declare -a spring_boot_times
declare -a health_check_times

# ============================================================================
# Helper Functions
# ============================================================================

print_header() {
    echo ""
    echo "==========================================="
    echo "$1"
    echo "==========================================="
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

cleanup_container() {
    local container_name=$1
    if docker ps -a --format '{{.Names}}' | grep -q "^${container_name}$"; then
        print_info "Cleaning up container: $container_name"
        docker stop "$container_name" >/dev/null 2>&1 || true
        docker rm "$container_name" >/dev/null 2>&1 || true
    fi
}

check_port_available() {
    local port=$1
    if lsof -Pi :"$port" -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        return 1
    else
        return 0
    fi
}

# Calculate statistics
calculate_stats() {
    local array_name=$1
    eval "local -a arr=(\"\${${array_name}[@]}\")"
    local count=${#arr[@]}
    
    if [ "$count" -eq 0 ]; then
        echo "N/A"
        return
    fi
    
    # Sort array
    IFS=$'\n' sorted=($(sort -n <<<"${arr[*]}"))
    unset IFS
    
    # Min and Max
    local min=${sorted[0]}
    local max=${sorted[$((count-1))]}
    
    # Average
    local sum=0
    for val in "${arr[@]}"; do
        sum=$(echo "$sum + $val" | bc)
    done
    local avg=$(echo "scale=3; $sum / $count" | bc)
    
    # Median
    local median
    if [ $((count % 2)) -eq 0 ]; then
        local mid1=${sorted[$((count/2 - 1))]}
        local mid2=${sorted[$((count/2))]}
        median=$(echo "scale=3; ($mid1 + $mid2) / 2" | bc)
    else
        median=${sorted[$((count/2))]}
    fi
    
    echo -e "  Min:     ${min}s"
    echo -e "  Max:     ${max}s"
    echo -e "  Average: ${avg}s"
    echo -e "  Median:  ${median}s"
}

# ============================================================================
# Main Benchmark Logic
# ============================================================================

print_header "Product API Startup Benchmark"

# Check if JAR exists
if [ ! -f "build/libs/product-api.jar" ]; then
    print_error "JAR file not found: build/libs/product-api.jar"
    print_info "Please run: ./gradlew clean build"
    exit 1
fi

# Determine which port to use
PORT=8080
if ! check_port_available $PORT; then
    print_warning "Port 8080 is in use, trying port 8081..."
    PORT=8081
    if ! check_port_available $PORT; then
        print_error "Both ports 8080 and 8081 are in use. Please free up a port."
        exit 1
    fi
    HEALTH_ENDPOINT="http://localhost:8081/v1/health"
fi

print_info "Using port: $PORT"

# Build Docker image
print_info "Building Docker image..."
build_start=$(date +%s)
if docker build -t "$IMAGE_NAME" . >/dev/null 2>&1; then
    build_end=$(date +%s)
    build_time=$((build_end - build_start))
    print_success "Build completed in ${build_time}s"
else
    print_error "Docker build failed"
    exit 1
fi

# Get image size
image_size=$(docker images "$IMAGE_NAME" --format "{{.Size}}" | head -1)
print_info "Image size: $image_size"

echo ""
print_info "Running $ITERATIONS benchmark iterations..."
echo ""

# Run benchmark iterations
for i in $(seq 1 "$ITERATIONS"); do
    echo "----------------------------------------"
    echo "Iteration $i/$ITERATIONS"
    echo "----------------------------------------"
    
    container_name="${CONTAINER_PREFIX}-$(date +%s)-${i}"
    
    # Cleanup any existing container with same name
    cleanup_container "$container_name"
    
    # Start container
    print_info "Starting container..."
    iteration_start=$(date +%s.%N)
    
    if ! docker run -d -p ${PORT}:8080 --name "$container_name" "$IMAGE_NAME" >/dev/null 2>&1; then
        print_error "Failed to start container"
        cleanup_container "$container_name"
        continue
    fi
    
    # Wait for Spring Boot startup message in logs
    print_info "Waiting for Spring Boot to start..."
    spring_boot_time=""
    timeout_counter=0
    
    while [ -z "$spring_boot_time" ] && [ $timeout_counter -lt $HEALTH_TIMEOUT ]; do
        sleep 0.5
        timeout_counter=$((timeout_counter + 1))
        
        # Extract startup time from logs
        log_output=$(docker logs "$container_name" 2>&1)
        if echo "$log_output" | grep -q "Started Main in"; then
            spring_boot_time=$(echo "$log_output" | grep "Started Main in" | sed -n 's/.*Started Main in \([0-9.]*\) seconds.*/\1/p' | tail -1)
            break
        fi
        
        # Check if container exited
        if ! docker ps --format '{{.Names}}' | grep -q "^${container_name}$"; then
            print_error "Container exited unexpectedly"
            docker logs "$container_name" 2>&1
            cleanup_container "$container_name"
            break
        fi
    done
    
    if [ -z "$spring_boot_time" ]; then
        print_error "Timeout waiting for Spring Boot startup"
        cleanup_container "$container_name"
        continue
    fi
    
    print_success "Spring Boot started in: ${spring_boot_time}s"
    spring_boot_times+=("$spring_boot_time")
    
    # Wait for health check endpoint
    print_info "Waiting for health check endpoint..."
    health_check_success=false
    
    for attempt in $(seq 1 $HEALTH_TIMEOUT); do
        if curl -sf "$HEALTH_ENDPOINT" >/dev/null 2>&1; then
            iteration_end=$(date +%s.%N)
            health_check_time=$(echo "$iteration_end - $iteration_start" | bc)
            print_success "Health check ready in: ${health_check_time}s"
            health_check_times+=("$health_check_time")
            health_check_success=true
            break
        fi
        sleep 1
    done
    
    if [ "$health_check_success" = false ]; then
        print_error "Timeout waiting for health check endpoint"
    fi
    
    # Cleanup
    cleanup_container "$container_name"
    
    # Wait between iterations
    if [ "$i" -lt "$ITERATIONS" ]; then
        sleep $WAIT_BETWEEN_ITERATIONS
    fi
    
    echo ""
done

# ============================================================================
# Results Summary
# ============================================================================

print_header "Results Summary"

echo ""
echo "Spring Boot Startup Time:"
calculate_stats spring_boot_times

echo ""
echo "Time to Health Check Ready:"
calculate_stats health_check_times

echo ""
echo "Image Size: $image_size"
echo "Build Time: ${build_time}s"

# Save results to file
timestamp=$(date +%Y-%m-%d-%H-%M-%S)
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
results_file="${script_dir}/benchmark-results-${timestamp}.txt"

{
    echo "==========================================="
    echo "Product API Startup Benchmark"
    echo "Timestamp: $(date)"
    echo "==========================================="
    echo ""
    echo "Configuration:"
    echo "  Iterations: $ITERATIONS"
    echo "  Image: $IMAGE_NAME"
    echo "  Port: $PORT"
    echo ""
    echo "Spring Boot Startup Time:"
    calculate_stats spring_boot_times
    echo ""
    echo "Time to Health Check Ready:"
    calculate_stats health_check_times
    echo ""
    echo "Image Size: $image_size"
    echo "Build Time: ${build_time}s"
    echo ""
    echo "Raw Data:"
    echo "  Spring Boot Times: ${spring_boot_times[*]}"
    echo "  Health Check Times: ${health_check_times[*]}"
} > "$results_file"

echo ""
print_success "Results saved to: $results_file"
echo ""
