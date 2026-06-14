package com.cpt202.context;

import com.cpt202.security.AuthContext;

/**
 * Stores the authenticated user for the current request thread.
 */
public final class BaseContext {

    private static final ThreadLocal<AuthContext> AUTH_CONTEXT_HOLDER = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void setCurrent(AuthContext authContext) {
        AUTH_CONTEXT_HOLDER.set(authContext);
    }

    public static AuthContext getCurrent() {
        return AUTH_CONTEXT_HOLDER.get();
    }

    public static Long getCurrentUserId() {
        AuthContext authContext = getCurrent();
        return authContext == null ? null : authContext.userId();
    }

    public static void clear() {
        AUTH_CONTEXT_HOLDER.remove();
    }
}
