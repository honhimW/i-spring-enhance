package io.github.honhimw.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author hon_him
 * @since 2024-11-22
 */

public class SafeDateFormatTest {

    @Test
    @SneakyThrows
    void format() {
        SafeDateFormat dateFormat = SafeDateFormat.create(DateTimeUtils.DEFAULT_DATE_TIME_PATTERN + ".SSS");
        String format = dateFormat.format(new Date());
        System.out.println(format);
    }

    @Test
    @SneakyThrows
    void parse() {
        SafeDateFormat dateFormat = SafeDateFormat.create(DateTimeUtils.DEFAULT_DATE_TIME_PATTERN + ".SSS");
        Date parse = dateFormat.parse("2024-11-22 11:11:11.111");
        System.out.println(parse);
    }

    @Test
    @SneakyThrows
    void multiThread() {
        SafeDateFormat dateFormat = SafeDateFormat.create(DateTimeUtils.DEFAULT_DATE_TIME_PATTERN + ".SSS");
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Thread.sleep(new Random().nextInt(10, 100));
            dateList.add(new Date());
        }
        List<String> results = ThreadUtils.block(20, dateList, date -> {
            String format = dateFormat.format(date);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), DateTimeUtils.getSystemOffset());
            System.out.println(format);
            assert format.equals(DateTimeUtils.format(localDateTime, DateTimeUtils.DEFAULT_DATE_TIME_PATTERN + ".SSS")): format;
            return Stream.of(format);
        });

        ThreadUtils.block(20, results, format -> {
            try {
                Date date = dateFormat.parse(format);
                LocalDateTime localDateTime = DateTimeUtils.parseLocalDateTime(format, DateTimeUtils.DEFAULT_DATE_TIME_PATTERN + ".SSS");
                assert date.toInstant().equals(localDateTime.toInstant(DateTimeUtils.getSystemOffset()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return Stream.empty();
        });

    }
}
