# Auth flow (cURL demo)

## 1) Verify Gov ID (upserts user/roles)
```bash
curl -X POST http://localhost:8080/api/auth/verify-id \
  -H 'Content-Type: application/json' \
  -d '{"govId":"ID-UK-001"}'
```

**Expected Response:**
```json
{
  "id": 1,
  "govId": "ID-UK-001",
  "firstName": "User1",
  "lastName": "Demo",
  "roles": ["RES"]
}
```

## 2) Request login code (returns code in response for demo)
```bash
curl -X POST http://localhost:8080/api/auth/request-code \
  -H 'Content-Type: application/json' \
  -d '{"contact":"test@example.com"}'
```

**Expected Response:**
```json
{
  "code": "123456"
}
```

## 3) Verify code (establishes session cookie)
```bash
curl -i -X POST http://localhost:8080/api/auth/verify-code \
  -H 'Content-Type: application/json' \
  -d '{"code":"123456"}' \
  -c cookies.txt
```

**Expected Response:**
```json
{
  "id": 1,
  "govId": "ID-UK-001", 
  "firstName": "User1",
  "lastName": "Demo",
  "roles": ["RES"]
}
```

**Note:** Session cookie `SMCCCMS_SESSION` will be saved to `cookies.txt`

## Complete Flow Example

```bash
# Step 1: Verify ID
RESPONSE1=$(curl -s -X POST http://localhost:8080/api/auth/verify-id \
  -H 'Content-Type: application/json' \
  -d '{"govId":"ID-UK-001"}')

echo "Step 1 - Verify ID: $RESPONSE1"

# Step 2: Request code  
RESPONSE2=$(curl -s -X POST http://localhost:8080/api/auth/request-code \
  -H 'Content-Type: application/json' \
  -d '{"contact":"test@example.com"}')

echo "Step 2 - Request Code: $RESPONSE2"

# Step 3: Extract code and verify
CODE=$(echo "$RESPONSE2" | jq -r '.code')
RESPONSE3=$(curl -s -X POST http://localhost:8080/api/auth/verify-code \
  -H 'Content-Type: application/json' \
  -d "{\"code\":\"$CODE\"}" \
  -c cookies.txt)

echo "Step 3 - Verify Code: $RESPONSE3"
echo "Session cookie saved to cookies.txt"
```

## Different User Roles

Test with different Gov IDs to see different roles:

```bash
# Regular user (RES)
curl -X POST http://localhost:8080/api/auth/verify-id \
  -H 'Content-Type: application/json' \
  -d '{"govId":"ID-UK-001"}'

# Solicitor (SOL) - every 7th user
curl -X POST http://localhost:8080/api/auth/verify-id \
  -H 'Content-Type: application/json' \
  -d '{"govId":"ID-UK-007"}'

# Caseworker (CWS) - every 11th user  
curl -X POST http://localhost:8080/api/auth/verify-id \
  -H 'Content-Type: application/json' \
  -d '{"govId":"ID-UK-011"}'

# Judge (JDG) - every 17th user
curl -X POST http://localhost:8080/api/auth/verify-id \
  -H 'Content-Type: application/json' \
  -d '{"govId":"ID-UK-017"}'
```
