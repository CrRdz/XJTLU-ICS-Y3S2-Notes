package com.cpt202.exception;

/**
 * 越权访问异常。
 * 当用户身份不存在或角色与所请求资源不匹配时抛出。
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
