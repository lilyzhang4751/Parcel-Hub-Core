package com.lily.parcelhubcore.common.util;

import java.time.Instant;
import java.time.OffsetDateTime;
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
     * epoch milli -> Instant
     */
    public static Instant toInstant(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli);
    }

    /**
     * Instant -> epoch milli
     */
    public static Long toEpochMilli(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    /**
     * epoch milli -> OffsetDateTime(UTC)
     * 仅当你某些地方明确想用 OffsetDateTime 时再用
     */
    public static OffsetDateTime toOffsetDateTime(Long epochMilli) {
        return epochMilli == null ? null : Instant.ofEpochMilli(epochMilli).atOffset(UTC);
    }

    /**
     * OffsetDateTime -> epoch milli
     */
    public static Long toEpochMilli(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toInstant().toEpochMilli();
    }
}
