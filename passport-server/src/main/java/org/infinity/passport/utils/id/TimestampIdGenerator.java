package org.infinity.passport.utils.id;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 通过ID能够知道大致请求的时间
 *
 * <pre>
 * 	目前是currentTimeMillis * (2^20) + offset.incrementAndGet()
 * 	通过ID / (2^20 * 1000) 能够得到秒
 * </pre>
 */
@ThreadSafe
final class TimestampIdGenerator {
    protected static final AtomicLong OFFSET               = new AtomicLong(0);
    protected static final int        BITS                 = 20;
    protected static final long       MAX_COUNT_PER_MILLIS = 1 << BITS;

    /**
     * Generate a thread-safe digit format ID
     *
     * @return 19 bits length，e.g：1672888135850179037
     */
    protected static long nextId() {
        long currentTime = System.currentTimeMillis();
        long count = OFFSET.incrementAndGet();
        while (count >= MAX_COUNT_PER_MILLIS) {
            synchronized (TimestampIdGenerator.class) {
                if (OFFSET.get() >= MAX_COUNT_PER_MILLIS) {
                    OFFSET.set(0);
                }
            }
            count = OFFSET.incrementAndGet();
        }
        return (currentTime << BITS) + count;
    }
}