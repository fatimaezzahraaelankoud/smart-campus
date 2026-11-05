package ma.smartcampus.gateway;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlockedPathsFilterTest {

    @Test
    void should_block_internal_and_non_health_actuator_paths() {
        assertThat(BlockedPathsFilter.isBlocked("/internal/v1/x")).isTrue();
        assertThat(BlockedPathsFilter.isBlocked("/api/v1/rooms/internal/x")).isTrue();
        assertThat(BlockedPathsFilter.isBlocked("/internal")).isTrue();
        assertThat(BlockedPathsFilter.isBlocked("/actuator/env")).isTrue();
        assertThat(BlockedPathsFilter.isBlocked("/actuator")).isTrue();
    }

    @Test
    void should_allow_public_api_and_health_paths() {
        assertThat(BlockedPathsFilter.isBlocked("/api/v1/auth/login")).isFalse();
        assertThat(BlockedPathsFilter.isBlocked("/actuator/health")).isFalse();
        assertThat(BlockedPathsFilter.isBlocked("/actuator/health/liveness")).isFalse();
        assertThat(BlockedPathsFilter.isBlocked("/api/v1/internals-report")).isFalse();
    }
}
