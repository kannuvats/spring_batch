package com.batchprocessing.batch.response;

import javax.servlet.http.HttpServletResponse;

public class ResponseHolder {
    private static final ThreadLocal<HttpServletResponse> responseHolder = new ThreadLocal<>();

    public static void setResponse(HttpServletResponse response) {
        responseHolder.set(response);
    }

    public static HttpServletResponse getResponse() {
        return responseHolder.get();
    }

    public static void clear() {
        responseHolder.remove();
    }
}