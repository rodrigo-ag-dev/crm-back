CREATE SCHEMA IF NOT EXISTS crm;

CREATE TABLE IF NOT EXISTS crm."user" (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE crm."user"
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);

UPDATE crm."user"
SET full_name = username
WHERE full_name IS NULL;

ALTER TABLE crm."user"
    ALTER COLUMN full_name SET NOT NULL;

ALTER TABLE crm."user"
    ADD COLUMN IF NOT EXISTS role VARCHAR(20);

UPDATE crm."user"
SET role = 'USER'
WHERE role IS NULL;

ALTER TABLE crm."user"
    ALTER COLUMN role SET DEFAULT 'USER';

ALTER TABLE crm."user"
    ALTER COLUMN role SET NOT NULL;

ALTER TABLE crm."user"
    ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN;

UPDATE crm."user"
SET must_change_password = FALSE
WHERE must_change_password IS NULL;

ALTER TABLE crm."user"
    ALTER COLUMN must_change_password SET DEFAULT FALSE;

ALTER TABLE crm."user"
    ALTER COLUMN must_change_password SET NOT NULL;

CREATE TABLE IF NOT EXISTS crm.parameter (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    value VARCHAR(255),
    is_user_specific BOOLEAN NOT NULL DEFAULT false,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.parameter_user (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    parameter_id VARCHAR(36) NOT NULL,
    value VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES crm."user"(id) ON DELETE CASCADE,
    FOREIGN KEY (parameter_id) REFERENCES crm.parameter(id) ON DELETE CASCADE,
    UNIQUE (user_id, parameter_id)
);

CREATE TABLE IF NOT EXISTS crm.log (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) REFERENCES crm."user"(id) ON DELETE SET NULL,
    value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.company (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    alias VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    description TEXT,
    id_regional VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.contact (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    alias VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.stage (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    color VARCHAR(36),
    "order" INTEGER NOT NULL DEFAULT 0,
    days_sla INTEGER DEFAULT 0 NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO crm.stage (id, name, description, color, updated_at, created_at, "order")
SELECT '8a459b9c-54b3-4833-8831-55b791499bbf', 'Prospecção', 'Pesquisar leads, usar redes sociais, cold calls', 'ff05a4', '2026-05-19 09:37:26.732', '2026-05-19 09:37:26.732', 0
WHERE NOT EXISTS (SELECT 1 FROM crm.stage WHERE id = '8a459b9c-54b3-4833-8831-55b791499bbf');

INSERT INTO crm.stage (id, name, description, color, updated_at, created_at, "order")
SELECT 'b3f2c9d1-1a4e-4f2b-9c8d-2e7a1f4b6cde', 'Qualificação', 'Perguntas sobre orçamento, necessidade, autoridade de decisão', 'ff50de', '2026-05-19 14:19:03.332', '2026-05-19 14:19:03.332', 1
WHERE NOT EXISTS (SELECT 1 FROM crm.stage WHERE id = 'b3f2c9d1-1a4e-4f2b-9c8d-2e7a1f4b6cde');

INSERT INTO crm.stage (id, name, description, color, updated_at, created_at, "order")
SELECT 'c4d8e7f2-2b5f-4a3c-8d9e-3f8b2a5d7ef0', 'Demonstração', 'Reuniões, demonstrações de produto, envio de materiais', 'ff50de', '2026-05-20 10:34:59.596', '2026-05-20 10:34:59.596', 2
WHERE NOT EXISTS (SELECT 1 FROM crm.stage WHERE id = 'c4d8e7f2-2b5f-4a3c-8d9e-3f8b2a5d7ef0');

INSERT INTO crm.stage (id, name, description, color, updated_at, created_at, "order")
SELECT 'd5e9f803-3c6a-4b4d-9eaf-4a9c3b6e8f01', 'Proposta', 'Envio de proposta comercial, detalhamento de preço e condições', 'ff50de', '2026-05-20 10:34:59.597', '2026-05-20 10:34:59.597', 3
WHERE NOT EXISTS (SELECT 1 FROM crm.stage WHERE id = 'd5e9f803-3c6a-4b4d-9eaf-4a9c3b6e8f01');

INSERT INTO crm.stage (id, name, description, color, updated_at, created_at, "order")
SELECT 'e6fa0b14-4d7b-4c5e-a0bf-5bad4c7f9012', 'Negociação', 'Descontos, condições de pagamento, ajustes contratuais', 'ff50de', '2026-05-20 10:34:59.597', '2026-05-20 10:34:59.597', 4
WHERE NOT EXISTS (SELECT 1 FROM crm.stage WHERE id = 'e6fa0b14-4d7b-4c5e-a0bf-5bad4c7f9012');

INSERT INTO crm.stage (id, name, description, color, updated_at, created_at, "order")
SELECT 'f71b1c25-5e8c-4d6f-b1cf-6cbe5d809123', 'Fechamento', 'Assinatura de contrato, emissão de pedido', 'ff50de', '2026-05-20 10:34:59.599', '2026-05-20 10:34:59.599', 5
WHERE NOT EXISTS (SELECT 1 FROM crm.stage WHERE id = 'f71b1c25-5e8c-4d6f-b1cf-6cbe5d809123');

INSERT INTO crm.stage (id, name, description, color, updated_at, created_at, "order")
SELECT '07c22d36-6f9d-4e80-c2df-7dcf6e910234', 'Pós-venda', 'Suporte, acompanhamento, upsell/cross-sell', 'ff50de', '2026-05-20 10:34:59.600', '2026-05-20 10:34:59.600', 6
WHERE NOT EXISTS (SELECT 1 FROM crm.stage WHERE id = '07c22d36-6f9d-4e80-c2df-7dcf6e910234');

CREATE TABLE IF NOT EXISTS crm.deal (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL REFERENCES crm.company(id) ON DELETE CASCADE,
    contact_id VARCHAR(36) NOT NULL REFERENCES crm.contact(id) ON DELETE CASCADE,
    owner_id VARCHAR(36) REFERENCES crm."user"(id) ON DELETE SET NULL,
    stage_id VARCHAR(36) REFERENCES crm.stage(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    probability INTEGER DEFAULT 0,
    amount DECIMAL(19,2) DEFAULT 0.00,
    close_date_expected TIMESTAMP,
    won BOOLEAN NOT NULL DEFAULT FALSE,
    lost BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.deal_stage_history (
    id VARCHAR(36) PRIMARY KEY,
    deal_id VARCHAR(36) REFERENCES crm.deal(id) ON DELETE CASCADE,
    from_stage_id VARCHAR(36) REFERENCES crm.stage(id) ON DELETE SET NULL,
    to_stage_id VARCHAR(36) REFERENCES crm.stage(id) ON DELETE SET NULL,
    changed_by_id VARCHAR(36) REFERENCES crm."user"(id) ON DELETE SET NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.deal_activity (
  id VARCHAR(36) PRIMARY KEY,
  deal_id VARCHAR(36) REFERENCES crm.deal(id) ON DELETE CASCADE,
  contact_id VARCHAR(36) REFERENCES crm.contact(id) ON DELETE SET NULL,
  company_id VARCHAR(36) REFERENCES crm.company(id) ON DELETE SET NULL,
  type VARCHAR(50),
  subject VARCHAR(255),
  due_date TIMESTAMP,
  owner_id VARCHAR(36) REFERENCES crm."user"(id) ON DELETE SET NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.activity_logs (
  id VARCHAR(36) PRIMARY KEY,
  activity_id VARCHAR(36) REFERENCES crm.deal_activity(id) ON DELETE CASCADE,
  action VARCHAR(50),
  notes TEXT,
  performed_by VARCHAR(36) REFERENCES crm."user"(id) ON DELETE SET NULL,
  performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.audit_logs (
  id VARCHAR(36) PRIMARY KEY,
  entity_type VARCHAR(50) NOT NULL,
  entity_id VARCHAR(36) NOT NULL,
  action VARCHAR(50) NOT NULL,
  changed_by VARCHAR(36) REFERENCES crm."user"(id) ON DELETE SET NULL,
  changes JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.ticket_stage (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    color VARCHAR(36),
    "order" INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO crm.ticket_stage (id, name, description, color, updated_at, created_at, "order")
SELECT '11111111-1111-1111-1111-111111111111', 'Open', 'Ticket is open and waiting to be handled', 'ffb703', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM crm.ticket_stage WHERE id = '11111111-1111-1111-1111-111111111111');

INSERT INTO crm.ticket_stage (id, name, description, color, updated_at, created_at, "order")
SELECT '22222222-2222-2222-2222-222222222222', 'In Progress', 'Ticket is currently being worked on', '219ebc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1
WHERE NOT EXISTS (SELECT 1 FROM crm.ticket_stage WHERE id = '22222222-2222-2222-2222-222222222222');

INSERT INTO crm.ticket_stage (id, name, description, color, updated_at, created_at, "order")
SELECT '33333333-3333-3333-3333-333333333333', 'Closed', 'Ticket has been completed and closed', '2a9d8f', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2
WHERE NOT EXISTS (SELECT 1 FROM crm.ticket_stage WHERE id = '33333333-3333-3333-3333-333333333333');

CREATE TABLE IF NOT EXISTS crm.ticket (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL REFERENCES crm.company(id) ON DELETE CASCADE,
    contact_id VARCHAR(36) NOT NULL REFERENCES crm.contact(id) ON DELETE CASCADE,
    owner_id VARCHAR(36) REFERENCES crm."user"(id) ON DELETE SET NULL,
    ticket_stage_id VARCHAR(36) REFERENCES crm.ticket_stage(id) ON DELETE SET NULL,
    canceled_stage_id VARCHAR(36) REFERENCES crm.ticket_stage(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date TIMESTAMP,
    closed_at TIMESTAMP,
    canceled_at TIMESTAMP,
    is_canceled BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.ticket_stage_history (
    id VARCHAR(36) PRIMARY KEY,
    ticket_id VARCHAR(36) REFERENCES crm.ticket(id) ON DELETE CASCADE,
    from_stage_id VARCHAR(36) REFERENCES crm.ticket_stage(id) ON DELETE SET NULL,
    to_stage_id VARCHAR(36) REFERENCES crm.ticket_stage(id) ON DELETE SET NULL,
    changed_by_id VARCHAR(36) REFERENCES crm."user"(id) ON DELETE SET NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm.ticket_comment (
    id VARCHAR(36) PRIMARY KEY,
    ticket_id VARCHAR(36) NOT NULL REFERENCES crm.ticket(id) ON DELETE CASCADE,
    author_id VARCHAR(36) REFERENCES crm."user"(id) ON DELETE SET NULL,
    type VARCHAR(30) NOT NULL,
    body TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
