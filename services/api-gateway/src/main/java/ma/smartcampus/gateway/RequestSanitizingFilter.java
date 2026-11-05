package ma.smartcampus.gateway;

import ma.smartcampus.common.security.Roles;
import ma.smartcampus.common.web.CorrelationId;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Premier filtre de la gateway :
 * <ul>
 *   <li>garantit un {@code X-Correlation-Id} (celui du client s'il est sûr, sinon généré), transmis aux services
 *       et renvoyé dans la réponse ;</li>
 *   <li>supprime {@code X-User-Id} et {@code X-User-Role} envoyés par le client : seule la gateway les pose,
 *       après validation du JWT (voir {@link UserHeadersGlobalFilter}).</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestSanitizingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst(CorrelationId.HEADER);
        String id = CorrelationId.isAcceptable(incoming) ? incoming : CorrelationId.generate();

        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.remove(Roles.HEADER_USER_ID);
                    h.remove(Roles.HEADER_USER_ROLE);
                    h.set(CorrelationId.HEADER, id);
                }))
                .build();
        mutated.getResponse().getHeaders().set(CorrelationId.HEADER, id);
        return chain.filter(mutated);
    }
}
