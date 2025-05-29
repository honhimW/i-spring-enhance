package io.github.honhimw.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hon_him
 * @since 2024-08-22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisJavaSerial implements Serializable {

    private String string;

    private Integer integer;

    private Boolean booleanValue;

    private Short shortValue;

    private Long longValue;

    private Byte byteValue;

    private Double doubleValue;

    private Float floatValue;

    private int i;

    private short s;

    private long l;

    private byte by;

    private double d;

    private float f;

    private boolean b;

    public static RedisJavaSerial defaultObject() {
        RedisJavaSerial redisJavaSerial = new RedisJavaSerial();
        redisJavaSerial.setString("string");
        redisJavaSerial.setInteger(Integer.MAX_VALUE - 1);
        redisJavaSerial.setBooleanValue(true);
        redisJavaSerial.setShortValue(Short.MAX_VALUE);
        redisJavaSerial.setLongValue(Long.MAX_VALUE);
        redisJavaSerial.setByteValue(Byte.MAX_VALUE);
        redisJavaSerial.setDoubleValue(Double.MAX_VALUE);
        redisJavaSerial.setFloatValue(Float.MAX_VALUE);
        return redisJavaSerial;
    }

}
