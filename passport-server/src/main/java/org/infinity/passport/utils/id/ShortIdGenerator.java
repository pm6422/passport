package org.infinity.passport.utils.id;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Bad performance
 */
@ThreadSafe
final class ShortIdGenerator {

    private final static long twepoch            = 1288834974657L;
    // 机器标识位数
    private final static long workerIdBits       = 0L;
    // 数据中心标识位数
    private final static long datacenterIdBits   = 0L;
    // 毫秒内自增位
    private final static long sequenceBits       = 0L;
    // 机器ID偏左移12位
    private final static long workerIdShift      = sequenceBits;
    // 数据中心ID左移17位
    private final static long datacenterIdShift  = sequenceBits + workerIdBits;
    // 时间毫秒左移22位
    private final static long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    private final static long sequenceMask = -1L ^ (-1L << sequenceBits);

    private static long lastTimestamp = -1L;

    private long sequence = 0L;

    /**
     * @return 12 bits length，like：306554419571
     */
    protected synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id for "
                    + (lastTimestamp - timestamp) + " milliseconds");
        }

        if (lastTimestamp == timestamp) {
            // 当前毫秒内，则+1
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                // 当前毫秒内计数满了，则等待下一秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        // ID偏移组合生成最终的ID，并返回ID

        long workerId = 0L;
        long datacenterId = 0L;
        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift) | sequence;
    }

    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}