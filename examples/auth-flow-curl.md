# Auth flow (cURL demo)

# 1) Verify Gov ID (upserts user/roles)
curl -sS -X POST http://localhost:18081/auth/verify-id \
-H 'Content-Type: application/json' \
-d '{"govId":"ID-UK-001"}'

# 2) Request login code (returns code in response for demo)
curl -sS -X POST http://localhost:18081/auth/request-code \
-H 'Content-Type: application/json' \
-d '{"contact":"anything@demo"}'

# 3) Verify code (establishes session cookie)
curl -i -sS -X POST http://localhost:18081/auth/verify-code \
-H 'Content-Type: application/json' \
-d '{"code":"123456"}'
