package io.github.honhimw.spring;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * <span style="font-weight:bold;font-size:x-large;color:green">Number of bytes</span>
 * <pre>
 * KB(kilobyte)  : 1,024
 * MB(megabyte)  : 1,048,576
 * GB(gigabyte)  : 1,073,741,824
 * TB(terabyte)  : 1,099,511,627,776
 * PB(petabyte)  : 1,125,899,906,842,624
 * EB(exabyte)   : 1,152,921,504,606,846,976
 * ZB(zettabyte) : 1,180,591,620,717,411,303,424
 * YB(yottabyte) : 1,208,925,819,614,629,174,706,176
 * BB(brontobyte): 1,237,940,039,285,380,274,899,124,224
 * NB(nanobyte)  : 1,267,650,600,228,229,401,496,703,205,376
 * DB(doggabyte) : 1,298,074,214,633,706,907,132,624,082,305,024
 * </pre>
 *
 * @author hon_him
 * @since 2024-07-12
 */

public class IDataSize implements Serializable, Comparable<IDataSize> {

    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a kilobyte.
     */
    public static final BigDecimal ONE_KB_VAL = BigDecimal.valueOf(ONE_KB);

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a megabyte.
     */
    public static final BigDecimal ONE_MB_VAL = BigDecimal.valueOf(ONE_MB);

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_MB * ONE_KB;

    /**
     * The number of bytes in a gigabyte.
     */
    public static final BigDecimal ONE_GB_VAL = BigDecimal.valueOf(ONE_GB);

    /**
     * The number of bytes in a terabyte.
     */
    public static final long ONE_TB = ONE_GB * ONE_KB;

    /**
     * The number of bytes in a terabyte.
     */
    public static final BigDecimal ONE_TB_VAL = BigDecimal.valueOf(ONE_TB);

    /**
     * The number of bytes in a petabyte.
     */
    public static final long ONE_PB = ONE_TB * ONE_KB;

    /**
     * The number of bytes in a petabyte.
     */
    public static final BigDecimal ONE_PB_VAL = BigDecimal.valueOf(ONE_PB);

    /**
     * The number of bytes in an exabyte.
     */
    public static final long ONE_EB = ONE_PB * ONE_KB;

    /**
     * The number of bytes in an exabyte.
     */
    public static final BigDecimal ONE_EB_VAL = BigDecimal.valueOf(ONE_EB);

    /**
     * The number of bytes in a zettabyte.
     */
    public static final BigDecimal ONE_ZB_VAL = ONE_KB_VAL.multiply(ONE_EB_VAL);

    /**
     * The number of bytes in a yottabyte.
     */
    public static final BigDecimal ONE_YB_VAL = ONE_KB_VAL.multiply(ONE_ZB_VAL);

    /**
     * The number of bytes in a brontobyte.
     */
    public static final BigDecimal ONE_BB_VAL = ONE_KB_VAL.multiply(ONE_YB_VAL);

    /**
     * The number of bytes in a nanobyte.
     */
    public static final BigDecimal ONE_NB_VAL = ONE_KB_VAL.multiply(ONE_BB_VAL);

    /**
     * The number of bytes in a doggabyte.
     */
    public static final BigDecimal ONE_DB_VAL = ONE_KB_VAL.multiply(ONE_NB_VAL);

    private final BigDecimal bytes;

    private IDataSize(BigDecimal bytes) {
        this.bytes = bytes;
    }

    public static IDataSize of(long bytes) {
        return new IDataSize(BigDecimal.valueOf(bytes));
    }

    public static IDataSize of(BigDecimal bytes) {
        Objects.requireNonNull(bytes, "bytes cannot be null");
        return new IDataSize(bytes);
    }

    public static IDataSize ofKB(long kilobytes) {
        return new IDataSize(ONE_KB_VAL.multiply(BigDecimal.valueOf(kilobytes)));
    }

    public static IDataSize ofMB(long megabytes) {
        return new IDataSize(ONE_MB_VAL.multiply(BigDecimal.valueOf(megabytes)));
    }

    public static IDataSize ofGB(long gigabytes) {
        return new IDataSize(ONE_GB_VAL.multiply(BigDecimal.valueOf(gigabytes)));
    }

    public static IDataSize ofTB(long terabytes) {
        return new IDataSize(ONE_TB_VAL.multiply(BigDecimal.valueOf(terabytes)));
    }

    public static IDataSize ofPB(long petabytes) {
        return new IDataSize(ONE_PB_VAL.multiply(BigDecimal.valueOf(petabytes)));
    }

    public static IDataSize ofEB(long exabytes) {
        return new IDataSize(ONE_EB_VAL.multiply(BigDecimal.valueOf(exabytes)));
    }

    public static IDataSize ofZB(long zettabytes) {
        return new IDataSize(ONE_ZB_VAL.multiply(BigDecimal.valueOf(zettabytes)));
    }

    public static IDataSize ofYB(long yottabytes) {
        return new IDataSize(ONE_YB_VAL.multiply(BigDecimal.valueOf(yottabytes)));
    }

    public static IDataSize ofBB(long brontobytes) {
        return new IDataSize(ONE_BB_VAL.multiply(BigDecimal.valueOf(brontobytes)));
    }

    public static IDataSize ofNB(long nanobytes) {
        return new IDataSize(ONE_NB_VAL.multiply(BigDecimal.valueOf(nanobytes)));
    }

    public static IDataSize ofDB(long doggabytes) {
        return new IDataSize(ONE_DB_VAL.multiply(BigDecimal.valueOf(doggabytes)));
    }

    @Override
    public int compareTo(IDataSize other) {
        return bytes.compareTo(other.bytes);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IDataSize iDataSize = (IDataSize) o;
        return Objects.equals(bytes, iDataSize.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bytes);
    }

    @Override
    public String toString() {
        return format(2);
    }

    public IDataSize plus(IDataSize other) {
        return new IDataSize(this.bytes.add(other.bytes));
    }

    public IDataSize minus(IDataSize other) {
        return new IDataSize(this.bytes.subtract(other.bytes));
    }

    public IDataSize multiply(IDataSize other) {
        return new IDataSize(this.bytes.multiply(other.bytes));
    }

    public BigInteger toBytes() {
        return this.bytes.toBigInteger();
    }

    public BigInteger toKB() {
        return this.bytes.divide(ONE_KB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public BigInteger toMB() {
        return this.bytes.divide(ONE_MB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public BigInteger toGB() {
        return this.bytes.divide(ONE_GB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public BigInteger toTB() {
        return this.bytes.divide(ONE_TB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public BigInteger toPB() {
        return this.bytes.divide(ONE_PB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public BigInteger toEB() {
        return this.bytes.divide(ONE_EB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public BigInteger toZB() {
        return this.bytes.divide(ONE_ZB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public BigInteger toYB() {
        return this.bytes.divide(ONE_YB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public BigInteger toBB() {
        return this.bytes.divide(ONE_BB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public BigInteger toNB() {
        return this.bytes.divide(ONE_NB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public BigInteger toDB() {
        return this.bytes.divide(ONE_DB_VAL, 0, RoundingMode.HALF_UP).toBigInteger();
    }

    public String format() {
        return format(0);
    }

    public String format(int scale) {
        scale = Math.abs(scale);

        BigDecimal number = this.bytes;
        String unit;
        if (number.compareTo(ONE_DB_VAL) > 0) {
            number = number.divide(ONE_DB_VAL, scale, RoundingMode.HALF_UP);
            unit = "DB";
        } else if (number.compareTo(ONE_NB_VAL) > 0) {
            number = number.divide(ONE_NB_VAL, scale, RoundingMode.HALF_UP);
            unit = "NB";
        } else if (number.compareTo(ONE_BB_VAL) > 0) {
            number = number.divide(ONE_BB_VAL, scale, RoundingMode.HALF_UP);
            unit = "BB";
        } else if (number.compareTo(ONE_YB_VAL) > 0) {
            number = number.divide(ONE_YB_VAL, scale, RoundingMode.HALF_UP);
            unit = "YB";
        } else if (number.compareTo(ONE_ZB_VAL) > 0) {
            number = number.divide(ONE_ZB_VAL, scale, RoundingMode.HALF_UP);
            unit = "ZB";
        } else if (number.compareTo(ONE_EB_VAL) > 0) {
            number = number.divide(ONE_EB_VAL, scale, RoundingMode.HALF_UP);
            unit = "EB";
        } else if (number.compareTo(ONE_PB_VAL) > 0) {
            number = number.divide(ONE_PB_VAL, scale, RoundingMode.HALF_UP);
            unit = "PB";
        } else if (number.compareTo(ONE_TB_VAL) > 0) {
            number = number.divide(ONE_TB_VAL, scale, RoundingMode.HALF_UP);
            unit = "TB";
        } else if (number.compareTo(ONE_GB_VAL) > 0) {
            number = number.divide(ONE_GB_VAL, scale, RoundingMode.HALF_UP);
            unit = "GB";
        } else if (number.compareTo(ONE_MB_VAL) > 0) {
            number = number.divide(ONE_MB_VAL, scale, RoundingMode.HALF_UP);
            unit = "MB";
        } else if (number.compareTo(ONE_KB_VAL) > 0) {
            number = number.divide(ONE_KB_VAL, scale, RoundingMode.HALF_UP);
            unit = "KB";
        } else {
            number = number.divide(BigDecimal.ONE, scale, RoundingMode.HALF_UP);
            unit = "B";
        }

        return number.toPlainString() + " " + unit;
    }

}
