package ma.smartcampus.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.smartcampus.common.error.ErrorResponse;
import ma.smartcampus.common.web.CorrelationId;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Écrit une {@link ErrorResponse} (format commun de la plateforme) dans la réponse de la gateway.
 */
@Component
public class ErrorWriter {

    private final ObjectMapper mapper;

    public ErrorWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Mono<Void> write(ServerWebExchange exchange, int status, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.empty();
        }
        response.setStatusCode(HttpStatusCode.valueOf(status));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ErrorResponse body = ErrorResponse.of(status, code, message,
                exchange.getRequest().getURI().getPath(),
                response.getHeaders().getFirst(CorrelationId.HEADER));
        try {
            byte[] bytes = mapper.writeValueAsBytes(body);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }
}
