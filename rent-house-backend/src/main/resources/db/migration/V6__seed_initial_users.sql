INSERT INTO sys_user (id, role, mobile, password_hash, nickname, status, created_at, updated_at) VALUES
(1001, 'AGENT', '13800000001', '$2b$12$xKwW4FU/13vPuOhFkSrRBOC2PhSkhXkuJX3DK.9CXGDB/VAY9nYa.', '中介小王', 'ACTIVE', NOW(3), NOW(3)),
(1002, 'TENANT', '13800000002', '$2b$12$xKwW4FU/13vPuOhFkSrRBOC2PhSkhXkuJX3DK.9CXGDB/VAY9nYa.', '测试租客', 'ACTIVE', NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE updated_at = NOW(3);
