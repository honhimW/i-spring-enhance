package io.github.honhimw.util;

import com.github.luben.zstd.RecyclingBufferPool;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.google.errorprone.annotations.ThreadSafe;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.SingleXZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.*;
import java.util.function.Function;
import java.util.zip.*;

/**
 * @author hon_him
 * @since 2024-12-24
 */

@ThreadSafe
public class ArchiveUtils {

    public static final int BUFFER_SIZE = 1 << 12;

    private ArchiveUtils() {
    }

    public static <T extends ArchiverBuilder<T>> Archiver create(Function<Selector, ArchiverBuilder<T>> builder) {
        Selector selector = new Selector();
        ArchiverBuilder<T> apply = builder.apply(selector);
        return apply.build();
    }

    public static Archiver kindOf(Kind kind) {
        if (kind == null) {
            return UnSupported.INSTANCE;
        }
        checkAvailable(kind);
        return switch (kind) {
            case DEFLATE -> Deflate.INSTANCE;
            case GZIP -> Gzip.INSTANCE;
            case XZ -> Xz.INSTANCE;
            case Z_STD -> Zstd.INSTANCE;
        };
    }

    public static Archiver deflate() {
        return kindOf(Kind.DEFLATE);
    }

    public static Archiver gzip() {
        return kindOf(Kind.GZIP);
    }

    public static Archiver xz() {
        return kindOf(Kind.XZ);
    }

    public static Archiver zstd() {
        return kindOf(Kind.Z_STD);
    }

    public enum Kind {
        DEFLATE,
        GZIP,
        XZ {
            private final boolean available = isXZCompressionAvailable();

            @Override
            public boolean available() {
                return available;
            }

            @Override
            public String message() {
                return "can't find `%s` in classpath.".formatted("org.tukaani:xz");
            }
        },
        Z_STD {
            private final boolean available = isZstdCompressionAvailable();

            @Override
            public boolean available() {
                return available;
            }

            @Override
            public String message() {
                return "can't find `%s` in classpath.".formatted("com.github.luben:zstd-jni");
            }
        },
        ;

        public boolean available() {
            return true;
        }

        public String message() {
            return "[%s] is not available.".formatted(this);
        }

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Selector {
        public GzipBuilder gzip() {
            return new Gzip.Builder();
        }

        public DeflateBuilder deflate() {
            return new Deflate.Builder();
        }

        public XzBuilder xz() {
            checkAvailable(Kind.XZ);
            return new Xz.Builder();
        }

        public ZstdBuilder zstd() {
            checkAvailable(Kind.Z_STD);
            return new Zstd.Builder();
        }
    }

    public interface ArchiverBuilder<T extends ArchiverBuilder<T>> {
        T autoClose(boolean autoClose);

        T bufferSize(int bufferSize);

        T useBufferedStream(boolean useBufferedStream);

        Archiver build();
    }

    @SuppressWarnings("unchecked")
    private static abstract class AbstractArchiverBuilder<T extends ArchiverBuilder<T>> implements ArchiverBuilder<T> {
        boolean autoClose = true;
        int bufferSize = BUFFER_SIZE;
        boolean useBufferedStream = true;

        @Override
        public T autoClose(boolean autoClose) {
            this.autoClose = autoClose;
            return (T) this;
        }

        @Override
        public T bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return (T) this;
        }

        @Override
        public T useBufferedStream(boolean useBufferedStream) {
            this.useBufferedStream = useBufferedStream;
            return (T) this;
        }
    }

    public interface DeflateBuilder extends ArchiverBuilder<DeflateBuilder> {
        /**
         * @param level 0-9, -1 for default
         * @return this
         */
        DeflateBuilder level(int level);

        /**
         * @param size default 512
         * @return this
         * @see DeflaterOutputStream#DeflaterOutputStream(OutputStream, Deflater, int, boolean)
         */
        DeflateBuilder size(int size);

        /**
         * @param syncFlush default false
         * @return this
         * @see GZIPOutputStream#GZIPOutputStream(OutputStream, int, boolean)
         */
        DeflateBuilder syncFlush(boolean syncFlush);
    }

    public interface GzipBuilder extends ArchiverBuilder<GzipBuilder> {
        /**
         * @param size default 512
         * @return this
         * @see GZIPInputStream#GZIPInputStream(InputStream, int)
         * @see GZIPOutputStream#GZIPOutputStream(OutputStream, int, boolean)
         */
        GzipBuilder size(int size);

        /**
         * @param syncFlush default false
         * @return this
         * @see GZIPOutputStream#GZIPOutputStream(OutputStream, int, boolean)
         */
        GzipBuilder syncFlush(boolean syncFlush);
    }

    public interface XzBuilder extends ArchiverBuilder<XzBuilder> {
        /**
         * @param preset 0-9, 6 for default
         * @return this
         */
        XzBuilder preset(int preset);
    }

    public interface ZstdBuilder extends ArchiverBuilder<ZstdBuilder> {
        /**
         * @param level [-7,22], 3 for default
         * @return this
         */
        ZstdBuilder level(int level);
    }

    public interface Archiver {
        void compress(InputStream ips, OutputStream ops) throws IOException;

        void decompress(InputStream ips, OutputStream ops) throws IOException;

        default Bytes compress(Bytes bytes) {
            InputStream inputStream = bytes.toInputStream();
            ByteArrayOutputStream ops = new ByteArrayOutputStream();
            try {
                compress(inputStream, ops);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            return Bytes.wrap(ops.toByteArray());
        }

        default Bytes decompress(Bytes bytes) {
            InputStream inputStream = bytes.toInputStream();
            ByteArrayOutputStream ops = new ByteArrayOutputStream();
            try {
                decompress(inputStream, ops);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            return Bytes.wrap(ops.toByteArray());
        }
    }

    private static abstract class AbstractArchiver implements Archiver {
        final boolean autoClose;
        final int bufferSize;
        final boolean useBufferedStream;

        AbstractArchiver(boolean autoClose, int bufferSize, boolean useBufferedStream) {
            this.autoClose = autoClose;
            this.bufferSize = bufferSize;
            this.useBufferedStream = useBufferedStream;
        }

        void copy(InputStream ips, OutputStream ops) throws IOException {
            final InputStream _ips;
            final OutputStream _ops;
            if (useBufferedStream) {
                _ips = new BufferedInputStream(ips, bufferSize);
                _ops = new BufferedOutputStream(ops, bufferSize);
            } else {
                _ips = ips;
                _ops = ops;
            }
            int count;
            byte[] buffer = new byte[bufferSize];
            while ((count = _ips.read(buffer, 0, bufferSize)) != -1) {
                _ops.write(buffer, 0, count);
            }
        }

        void tryClose(InputStream ips, OutputStream ops) throws IOException {
            if (autoClose) {
                ips.close();
                ops.close();
            }
        }

    }

    private static final class UnSupported implements Archiver {

        private static final UnSupported INSTANCE = new UnSupported();

        @Override
        public void compress(InputStream ips, OutputStream ops) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void decompress(InputStream ips, OutputStream ops) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bytes compress(Bytes bytes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bytes decompress(Bytes bytes) {
            throw new UnsupportedOperationException();
        }
    }

    private static class Deflate extends AbstractArchiver {

        private static final Deflate INSTANCE = new Deflate(new Builder());

        private final int level;
        private final int size;
        private final boolean syncFlush;

        Deflate(Deflate.Builder builder) {
            super(builder.autoClose, builder.bufferSize, builder.useBufferedStream);
            this.level = builder.level;
            this.size = builder.size;
            this.syncFlush = builder.syncFlush;
        }

        @Override
        public void compress(InputStream ips, OutputStream ops) throws IOException {
            Deflater def = new Deflater(level);
            try {
                DeflaterOutputStream dfops = new DeflaterOutputStream(ops, def, size, syncFlush);
                copy(ips, dfops);
                dfops.finish();
                dfops.flush();
                tryClose(ips, dfops);
            } finally {
                def.end();
            }
        }

        @Override
        public void decompress(InputStream ips, OutputStream ops) throws IOException {
            Deflater def = new Deflater();
            try {
                DeflaterInputStream dfips = new DeflaterInputStream(ips, def, size);
                copy(dfips, ops);
                dfips.close();
            } finally {
                def.end();
            }
        }

        private static class Builder extends AbstractArchiverBuilder<DeflateBuilder> implements DeflateBuilder {
            private int level = -1;
            private int size = 512;
            private boolean syncFlush = false;

            @Override
            public DeflateBuilder level(int level) {
                this.level = level;
                return this;
            }

            @Override
            public DeflateBuilder size(int size) {
                this.size = size;
                return this;
            }

            @Override
            public DeflateBuilder syncFlush(boolean syncFlush) {
                this.syncFlush = syncFlush;
                return this;
            }

            @Override
            public Archiver build() {
                return new Deflate(this);
            }
        }

    }

    private static class Gzip extends AbstractArchiver {

        private static final Gzip INSTANCE = new Gzip(new Builder());

        private final int size;
        private final boolean syncFlush;

        Gzip(Builder builder) {
            super(builder.autoClose, builder.bufferSize, builder.useBufferedStream);
            this.size = builder.size;
            this.syncFlush = builder.syncFlush;
        }

        @Override
        public void compress(InputStream ips, OutputStream ops) throws IOException {
            GZIPOutputStream gzops = new GZIPOutputStream(ops, size, syncFlush);
            copy(ips, gzops);
            gzops.finish();
            gzops.flush();
            tryClose(ips, gzops);
        }

        @Override
        public void decompress(InputStream ips, OutputStream ops) throws IOException {
            GZIPInputStream gzips = new GZIPInputStream(ips, size);
            copy(gzips, ops);
            tryClose(gzips, ops);
        }

        private static class Builder extends AbstractArchiverBuilder<GzipBuilder> implements GzipBuilder {
            private int size = 512;
            private boolean syncFlush = false;

            @Override
            public GzipBuilder size(int size) {
                this.size = size;
                return this;
            }

            @Override
            public GzipBuilder syncFlush(boolean syncFlush) {
                this.syncFlush = syncFlush;
                return this;
            }

            @Override
            public Archiver build() {
                return new Gzip(this);
            }
        }

    }

    private static class Xz extends AbstractArchiver {

        private static final Xz INSTANCE = new Xz(new Builder());

        private final int preset;

        public Xz(Builder builder) {
            super(builder.autoClose, builder.bufferSize, builder.useBufferedStream);
            this.preset = builder.preset;
        }

        @Override
        public void compress(InputStream ips, OutputStream ops) throws IOException {
            XZOutputStream xzops = new XZOutputStream(ops, new LZMA2Options(preset));
            copy(ips, xzops);
            xzops.flush();
            xzops.finish();
            tryClose(ips, xzops);
        }

        @Override
        public void decompress(InputStream ips, OutputStream ops) throws IOException {
            SingleXZInputStream xzips = new SingleXZInputStream(ips);
            copy(xzips, ops);
            tryClose(xzips, ops);
        }

        private static class Builder extends AbstractArchiverBuilder<XzBuilder> implements XzBuilder {
            private int preset = 6;

            @Override
            public XzBuilder preset(int preset) {
                this.preset = preset;
                return this;
            }

            @Override
            public Archiver build() {
                return new Xz(this);
            }
        }

    }

    private static class Zstd extends AbstractArchiver {

        private static final Zstd INSTANCE = new Zstd(new Builder());

        private final int level;

        public Zstd(Builder builder) {
            super(builder.autoClose, builder.bufferSize, builder.useBufferedStream);
            this.level = builder.level;
        }

        @Override
        public void compress(InputStream ips, OutputStream ops) throws IOException {
            ZstdOutputStream zstdops = new ZstdOutputStream(ops, RecyclingBufferPool.INSTANCE, level);
            zstdops.setCloseFrameOnFlush(true);
            copy(ips, zstdops);
            zstdops.flush();
            tryClose(ips, zstdops);
        }

        @Override
        public void decompress(InputStream ips, OutputStream ops) throws IOException {
            ZstdInputStream zstdips = new ZstdInputStream(ips, RecyclingBufferPool.INSTANCE);
            copy(zstdips, ops);
            tryClose(zstdips, ops);
        }

        private static class Builder extends AbstractArchiverBuilder<ZstdBuilder> implements ZstdBuilder {
            private int level = 3;

            @Override
            public ZstdBuilder level(int level) {
                this.level = level;
                return this;
            }

            @Override
            public Archiver build() {
                return new Zstd(this);
            }
        }
    }

    private static void checkAvailable(Kind kind) {
        if (!kind.available()) {
            throw new UnsupportedOperationException(kind.message());
        }
    }

    private static boolean isXZCompressionAvailable() {
        try {
            Class.forName("org.tukaani.xz.XZ");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static boolean isZstdCompressionAvailable() {
        try {
            Class.forName("com.github.luben.zstd.Zstd");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
