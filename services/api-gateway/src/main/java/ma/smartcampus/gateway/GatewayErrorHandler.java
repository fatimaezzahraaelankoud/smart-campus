package ma.smartcampus.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

/**
 * Remplace la réponse d'erreur par défaut de Spring Cloud Gateway par le format commun de la plateforme
 * (aucune trace technique exposée). Service injoignable : 503 ; délai dépassé : 504.
 */
@Component
@Order(-2)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayErrorHandler.class);

    private final ErrorWriter errors;

    public GatewayErrorHandler(ErrorWriter errors) {
        this.errors = errors;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        int status = resolveStatus(ex);
        if (status >= 500) {
            log.warn("Erreur gateway {} sur {} : {}", status, exchange.getRequest().getURI().getPath(), ex.toString());
        }
        return errors.write(exchange, status, codeFor(status), messageFor(status));
    }

    static int resolveStatus(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof ResponseStatusException r) {
                return r.getStatusCode().value();
            }
            if (t instanceof ConnectException || t.getClass().getName().contains("ConnectTimeout")) {
                return 503;
            }
            if (t instanceof TimeoutException || t.getClass().getName().contains("ReadTimeout")) {
                return 504;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return 500;
    }

    static String codeFor(int status) {
        return switch (status) {
            case 400 -> "MALFORMED_REQUEST";
            case 404 -> "NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 502 -> "BAD_GATEWAY";
            case 503 -> "SERVICE_UNAVAILABLE";
            case 504 -> "GATEWAY_TIMEOUT";
            default -> status >= 500 ? "INTERNAL_ERROR" : "REQUEST_ERROR";
        };
    }

    static String messageFor(int status) {
        return switch (status) {
            case 404 -> "Ressource introuvable";
            case 503 -> "Service momentanément indisponible";
            case 504 -> "Le service a mis trop de temps à répondre";
            default -> status >= 500 ? "Une erreur interne est survenue" : "Requête invalide";
        };
    }
}
