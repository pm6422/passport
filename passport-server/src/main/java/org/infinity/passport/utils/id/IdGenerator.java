package org.infinity.passport.utils.id;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public abstract class IdGenerator {
    private static final ShortIdGenerator     SHORT_ID_GENERATOR      = new ShortIdGenerator();
    private static final SnowFlakeIdGenerator SNOW_FLAKE_ID_GENERATOR = new SnowFlakeIdGenerator(1L, false, false);

    /**
     * Generate a thread-safe digit format ID
     *
     * @return 18 bits length，e.g：317297928250941551
     */
    public static long generateSnowFlakeId() {
        return SNOW_FLAKE_ID_GENERATOR.nextId();
    }

    /**
     * Generate a thread-safe digit format ID
     *
     * @return 19 bits length，e.g：1672888135850179037
     */
    public static long generateTimestampId() {
        return TimestampIdGenerator.nextId();
    }

    /**
     * Generate a thread-safe digit format ID, it can be used in a low concurrency environment
     *
     * @return 12 bits length，e.g：306554419571
     */
    public static long generateShortId() {
        return SHORT_ID_GENERATOR.nextId();
    }
}
