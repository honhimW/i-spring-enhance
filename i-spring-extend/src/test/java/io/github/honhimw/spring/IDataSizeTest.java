package io.github.honhimw.spring;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.DecimalFormat;

class IDataSizeTest {
    
    private final BigDecimal scale = new BigDecimal("1024");

    @Test
    void of() {
        IDataSize iDataSize = IDataSize.of(8);
        assert iDataSize.toBytes().intValue() == 8;
    }

    @Test
    void ofKB() {
        IDataSize iDataSize = IDataSize.ofKB(8);
        assert iDataSize.toBytes().intValue() == 8 * 1024;
    }

    @Test
    void ofMB() {
        IDataSize iDataSize = IDataSize.ofMB(8);
        assert iDataSize.toBytes().intValue() == 8 * 1024 * 1024;
    }

    @Test
    void ofGB() {
        IDataSize iDataSize = IDataSize.ofGB(8);
        assert iDataSize.toBytes().equals(new BigDecimal("8").multiply(scale.pow(3)).toBigInteger());
    }

    @Test
    void ofTB() {
        IDataSize iDataSize = IDataSize.ofTB(8);
        assert iDataSize.toBytes().equals(new BigDecimal("8").multiply(scale.pow(4)).toBigInteger());
    }

    @Test
    void ofPB() {
        IDataSize iDataSize = IDataSize.ofPB(8);
        assert iDataSize.toBytes().equals(new BigDecimal("8").multiply(scale.pow(5)).toBigInteger());
    }

    @Test
    void ofEB() {
        IDataSize iDataSize = IDataSize.ofEB(8);
        assert iDataSize.toBytes().equals(new BigDecimal("8").multiply(scale.pow(6)).toBigInteger());
    }

    @Test
    void ofZB() {
        IDataSize iDataSize = IDataSize.ofZB(8);
        assert iDataSize.toBytes().equals(new BigDecimal("8").multiply(scale.pow(7)).toBigInteger());
    }

    @Test
    void ofYB() {
        IDataSize iDataSize = IDataSize.ofYB(8);
        assert iDataSize.toBytes().equals(new BigDecimal("8").multiply(scale.pow(8)).toBigInteger());
    }

    @Test
    void ofBB() {
        IDataSize iDataSize = IDataSize.ofBB(8);
        assert iDataSize.toBytes().equals(new BigDecimal("8").multiply(scale.pow(9)).toBigInteger());
    }

    @Test
    void ofNB() {
        IDataSize iDataSize = IDataSize.ofNB(8);
        assert iDataSize.toBytes().equals(new BigDecimal("8").multiply(scale.pow(10)).toBigInteger());
    }

    @Test
    void ofDB() {
        IDataSize iDataSize = IDataSize.ofDB(8);
        assert iDataSize.toBytes().equals(new BigDecimal("8").multiply(scale.pow(11)).toBigInteger());
    }

    @Test
    void compareTo() {
        IDataSize iDataSize = IDataSize.of(8);
        assert iDataSize.compareTo(IDataSize.of(8)) == 0;
        assert iDataSize.compareTo(IDataSize.of(9)) < 0;
        assert iDataSize.compareTo(IDataSize.of(7)) > 0;
    }

    @Test
    void testToString() {
        IDataSize iDataSize = IDataSize.of(8);
        assert iDataSize.toString().equals("8.00 B");
    }

    @Test
    void plus() {
        IDataSize iDataSize = IDataSize.of(8);
        assert iDataSize.plus(IDataSize.of(8)).equals(IDataSize.of(16));
    }

    @Test
    void minus() {
        IDataSize iDataSize = IDataSize.of(8);
        assert iDataSize.minus(IDataSize.of(8)).equals(IDataSize.of(0));
    }

    @Test
    void multiply() {
        IDataSize iDataSize = IDataSize.of(8);
        assert iDataSize.multiply(IDataSize.of(scale)).equals(IDataSize.ofKB(8));
    }

    @Test
    void toBytes() {
        IDataSize iDataSize = IDataSize.of(8);
        assert iDataSize.toBytes().intValue() == 8;
    }

    @Test
    void toKB() {
        IDataSize iDataSize = IDataSize.ofKB(8);
        assert iDataSize.toKB().intValue() == 8;
    }

    @Test
    void toMB() {
        IDataSize iDataSize = IDataSize.ofMB(8);
        assert iDataSize.toMB().intValue() == 8;
    }

    @Test
    void toGB() {
        IDataSize iDataSize = IDataSize.ofGB(8);
        assert iDataSize.toGB().intValue() == 8;
    }

    @Test
    void toTB() {
        IDataSize iDataSize = IDataSize.ofTB(8);
        assert iDataSize.toTB().intValue() == 8;
    }

    @Test
    void toPB() {
        IDataSize iDataSize = IDataSize.ofPB(8);
        assert iDataSize.toPB().intValue() == 8;
    }

    @Test
    void toEB() {
        IDataSize iDataSize = IDataSize.ofEB(8);
        assert iDataSize.toEB().intValue() == 8;
    }

    @Test
    void toZB() {
        IDataSize iDataSize = IDataSize.ofZB(8);
        assert iDataSize.toZB().intValue() == 8;
    }

    @Test
    void toYB() {
        IDataSize iDataSize = IDataSize.ofYB(8);
        assert iDataSize.toYB().intValue() == 8;
    }

    @Test
    void toBB() {
        IDataSize iDataSize = IDataSize.ofBB(8);
        assert iDataSize.toBB().intValue() == 8;
    }

    @Test
    void toNB() {
        IDataSize iDataSize = IDataSize.ofNB(8);
        assert iDataSize.toNB().intValue() == 8;
    }

    @Test
    void toDB() {
        IDataSize iDataSize = IDataSize.ofDB(8);
        assert iDataSize.toDB().intValue() == 8;
    }

    @Test
    void format() {
        IDataSize iDataSize = IDataSize.of(123 * 1024 * 1024 + 456 * 1024 + 789);
        assert iDataSize.format().equals("123 MB");
        assert iDataSize.format(1).equals("123.4 MB");
        assert iDataSize.format(2).equals("123.45 MB");
    }

    @Test
    void echo() {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        System.out.println("1KB: " + decimalFormat.format(IDataSize.ofKB(1).toBytes()));
        System.out.println("1MB: " + decimalFormat.format(IDataSize.ofMB(1).toBytes()));
        System.out.println("1GB: " + decimalFormat.format(IDataSize.ofGB(1).toBytes()));
        System.out.println("1TB: " + decimalFormat.format(IDataSize.ofTB(1).toBytes()));
        System.out.println("1PB: " + decimalFormat.format(IDataSize.ofPB(1).toBytes()));
        System.out.println("1EB: " + decimalFormat.format(IDataSize.ofEB(1).toBytes()));
        System.out.println("1ZB: " + decimalFormat.format(IDataSize.ofZB(1).toBytes()));
        System.out.println("1YB: " + decimalFormat.format(IDataSize.ofYB(1).toBytes()));
        System.out.println("1BB: " + decimalFormat.format(IDataSize.ofBB(1).toBytes()));
        System.out.println("1NB: " + decimalFormat.format(IDataSize.ofNB(1).toBytes()));
        System.out.println("1DB: " + decimalFormat.format(IDataSize.ofDB(1).toBytes()));
    }

}