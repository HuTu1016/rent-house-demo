-- V2/V3 are immutable Flyway history. Remove their legacy demo rows after migration.
DELETE FROM listing_media WHERE id IN (5001, 5002, 5003, 5004);
DELETE FROM house_listing WHERE id IN (4001, 4002, 4003);
DELETE FROM property_unit WHERE id IN (3001, 3002, 3003);
DELETE FROM property_building WHERE id = 2001;
