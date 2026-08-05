-- 清理 V1 遗留租客表中的身份证字段。
ALTER TABLE tenant_profile
    DROP COLUMN id_number_masked;
