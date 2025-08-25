# SMCCCMS — Architecture (demo)

- **Goal**: 48-hour demo of a UK small-claims CMS flow with GOV.UK-style login, role dashboards, case lifecycle, hearing wall, and AI helpers.
- **Services**:
    - **Backend** (`smcccms_be`): Spring Boot 3.2.12, Java 21, JDBC, Flyway, SpringDoc.
    - **Database**: Postgres 16 in Docker only (host port 57542).
    - **Frontend**: Node/TS GOV.UK-styled app (added in later milestone).
- **Identity**:
    - Step 1: Gov ID verification against external provider; upsert local user & roles.
    - Step 2: Contact → 6-digit code (24h) → session cookie.
- **Roles**: RES, SOL, CWS, JDG. Per-case roles: CLAIMANT, DEFENDANT, JUDGE, CASEWORKER (+ optional solicitors).
- **Case states**: DRAFT → SUBMITTED → REVIEW → DEFENCE → HEARING → DECISION → CLOSED.
- **AI**: Backend proxies to Claude 3 Haiku for rewrite, amount suggestion, and decision support.
