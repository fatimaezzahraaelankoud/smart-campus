package ma.smartcampus.common.security;

public class JwtVerificationException extends RuntimeException {

    public JwtVerificationException(String message) {
        super(message);
    }

    public JwtVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
