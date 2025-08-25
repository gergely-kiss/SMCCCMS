# SMCCCMS — Architecture (demo)

- **Goal**: 48-hour demo of a UK small-claims CMS flow with GOV.UK-style login, role dashboards, case lifecycle, hearing wall, and AI helpers.
- **Services**:
    - **Backend** (`smcccms_be`): Spring Boot 3.2.12, Java 21, JDBC, Flyway, SpringDoc.
    - **Database**: Postgres 16 in Docker only (host port 57542).
    - **Frontend**: Node/TS GOV.UK-styled app (added in later milestone).
- **Identity**:
    - Step 1: Gov ID verification against external provider; upsert local user & roles.
    - Step 2: Contact → 6-digit code (24h) → session cookie.

## Auth Flow

The authentication system follows a 3-step process:

### 1. Government ID Verification (`POST /api/auth/verify-id`)
- Accepts Gov ID in format `ID-UK-xxx` (e.g., `ID-UK-001`)
- Calls external USER_PROVIDER service with Basic auth
- Falls back to demo data when provider unavailable
- Upserts user record and syncs system roles (RES/SOL/CWS/JDG)
- Returns normalized user object with roles

### 2. Verification Code Request (`POST /api/auth/request-code`)  
- Accepts any contact string (demo mode)
- Generates secure 6-digit numeric code with 24-hour TTL
- Stores in `login_codes` table with expiry timestamp
- Returns code directly in response (demo shortcut for testing)

### 3. Code Verification (`POST /api/auth/verify-code`)
- Validates unused, unexpired 6-digit code
- Creates signed session cookie (`SMCCCMS_SESSION`)
- Marks code as consumed in database
- Returns user object with roles for frontend

### Session Management
- HTTP-only cookies with signed session IDs
- In-memory session storage for demo (not production-ready)
- 24-hour session lifetime
- Sessions include user ID for authorization

### Role Assignment
Demo users are assigned roles based on Gov ID number:
- **RES** (Resident): Default role for all users
- **SOL** (Solicitor): Every 7th user (007, 014, 021...)  
- **CWS** (Caseworker): Every 11th user (011, 022, 033...)
- **JDG** (Judge): Every 17th user (017, 034, 051...)
- **Roles**: RES, SOL, CWS, JDG. Per-case roles: CLAIMANT, DEFENDANT, JUDGE, CASEWORKER (+ optional solicitors).
- **Case states**: DRAFT → SUBMITTED → REVIEW → DEFENCE → HEARING → DECISION → CLOSED.
- **AI**: Backend proxies to Claude 3 Haiku for rewrite, amount suggestion, and decision support.
