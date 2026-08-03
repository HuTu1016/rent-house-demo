CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    role VARCHAR(24) NOT NULL,
    mobile VARCHAR(32),
    password_hash VARCHAR(255),
    wechat_openid VARCHAR(128),
    nickname VARCHAR(64) NOT NULL,
    avatar_url VARCHAR(512),
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME(3),
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    deleted_at DATETIME(3),
    UNIQUE KEY uk_user_mobile (mobile),
    UNIQUE KEY uk_user_openid (wechat_openid),
    KEY idx_user_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE landlord_profile (
    user_id BIGINT PRIMARY KEY,
    company_name VARCHAR(128),
    verification_status VARCHAR(24) NOT NULL DEFAULT 'UNVERIFIED',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tenant_profile (
    user_id BIGINT PRIMARY KEY,
    real_name VARCHAR(64),
    id_number_masked VARCHAR(64),
    emergency_contact VARCHAR(64),
    emergency_mobile VARCHAR(32),
    blacklist_reason VARCHAR(255),
    blacklist_at DATETIME(3),
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE property_building (
    id BIGINT PRIMARY KEY,
    landlord_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(255) NOT NULL,
    longitude DECIMAL(10,7),
    latitude DECIMAL(10,7),
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    KEY idx_building_landlord (landlord_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE property_unit (
    id BIGINT PRIMARY KEY,
    building_id BIGINT NOT NULL,
    landlord_id BIGINT NOT NULL,
    unit_no VARCHAR(64) NOT NULL,
    title VARCHAR(128) NOT NULL,
    room_count TINYINT NOT NULL,
    hall_count TINYINT NOT NULL DEFAULT 0,
    bathroom_count TINYINT NOT NULL DEFAULT 1,
    area_sqm DECIMAL(8,2),
    floor_no SMALLINT,
    total_floor SMALLINT,
    orientation VARCHAR(32),
    occupancy_status VARCHAR(32) NOT NULL DEFAULT 'VACANT',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    UNIQUE KEY uk_unit_building_no (building_id, unit_no),
    KEY idx_unit_landlord_status (landlord_id, occupancy_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE house_listing (
    id BIGINT PRIMARY KEY,
    unit_id BIGINT NOT NULL,
    landlord_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    community_name VARCHAR(128) NOT NULL,
    district VARCHAR(64) NOT NULL,
    address VARCHAR(255) NOT NULL,
    rent_cent INT NOT NULL,
    deposit_cent INT NOT NULL DEFAULT 0,
    payment_cycle VARCHAR(24) NOT NULL DEFAULT 'MONTHLY',
    tags_json JSON,
    facilities_json JSON,
    description TEXT,
    publish_status VARCHAR(24) NOT NULL DEFAULT 'DRAFT',
    is_special TINYINT(1) NOT NULL DEFAULT 0,
    special_sort INT NOT NULL DEFAULT 0,
    published_at DATETIME(3),
    offline_at DATETIME(3),
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    KEY idx_listing_public (publish_status, is_special, published_at),
    KEY idx_listing_landlord (landlord_id, publish_status),
    KEY idx_listing_unit (unit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE listing_media (
    id BIGINT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    media_type VARCHAR(16) NOT NULL,
    url VARCHAR(512) NOT NULL,
    cover_url VARCHAR(512),
    sort_no INT NOT NULL DEFAULT 0,
    duration_seconds INT,
    created_at DATETIME(3) NOT NULL,
    KEY idx_media_listing_sort (listing_id, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tenant_favorite (
    tenant_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL,
    PRIMARY KEY (tenant_id, listing_id),
    KEY idx_favorite_listing (listing_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tenant_browse_history (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL,
    viewed_at DATETIME(3) NOT NULL,
    UNIQUE KEY uk_history_tenant_listing (tenant_id, listing_id),
    KEY idx_history_tenant_time (tenant_id, viewed_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE conversation (
    id BIGINT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    landlord_id BIGINT NOT NULL,
    last_message_preview VARCHAR(255),
    last_message_at DATETIME(3),
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    UNIQUE KEY uk_conversation (listing_id, tenant_id, landlord_id),
    KEY idx_conversation_tenant (tenant_id, last_message_at DESC),
    KEY idx_conversation_landlord (landlord_id, last_message_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_type VARCHAR(24) NOT NULL,
    content TEXT,
    appointment_id BIGINT,
    read_at DATETIME(3),
    created_at DATETIME(3) NOT NULL,
    KEY idx_message_conversation (conversation_id, id),
    KEY idx_message_sender (sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE appointment (
    id BIGINT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    landlord_id BIGINT NOT NULL,
    conversation_id BIGINT,
    scheduled_at DATETIME(3) NOT NULL,
    contact_name VARCHAR(64) NOT NULL,
    contact_mobile VARCHAR(32) NOT NULL,
    note VARCHAR(500),
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    reject_reason VARCHAR(255),
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    KEY idx_appointment_tenant (tenant_id, scheduled_at DESC),
    KEY idx_appointment_landlord (landlord_id, status, scheduled_at),
    KEY idx_appointment_listing (listing_id, scheduled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tenancy_contract (
    id BIGINT PRIMARY KEY,
    contract_no VARCHAR(40) NOT NULL,
    listing_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    landlord_id BIGINT NOT NULL,
    appointment_id BIGINT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    rent_cent INT NOT NULL,
    deposit_cent INT NOT NULL,
    payment_day TINYINT NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    draft_expire_at DATETIME(3),
    signed_at DATETIME(3),
    checkout_apply_at DATETIME(3),
    terminated_at DATETIME(3),
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    UNIQUE KEY uk_contract_no (contract_no),
    KEY idx_contract_tenant (tenant_id, status),
    KEY idx_contract_landlord (landlord_id, status),
    KEY idx_contract_unit (unit_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bill (
    id BIGINT PRIMARY KEY,
    bill_no VARCHAR(40) NOT NULL,
    contract_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    landlord_id BIGINT NOT NULL,
    bill_type VARCHAR(24) NOT NULL,
    period_start DATE,
    period_end DATE,
    amount_cent INT NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    paid_at DATETIME(3),
    verified_at DATETIME(3),
    verifier_id BIGINT,
    remark VARCHAR(500),
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    UNIQUE KEY uk_bill_no (bill_no),
    KEY idx_bill_tenant (tenant_id, status, due_date),
    KEY idx_bill_landlord (landlord_id, status, due_date),
    KEY idx_bill_contract (contract_id, due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bill_payment_report (
    id BIGINT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    voucher_url VARCHAR(512),
    paid_amount_cent INT NOT NULL,
    paid_at DATETIME(3) NOT NULL,
    note VARCHAR(500),
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    KEY idx_payment_report_bill (bill_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE repair_ticket (
    id BIGINT PRIMARY KEY,
    ticket_no VARCHAR(40) NOT NULL,
    contract_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    landlord_id BIGINT NOT NULL,
    category VARCHAR(32) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    images_json JSON,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    assignee_name VARCHAR(64),
    assignee_mobile VARCHAR(32),
    handling_note VARCHAR(1000),
    completed_at DATETIME(3),
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    UNIQUE KEY uk_ticket_no (ticket_no),
    KEY idx_repair_tenant (tenant_id, status, created_at DESC),
    KEY idx_repair_landlord (landlord_id, status, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE review (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    landlord_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    content VARCHAR(1000),
    reply_content VARCHAR(1000),
    replied_at DATETIME(3),
    created_at DATETIME(3) NOT NULL,
    UNIQUE KEY uk_review_contract (contract_id),
    KEY idx_review_landlord (landlord_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE system_notification (
    id BIGINT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    notification_type VARCHAR(32) NOT NULL,
    title VARCHAR(128) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    related_type VARCHAR(32),
    related_id BIGINT,
    read_at DATETIME(3),
    created_at DATETIME(3) NOT NULL,
    KEY idx_notification_recipient (recipient_id, read_at, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY,
    actor_id BIGINT,
    actor_role VARCHAR(24),
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64),
    target_id BIGINT,
    detail_json JSON,
    ip VARCHAR(64),
    created_at DATETIME(3) NOT NULL,
    KEY idx_audit_target (target_type, target_id, created_at DESC),
    KEY idx_audit_actor (actor_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
