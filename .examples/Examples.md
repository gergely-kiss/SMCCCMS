2) Labels & Milestones (paste into GitHub or let Claude create)

Labels

feature (00b8d9)

backend (1f77b4)

frontend (ff7f0e)

db (2ca02c)

ai (9467bd)

infra (7f7f7f)

docs (8c564b)

ux (e377c2)

tech-debt (bcbd22)

bug (d62728)

blocked (000000)

good-first-issue (17becf)

Milestones

M0 Scaffold & Ops — repo, DB compose, runner, wrappers, FE/BE skeletons

M1 Auth & Identity — 3-screen login, sessions, seeding 56 users

M2 Case Basics — creation, assignment, review, defence

M3 Hearing & Scoring — wall, finish flags, scoring, outcome

M4 AI Helpers — rewrite, amount suggestion, decision support

M5 Polish — UX, notifications, docs, metrics

3) Snippets to include in issues (copy-ready)
   ops/docker-compose.db.yml
   services:
   postgres:
   image: postgres:16
   container_name: smcccms_db
   environment:
   POSTGRES_DB: smcccms
   POSTGRES_USER: pg_admin
   POSTGRES_PASSWORD: SMCCCMSdemo
   ports:
    - "57542:5432"   # host:container
      healthcheck:
      test: ["CMD-SHELL", "pg_isready -U pg_admin -d smcccms"]
      interval: 10s
      timeout: 5s
      retries: 10
      volumes:
    - smcccms_data:/var/lib/postgresql/data
      volumes:
      smcccms_data:
      driver: local

ops/runner.sh
#!/usr/bin/env bash
set -euo pipefail

# Load env (create ops/env from ops/env.example)
if [ -f ops/env ]; then source ops/env; fi

echo "▶ Starting Postgres on 57542…"
docker compose -f ops/docker-compose.db.yml up -d

echo "▶ Waiting for DB…"
until docker exec smcccms_db pg_isready -U pg_admin -d smcccms >/dev/null 2>&1; do
sleep 1
done

echo "▶ Backend dev"
( cd smcccms_be && ./mvnw -q spring-boot:run )

# In a separate shell, run FE:
# ( cd smcccms_fe && npm ci && npm run dev )

Flyway baseline (excerpt) — smcccms_be/src/main/resources/db/migration/V1__init.sql
CREATE TABLE users (
id BIGSERIAL PRIMARY KEY,
provider_user_id VARCHAR(64) UNIQUE NOT NULL,
gov_id VARCHAR(32) UNIQUE NOT NULL,
first_name VARCHAR(80),
last_name VARCHAR(80),
created_at TIMESTAMP NOT NULL DEFAULT NOW(),
updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE TABLE roles (
id BIGSERIAL PRIMARY KEY,
code VARCHAR(16) UNIQUE NOT NULL,
name VARCHAR(64) NOT NULL
);
CREATE TABLE user_roles (
user_id BIGINT NOT NULL REFERENCES users(id),
role_id BIGINT NOT NULL REFERENCES roles(id),
UNIQUE(user_id, role_id)
);
CREATE TABLE login_codes (
id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL REFERENCES users(id),
code CHAR(6) NOT NULL,
expires_at TIMESTAMP NOT NULL,
consumed_at TIMESTAMP
);
CREATE TABLE cases (
id BIGSERIAL PRIMARY KEY,
reference VARCHAR(32) UNIQUE NOT NULL,
status VARCHAR(16) NOT NULL,
claim_text TEXT NOT NULL,
claim_amount_suggested NUMERIC(12,2),
created_by BIGINT REFERENCES users(id),
created_at TIMESTAMP NOT NULL DEFAULT NOW(),
updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE TABLE case_participants (
case_id BIGINT NOT NULL REFERENCES cases(id),
user_id BIGINT NOT NULL REFERENCES users(id),
case_role VARCHAR(32) NOT NULL,
UNIQUE(case_id, case_role)
);
CREATE TABLE hearing_messages (
id BIGSERIAL PRIMARY KEY,
case_id BIGINT NOT NULL REFERENCES cases(id),
author_user_id BIGINT REFERENCES users(id),
side VARCHAR(16) NOT NULL, -- CLAIMANT|DEFENDANT
text TEXT NOT NULL,
score_delta SMALLINT,
created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

Backend POM (top lines to pin like your reference)

Use your earlier pinned style (Boot 3.2.12, Java 21, Flyway, Postgres, SpringDoc, Surefire, Jacoco) and add Maven Wrapper so devs don’t change local versions.
