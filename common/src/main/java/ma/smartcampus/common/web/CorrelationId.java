package ma.smartcampus.common.web;

import java.util.UUID;

public final class CorrelationId {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    /** Un identifiant fourni par un client n'est accepté que s'il est court et sans caractère de contrôle. */
    public static boolean isAcceptable(String value) {
        return value != null && !value.isBlank() && value.length() <= 64 && value.matches("[A-Za-z0-9._-]+");
    }
}
