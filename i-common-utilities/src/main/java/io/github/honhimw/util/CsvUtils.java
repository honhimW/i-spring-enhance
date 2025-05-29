package io.github.honhimw.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.collections4.IteratorUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Require:
 * <pre>
 * ```gradle
 * implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-csv'
 * ```
 * </pre
 *
 * @author honhimW
 * @since 2025-05-20
 */

public class CsvUtils {

    private static final byte[] UTF_8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private static CsvMapper MAPPER = defaultBuilder().build();

    private static Encoding ENCODING = Encoding.UTF_8_WITH_BOM;

    public static CsvMapper mapper() {
        return MAPPER;
    }

    public static void mapper(CsvMapper mapper) {
        MAPPER = mapper;
    }

    public static CsvMapper.Builder defaultBuilder() {
        return builder(JsonUtils.mapper());
    }

    public static CsvMapper.Builder builder(ObjectMapper objectMapper) {
        CsvMapper.Builder builder = CsvMapper.builder();
        builder
            .serializerFactory(objectMapper.getSerializerFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return builder;
    }

    public enum Encoding {
        GBK(Charset.forName("GBK")),
        UTF_8_WITH_BOM(StandardCharsets.UTF_8),
        UTF_8(StandardCharsets.UTF_8),
        ;
        private final Charset charset;

        Encoding(Charset charset) {
            this.charset = charset;
        }
    }

    public static void encoding(Encoding encoding) {
        if (encoding != null) {
            ENCODING = encoding;
        }
    }

    public static boolean isUtf8Bom(byte[] bytes) {
        if (bytes.length > 3) {
            return bytes[0] == UTF_8_BOM[0] && bytes[1] == UTF_8_BOM[1] && bytes[2] == UTF_8_BOM[2];
        }
        return false;
    }

    public static String toCsv(Collection<?> col, CsvSchema schema) {
        return new Handler(MAPPER, ENCODING).toCsv(col, schema);
    }

    public static <T> String toCsv(Collection<T> col, Class<T> clazz) {
        return new Handler(MAPPER, ENCODING).toCsv(col, clazz);
    }

    public static <T> List<T> fromCsv(String csv, Class<T> clazz) {
        return new Handler(MAPPER, ENCODING).fromCsv(csv, clazz);
    }

    public static <T> List<T> fromCsv(InputStream in, Class<T> clazz) {
        return new Handler(MAPPER, ENCODING).fromCsv(in, clazz);
    }

    public static <T> List<T> fromCsv(byte[] bytes, Class<T> clazz) {
        return new Handler(MAPPER, ENCODING).fromCsv(bytes, clazz);
    }

    public static <T> List<T> fromCsv(byte[] bytes, TypeReference<T> typeRef) {
        return new Handler(MAPPER, ENCODING).fromCsv(bytes, typeRef);
    }

    public static <T> List<T> fromCsv(byte[] bytes, JavaType type) {
        return new Handler(MAPPER, ENCODING).fromCsv(bytes, type);
    }

    public static void write(Collection<?> col, CsvSchema schema, OutputStream out) {
        new Handler(MAPPER, ENCODING).write(col, schema, out);
    }

    public static <T> void write(Collection<T> col, Class<T> clazz, OutputStream out) {
        new Handler(MAPPER, ENCODING).write(col, clazz, out);
    }

    public static class Handler {
        private final CsvMapper mapper;

        private final Encoding encoding;

        public Handler(CsvMapper mapper, Encoding encoding) {
            this.mapper = mapper;
            this.encoding = encoding;
        }

        public String toCsv(Collection<?> col, CsvSchema schema) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(col, schema, out);
            return out.toString(encoding.charset);
        }

        public <T> String toCsv(Collection<T> col, Class<T> clazz) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(col, clazz, out);
            return out.toString(encoding.charset);
        }

        public <T> List<T> fromCsv(String csv, Class<T> clazz) {
            return fromCsv(csv.getBytes(encoding.charset), clazz);
        }

        public <T> List<T> fromCsv(InputStream in, Class<T> clazz) {
            byte[] bytes;
            try {
                bytes = in.readAllBytes();
            } catch (IOException e) {
                throw new IllegalStateException("can't read input-stream.", e);
            }
            return fromCsv(bytes, clazz);
        }

        public <T> List<T> fromCsv(byte[] bytes, Class<T> clazz) {
            return fromCsv(bytes, mapper.constructType(clazz));
        }

        public <T> List<T> fromCsv(byte[] bytes, TypeReference<T> typeRef) {
            return fromCsv(bytes, mapper.constructType(typeRef));
        }

        public <T> List<T> fromCsv(byte[] bytes, JavaType type) {
            int offset = 0;
            Charset charset;
            if (isUtf8Bom(bytes)) {
                charset = StandardCharsets.UTF_8;
                offset = 3;
            } else {
                charset = encoding.charset;
            }
            InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bytes, offset, bytes.length), charset);
            try (MappingIterator<T> iterator = mapper.readerFor(type).with(mapper.schemaWithHeader()).readValues(reader)) {
                return IteratorUtils.toList(iterator);
            } catch (Exception e) {
                throw new IllegalArgumentException("can't deserialize from csv.", e);
            }
        }

        public void write(Collection<?> col, CsvSchema schema, OutputStream out) {
            try {
                OutputStreamWriter writer = new OutputStreamWriter(out, encoding.charset);
                SequenceWriter sequenceWriter = mapper.writer().with(schema).writeValues(writer).writeAll(col);
                sequenceWriter.flush();
            } catch (IOException e) {
                throw new IllegalArgumentException("can't serialize as csv.", e);
            }
        }

        public <T> void write(Collection<T> col, Class<T> clazz, OutputStream out) {
            try {
                if (encoding == Encoding.UTF_8_WITH_BOM) {
                    out.write(UTF_8_BOM);
                }
                OutputStreamWriter writer = new OutputStreamWriter(out, encoding.charset);
                CsvSchema columns = mapper.schemaFor(clazz).withHeader();
                SequenceWriter sequenceWriter = mapper.writer().with(columns).writeValues(writer).writeAll(col);
                sequenceWriter.flush();
            } catch (IOException e) {
                throw new IllegalArgumentException("can't serialize as csv.", e);
            }
        }

    }

}
