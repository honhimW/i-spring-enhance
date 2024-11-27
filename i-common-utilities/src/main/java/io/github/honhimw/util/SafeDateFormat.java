package io.github.honhimw.util;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author hon_him
 * @since 2024-11-22
 */

public class SafeDateFormat extends DateFormat {

    private FastDateFormat fastDateFormat;

    public SafeDateFormat(FastDateFormat fastDateFormat) {
        new SimpleDateFormat();
        this.fastDateFormat = fastDateFormat;
        calendar = Calendar.getInstance(fastDateFormat.getTimeZone());
        numberFormat = NumberFormat.getInstance(fastDateFormat.getLocale());
    }

    public static SafeDateFormat create(String pattern) {
        return new SafeDateFormat(FastDateFormat.getInstance(pattern));
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return fastDateFormat.format(date, toAppendTo, fieldPosition);
    }

    @Override
    public Date parse(String text, ParsePosition pos) {
        return fastDateFormat.parse(text, pos);
    }

    @Override
    public Date parse(String text) throws ParseException {
        return fastDateFormat.parse(text);
    }

    @Override
    public Object parseObject(String text, ParsePosition pos) {
        return fastDateFormat.parseObject(text, pos);
    }

    @Override
    public Object parseObject(String text) throws ParseException {
        return fastDateFormat.parseObject(text);
    }

    @SuppressWarnings({"all"})
    @Override
    public Object clone() {
        return create(fastDateFormat.getPattern());
    }

    @Override
    public void setCalendar(Calendar newCalendar) {
        super.setCalendar(newCalendar);
        this.fastDateFormat = FastDateFormat.getInstance(fastDateFormat.getPattern(), newCalendar.getTimeZone());
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        this.fastDateFormat = FastDateFormat.getInstance(fastDateFormat.getPattern(), zone);
    }
}
