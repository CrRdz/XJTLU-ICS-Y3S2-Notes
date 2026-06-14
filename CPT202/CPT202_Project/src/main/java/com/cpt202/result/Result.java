package com.cpt202.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Unified API response wrapper.
 * <p>
 * Convention:
 * 1. code = 1 means success
 * 2. code = 0 means business failure
 * 3. data contains the response payload
 * 4. msg is mainly used for error messages
 *
 * @param <T> response payload type
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(1, null, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(1, null, data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(0, msg, null);
    }
}
