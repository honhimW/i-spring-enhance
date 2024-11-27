package io.github.honhimw.ddd.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Execute commonly means delete
 *
 * @author hon_him
 * @since 2023-04-06
 */

@SuppressWarnings("unused")
@Getter
public enum ResourceMod {

    NONE(0b000, "None"),
    R__(0b100, "Read"),
    _W_(0b010, "Write"),
    __X(0b001, "Execute"),
    R_X(0b101, "Read/Execute"),
    RW_(0b110, "Read/Write"),
    _WX(0b011, "Write/Execute"),
    RWX(0b111, "Read/Write/Execute"),
    ;

    private final int code;

    private final String _zhCN;

    ResourceMod(int code, String zhCN) {
        this.code = code;
        this._zhCN = zhCN;
    }

    public boolean canRead() {
        return ((1 << 2) & this.code) != 0;
    }

    public boolean canWrite() {
        return ((1 << 1) & this.code) != 0;
    }

    public boolean canExecute() {
        return (1 & this.code) != 0;
    }

    private static final Map<Integer, ResourceMod> CODE_MAP;

    static {
        CODE_MAP = Arrays.stream(values()).collect(Collectors.toMap(ResourceMod::getCode, resourceMod -> resourceMod));
    }

    public static ResourceMod of(int code) {
        return CODE_MAP.get(code);
    }

}
