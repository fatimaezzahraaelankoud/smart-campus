package ma.smartcampus.common;

import ma.smartcampus.common.error.ApiException;
import ma.smartcampus.common.error.ErrorResponse;
import ma.smartcampus.common.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void should_map_api_exception_to_common_error_format() {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/reservations");
        req.setAttribute("correlationId", "cid-1");

        ResponseEntity<ErrorResponse> res = handler.handleApi(
                ApiException.conflict("ROOM_ALREADY_BOOKED", "La salle A12 est déjà réservée"), req);

        assertThat(res.getStatusCode().value()).isEqualTo(409);
        assertThat(res.getBody().code()).isEqualTo("ROOM_ALREADY_BOOKED");
        assertThat(res.getBody().path()).isEqualTo("/api/v1/reservations");
        assertThat(res.getBody().correlationId()).isEqualTo("cid-1");
    }

    @Test
    void should_hide_technical_details_on_unexpected_error() {
        ResponseEntity<ErrorResponse> res = handler.handleOther(
                new IllegalStateException("password=secret jdbc:postgresql://db"), new MockHttpServletRequest());

        assertThat(res.getStatusCode().value()).isEqualTo(500);
        assertThat(res.getBody().message()).doesNotContain("secret").doesNotContain("jdbc");
    }
}
