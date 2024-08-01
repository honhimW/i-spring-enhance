package io.github.honhimw.spring;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2022-06-28
 */
@SuppressWarnings("unused")
public interface TypeMapping {

    TypeMapping INSTANCE = new TypeMapping() {
    };

    default Long date2Long(Date date) {
        return nullSafety(date, Date::getTime);
    }

    default Date long2Date(Long l) {
        return nullSafety(l, Date::new);
    }

    default Long localDateTime2Long(LocalDateTime localDateTime) {
        return nullSafety(localDateTime, ldt -> Timestamp.valueOf(ldt).getTime());
    }

    default LocalDateTime long2LocalDateTime(Long l) {
        return nullSafety(l, aLong -> LocalDateTime.ofInstant(Instant.ofEpochMilli(aLong), ZoneId.systemDefault()));
    }

    default Date instant2Date(Instant instant) {
        return nullSafety(instant, Date::from);
    }

    default Instant date2Instant(Date date) {
        return nullSafety(date, Date::toInstant);
    }

    default Long instant2Long(Instant instant) {
        return nullSafety(instant, Instant::toEpochMilli);
    }

    default Instant localDateTime2Instant(LocalDateTime localDateTime) {
        return nullSafety(localDateTime, ldt -> ldt.toInstant(ZoneOffset.ofHours(8)));
    }

    default LocalDateTime instant2LocalDateTime(Instant instant) {
        return nullSafety(instant, i -> LocalDateTime.ofInstant(i, ZoneId.systemDefault()));
    }

    default LocalDateTime date2LocalDateTime(Date date) {
        return nullSafety(date, d -> LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()));
    }

    default Date localDateTime2Date(LocalDateTime localDateTime) {
        return nullSafety(localDateTime, ldt -> new Date(localDateTime2Long(ldt)));
    }


    default <T, R> R nullSafety(T t, Function<T, R> function) {
        return Optional.ofNullable(t).map(function).orElse(null);
    }

}
