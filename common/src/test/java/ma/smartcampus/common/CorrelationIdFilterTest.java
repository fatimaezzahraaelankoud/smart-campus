package ma.smartcampus.common;

import ma.smartcampus.common.web.CorrelationId;
import ma.smartcampus.common.web.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void should_generate_id_when_header_is_absent() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), res, new MockFilterChain());

        assertThat(res.getHeader(CorrelationId.HEADER)).isNotBlank();
    }

    @Test
    void should_keep_id_when_header_is_valid() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(CorrelationId.HEADER, "abc-123");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, new MockFilterChain());

        assertThat(res.getHeader(CorrelationId.HEADER)).isEqualTo("abc-123");
    }

    @Test
    void should_replace_id_when_header_contains_unsafe_characters() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(CorrelationId.HEADER, "bad value\"<script>");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, new MockFilterChain());

        assertThat(res.getHeader(CorrelationId.HEADER)).doesNotContain("script");
    }
}
