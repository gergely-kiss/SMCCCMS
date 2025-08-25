#!/bin/bash

# SMCCCMS Full Stack Runner
# Starts database, backend, and frontend together

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[SMCCCMS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Cleanup function
cleanup() {
    print_status "Shutting down all services..."
    
    # Kill background processes
    if [[ -n $DB_PID ]]; then
        print_info "Stopping database..."
        docker-compose -f docker-compose.db.yml down
    fi
    
    if [[ -n $BE_PID ]]; then
        print_info "Stopping backend (PID: $BE_PID)..."
        kill $BE_PID 2>/dev/null || true
    fi
    
    if [[ -n $FE_PID ]]; then
        print_info "Stopping frontend (PID: $FE_PID)..."
        kill $FE_PID 2>/dev/null || true
    fi
    
    print_status "All services stopped. Goodbye!"
    exit 0
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

print_status "Starting SMCCCMS full stack..."
print_info "Project root: $PROJECT_ROOT"

# Check prerequisites
command -v docker >/dev/null 2>&1 || { print_error "Docker is required but not installed. Aborting."; exit 1; }
command -v node >/dev/null 2>&1 || { print_error "Node.js is required but not installed. Aborting."; exit 1; }

# Start database
print_status "Starting PostgreSQL database..."
cd "$SCRIPT_DIR"
docker-compose -f docker-compose.db.yml up -d
DB_PID=1  # Docker runs as daemon

# Wait for database to be ready
print_info "Waiting for database to be ready..."
sleep 5

# Start backend
print_status "Starting Spring Boot backend..."
cd "$PROJECT_ROOT/smcccms_be"
if [[ ! -f "./mvnw" ]]; then
    print_error "Maven wrapper not found in smcccms_be directory"
    exit 1
fi

./mvnw spring-boot:run > ../logs/backend.log 2>&1 &
BE_PID=$!
print_info "Backend started with PID: $BE_PID"

# Wait for backend to start
print_info "Waiting for backend to start..."
sleep 10

# Check if backend is responding
for i in {1..30}; do
    if curl -s http://localhost:8080/api/actuator/health >/dev/null 2>&1; then
        print_status "Backend is ready!"
        break
    fi
    if [[ $i -eq 30 ]]; then
        print_error "Backend failed to start after 30 attempts"
        cleanup
        exit 1
    fi
    sleep 2
done

# Start frontend
print_status "Starting Vite frontend..."
cd "$PROJECT_ROOT/smcccms_fe"
if [[ ! -f "package.json" ]]; then
    print_error "package.json not found in smcccms_fe directory"
    exit 1
fi

# Install dependencies if node_modules doesn't exist
if [[ ! -d "node_modules" ]]; then
    print_info "Installing frontend dependencies..."
    npm ci
fi

npm run dev > ../logs/frontend.log 2>&1 &
FE_PID=$!
print_info "Frontend started with PID: $FE_PID"

# Create logs directory
mkdir -p "$PROJECT_ROOT/logs"

# Print status
print_status "🚀 All services started successfully!"
echo
echo -e "${GREEN}===============================================${NC}"
echo -e "${GREEN}  SMCCCMS Development Stack${NC}"
echo -e "${GREEN}===============================================${NC}"
echo -e "${BLUE}Frontend:${NC}    http://localhost:5173"
echo -e "${BLUE}Backend:${NC}     http://localhost:8080/api"
echo -e "${BLUE}Swagger UI:${NC}  http://localhost:8080/api/swagger-ui"
echo -e "${BLUE}Database:${NC}    localhost:57542 (PostgreSQL)"
echo -e "${GREEN}===============================================${NC}"
echo
print_info "Press Ctrl+C to stop all services"
echo
print_status "Logs are being written to:"
print_info "Backend:  $PROJECT_ROOT/logs/backend.log"
print_info "Frontend: $PROJECT_ROOT/logs/frontend.log"
echo

# Wait for services to run
wait