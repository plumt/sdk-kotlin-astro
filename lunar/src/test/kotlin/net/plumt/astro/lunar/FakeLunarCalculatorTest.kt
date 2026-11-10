package net.plumt.astro.lunar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset

class FakeLunarCalculatorTest {

    private val real = LunarCalculator.create()

    @Test
    fun `값은 실제 계산과 같다`() {
        val fake = FakeLunarCalculator()
        val at = LocalDateTime.of(2026, 11, 10, 21, 0)
        assertEquals(real.phasesOfMonth(2026, 11), fake.phasesOfMonth(2026, 11))
        assertEquals(real.moonToday(at), fake.moonToday(at))
        assertEquals(real.riseSet(LocalDate.of(2026, 11, 10)), fake.riseSet(LocalDate.of(2026, 11, 10)))
        assertEquals(real.riseSetsOfMonth(YearMonth.of(2026, 11)), fake.riseSetsOfMonth(YearMonth.of(2026, 11)))
        assertEquals(real.solarTermsOfYear(2026), fake.solarTermsOfYear(2026))
        assertEquals(real.distanceKm(at), fake.distanceKm(at), 0.0)
    }

    @Test
    fun `설정한 시간대를 쓴다`() {
        val fake = FakeLunarCalculator(LunarCalculatorConfig.builder().zone(ZoneOffset.UTC).build())
        assertEquals(ZoneOffset.UTC, fake.zone)
        assertEquals(
            LunarCalculator.create(LunarCalculatorConfig.builder().zone(ZoneOffset.UTC).build()).phasesOfMonth(2026, 1),
            fake.phasesOfMonth(2026, 1),
        )
    }

    @Test
    fun `부른 함수를 순서대로 기록한다`() {
        val fake = FakeLunarCalculator()
        fake.phaseAt(LocalDateTime.of(2026, 1, 1, 0, 0))
        fake.solarTermsOfYear(2026)
        fake.zone
        assertEquals(listOf("phaseAt", "solarTermsOfYear"), fake.calls)
        fake.clearCalls()
        assertTrue(fake.calls.isEmpty())
    }

    @Test
    fun `failWith 는 인자 검사보다 먼저 던지고 succeed 로 되돌린다`() {
        val fake = FakeLunarCalculator()
        val error = LunarCalculationException("시험")
        fake.failWith(error)
        try {
            fake.specialMoonsOfYear(1800)
            fail("던져야 한다")
        } catch (e: LunarCalculationException) {
            assertSame(error, e)
        }
        fake.succeed()
        try {
            fake.specialMoonsOfYear(1800)
            fail("인자 검사가 던져야 한다")
        } catch (expected: IllegalArgumentException) {
            // 실제와 같은 인자 검사
        }
        assertEquals(listOf("specialMoonsOfYear", "specialMoonsOfYear"), fake.calls)
    }
}
