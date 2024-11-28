package io.github.honhimw.core;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2024-11-11
 */
@Getter
@Setter
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class IResult<T> implements Serializable {

    public static final Codes OK = Codes.OK;
    public static final Codes BAD = Codes.BAD;
    public static final Codes ERROR = Codes.ERROR;

    protected Integer code;

    protected String msg;

    protected T data;

    protected IResult() {
    }

    protected IResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Integer code() {
        return this.getCode();
    }

    public IResult<T> code(Integer code) {
        this.code = code;
        return this;
    }

    public String msg() {
        return this.getMsg();
    }

    public IResult<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public T data() {
        return this.getData();
    }

    public IResult<T> data(T data) {
        this.data = data;
        return this;
    }

    public static IResult<Void> none() {
        return new IResult<>();
    }

    public static <T> IResult<T> empty() {
        return new IResult<>();
    }

    public static <T> IResult<T> ok() {
        return Codes.OK.$();
    }

    public static <T> IResult<T> okWithMsg(String msg) {
        return Codes.OK.msg(msg);
    }

    public static <T> IResult<T> ok(T data) {
        return Codes.OK.of(data);
    }

    public static <T> IResult<T> bad() {
        return Codes.BAD.$();
    }

    public static <T> IResult<T> bad(String msg) {
        return Codes.BAD.msg(msg);
    }

    public static <T> IResult<T> err() {
        return Codes.ERROR.$();
    }

    public static <T> IResult<T> err(String msg) {
        return Codes.ERROR.msg(msg);
    }

    public static <T> IResult<T> of(Integer code, String msg) {
        return new IResult<>(code, msg, null);
    }

    public <R> IResult<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper should not be null.");
        R r = mapper.apply(this.data);
        return new IResult<>(this.code, this.msg, r);
    }

    public IResult<Void> toNone() {
        return new IResult<>(this.code, this.msg, null);
    }

    public boolean okay() {
        return Objects.equals(OK.code, this.code);
    }

    public boolean fail() {
        return !okay();
    }

    @Override
    public String toString() {
        return "code: [%s] - msg: [%s] - data: [%s]".formatted(code, msg, data);
    }

    public enum Codes {
        OK(0, "{okay}"),
        BAD(400, "{bad.request}"),
        ERROR(500, "{internal.server.error}");

        private final int code;
        private final String brief;

        Codes(int code, String brief) {
            this.code = code;
            this.brief = brief;
        }

        public int code() {
            return this.code;
        }

        public String brief() {
            return this.brief;
        }

        public <T> IResult<T> $() {
            return new IResult<>(this.code, this.brief, null);
        }

        public IResult<Void> none() {
            return new IResult<>(this.code, this.brief, null);
        }

        public <T> IResult<T> msg(String message) {
            return new IResult<>(this.code, message, null);
        }

        public <T> IResult<T> of(T data) {
            return new IResult<>(this.code, this.brief, data);
        }
    }

}
