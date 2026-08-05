INSERT IGNORE INTO property_building (id, landlord_id, name, address, longitude, latitude, status, created_at, updated_at) VALUES
(2001, 1001, '云栖花园', '杭州市西湖区文一西路 88 号', 120.0860000, 30.2860000, 'ACTIVE', NOW(3), NOW(3));
INSERT IGNORE INTO property_unit (id, building_id, landlord_id, unit_no, title, room_count, hall_count, bathroom_count, area_sqm, floor_no, total_floor, orientation, occupancy_status, created_at, updated_at) VALUES
(3001, 2001, 1001, '2幢 1203', '云栖花园精装两居', 2, 1, 1, 68.00, 12, 18, '南', 'VACANT', NOW(3), NOW(3)),
(3002, 2001, 1001, '5幢 806', '地铁口阳光一居', 1, 1, 1, 42.00, 8, 16, '南', 'VACANT', NOW(3), NOW(3)),
(3003, 2001, 1001, '1幢 1602', '品质三居整租', 3, 2, 2, 96.00, 16, 20, '南北', 'VACANT', NOW(3), NOW(3));
INSERT IGNORE INTO house_listing (id, unit_id, landlord_id, title, community_name, district, address, rent_cent, deposit_cent, payment_cycle, tags_json, facilities_json, description, publish_status, is_special, special_sort, published_at, created_at, updated_at) VALUES
(4001, 3001, 1001, '云栖花园 · 精装两居室', '云栖花园', '西湖区', '文一西路 88 号 2 幢', 420000, 420000, 'MONTHLY', JSON_ARRAY('近地铁','精装修','随时看房'), JSON_ARRAY('电梯','空调','冰箱','洗衣机','可做饭'), '南向采光，步行至地铁站约 8 分钟，适合情侣或好友合租。', 'PUBLISHED', 1, 1, NOW(3), NOW(3), NOW(3)),
(4002, 3002, 1001, '云栖花园 · 地铁口阳光一居', '云栖花园', '西湖区', '文一西路 88 号 5 幢', 300000, 300000, 'MONTHLY', JSON_ARRAY('地铁口','独立一居','拎包入住'), JSON_ARRAY('电梯','空调','热水器','衣柜'), '独立一居，通勤便利，家具家电齐全。', 'PUBLISHED', 1, 2, NOW(3), NOW(3), NOW(3)),
(4003, 3003, 1001, '云栖花园 · 品质三居整租', '云栖花园', '西湖区', '文一西路 88 号 1 幢', 620000, 620000, 'MONTHLY', JSON_ARRAY('整租','三居','品质社区'), JSON_ARRAY('电梯','空调','冰箱','洗衣机','停车位'), '三居两卫，空间充足，适合家庭居住。', 'PUBLISHED', 0, 0, NOW(3), NOW(3), NOW(3));
INSERT IGNORE INTO listing_media (id, listing_id, media_type, url, cover_url, sort_no, created_at) VALUES
(5001, 4001, 'IMAGE', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1200&q=80', NULL, 1, NOW(3)),
(5002, 4001, 'IMAGE', 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80', NULL, 2, NOW(3)),
(5003, 4002, 'IMAGE', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?auto=format&fit=crop&w=1200&q=80', NULL, 1, NOW(3)),
(5004, 4003, 'IMAGE', 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1200&q=80', NULL, 1, NOW(3));
