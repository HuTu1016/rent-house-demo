-- 业务边界收敛：删除不属于二手中介房源/看房业务的运营表。
DROP TABLE IF EXISTS community_join_event;
DROP TABLE IF EXISTS community_config;
DROP TABLE IF EXISTS system_notification;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS landlord_profile;

ALTER TABLE property_building CHANGE COLUMN landlord_id agent_id BIGINT NOT NULL;
ALTER TABLE property_unit CHANGE COLUMN landlord_id agent_id BIGINT NOT NULL;
ALTER TABLE house_listing CHANGE COLUMN landlord_id agent_id BIGINT NOT NULL;
ALTER TABLE conversation CHANGE COLUMN landlord_id agent_id BIGINT NOT NULL;
ALTER TABLE appointment CHANGE COLUMN landlord_id agent_id BIGINT NOT NULL;

-- 历史迁移使用 landlord_id，本版本统一重命名为 agent_id；应用层不再暴露 landlord 命名。
ALTER TABLE sys_user MODIFY COLUMN role VARCHAR(24) NOT NULL;

CREATE TABLE IF NOT EXISTS tenant_identity_profile (
    user_id BIGINT PRIMARY KEY,
    real_name VARCHAR(64) NOT NULL,
    id_number_cipher VARCHAR(512),
    id_number_masked VARCHAR(32),
    mobile_cipher VARCHAR(512),
    mobile_hash CHAR(64),
    home_address VARCHAR(255),
    company_name VARCHAR(128),
    company_address VARCHAR(255),
    emergency_contact VARCHAR(64),
    emergency_mobile_cipher VARCHAR(512),
    profile_status VARCHAR(24) NOT NULL DEFAULT 'INCOMPLETE',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    KEY idx_tenant_identity_mobile_hash (mobile_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
