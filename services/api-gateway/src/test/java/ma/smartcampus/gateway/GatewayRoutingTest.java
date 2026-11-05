package ma.smartcampus.gateway;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * Tests d'intégration de la gateway : un faux auth-service (serveur HTTP du JDK) renvoie les en-têtes qu'il reçoit,
 * ce qui permet de vérifier ce que la gateway transmet réellement.
 */
@SpringBootTest(properties = {
        "campus.security.jwk-set-uri=http://localhost:1/jwks",   // jamais appelé : mockJwt() contourne le décodage
        "campus.security.issuer=smart-campus",
        "campus.cors.allowed-origin=http://localhost:3000"
})
class GatewayRoutingTest {

    private static final HttpServer DOWNSTREAM = startDownstream();

    @DynamicPropertySource
    static void downstream(DynamicPropertyRegistry registry) {
        registry.add("services.auth.url", () -> "http://localhost:" + DOWNSTREAM.getAddress().getPort());
    }

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToApplicationContext(context).apply(springSecurity()).configureClient().build();
    }

    @AfterAll
    static void stop() {
        DOWNSTREAM.stop(0);
    }

    private static HttpServer startDownstream() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                String body = "uid=" + header(exchange, "X-User-Id")
                        + ";role=" + header(exchange, "X-User-Role")
                        + ";cid=" + header(exchange, "X-Correlation-Id");
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String header(com.sun.net.httpserver.HttpExchange exchange, String name) {
        String v = exchange.getRequestHeaders().getFirst(name);
        return v == null ? "" : v;
    }

    @Test
    void should_expose_health_without_token() {
        client.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    void should_return_401_in_common_format_when_token_is_missing() {
        client.get().uri("/api/v1/users").header("X-Correlation-Id", "trace-42").exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().valueEquals("X-Correlation-Id", "trace-42")
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED")
                .jsonPath("$.path").isEqualTo("/api/v1/users")
                .jsonPath("$.correlationId").isEqualTo("trace-42");
    }

    @Test
    void should_return_401_when_bearer_token_is_garbage() {
        client.get().uri("/api/v1/users").header("Authorization", "Bearer not-a-jwt").exchange()
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.code").isEqualTo("UNAUTHORIZED");
    }

    @Test
    void should_route_login_without_token() {
        client.post().uri("/api/v1/auth/login").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(b -> org.assertj.core.api.Assertions.assertThat(b).contains("uid=;"));
    }

    @Test
    void should_forward_identity_headers_set_from_the_jwt() {
        client.mutateWith(mockJwt().jwt(j -> j.subject("11111111-1111-1111-1111-111111111111").claim("role", "ETUDIANT")))
                .get().uri("/api/v1/users/me").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(b -> org.assertj.core.api.Assertions.assertThat(b)
                        .contains("uid=11111111-1111-1111-1111-111111111111")
                        .contains("role=ETUDIANT"));
    }

    @Test
    void should_overwrite_identity_headers_forged_by_the_client() {
        client.mutateWith(mockJwt().jwt(j -> j.subject("real-user").claim("role", "ETUDIANT")))
                .get().uri("/api/v1/users/me")
                .header("X-User-Id", "attacker").header("X-User-Role", "ADMIN")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(b -> org.assertj.core.api.Assertions.assertThat(b)
                        .contains("uid=real-user").contains("role=ETUDIANT")
                        .doesNotContain("attacker").doesNotContain("ADMIN"));
    }

    @Test
    void should_strip_forged_identity_headers_on_public_routes() {
        client.post().uri("/api/v1/auth/login").header("X-User-Id", "attacker").header("X-User-Role", "ADMIN")
                .exchange().expectStatus().isOk()
                .expectBody(String.class).value(b -> org.assertj.core.api.Assertions.assertThat(b)
                        .contains("uid=;").contains("role=;"));
    }

    @Test
    void should_generate_correlation_id_and_forward_it() {
        client.post().uri("/api/v1/auth/login").exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Correlation-Id")
                .expectBody(String.class).value(b -> org.assertj.core.api.Assertions.assertThat(b)
                        .doesNotContain("cid=;"));
    }

    @Test
    void should_return_404_on_internal_paths_even_with_a_valid_token() {
        client.mutateWith(mockJwt().jwt(j -> j.subject("u").claim("role", "ADMIN")))
                .get().uri("/internal/v1/groups/1/members").exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo("NOT_FOUND");
    }

    @Test
    void should_return_404_on_nested_internal_paths() {
        client.mutateWith(mockJwt().jwt(j -> j.subject("u").claim("role", "ADMIN")))
                .get().uri("/api/v1/users/internal/secret").exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void should_return_404_on_actuator_endpoints_other_than_health() {
        client.get().uri("/actuator/env").exchange().expectStatus().isNotFound();
        client.get().uri("/actuator/prometheus").exchange().expectStatus().isNotFound();
    }

    @Test
    void should_return_404_in_common_format_when_no_route_matches() {
        client.mutateWith(mockJwt().jwt(j -> j.subject("u").claim("role", "ADMIN")))
                .get().uri("/api/v1/unknown-thing").exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo("NOT_FOUND");
    }

    @Test
    void should_answer_cors_preflight_for_allowed_origin() {
        client.options().uri("/api/v1/users")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:3000");
    }

    @Test
    void should_refuse_cors_preflight_for_unknown_origin() {
        client.options().uri("/api/v1/users")
                .header("Origin", "http://evil.example")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().isForbidden();
    }
}
