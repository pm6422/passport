package org.infinity.passport.utils;

import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

import static org.infinity.passport.utils.id.IdGenerator.generateTimestampId;

/**
 * https://www.toutiao.com/i6807750667449401869/
 * https://www.toutiao.com/i6949421858303377923/
 */
public class TraceIdUtils {

    public static final String TRACE_ID = "traceId";

    public static void setTraceId(HttpServletRequest request) {
        String traceId = Optional.ofNullable(request.getHeader(TRACE_ID)).orElse(generateTraceId());
        MDC.put(TRACE_ID, traceId);
    }

    public static void setTraceId(HttpServletResponse response) {
        Optional.ofNullable(response).ifPresent(resp -> resp.setHeader(TRACE_ID, MDC.get(TRACE_ID)));
    }

    public static String getTraceId() {
        return Optional.ofNullable(MDC.get(TRACE_ID)).orElse(generateTraceId());
    }

    public static void remove() {
        MDC.remove(TRACE_ID);
    }

    /**
     * Bind MDC of parent thread to its child thread
     *
     * @param context context map
     */
    public static void setParentMdcToChild(Map<String, String> context) {
        if (context == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(context);
        }
        putTraceIdIfAbsent();
    }

    private static String generateTraceId() {
        return "T" + generateTimestampId();
    }

    private static void putTraceIdIfAbsent() {
        if (MDC.get(TRACE_ID) == null) {
            MDC.put(TRACE_ID, generateTraceId());
        }
    }
}
