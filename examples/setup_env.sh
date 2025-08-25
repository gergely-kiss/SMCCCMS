#!/bin/bash

# Environment Setup Script for CLI LLM Management System
# This script sets up Java 21 and Maven environment without overriding system versions

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Environment file to store paths
ENV_FILE="$SCRIPT_DIR/.clms_env"

echo -e "${BLUE}🔧 Setting up Java 21 and Maven environment${NC}"
echo "================================================"

# Function to detect Java 21+ in common locations
find_java_21() {
    local java_paths=(
        # Check Maven's Java first
        "$(mvn -version 2>/dev/null | grep "Java version" | sed 's/.*runtime: \(.*\)$/\1/' | head -n1)/bin/java"
        # Common macOS Java locations
        "/Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java"
        "/Users/$USER/Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java"
        # Common Linux locations
        "/usr/lib/jvm/java-21-*/bin/java"
        "/usr/lib/jvm/java-22-*/bin/java"
        "/usr/lib/jvm/java-23-*/bin/java"
        "/usr/lib/jvm/java-24-*/bin/java"
        # SDKMAN locations
        "$HOME/.sdkman/candidates/java/21*/bin/java"
        "$HOME/.sdkman/candidates/java/22*/bin/java"
        "$HOME/.sdkman/candidates/java/23*/bin/java"
        "$HOME/.sdkman/candidates/java/24*/bin/java"
        # Homebrew locations
        "/opt/homebrew/opt/openjdk@21/bin/java"
        "/opt/homebrew/opt/openjdk@22/bin/java"
        "/opt/homebrew/opt/openjdk@23/bin/java"
        "/opt/homebrew/opt/openjdk@24/bin/java"
        "/usr/local/opt/openjdk@21/bin/java"
        "/usr/local/opt/openjdk@22/bin/java"
        "/usr/local/opt/openjdk@23/bin/java"
        "/usr/local/opt/openjdk@24/bin/java"
    )
    
    for java_path in "${java_paths[@]}"; do
        # Expand wildcards
        for expanded_path in $java_path; do
            if [[ -x "$expanded_path" ]]; then
                local version=$("$expanded_path" -version 2>&1 | head -n1)
                if echo "$version" | grep -qE "version \"(2[1-9]|[3-9][0-9])"; then
                    local java_home=$(dirname $(dirname "$expanded_path"))
                    echo "$java_home"
                    return 0
                fi
            fi
        done
    done
    
    return 1
}

# Function to find Maven installation
find_maven() {
    local maven_paths=(
        # System Maven
        "$(which mvn 2>/dev/null)"
        # Homebrew Maven
        "/opt/homebrew/bin/mvn"
        "/usr/local/bin/mvn"
        # Common Linux locations
        "/usr/share/maven/bin/mvn"
        "/opt/maven/bin/mvn"
        # SDKMAN locations
        "$HOME/.sdkman/candidates/maven/current/bin/mvn"
    )
    
    for mvn_path in "${maven_paths[@]}"; do
        if [[ -x "$mvn_path" ]]; then
            # Get Maven home from mvn -version
            local maven_home=$(cd "$(dirname "$mvn_path")/.." && pwd)
            if [[ -d "$maven_home/lib" ]]; then
                echo "$maven_home"
                return 0
            fi
        fi
    done
    
    return 1
}

# Try to find Java 21+
echo -e "${YELLOW}🔍 Searching for Java 21+ installation...${NC}"
if FOUND_JAVA_HOME=$(find_java_21); then
    echo -e "${GREEN}✅ Found Java 21+ at: $FOUND_JAVA_HOME${NC}"
    CLMS_JAVA_HOME="$FOUND_JAVA_HOME"
else
    echo -e "${YELLOW}⚠️  Java 21+ not found locally${NC}"
    CLMS_JAVA_HOME=""
fi

# Try to find Maven
echo -e "${YELLOW}🔍 Searching for Maven installation...${NC}"
if FOUND_MAVEN_HOME=$(find_maven); then
    echo -e "${GREEN}✅ Found Maven at: $FOUND_MAVEN_HOME${NC}"
    CLMS_MAVEN_HOME="$FOUND_MAVEN_HOME"
else
    echo -e "${YELLOW}⚠️  Maven not found locally${NC}"
    CLMS_MAVEN_HOME=""
fi

# Write environment variables to file
cat > "$ENV_FILE" << EOF
# CLI LLM Management System Environment
# Generated on $(date)

# Java 21+ home (empty if using Docker)
export CLMS_JAVA_HOME="$CLMS_JAVA_HOME"

# Maven home (empty if using Docker)
export CLMS_MAVEN_HOME="$CLMS_MAVEN_HOME"

# Docker mode flag
export CLMS_USE_DOCKER="false"
EOF

# If either Java 21 or Maven is missing, set up Docker container
if [[ -z "$CLMS_JAVA_HOME" ]] || [[ -z "$CLMS_MAVEN_HOME" ]]; then
    echo ""
    echo -e "${YELLOW}📦 Setting up Docker container with Java 21 and Maven...${NC}"
    
    # Update environment file for Docker mode
    cat > "$ENV_FILE" << EOF
# CLI LLM Management System Environment
# Generated on $(date)

# Using Docker for Java and Maven
export CLMS_USE_DOCKER="true"
EOF
    
    # Create Docker directory if it doesn't exist
    mkdir -p "$SCRIPT_DIR/docker/tools"
    
    # Create Dockerfile for build environment
    cat > "$SCRIPT_DIR/docker/tools/Dockerfile.build" << 'DOCKERFILE'
FROM maven:3.9.11-eclipse-temurin-21

# Install additional tools
RUN apt-get update && apt-get install -y \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /workspace

# Create volume mount points
VOLUME ["/workspace", "/root/.m2"]

# Default command
CMD ["bash"]
DOCKERFILE
    
    # Create docker-compose for build environment
    cat > "$SCRIPT_DIR/docker/tools/docker-compose.build.yml" << 'COMPOSE'
services:
  build-env:
    build:
      context: .
      dockerfile: Dockerfile.build
    image: clms-build-env:latest
    container_name: clms_build_env
    volumes:
      - ../../:/workspace
      - maven-cache:/root/.m2
    network_mode: host
    stdin_open: true
    tty: true
    environment:
      - PSQL_CLI_MANAGER_PORT=${PSQL_CLI_MANAGER_PORT:-9998}
      - PSQL_CLI_MANAGER_DB=${PSQL_CLI_MANAGER_DB:-cc_admin}
      - PSQL_CLI_MANAGER_USER=${PSQL_CLI_MANAGER_USER:-cc_admin}
      - PSQL_CLI_MANAGER_PASSWORD=${PSQL_CLI_MANAGER_PASSWORD:-cc_admin}

volumes:
  maven-cache:
    name: clms_maven_cache
COMPOSE
    
    # Build the Docker image
    echo -e "${YELLOW}🔨 Building Docker image...${NC}"
    if docker compose -f "$SCRIPT_DIR/docker/tools/docker-compose.build.yml" build; then
        echo -e "${GREEN}✅ Docker build environment ready${NC}"
    else
        echo -e "${RED}❌ Failed to build Docker environment${NC}"
        exit 1
    fi
fi

echo ""
echo -e "${GREEN}✅ Environment setup complete!${NC}"
echo ""
echo -e "${BLUE}📋 Configuration saved to: $ENV_FILE${NC}"

if [[ "$CLMS_USE_DOCKER" == "true" ]]; then
    echo -e "${YELLOW}📦 Using Docker container for Java 21 and Maven${NC}"
    echo "   Container will be used automatically by run_clms script"
else
    echo -e "${GREEN}✅ Using local installations:${NC}"
    echo "   Java: $CLMS_JAVA_HOME"
    echo "   Maven: $CLMS_MAVEN_HOME"
fi

echo ""
echo -e "${BLUE}💡 Next step: Run './run_clms' to start the application${NC}"