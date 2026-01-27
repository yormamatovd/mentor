#!/bin/bash

################################################################################
# Main Build Orchestrator
# Builds macOS installers locally and triggers Windows builds via GitHub Actions
################################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo "========================================"
    echo "$1"
    echo "========================================"
}

# Check if running on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    log_error "This script must be run on macOS"
    exit 1
fi

cd "$PROJECT_ROOT"

# Build macOS installers
print_header "Building macOS Installers"
log_info "Running build-mac.sh..."
bash "$SCRIPT_DIR/build-mac.sh"

if [ $? -eq 0 ]; then
    log_info "macOS build completed successfully"
else
    log_error "macOS build failed"
    exit 1
fi

# Trigger Windows build via GitHub Actions
print_header "Triggering Windows Build"

read -p "Trigger Windows build via GitHub Actions? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    log_info "Checking for gh CLI..."
    
    if ! command -v gh &> /dev/null; then
        log_warn "GitHub CLI (gh) not found"
        log_info "Install with: brew install gh"
        log_info "Alternatively, push to GitHub and trigger workflow manually"
        exit 0
    fi
    
    log_info "Checking authentication..."
    if ! gh auth status &> /dev/null; then
        log_warn "Not authenticated with GitHub"
        log_info "Run: gh auth login"
        exit 0
    fi
    
    log_info "Triggering workflow..."
    gh workflow run windows-build.yml
    
    if [ $? -eq 0 ]; then
        log_info "Workflow triggered successfully"
        log_info "Monitor progress: gh run list --workflow=windows-build.yml"
        log_info "Or visit: https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/actions"
    else
        log_error "Failed to trigger workflow"
        exit 1
    fi
else
    log_info "Skipping Windows build"
    log_info "To build manually:"
    log_info "  1. Push changes to GitHub"
    log_info "  2. Go to Actions tab"
    log_info "  3. Run 'Windows Build' workflow"
fi

print_header "Build Summary"
log_info "macOS installers: $PROJECT_ROOT/target/installer/"
log_info "Windows installers will be available as GitHub Actions artifacts"

echo ""
log_info "Build process complete!"
