package com.lily.parcelhubcore.shared.util;

import java.time.Instant;
import java.time.ZoneOffset;

public final class TimeConvertUtils {

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    private TimeConvertUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * epoch milli -> Instant
     */
    public static Instant toInstant(Long epochMilli) {
        return epochMilli == null ? null : Instant.ofEpochMilli(epochMilli);
    }

    /**
     * Instant -> epoch milli
     */
    public static Long toEpochMilli(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }
}
