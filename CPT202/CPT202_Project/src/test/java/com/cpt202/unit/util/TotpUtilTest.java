package com.cpt202.unit.util;

import com.cpt202.util.TotpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

/** Unit tests for deterministic TOTP validation windows. */
class TotpUtilTest {

    private static final String FIXED_SECRET = "JBSWY3DPEHPK3PXP";

    /**
     * With a fixed clock, the same secret must always produce the same code for
     * the same 30-second time window. This is the cheapest proof that fake-time
     * TOTP testing is feasible without touching production code.
     */
    @Test
    void isValidCodeShouldAcceptCodeForFixedInstant() {
        Instant fixedInstant = Instant.parse("2026-05-04T12:00:00Z");
        String code = generateCodeForInstant(FIXED_SECRET, fixedInstant);

        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            mockedInstant.when(Instant::now).thenReturn(fixedInstant);

            assertThat(TotpUtil.isValidCode(FIXED_SECRET, code)).isTrue();
        }
    }

    /**
     * The implementation intentionally accepts one previous and one next window
     * to tolerate small clock drift, so the adjacent-window code should still be
     * considered valid at the fixed time.
     */
    @Test
    void isValidCodeShouldAcceptAdjacentTimeWindow() {
        Instant fixedInstant = Instant.parse("2026-05-04T12:00:00Z");
        String previousWindowCode = generateCodeForInstant(FIXED_SECRET, fixedInstant.minusSeconds(30));
        String nextWindowCode = generateCodeForInstant(FIXED_SECRET, fixedInstant.plusSeconds(30));

        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            mockedInstant.when(Instant::now).thenReturn(fixedInstant);

            assertThat(TotpUtil.isValidCode(FIXED_SECRET, previousWindowCode)).isTrue();
            assertThat(TotpUtil.isValidCode(FIXED_SECRET, nextWindowCode)).isTrue();
        }
    }

    /** Rejects malformed TOTP values before any time-window comparison. */
    @Test
    void isValidCodeShouldRejectMalformedCode() {
        assertThat(TotpUtil.isValidCode(FIXED_SECRET, "12AB56")).isFalse();
        assertThat(TotpUtil.isValidCode(FIXED_SECRET, "12345")).isFalse();
    }

    /** Generates a deterministic TOTP code for the supplied instant. */
    private String generateCodeForInstant(String secret, Instant instant) {
        long counter = instant.getEpochSecond() / 30;
        return ReflectionTestUtils.invokeMethod(TotpUtil.class, "generateCode", secret, counter);
    }
}