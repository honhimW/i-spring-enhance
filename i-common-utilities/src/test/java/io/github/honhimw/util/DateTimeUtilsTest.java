package io.github.honhimw.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class DateTimeUtilsTest {

    @Test
    void format() {
        String now = DateTimeUtils.format();
        LocalDateTime parse = LocalDateTime.parse(now, DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER);
        Assertions.assertNotNull(parse);
    }

    @Test
    void testFormat() {
    }

    @Test
    void testFormat1() {
    }

    @Test
    void testFormat2() {
    }

    @Test
    void testFormat3() {
    }

    @Test
    void testFormat4() {
    }

    @Test
    void testFormat5() {
    }

    @Test
    void testFormat6() {
    }

    @Test
    void testFormat7() {
    }

    @Test
    void testFormat8() {
    }

    @Test
    void parseLocalDateTime() {
    }

    @Test
    void testParseLocalDateTime() {
    }

    @Test
    void parseIsoLocalDateTime() {
    }

    @Test
    void parseLocalDate() {
    }

    @Test
    void testParseLocalDateTime1() {
    }

    @Test
    void toLocalDateTime() {
    }
}