package com.renthouse.common.id;

import org.springframework.stereotype.Component;

/** 64 位趋势递增 ID：41 位时间戳、10 位机器号、12 位序列。 */
@Component
public class SnowflakeIdGenerator {
    private static final long EPOCH = 1_704_067_200_000L; // 2024-01-01 UTC
    private static final long SEQUENCE_MASK = 0xFFFL;
    private final long workerId = (System.getProperty("rent.house.worker-id", "1").hashCode() & 0x3FFL);
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) timestamp = lastTimestamp;
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                do { timestamp = System.currentTimeMillis(); } while (timestamp <= lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << 22) | (workerId << 12) | sequence;
    }
}
