package org.strangerlink.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DateTimeUtils {

    public static LocalDateTime fromEpocToDateTime(long timestampMillis) {
        return LocalDateTime.ofEpochSecond(timestampMillis/1000, 0, java.time.ZoneOffset.UTC);
    }
}
