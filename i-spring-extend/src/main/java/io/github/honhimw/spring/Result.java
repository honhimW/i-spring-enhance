package io.github.honhimw.spring;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2024-07-31
 */

@SuppressWarnings("unused")
public class Result<T> implements Serializable {

    protected String code;

    protected String msg;

    protected T data;

    protected Result() {
    }

    protected Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public String code() {
        return code;
    }

    public Result<T> code(String code) {
        this.code = code;
        return this;
    }

    public String msg() {
        return msg;
    }

    public Result<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public T data() {
        return data;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    public static Result<Void> empty() {
        return new Result<>();
    }

    public static Result<Void> any() {
        return new Result<>();
    }

    public static <T> Result<T> ok() {
        return new Result<>("200", "okay", null);
    }

    public static <T> Result<T> okWithMsg(String msg) {
        return new Result<>("200", msg, null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>("200", "okay", data);
    }

    public static <T> Result<T> error() {
        return new Result<>("500", "error", null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>("500", msg, null);
    }

    public static <T> Result<T> error(String code, String msg) {
        return new Result<>(code, msg, null);
    }

    public <R> Result<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper should not be null.");
        R r = mapper.apply(this.data);
        return new Result<>(this.code, this.msg, r);
    }

    public Result<Void> toEmpty() {
        return new Result<>(this.code, this.msg, null);
    }

    @Override
    public String toString() {
        return "code='%s', msg='%s', data=%s".formatted(code, msg, data);
    }



}
