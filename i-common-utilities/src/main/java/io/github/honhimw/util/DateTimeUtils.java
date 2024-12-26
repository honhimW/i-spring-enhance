package io.github.honhimw.util;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author hon_him
 * @since 2022-05-31
 */
public class DateTimeUtils {

    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();

    public static final ZoneOffset UTC_PLUS_8 = ZoneOffset.ofHours(8);

    /**
     * Chinese Standard Time
     * <p>
     * Warning: CST also means Central Standard Time, which is means UTC-6
     */
    public static final ZoneOffset CST = UTC_PLUS_8;

    public static ZoneOffset DEFAULT_ZONE_OFFSET = UTC_PLUS_8;

    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    public static final String DEFAULT_DATE_TIME_PATTERN = DEFAULT_DATE_PATTERN + " " + DEFAULT_TIME_PATTERN;

    /**
     * RFC-3339 date format pattern.
     */
    public static final String RFC_3339 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * RFC-3339 date format formatter.
     */
    public static final DateTimeFormatter RFC_3339_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd")
        .appendLiteral('T')
        .appendPattern("HH:mm:ss")
        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
        .appendLiteral('Z')
        .toFormatter();

    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);

    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);

    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);

    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * ==================================================================================
     * Time to String
     * ==================================================================================
     */

    public static String format() {
        return format(LocalDateTime.now());
    }

    public static String format(LocalTime localTime) {
        return format(localTime, DEFAULT_TIME_FORMATTER);
    }

    public static String format(LocalDate localDate) {
        return format(localDate, DEFAULT_DATE_FORMATTER);
    }

    public static String format(LocalDateTime localDateTime) {
        return format(localDateTime, DEFAULT_DATE_TIME_FORMATTER);
    }

    public static String format(Instant instant) {
        return format(instant, DEFAULT_DATE_TIME_FORMATTER);
    }

    public static String format(LocalDate localDate, String pattern) {
        return format(localDate, DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalTime localTime, String pattern) {
        return format(localTime, DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDateTime localDateTime, String pattern) {
        return format(localDateTime, DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(Instant instant, String pattern) {
        return format(instant, DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDate localDate, DateTimeFormatter formatter) {
        return localDate.format(formatter);
    }

    public static String format(LocalTime localTime, DateTimeFormatter formatter) {
        return localTime.format(formatter);
    }

    public static String format(LocalDateTime localDateTime, DateTimeFormatter formatter) {
        return localDateTime.format(formatter);
    }

    public static String format(Instant instant, DateTimeFormatter formatter) {
        return instant.atOffset(DEFAULT_ZONE_OFFSET).format(formatter);
    }

    /**
     * ==================================================================================
     * String to Time
     * ==================================================================================
     */
    public static LocalDateTime parseLocalDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DEFAULT_DATE_TIME_FORMATTER);
    }

    public static Instant parseInstant(String dateTimeString) {
        return parseInstant(dateTimeString, DEFAULT_DATE_TIME_PATTERN);
    }

    public static LocalDateTime parseLocalDateTime(Date date) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat(DEFAULT_DATE_TIME_PATTERN);
        String dateTimeString = simpleFormat.format(date);
        return LocalDateTime.parse(dateTimeString, DEFAULT_DATE_TIME_FORMATTER);
    }

    public static LocalDateTime parseIsoLocalDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, ISO_DATE_TIME_FORMATTER);
    }

    public static LocalDate parseLocalDate(String dateString, String pattern) {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parseLocalDateTime(String dateTimeString, String pattern) {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern));
    }

    public static Instant parseInstant(String dateTimeString, String pattern) {
        LocalDateTime localDateTime = parseLocalDateTime(dateTimeString, pattern);
        return localDateTime.toInstant(DEFAULT_ZONE_OFFSET);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), getSystemOffset());
    }

    public static ZoneOffset getSystemOffset() {
        return ZoneId.systemDefault().getRules().getOffset(Instant.now());
    }

    public static boolean equal(LocalDateTime localDateTime, Date date) {
        return localDateTime.toInstant(DEFAULT_ZONE_OFFSET).equals(date.toInstant());
    }


}
