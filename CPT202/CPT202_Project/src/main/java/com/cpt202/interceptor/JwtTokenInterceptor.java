package com.cpt202.interceptor;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.SecurityConstants;
import com.cpt202.context.BaseContext;
import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.model.entity.User;
import com.cpt202.properties.JwtProperties;
import com.cpt202.result.Result;
import com.cpt202.security.AuthContext;
import com.cpt202.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Validates JWT tokens for protected API routes and exposes the current user.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        User.UserRole requiredRole = resolveRequiredRole(request.getRequestURI());
        if (requiredRole == null) {
            return true;
        }

        try {
            String token = extractToken(request);
            AuthContext authContext = jwtTokenService.parseToken(token);
            validateRole(authContext, requiredRole);
            BaseContext.setCurrent(authContext);
            return true;
        } catch (UnauthorizedAccessException ex) {
            writeUnauthorized(response, ex.getMessage());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader(jwtProperties.getTokenName());
        String tokenPrefix = jwtProperties.getTokenPrefix();

        if (!StringUtils.hasText(authorization) || !authorization.startsWith(tokenPrefix)) {
            throw new UnauthorizedAccessException(MessageConstants.INVALID_BEARER_TOKEN);
        }

        String token = authorization.substring(tokenPrefix.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedAccessException(MessageConstants.INVALID_BEARER_TOKEN);
        }
        return token;
    }

    private void validateRole(AuthContext authContext, User.UserRole requiredRole) {
        if (authContext.role() != requiredRole) {
            throw new UnauthorizedAccessException(
                    String.format(MessageConstants.ROLE_REQUIRED_TEMPLATE, authContext.role(), requiredRole));
        }
    }

    private User.UserRole resolveRequiredRole(String requestUri) {
        if (requestUri.startsWith("/api/admin/")) {
            return User.UserRole.ADMIN;
        }
        if (requestUri.startsWith("/api/teacher/")) {
            return User.UserRole.TEACHER;
        }
        if (requestUri.startsWith("/api/student/")) {
            return User.UserRole.STUDENT;
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(message)));
    }
}
