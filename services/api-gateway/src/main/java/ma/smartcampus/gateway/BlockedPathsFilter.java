package ma.smartcampus.gateway;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Locale;

/**
 * Les chemins {@code /internal/**} (appels de service à service) et les endpoints Actuator autres que la santé
 * ne sont jamais accessibles depuis l'extérieur : réponse 404 (on ne révèle pas leur existence).
 * Ce filtre passe avant Spring Security, donc même un utilisateur authentifié est bloqué.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class BlockedPathsFilter implements WebFilter {

    private final ErrorWriter errors;

    public BlockedPathsFilter(ErrorWriter errors) {
        this.errors = errors;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // getURI().getPath() est déjà décodé : %2e, %2f et variantes sont vus tels quels
        String path = exchange.getRequest().getURI().getPath().toLowerCase(Locale.ROOT);
        if (isBlocked(path)) {
            return errors.write(exchange, 404, "NOT_FOUND", "Ressource introuvable");
        }
        return chain.filter(exchange);
    }

    static boolean isBlocked(String path) {
        boolean internal = path.equals("/internal") || path.startsWith("/internal/") || path.contains("/internal/")
                || path.endsWith("/internal");
        boolean actuator = path.startsWith("/actuator") && !path.startsWith("/actuator/health");
        return internal || actuator;
    }
}
