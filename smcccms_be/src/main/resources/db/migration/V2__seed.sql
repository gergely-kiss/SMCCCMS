-- Roles
INSERT INTO roles(code,name) VALUES
                                 ('RES','Resident'),
                                 ('SOL','Solicitor'),
                                 ('CWS','Caseworker'),
                                 ('JDG','Judge')
ON CONFLICT (code) DO NOTHING;

-- Users 1..56 with default names; provider ids are synthetic for demo
INSERT INTO users(provider_user_id, gov_id, first_name, last_name)
SELECT 'provider-'||i, 'ID-UK-'||LPAD(i::text,3,'0'), 'User'||i, 'Demo'
FROM generate_series(1,56) s(i)
ON CONFLICT (gov_id) DO NOTHING;

-- Attach most as RES; sprinkle SOL/CWS/JDG for realism
WITH u AS (
    SELECT id, gov_id,
           ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM users
)
INSERT INTO user_roles(user_id, role_id)
SELECT u.id, r.id
FROM u
         JOIN roles r ON r.code = CASE
                                      WHEN u.rn % 17 = 0 THEN 'JDG'
                                      WHEN u.rn % 11 = 0 THEN 'CWS'
                                      WHEN u.rn % 7  = 0 THEN 'SOL'
                                      ELSE 'RES'
    END
ON CONFLICT DO NOTHING;
