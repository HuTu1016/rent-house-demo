/*
 Navicat Premium Dump SQL

 Source Server         : rent_house
 Source Server Type    : MySQL
 Source Server Version : 80411 (8.4.11)
 Source Host           : localhost:3306
 Source Schema         : rent_house

 Target Server Type    : MySQL
 Target Server Version : 80411 (8.4.11)
 File Encoding         : 65001

 Date: 05/08/2026 17:12:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for appointment
-- ----------------------------
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment`  (
  `id` bigint NOT NULL,
  `listing_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `agent_id` bigint NOT NULL,
  `conversation_id` bigint NULL DEFAULT NULL,
  `scheduled_at` datetime(3) NOT NULL,
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `contact_mobile` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_appointment_tenant`(`tenant_id` ASC, `scheduled_at` DESC) USING BTREE,
  INDEX `idx_appointment_landlord`(`agent_id` ASC, `status` ASC, `scheduled_at` ASC) USING BTREE,
  INDEX `idx_appointment_listing`(`listing_id` ASC, `scheduled_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of appointment
-- ----------------------------

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL,
  `conversation_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  `message_type` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `appointment_id` bigint NULL DEFAULT NULL,
  `read_at` datetime(3) NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_conversation`(`conversation_id` ASC, `id` ASC) USING BTREE,
  INDEX `idx_message_sender`(`sender_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (343294217289928704, 343294203842990080, 1002, 'TEXT', '111', NULL, NULL, '2026-08-05 15:28:39.501');
INSERT INTO `chat_message` VALUES (343294240291491840, 343294203842990080, 1002, 'TEXT', '你好', NULL, NULL, '2026-08-05 15:28:44.985');

-- ----------------------------
-- Table structure for conversation
-- ----------------------------
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation`  (
  `id` bigint NOT NULL,
  `listing_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `agent_id` bigint NOT NULL,
  `last_message_preview` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `last_message_at` datetime(3) NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_conversation`(`listing_id` ASC, `tenant_id` ASC, `agent_id` ASC) USING BTREE,
  INDEX `idx_conversation_tenant`(`tenant_id` ASC, `last_message_at` DESC) USING BTREE,
  INDEX `idx_conversation_landlord`(`agent_id` ASC, `last_message_at` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversation
-- ----------------------------
INSERT INTO `conversation` VALUES (343294203842990080, 4005, 1002, 1001, '你好', '2026-08-05 15:28:44.985', '2026-08-05 15:28:36.294', '2026-08-05 15:28:44.985');
INSERT INTO `conversation` VALUES (343294281701855232, 4009, 1002, 1001, NULL, NULL, '2026-08-05 15:28:54.858', '2026-08-05 15:28:54.858');
INSERT INTO `conversation` VALUES (343294574934036480, 4013, 1002, 1001, NULL, NULL, '2026-08-05 15:30:04.770', '2026-08-05 15:30:04.770');

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history`  (
  `installed_rank` int NOT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int NULL DEFAULT NULL,
  `installed_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`) USING BTREE,
  INDEX `flyway_schema_history_s_idx`(`success` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flyway_schema_history
-- ----------------------------
INSERT INTO `flyway_schema_history` VALUES (1, '1', 'init schema', 'SQL', 'V1__init_schema.sql', -1725913030, 'rent_house', '2026-08-05 03:28:32', 1378, 1);
INSERT INTO `flyway_schema_history` VALUES (2, '2', 'seed demo listing', 'SQL', 'V2__seed_demo_listing.sql', -146899257, 'rent_house', '2026-08-05 03:28:32', 89, 1);
INSERT INTO `flyway_schema_history` VALUES (3, '3', 'community', 'SQL', 'V3__community.sql', -860853852, 'rent_house', '2026-08-05 03:28:32', 137, 1);
INSERT INTO `flyway_schema_history` VALUES (4, '4', 'agent scope cleanup', 'SQL', 'V4__agent_scope_cleanup.sql', 525855342, 'rent_house', '2026-08-05 03:28:37', 4408, 1);
INSERT INTO `flyway_schema_history` VALUES (5, '5', 'remove legacy seed data', 'SQL', 'V5__remove_legacy_seed_data.sql', 1028743431, 'rent_house', '2026-08-05 03:28:37', 37, 1);
INSERT INTO `flyway_schema_history` VALUES (6, '6', 'seed initial users', 'SQL', 'V6__seed_initial_users.sql', -1623955910, 'rent_house', '2026-08-05 05:59:00', 24, 1);
INSERT INTO `flyway_schema_history` VALUES (7, '7', 'seed demo houses', 'SQL', 'V7__seed_demo_houses.sql', -75731061, 'rent_house', '2026-08-05 06:10:44', 61, 1);
INSERT INTO `flyway_schema_history` VALUES (8, '8', 'simplify tenant identity', 'SQL', 'V8__simplify_tenant_identity.sql', 2071048672, 'rent_house', '2026-08-05 07:57:33', 525, 1);
INSERT INTO `flyway_schema_history` VALUES (9, '9', 'remove legacy tenant id number', 'SQL', 'V9__remove_legacy_tenant_id_number.sql', 1992817478, 'rent_house', '2026-08-05 08:12:20', 698, 1);

-- ----------------------------
-- Table structure for house_listing
-- ----------------------------
DROP TABLE IF EXISTS `house_listing`;
CREATE TABLE `house_listing`  (
  `id` bigint NOT NULL,
  `unit_id` bigint NOT NULL,
  `agent_id` bigint NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `community_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `district` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `rent_cent` int NOT NULL,
  `deposit_cent` int NOT NULL DEFAULT 0,
  `payment_cycle` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MONTHLY',
  `tags_json` json NULL,
  `facilities_json` json NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `publish_status` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT',
  `is_special` tinyint(1) NOT NULL DEFAULT 0,
  `special_sort` int NOT NULL DEFAULT 0,
  `published_at` datetime(3) NULL DEFAULT NULL,
  `offline_at` datetime(3) NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_listing_public`(`publish_status` ASC, `is_special` ASC, `published_at` ASC) USING BTREE,
  INDEX `idx_listing_landlord`(`agent_id` ASC, `publish_status` ASC) USING BTREE,
  INDEX `idx_listing_unit`(`unit_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of house_listing
-- ----------------------------
INSERT INTO `house_listing` VALUES (4001, 3001, 1001, '340-水斗老围村电梯大两房一厅', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 290000, 290000, 'MONTHLY', '[\"精装修\", \"带阳台\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '现特价 2900包管理网络，民水民电，押一付一，家电齐全。', 'PUBLISHED', 1, 1, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4002, 3002, 1001, '60-水斗新围村电梯5楼单间', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 105000, 105000, 'MONTHLY', '[\"采光好\", \"短租\"]', '[\"空调\", \"洗衣机\"]', '1050包管理，光线透亮，采光好，配套齐全。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4003, 3003, 1001, '117-水斗新围村一房一厅', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 155000, 155000, 'MONTHLY', '[\"通风好\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '水斗创艺电商10楼一房一厅转租1550元，拎包入住。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4004, 3004, 1001, '108-水斗新围村7楼单间', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 86000, 86000, 'MONTHLY', '[\"采光好\", \"短租\"]', '[\"空调\"]', '860元楼栋单间，光线透亮，出入方便。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4005, 3005, 1001, '353-富豪新村电梯两房一厅', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 170000, 170000, 'MONTHLY', '[\"带阳台\", \"天然气\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '1700包管理，民水民电，押一付一，带阳台天然气。', 'PUBLISHED', 1, 5, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4006, 3006, 1001, '168-水斗老围村大单间', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 105000, 105000, 'MONTHLY', '[\"精装修\", \"公寓\", \"携宠入住\"]', '[\"空调\", \"洗衣机\"]', '1050包管理，大单间带独卫，通风采光绝佳。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4007, 3007, 1001, '173-上油松单间', '上油松', '龙华区', '深圳市龙华区龙华街道上油松村', 95000, 95000, 'MONTHLY', '[\"通风好\", \"短租\"]', '[\"空调\", \"洗衣机\"]', '950包管理，空调、热水器、洗衣机全齐全包。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4008, 3008, 1001, '703-上油松精装大单间', '上油松', '龙华区', '深圳市龙华区龙华街道上油松村', 130000, 130000, 'MONTHLY', '[\"公寓\", \"采光好\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '位于上油松4楼，房间阳光好，通风霸道，1300可租。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4009, 3009, 1001, '143-水斗老围村一房一厅', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 180000, 180000, 'MONTHLY', '[\"天然气\", \"带阳台\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '1800包管理，带阳台天然气，民水民电，随时看房。', 'PUBLISHED', 1, 9, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4010, 3010, 1001, '13-水斗老围村独栋大单间', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 100000, 100000, 'MONTHLY', '[\"采光好\", \"短租\"]', '[\"空调\", \"洗衣机\"]', '独栋楼房，采光绝佳，1000可租，随时看房。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4011, 3011, 1001, '101-水斗新围村电梯单间', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 92000, 92000, 'MONTHLY', '[\"公寓\", \"半年起租\"]', '[\"空调\"]', '101-水斗新围电梯单间人客+100包管理，随时拎包入住。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4012, 3012, 1001, '119-水斗老围村靠山两房一厅', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 250000, 250000, 'MONTHLY', '[\"带阳台\", \"携宠入住\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '靠山风光优美，带大阳台，允许携宠入住。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4013, 3013, 1001, '22-水斗新围村大单间', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 120000, 120000, 'MONTHLY', '[\"精装修\", \"采光好\", \"短租\"]', '[\"空调\", \"洗衣机\"]', '22-水斗新围村大单间，1100包管理，通风采光佳。', 'PUBLISHED', 1, 13, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4014, 3014, 1001, '18-水斗新围村单间', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 90000, 90000, 'MONTHLY', '[\"通风好\", \"短租\"]', '[\"空调\"]', '位于05水斗新围村，配置齐全，拎包入住，900可租。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4015, 3015, 1001, '202-富豪新村精装两房一厅', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 320000, 320000, 'MONTHLY', '[\"精装修\", \"天然气\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '202两房一厅特价3200包管理网络，家电全送。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4016, 3016, 1001, '155-水斗老围村四房一厅', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 580000, 580000, 'MONTHLY', '[\"带阳台\", \"携宠入住\", \"精装修\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '大四房精装修，前后双阳台，可养猫狗。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4017, 3017, 1001, '58-水斗老围村单间', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 80000, 80000, 'MONTHLY', '[\"采光好\", \"短租\"]', '[\"空调\"]', '超平价实用单间，采光充足，押一付一。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4018, 3018, 1001, '96-富豪新村一房一厅', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 160000, 160000, 'MONTHLY', '[\"带阳台\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '特价一房一厅包管理，阳台落地窗，家电齐全。', 'PUBLISHED', 1, 18, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4019, 3019, 1001, '88-水斗新围村复式大单间', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 220000, 220000, 'MONTHLY', '[\"公寓\", \"精装修\", \"一年起租\"]', '[\"空调\", \"洗衣机\"]', '网红精装复式大单间，家具全齐，拎包即住。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4020, 3020, 1001, '52-富豪新村大三房一厅', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 420000, 420000, 'MONTHLY', '[\"天然气\", \"带阳台\", \"携宠入住\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '宽敞三房，管道天然气入户，允许携宠入住。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4021, 3021, 1001, '112-上油松电梯一房一厅', '上油松', '龙华区', '深圳市龙华区龙华街道上油松村', 190000, 190000, 'MONTHLY', '[\"通风好\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '电梯大一房一厅，光线充足通风好，1900包管理。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4022, 3022, 1001, '801-水斗老围村景观三房一厅', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 380000, 380000, 'MONTHLY', '[\"精装修\", \"带阳台\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '高楼层景观房，南北通透，带超大阳台。', 'PUBLISHED', 1, 22, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4023, 3023, 1001, '45-水斗新围村公寓单间', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 115000, 115000, 'MONTHLY', '[\"公寓\", \"采光好\", \"短租\"]', '[\"空调\"]', '精装小公寓，配独立卫浴，采光好。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4024, 3024, 1001, '309-富豪新村电梯大单间', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 140000, 140000, 'MONTHLY', '[\"精装修\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '电梯大单间，家电全送，1400包管理。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4025, 3025, 1001, '62-上油松二房一厅', '上油松', '龙华区', '深圳市龙华区龙华街道上油松村', 230000, 230000, 'MONTHLY', '[\"通风好\", \"天然气\", \"携宠入住\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '通透两居室，自带天然气与阳台，可养宠物。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4026, 3026, 1001, '502-水斗新围村阳台一房一厅', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 175000, 175000, 'MONTHLY', '[\"带阳台\", \"采光好\", \"一年起租\"]', '[\"空调\", \"洗衣机\"]', '独立大阳台，朝南采光好，1750包管理。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4027, 3027, 1001, '99-水斗老围村平价单间', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 78000, 78000, 'MONTHLY', '[\"短租\"]', '[\"空调\"]', '最便宜特价单间，短租过度的极佳选择。', 'PUBLISHED', 1, 27, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4028, 3028, 1001, '408-富豪新村四房一厅', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 620000, 620000, 'MONTHLY', '[\"精装修\", \"天然气\", \"带阳台\", \"携宠入住\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '豪华四房，全屋精装修，管道天然气，随时入住。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4029, 3029, 1001, '77-上油松大单间', '上油松', '龙华区', '深圳市龙华区龙华街道上油松村', 125000, 125000, 'MONTHLY', '[\"采光好\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '77-上油松大单间，1250包管理，采光极佳。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4030, 3030, 1001, '601-水斗新围村三房一厅', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 360000, 360000, 'MONTHLY', '[\"通风好\", \"天然气\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '3600包管理，客厅通风采光好，天然气已开通。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4031, 3031, 1001, '15-水斗老围村公寓大单间', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 150000, 150000, 'MONTHLY', '[\"公寓\", \"精装修\", \"短租\"]', '[\"空调\", \"洗衣机\"]', '精装小公寓，大单间配套齐，1350限时特惠。', 'PUBLISHED', 1, 31, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4032, 3032, 1001, '211-富豪新村一房一厅', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 165000, 165000, 'MONTHLY', '[\"带阳台\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '1650包管理，独立阳台晾衣方便，拎包入住。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4033, 3033, 1001, '83-上油松两房一厅', '上油松', '龙华区', '深圳市龙华区龙华街道上油松村', 260000, 260000, 'MONTHLY', '[\"精装修\", \"天然气\", \"携宠入住\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '精装两房一厅，民水民电，允许养宠物。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4034, 3034, 1001, '303-水斗新围村单间', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 98000, 98000, 'MONTHLY', '[\"采光好\", \"短租\"]', '[\"空调\"]', '980包管理，采光极佳，支持短租。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4035, 3035, 1001, '510-水斗老围村二房一厅', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 210000, 210000, 'MONTHLY', '[\"通风好\", \"一年起租\"]', '[\"空调\", \"洗衣机\"]', '两室一厅平价出租，2100包管理，通风舒适。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4036, 3036, 1001, '12-富豪新村公寓单间', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 130000, 130000, 'MONTHLY', '[\"公寓\", \"精装修\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '精装品牌公寓单间，押一付一，1300包管理。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4037, 3037, 1001, '166-上油松三房一厅', '上油松', '龙华区', '深圳市龙华区龙华街道上油松村', 390000, 390000, 'MONTHLY', '[\"带阳台\", \"天然气\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '大三房合租整租皆宜，天然气阳台全齐。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4038, 3038, 1001, '908-水斗新围村大单间', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 145000, 145000, 'MONTHLY', '[\"采光好\", \"短租\"]', '[\"空调\", \"洗衣机\"]', '高楼层视野无遮挡，阳光充足大单间。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4039, 3039, 1001, '205-水斗老围村一房一厅', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 170000, 170000, 'MONTHLY', '[\"通风好\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '户型方正，一房一厅1700包管理网络。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4040, 3040, 1001, '318-富豪新村其他复式', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 750000, 750000, 'MONTHLY', '[\"精装修\", \"天然气\", \"带阳台\", \"携宠入住\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '复式豪宅，前后双露台，管道天然气入户。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4041, 3041, 1001, '66-上油松单间', '上油松', '龙华区', '深圳市龙华区龙华街道上油松村', 88000, 88000, 'MONTHLY', '[\"通风好\", \"短租\"]', '[\"空调\"]', '平价干净单间，出入便捷，880包管理。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4042, 3042, 1001, '702-水斗新围村二房一厅', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 280000, 280000, 'MONTHLY', '[\"精装修\", \"带阳台\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '特惠两房一厅，带晒衣阳台，家电齐全。', 'PUBLISHED', 1, 42, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4043, 3043, 1001, '107-水斗老围村大单间', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 110000, 110000, 'MONTHLY', '[\"采光好\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '采光好，大单间带独立卫生间。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4044, 3044, 1001, '501-富豪新村一房一厅', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 160000, 160000, 'MONTHLY', '[\"公寓\", \"带阳台\", \"短租\"]', '[\"空调\", \"洗衣机\"]', '1600包管理，精装带阳台，支持短租。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4045, 3045, 1001, '28-上油松四房一厅', '上油松', '龙华区', '深圳市龙华区龙华街道上油松村', 550000, 550000, 'MONTHLY', '[\"精装修\", \"天然气\", \"携宠入住\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '大家庭大四房，全屋精装，带大阳台。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4046, 3046, 1001, '809-水斗新围村大单间', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 135000, 135000, 'MONTHLY', '[\"采光好\", \"短租\"]', '[\"空调\", \"洗衣机\"]', '1350包管理，采光充足，拎包入住。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4047, 3047, 1001, '102-水斗老围村一房一厅', '水斗老围村', '龙华区', '深圳市龙华区龙华街道水斗老围村', 165000, 165000, 'MONTHLY', '[\"通风好\", \"半年起租\"]', '[\"空调\", \"洗衣机\"]', '通风采光俱佳，配齐家电，随时看房。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4048, 3048, 1001, '305-富豪新村三房一厅', '富豪新村', '龙华区', '深圳市龙华区龙华街道富豪新村', 380000, 380000, 'MONTHLY', '[\"带阳台\", \"天然气\", \"一年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '标准三房，大阳台采光佳，管道天然气。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4049, 3049, 1001, '11-上油松单间', '上油松', '龙华区', '深圳市龙华区龙华街道上油松村', 92000, 92000, 'MONTHLY', '[\"采光好\", \"短租\"]', '[\"空调\"]', '920包管理，性价比高，出入便利。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');
INSERT INTO `house_listing` VALUES (4050, 3050, 1001, '901-水斗新围村复式二房一厅', '水斗新围村', '龙华区', '深圳市龙华区龙华街道水斗新围村', 310000, 310000, 'MONTHLY', '[\"精装修\", \"公寓\", \"带阳台\", \"半年起租\"]', '[\"空调\", \"洗衣机\", \"天然气\"]', '复式精装两房，自带独立阳台，采光绝佳。', 'PUBLISHED', 0, 0, '2026-08-05 06:10:04.105', NULL, '2026-08-05 06:10:04.105', '2026-08-05 06:10:04.105');

-- ----------------------------
-- Table structure for listing_media
-- ----------------------------
DROP TABLE IF EXISTS `listing_media`;
CREATE TABLE `listing_media`  (
  `id` bigint NOT NULL,
  `listing_id` bigint NOT NULL,
  `media_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `sort_no` int NOT NULL DEFAULT 0,
  `duration_seconds` int NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_media_listing_sort`(`listing_id` ASC, `sort_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of listing_media
-- ----------------------------
INSERT INTO `listing_media` VALUES (5001, 4001, 'IMAGE', '/images/houses/house_1.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5002, 4001, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5003, 4001, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5004, 4002, 'IMAGE', '/images/houses/house_2.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5005, 4002, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5006, 4002, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5007, 4003, 'IMAGE', '/images/houses/house_4.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5008, 4003, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5009, 4003, 'IMAGE', '/images/houses/house_1.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5010, 4004, 'IMAGE', '/images/houses/house_5.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5011, 4004, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5012, 4004, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5013, 4005, 'IMAGE', '/images/houses/house_3.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5014, 4005, 'IMAGE', '/images/houses/house_4.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5015, 4005, 'IMAGE', '/images/houses/house_2.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5016, 4006, 'IMAGE', '/images/houses/house_6.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5017, 4006, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5018, 4006, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5019, 4007, 'IMAGE', '/images/houses/house_7.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5020, 4007, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5021, 4007, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5022, 4008, 'IMAGE', '/images/houses/house_8.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5023, 4008, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5024, 4008, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5025, 4009, 'IMAGE', '/images/houses/house_9.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5026, 4009, 'IMAGE', '/images/houses/house_4.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5027, 4009, 'IMAGE', '/images/houses/house_2.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5028, 4010, 'IMAGE', '/images/houses/house_10.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5029, 4010, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5030, 4010, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5031, 4011, 'IMAGE', '/images/houses/house_11.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5032, 4011, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5033, 4011, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5034, 4012, 'IMAGE', '/images/houses/house_12.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5035, 4012, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5036, 4012, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5037, 4013, 'IMAGE', '/images/houses/house_13.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5038, 4013, 'IMAGE', '/images/houses/house_4.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5039, 4013, 'IMAGE', '/images/houses/house_2.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5040, 4014, 'IMAGE', '/images/houses/house_14.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5041, 4014, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5042, 4014, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5043, 4015, 'IMAGE', '/images/houses/house_15.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5044, 4015, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5045, 4015, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5046, 4016, 'IMAGE', '/images/houses/house_16.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5047, 4016, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5048, 4016, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5049, 4017, 'IMAGE', '/images/houses/house_17.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5050, 4017, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5051, 4017, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5052, 4018, 'IMAGE', '/images/houses/house_18.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5053, 4018, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5054, 4018, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5055, 4019, 'IMAGE', '/images/houses/house_19.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5056, 4019, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5057, 4019, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5058, 4020, 'IMAGE', '/images/houses/house_20.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5059, 4020, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5060, 4020, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5061, 4021, 'IMAGE', '/images/houses/house_1.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5062, 4021, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5063, 4021, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5064, 4022, 'IMAGE', '/images/houses/house_2.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5065, 4022, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5066, 4022, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5067, 4023, 'IMAGE', '/images/houses/house_3.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5068, 4023, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5069, 4023, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5070, 4024, 'IMAGE', '/images/houses/house_4.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5071, 4024, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5072, 4024, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5073, 4025, 'IMAGE', '/images/houses/house_5.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5074, 4025, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5075, 4025, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5076, 4026, 'IMAGE', '/images/houses/house_13.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5077, 4026, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5078, 4026, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5079, 4027, 'IMAGE', '/images/houses/house_14.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5080, 4027, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5081, 4027, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5082, 4028, 'IMAGE', '/images/houses/house_15.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5083, 4028, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5084, 4028, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5085, 4029, 'IMAGE', '/images/houses/house_6.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5086, 4029, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5087, 4029, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5088, 4030, 'IMAGE', '/images/houses/house_9.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5089, 4030, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5090, 4030, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5091, 4031, 'IMAGE', '/images/houses/house_10.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5092, 4031, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5093, 4031, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5094, 4032, 'IMAGE', '/images/houses/house_8.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5095, 4032, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5096, 4032, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5097, 4033, 'IMAGE', '/images/houses/house_11.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5098, 4033, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5099, 4033, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5100, 4034, 'IMAGE', '/images/houses/house_12.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5101, 4034, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5102, 4034, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5103, 4035, 'IMAGE', '/images/houses/house_16.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5104, 4035, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5105, 4035, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5106, 4036, 'IMAGE', '/images/houses/house_7.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5107, 4036, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5108, 4036, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5109, 4037, 'IMAGE', '/images/houses/house_20.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5110, 4037, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5111, 4037, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5112, 4038, 'IMAGE', '/images/houses/house_19.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5113, 4038, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5114, 4038, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5115, 4039, 'IMAGE', '/images/houses/house_17.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5116, 4039, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5117, 4039, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5118, 4040, 'IMAGE', '/images/houses/house_21.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5119, 4040, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5120, 4040, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5121, 4041, 'IMAGE', '/images/houses/house_18.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5122, 4041, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5123, 4041, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5124, 4042, 'IMAGE', '/images/houses/house_22.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5125, 4042, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5126, 4042, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5127, 4043, 'IMAGE', '/images/houses/house_23.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5128, 4043, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5129, 4043, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5130, 4044, 'IMAGE', '/images/houses/house_24.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5131, 4044, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5132, 4044, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5133, 4045, 'IMAGE', '/images/houses/house_25.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5134, 4045, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5135, 4045, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5136, 4046, 'IMAGE', '/images/houses/house_15.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5137, 4046, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5138, 4046, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5139, 4047, 'IMAGE', '/images/houses/house_7.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5140, 4047, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5141, 4047, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5142, 4048, 'IMAGE', '/images/houses/house_20.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5143, 4048, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5144, 4048, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5145, 4049, 'IMAGE', '/images/houses/house_19.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5146, 4049, 'IMAGE', '/images/houses/house_2.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5147, 4049, 'IMAGE', '/images/houses/house_4.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5148, 4050, 'IMAGE', '/images/houses/house_17.jpg', NULL, 1, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5149, 4050, 'IMAGE', '/images/houses/house_1.jpg', NULL, 2, NULL, '2026-08-05 06:10:04.155');
INSERT INTO `listing_media` VALUES (5150, 4050, 'IMAGE', '/images/houses/house_3.jpg', NULL, 3, NULL, '2026-08-05 06:10:04.155');

-- ----------------------------
-- Table structure for property_building
-- ----------------------------
DROP TABLE IF EXISTS `property_building`;
CREATE TABLE `property_building`  (
  `id` bigint NOT NULL,
  `agent_id` bigint NOT NULL,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `longitude` decimal(10, 7) NULL DEFAULT NULL,
  `latitude` decimal(10, 7) NULL DEFAULT NULL,
  `status` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_building_landlord`(`agent_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_building
-- ----------------------------
INSERT INTO `property_building` VALUES (2001, 1001, '水斗老围村', '深圳市龙华区龙华街道水斗老围村', 114.0300000, 22.6200000, 'ACTIVE', '2026-08-05 06:10:03.995', '2026-08-05 06:10:03.995');
INSERT INTO `property_building` VALUES (2002, 1001, '水斗新围村', '深圳市龙华区龙华街道水斗新围村', 114.0300000, 22.6200000, 'ACTIVE', '2026-08-05 06:10:03.995', '2026-08-05 06:10:03.995');
INSERT INTO `property_building` VALUES (2003, 1001, '富豪新村', '深圳市龙华区龙华街道富豪新村', 114.0300000, 22.6200000, 'ACTIVE', '2026-08-05 06:10:03.995', '2026-08-05 06:10:03.995');
INSERT INTO `property_building` VALUES (2004, 1001, '上油松', '深圳市龙华区龙华街道上油松村', 114.0300000, 22.6200000, 'ACTIVE', '2026-08-05 06:10:03.995', '2026-08-05 06:10:03.995');

-- ----------------------------
-- Table structure for property_unit
-- ----------------------------
DROP TABLE IF EXISTS `property_unit`;
CREATE TABLE `property_unit`  (
  `id` bigint NOT NULL,
  `building_id` bigint NOT NULL,
  `agent_id` bigint NOT NULL,
  `unit_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `room_count` tinyint NOT NULL,
  `hall_count` tinyint NOT NULL DEFAULT 0,
  `bathroom_count` tinyint NOT NULL DEFAULT 1,
  `area_sqm` decimal(8, 2) NULL DEFAULT NULL,
  `floor_no` smallint NULL DEFAULT NULL,
  `total_floor` smallint NULL DEFAULT NULL,
  `orientation` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `occupancy_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'VACANT',
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_unit_building_no`(`building_id` ASC, `unit_no` ASC) USING BTREE,
  INDEX `idx_unit_landlord_status`(`agent_id` ASC, `occupancy_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_unit
-- ----------------------------
INSERT INTO `property_unit` VALUES (3001, 2001, 1001, '340房', '340-水斗老围村电梯大两房一厅', 2, 1, 1, 55.00, 3, 9, '南', 'RENTED', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3002, 2002, 1001, '60房', '60-水斗新围村电梯5楼单间', 1, 0, 1, 20.00, 6, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3003, 2002, 1001, '117房', '117-水斗新围村一房一厅', 1, 1, 1, 35.00, 1, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3004, 2002, 1001, '108房', '108-水斗新围村7楼单间', 1, 0, 1, 18.00, 1, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3005, 2003, 1001, '353房', '353-富豪新村电梯两房一厅', 2, 1, 1, 60.00, 3, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3006, 2001, 1001, '168房', '168-水斗老围村大单间', 1, 0, 1, 30.00, 1, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3007, 2004, 1001, '173房', '173-上油松单间', 1, 0, 1, 22.00, 1, 9, '南', 'RENTED', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3008, 2004, 1001, '703房', '703-上油松精装大单间', 1, 0, 1, 32.00, 7, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3009, 2001, 1001, '143房', '143-水斗老围村一房一厅', 1, 1, 1, 42.00, 1, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3010, 2001, 1001, '13房', '13-水斗老围村独栋大单间', 1, 0, 1, 28.00, 1, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3011, 2002, 1001, '101房', '101-水斗新围村电梯单间', 1, 0, 1, 19.00, 1, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3012, 2001, 1001, '119房', '119-水斗老围村靠山两房一厅', 2, 1, 1, 68.00, 1, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3013, 2002, 1001, '22房', '22-水斗新围村大单间', 1, 0, 1, 30.00, 2, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3014, 2002, 1001, '18房', '18-水斗新围村单间', 1, 0, 1, 21.00, 1, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3015, 2003, 1001, '202房', '202-富豪新村精装两房一厅', 2, 1, 1, 70.00, 2, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3016, 2001, 1001, '155房', '155-水斗老围村四房一厅', 4, 1, 1, 130.00, 1, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3017, 2001, 1001, '58房', '58-水斗老围村单间', 1, 0, 1, 18.00, 5, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3018, 2003, 1001, '96房', '96-富豪新村一房一厅', 1, 1, 1, 38.00, 9, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3019, 2002, 1001, '88房', '88-水斗新围村复式大单间', 1, 0, 1, 36.00, 8, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3020, 2003, 1001, '52房', '52-富豪新村大三房一厅', 3, 1, 1, 105.00, 5, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3021, 2004, 1001, '112房', '112-上油松电梯一房一厅', 1, 1, 1, 45.00, 1, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3022, 2001, 1001, '801房', '801-水斗老围村景观三房一厅', 3, 1, 1, 95.00, 8, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3023, 2002, 1001, '45房', '45-水斗新围村公寓单间', 1, 0, 1, 18.00, 4, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3024, 2003, 1001, '309房', '309-富豪新村电梯大单间', 1, 0, 1, 29.00, 3, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3025, 2004, 1001, '62房', '62-上油松二房一厅', 2, 1, 1, 62.00, 6, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3026, 2002, 1001, '502房', '502-水斗新围村阳台一房一厅', 1, 1, 1, 40.00, 5, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3027, 2001, 1001, '99房', '99-水斗老围村平价单间', 1, 0, 1, 17.00, 9, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3028, 2003, 1001, '408房', '408-富豪新村四房一厅', 4, 1, 1, 125.00, 4, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3029, 2004, 1001, '77房', '77-上油松大单间', 1, 0, 1, 31.00, 7, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3030, 2002, 1001, '601房', '601-水斗新围村三房一厅', 3, 1, 1, 88.00, 6, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3031, 2001, 1001, '15房', '15-水斗老围村公寓大单间', 1, 0, 1, 33.00, 1, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3032, 2003, 1001, '211房', '211-富豪新村一房一厅', 1, 1, 1, 39.00, 2, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3033, 2004, 1001, '83房', '83-上油松两房一厅', 2, 1, 1, 65.00, 8, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3034, 2002, 1001, '303房', '303-水斗新围村单间', 1, 0, 1, 20.00, 3, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3035, 2001, 1001, '510房', '510-水斗老围村二房一厅', 2, 1, 1, 58.00, 5, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3036, 2003, 1001, '12房', '12-富豪新村公寓单间', 1, 0, 1, 22.00, 1, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3037, 2004, 1001, '166房', '166-上油松三房一厅', 3, 1, 1, 90.00, 1, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3038, 2002, 1001, '908房', '908-水斗新围村大单间', 1, 0, 1, 34.00, 9, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3039, 2001, 1001, '205房', '205-水斗老围村一房一厅', 1, 1, 1, 41.00, 2, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3040, 2003, 1001, '318房', '318-富豪新村其他复式', 4, 2, 1, 150.00, 3, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3041, 2004, 1001, '66房', '66-上油松单间', 1, 0, 1, 19.00, 6, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3042, 2002, 1001, '702房', '702-水斗新围村二房一厅', 2, 1, 1, 66.00, 7, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3043, 2001, 1001, '107房', '107-水斗老围村大单间', 1, 0, 1, 30.00, 1, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3044, 2003, 1001, '501房', '501-富豪新村一房一厅', 1, 1, 1, 38.00, 5, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3045, 2004, 1001, '28房', '28-上油松四房一厅', 4, 1, 1, 118.00, 2, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3046, 2002, 1001, '809房', '809-水斗新围村大单间', 1, 0, 1, 32.00, 8, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3047, 2001, 1001, '102房', '102-水斗老围村一房一厅', 1, 1, 1, 39.00, 1, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3048, 2003, 1001, '305房', '305-富豪新村三房一厅', 3, 1, 1, 92.00, 3, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3049, 2004, 1001, '11房', '11-上油松单间', 1, 0, 1, 20.00, 1, 9, '南', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');
INSERT INTO `property_unit` VALUES (3050, 2002, 1001, '901房', '901-水斗新围村复式二房一厅', 2, 1, 1, 68.00, 9, 9, '南北', 'VACANT', '2026-08-05 06:10:04.038', '2026-08-05 06:10:04.038');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL,
  `role` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `mobile` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `wechat_openid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `avatar_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE',
  `last_login_at` datetime(3) NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  `deleted_at` datetime(3) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_mobile`(`mobile` ASC) USING BTREE,
  UNIQUE INDEX `uk_user_openid`(`wechat_openid` ASC) USING BTREE,
  INDEX `idx_user_role_status`(`role` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1001, 'AGENT', '13800000001', '$2b$12$xKwW4FU/13vPuOhFkSrRBOC2PhSkhXkuJX3DK.9CXGDB/VAY9nYa.', NULL, '中介小王', NULL, 'ACTIVE', NULL, '2026-08-05 05:56:54.541', '2026-08-05 05:59:00.273', NULL);
INSERT INTO `sys_user` VALUES (1002, 'TENANT', '13800000002', '$2b$12$xKwW4FU/13vPuOhFkSrRBOC2PhSkhXkuJX3DK.9CXGDB/VAY9nYa.', NULL, '测试租客', NULL, 'ACTIVE', '2026-08-05 16:39:12.128', '2026-08-05 05:56:54.541', '2026-08-05 16:39:12.128', NULL);

-- ----------------------------
-- Table structure for tenant_browse_history
-- ----------------------------
DROP TABLE IF EXISTS `tenant_browse_history`;
CREATE TABLE `tenant_browse_history`  (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `listing_id` bigint NOT NULL,
  `viewed_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_history_tenant_listing`(`tenant_id` ASC, `listing_id` ASC) USING BTREE,
  INDEX `idx_history_tenant_time`(`tenant_id` ASC, `viewed_at` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_browse_history
-- ----------------------------
INSERT INTO `tenant_browse_history` VALUES (343283927643000832, 1002, 4005, '2026-08-05 15:50:17.706');
INSERT INTO `tenant_browse_history` VALUES (343283954528489472, 1002, 4027, '2026-08-05 14:47:52.667');
INSERT INTO `tenant_browse_history` VALUES (343298335014326272, 1002, 4018, '2026-08-05 15:45:01.242');

-- ----------------------------
-- Table structure for tenant_favorite
-- ----------------------------
DROP TABLE IF EXISTS `tenant_favorite`;
CREATE TABLE `tenant_favorite`  (
  `tenant_id` bigint NOT NULL,
  `listing_id` bigint NOT NULL,
  `created_at` datetime(3) NOT NULL,
  PRIMARY KEY (`tenant_id`, `listing_id`) USING BTREE,
  INDEX `idx_favorite_listing`(`listing_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_favorite
-- ----------------------------
INSERT INTO `tenant_favorite` VALUES (1002, 4005, '2026-08-05 15:17:18.856');

-- ----------------------------
-- Table structure for tenant_identity_profile
-- ----------------------------
DROP TABLE IF EXISTS `tenant_identity_profile`;
CREATE TABLE `tenant_identity_profile`  (
  `user_id` bigint NOT NULL,
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `home_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `profile_status` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'INCOMPLETE',
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_identity_profile
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_profile
-- ----------------------------
DROP TABLE IF EXISTS `tenant_profile`;
CREATE TABLE `tenant_profile`  (
  `user_id` bigint NOT NULL,
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `emergency_contact` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `emergency_mobile` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `blacklist_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `blacklist_at` datetime(3) NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_profile
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
