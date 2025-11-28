package io.github.honhimw.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SafeDateFormat extends DateFormat {

    private final DateTimeFormatter formatter;

    private SafeDateFormat(DateTimeFormatter formatter) {
        this.formatter = formatter;
        this.calendar = Calendar.getInstance(TimeZone.getTimeZone(formatter.getZone()), formatter.getLocale());
        this.numberFormat = NumberFormat.getInstance(formatter.getLocale());
    }

    private SafeDateFormat(String pattern, Locale locale, TimeZone timeZone) {
        this(DateTimeFormatter.ofPattern(pattern, locale).withZone(timeZone.toZoneId()));
    }

    public static SafeDateFormat create(DateTimeFormatter formatter) {
        return new SafeDateFormat(formatter);
    }

    public static SafeDateFormat create(String pattern) {
        return create(pattern, Locale.getDefault());
    }

    public static SafeDateFormat create(String pattern, Locale locale) {
        return create(pattern, locale, TimeZone.getDefault());
    }

    public static SafeDateFormat create(String pattern, Locale locale, TimeZone timeZone) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale).withZone(timeZone.toZoneId());
        return new SafeDateFormat(formatter);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        LocalDateTime localDateTime = Instant.ofEpochMilli(date.getTime()).atZone(formatter.getZone()).toLocalDateTime();
        toAppendTo.append(formatter.format(localDateTime));
        return toAppendTo;
    }

    @Override
    public Date parse(String text, ParsePosition pos) {
        LocalDateTime localDateTime = LocalDateTime.parse(text, formatter);
        Instant instant = localDateTime.atZone(formatter.getZone()).toInstant();
        pos.setIndex(text.length());
        return Date.from(instant);
    }

    @SuppressWarnings("all")
    @Override
    public Object clone() {
        return new SafeDateFormat(this.formatter);
    }

}
