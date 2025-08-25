#!/usr/bin/env bash
# One-time helper to create ops/env from the example and export it in current shell.

set -euo pipefail

if [ ! -f ops/env ]; then
  cat > ops/env <<'EOF'
# Database
DB_URL=jdbc:postgresql://localhost:57542/smcccms
DB_USER=smcccms_admin
DB_PASS=SMCCCMSdemo

# External user provider (adjust if different)
USER_PROVIDER_BASE_URL=http://localhost:18080
USER_PROVIDER_USERNAME=case-manager
USER_PROVIDER_PASSWORD=1966ItsComingHomeThisYear

# AI
ANTHROPIC_API_KEY="your_anthropic_api_key_here"

# Server
SERVER_PORT=18081
SESSION_SECRET=change_me_super_secret
EOF
  echo "Created ops/env"
else
  echo "ops/env already exists"
fi

echo "Exporting env into current shell..."
# shellcheck disable=SC1091
set -a; source ops/env; set +a
echo "Done."
