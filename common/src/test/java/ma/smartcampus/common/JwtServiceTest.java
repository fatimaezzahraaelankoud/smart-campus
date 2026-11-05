package ma.smartcampus.common;

import com.nimbusds.jose.jwk.RSAKey;
import ma.smartcampus.common.security.JwtClaims;
import ma.smartcampus.common.security.JwtService;
import ma.smartcampus.common.security.JwtVerificationException;
import ma.smartcampus.common.security.Roles;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static RSAKey key;

    @BeforeAll
    static void generate() {
        key = JwtService.generateKey("test-key");
    }

    @Test
    void should_return_claims_when_token_is_valid() {
        JwtService service = new JwtService(key, "smart-campus", Duration.ofMinutes(15));
        UUID id = UUID.randomUUID();

        JwtClaims claims = service.verify(service.issue(id, "sara@campus.ma", Roles.ETUDIANT));

        assertThat(claims.userId()).isEqualTo(id);
        assertThat(claims.email()).isEqualTo("sara@campus.ma");
        assertThat(claims.role()).isEqualTo(Roles.ETUDIANT);
        assertThat(claims.jti()).isNotBlank();
    }

    @Test
    void should_reject_when_token_is_expired() {
        Clock past = Clock.fixed(Instant.now().minus(Duration.ofHours(1)), ZoneOffset.UTC);
        String token = new JwtService(key, "smart-campus", Duration.ofMinutes(15), past)
                .issue(UUID.randomUUID(), "a@b.ma", Roles.ADMIN);

        assertThatThrownBy(() -> new JwtService(key, "smart-campus", Duration.ofMinutes(15)).verify(token))
                .isInstanceOf(JwtVerificationException.class)
                .hasMessageContaining("expiré");
    }

    @Test
    void should_reject_when_signed_with_another_key() {
        String token = new JwtService(JwtService.generateKey("other"), "smart-campus", Duration.ofMinutes(15))
                .issue(UUID.randomUUID(), "a@b.ma", Roles.ADMIN);

        assertThatThrownBy(() -> new JwtService(key, "smart-campus", Duration.ofMinutes(15)).verify(token))
                .isInstanceOf(JwtVerificationException.class);
    }

    @Test
    void should_reject_when_issuer_differs() {
        String token = new JwtService(key, "autre-emetteur", Duration.ofMinutes(15))
                .issue(UUID.randomUUID(), "a@b.ma", Roles.ADMIN);

        assertThatThrownBy(() -> new JwtService(key, "smart-campus", Duration.ofMinutes(15)).verify(token))
                .isInstanceOf(JwtVerificationException.class)
                .hasMessageContaining("Émetteur");
    }

    @Test
    void should_reject_when_token_is_tampered() {
        JwtService service = new JwtService(key, "smart-campus", Duration.ofMinutes(15));
        String token = service.issue(UUID.randomUUID(), "a@b.ma", Roles.ETUDIANT);
        String[] parts = token.split("\\.");
        String forged = parts[0] + "." + parts[1].substring(0, parts[1].length() - 2) + "xx." + parts[2];

        assertThatThrownBy(() -> service.verify(forged)).isInstanceOf(JwtVerificationException.class);
    }

    @Test
    void should_reject_unsigned_token() {
        JwtService service = new JwtService(key, "smart-campus", Duration.ofMinutes(15));
        String none = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ4In0.";

        assertThatThrownBy(() -> service.verify(none)).isInstanceOf(JwtVerificationException.class);
    }

    @Test
    void should_publish_only_public_key_in_jwks() {
        String jwks = new JwtService(key, "smart-campus", Duration.ofMinutes(15)).publicJwks();

        assertThat(jwks).contains("\"kid\":\"test-key\"").contains("\"n\":").doesNotContain("\"d\":");
    }
}
