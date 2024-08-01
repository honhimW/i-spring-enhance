package io.github.honhimw.spring.data.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2023-04-06
 */

@SuppressWarnings("unused")
@Getter
public enum ResourceMod {

    NONE(0b000, "无"),
    R__(0b100, "读"),
    _W_(0b010, "写"),
    __X(0b001, "执行"),
    R_X(0b101, "读/执行"),
    RW_(0b110, "读/写"),
    _WX(0b011, "写/执行"),
    RWX(0b111, "读/写/执行"),
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
