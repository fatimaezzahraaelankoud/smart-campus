package ma.smartcampus.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Enveloppe commune de tous les messages Kafka (cahier de développement, section 3.4).
 * Topics nommés {@code campus.<domaine>.<événement>}, clé de partition = identifiant de l'agrégat.
 */
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        String producer,
        String correlationId,
        T payload) {

    public static <T> EventEnvelope<T> of(String eventType, String producer, String correlationId, T payload) {
        return new EventEnvelope<>(UUID.randomUUID(), eventType, Instant.now(), producer, correlationId, payload);
    }
}
