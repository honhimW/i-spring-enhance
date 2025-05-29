package io.github.honhimw.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author honhimW
 * @since 2025-05-20
 */

public class CsvUtilsTest {

    @Test
    @SneakyThrows
    void writeString() {
        FbRecord fb = new FbRecord("bar");
        String csv = CsvUtils.toCsv(List.of(fb), FbRecord.class);
        System.out.println(csv);

        List<FbRecord> fbRecords = CsvUtils.fromCsv(csv, FbRecord.class);
        System.out.println(fbRecords);

    }

    public static class Fb {
        public String foo;

        public String getFoo() {
            return foo;
        }

        public Fb setFoo(String foo) {
            this.foo = foo;
            return this;
        }
    }

    public record FbRecord(String foo) {
    }

}
