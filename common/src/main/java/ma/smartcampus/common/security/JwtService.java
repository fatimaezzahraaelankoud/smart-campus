package ma.smartcampus.common.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Émission et vérification des JWT RS256 (access token, 15 minutes par défaut).
 * <p>
 * Utilisé par auth-service pour signer ({@link #issue}) et publier la clé publique ({@link #publicJwks()}),
 * et par les autres services pour vérifier ({@link #verify}). Seul l'algorithme RS256 est accepté
 * (jamais {@code none}, jamais HS256).
 */
public class JwtService {

    private final RSAKey key;
    private final String issuer;
    private final Duration ttl;
    private final Clock clock;

    public JwtService(RSAKey key, String issuer, Duration ttl, Clock clock) {
        this.key = key;
        this.issuer = issuer;
        this.ttl = ttl;
        this.clock = clock;
    }

    public JwtService(RSAKey key, String issuer, Duration ttl) {
        this(key, issuer, ttl, Clock.systemUTC());
    }

    /** Génère une paire RSA 2048 bits avec un {@code kid} (à stocker dans un Secret Kubernetes). */
    public static RSAKey generateKey(String kid) {
        try {
            return new RSAKeyGenerator(2048).keyID(kid).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Impossible de générer la clé RSA", e);
        }
    }

    public String issue(UUID userId, String email, String role) {
        Instant now = clock.instant();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .issuer(issuer)
                .claim("email", email)
                .claim("role", role)
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ttl)))
                .build();
        try {
            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims);
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Signature du JWT impossible", e);
        }
    }

    public JwtClaims verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm())) {
                throw new JwtVerificationException("Algorithme non accepté");
            }
            if (!jwt.verify(new RSASSAVerifier(key.toPublicJWK()))) {
                throw new JwtVerificationException("Signature invalide");
            }
            JWTClaimsSet c = jwt.getJWTClaimsSet();
            if (!issuer.equals(c.getIssuer())) {
                throw new JwtVerificationException("Émetteur inattendu");
            }
            Date exp = c.getExpirationTime();
            if (exp == null || !exp.toInstant().isAfter(clock.instant())) {
                throw new JwtVerificationException("Jeton expiré");
            }
            return new JwtClaims(UUID.fromString(c.getSubject()), c.getStringClaim("email"),
                    c.getStringClaim("role"), c.getJWTID(), exp.toInstant());
        } catch (ParseException | JOSEException | IllegalArgumentException e) {
            throw new JwtVerificationException("Jeton illisible", e);
        }
    }

    /** Contenu de {@code GET /api/v1/auth/.well-known/jwks.json} : clé publique uniquement. */
    public String publicJwks() {
        return new JWKSet(key.toPublicJWK()).toString();
    }
}
