package io.github.honhimw.util;


import java.io.Serializable;

/**
 * Twitter snowflake algorithm, resize {@link #workerIdBits} and {@link #dataCenterIdBits}
 * <a href="https://github.com/dromara/hutool/blob/56a2819861/hutool-core/src/main/java/cn/hutool/core/lang/Snowflake.java"/>
 *
 * @author hon_him
 * @since 2022-07-18
 */
@SuppressWarnings("unused")
public class SnowflakeUtils implements Serializable {

    private final long twepoch;
    private final long workerIdBits = 8L;
    // maxWorkerId, 0~31
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long dataCenterIdBits = 2L;
    // maxDataCenterId, 0~31
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long dataCenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
    @SuppressWarnings("FieldCanBeLocal")
    private final long sequenceMask = ~(-1L << sequenceBits);// 4095

    private final long workerId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public static SnowflakeUtils getInstance() {
        return getInstance(1658914580040L);
    }
    public static SnowflakeUtils getInstance(long twepoch) {
        String ipv4 = IpUtils.localIPv4();
        String id3 = ipv4.split("\\.")[3];
        return getInstance(twepoch, Long.parseLong(id3), 0L);
    }

    public static SnowflakeUtils getInstance(long twepoch, long workerId, long dataCenterId) {
        return new SnowflakeUtils(twepoch, workerId, dataCenterId);
    }

    private SnowflakeUtils(long twepoch, long workerId, long dataCenterId) {
        if (twepoch < 0) {
            throw new IllegalArgumentException("twepoch can't be greater than %s or less than 0");
        }
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("worker Id can't be greater than %s or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(
                String.format("datacenter Id can't be greater than %s or less than 0", maxDataCenterId));
        }
        this.twepoch = twepoch;
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public long getWorkerId(long id) {
        return id >> workerIdShift & ~(-1L << workerIdBits);
    }

    public long getDataCenterId(long id) {
        return id >> dataCenterIdShift & ~(-1L << dataCenterIdBits);
    }

    public long getGenerateDateTime(long id) {
        return (id >> timestampLeftShift & ~(-1L << 41L)) + twepoch;
    }

    public synchronized long nextId() {
        long timestamp = timestamp();
        if (timestamp < lastTimestamp) {
            if (lastTimestamp - timestamp < 2000) {
                // put up to 2 seconds to allow for NTP resync
                timestamp = lastTimestamp;
            } else {
                // if the clock is moving backwards
                throw new IllegalStateException(String.format("Clock moved backwards. Refusing to generate id for %sms",
                    lastTimestamp - timestamp));
            }
        }

        if (timestamp == lastTimestamp) {
            final long seq = (sequence + 1) & sequenceMask;
            if (seq == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
            sequence = seq;
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (dataCenterId
                                                                << dataCenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    public String nextIdStr() {
        return Long.toString(nextId());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------ Private method start

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = timestamp();
        // get current timestamp until there is a change
        while (timestamp == lastTimestamp) {
            timestamp = timestamp();
        }
        if (timestamp < lastTimestamp) {
            // if this happens, something is wrong
            throw new IllegalStateException(
                String.format("Clock moved backwards. Refusing to generate id for %sms", lastTimestamp - timestamp));
        }
        return timestamp;
    }

    private static long timestamp() {
        return System.currentTimeMillis();
    }

}
