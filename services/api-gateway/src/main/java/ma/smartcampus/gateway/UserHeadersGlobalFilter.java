package ma.smartcampus.gateway;

import ma.smartcampus.common.security.Roles;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Après validation du JWT par Spring Security, transmet l'identité aux services via {@code X-User-Id} et
 * {@code X-User-Role}. Les services re-valident néanmoins le jeton (défense en profondeur) : ces en-têtes
 * servent aux journaux et à la limitation de débit, pas de preuve d'identité.
 */
@Component
public class UserHeadersGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken())
                .map(jwt -> exchange.mutate()
                        .request(r -> r.headers(h -> {
                            h.set(Roles.HEADER_USER_ID, jwt.getSubject());
                            String role = jwt.getClaimAsString("role");
                            if (role != null) {
                                h.set(Roles.HEADER_USER_ROLE, role);
                            }
                        }))
                        .build())
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
