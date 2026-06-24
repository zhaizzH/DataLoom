package top.zhaizz.common;

import lombok.Data;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
public class Result<T> {

    private int code;
    private boolean success;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.success = true;
        result.message = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = success(data);
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.success = false;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = error(message);
        result.code = code;
        return result;
    }

}
