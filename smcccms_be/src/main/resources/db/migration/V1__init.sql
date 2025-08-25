-- Relational tables only (no JSON/JSONB)

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
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            UNIQUE(user_id, role_id)
);

CREATE TABLE login_codes (
                             id BIGSERIAL PRIMARY KEY,
                             user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
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
                                   case_id BIGINT NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
                                   user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   case_role VARCHAR(32) NOT NULL,
                                   UNIQUE(case_id, case_role)
);

CREATE TABLE hearing_messages (
                                  id BIGSERIAL PRIMARY KEY,
                                  case_id BIGINT NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
                                  author_user_id BIGINT REFERENCES users(id),
                                  side VARCHAR(16) NOT NULL, -- CLAIMANT|DEFENDANT
                                  text TEXT NOT NULL,
                                  score_delta SMALLINT,
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
