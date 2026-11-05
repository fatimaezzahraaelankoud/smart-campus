package ma.smartcampus.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Format d'erreur unique de toute la plateforme (voir cahier de développement, section 3.3).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String correlationId,
        List<FieldError> details) {

    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(int status, String code, String message, String path, String correlationId) {
        return new ErrorResponse(Instant.now(), status, code, message, path, correlationId, null);
    }

    public static ErrorResponse of(int status, String code, String message, String path, String correlationId,
                                   List<FieldError> details) {
        return new ErrorResponse(Instant.now(), status, code, message, path, correlationId, details);
    }
}
