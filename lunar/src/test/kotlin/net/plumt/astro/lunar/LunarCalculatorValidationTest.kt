package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.algorithm.AstroTime
import net.plumt.astro.lunar.model.GeoLocation
import net.plumt.astro.lunar.model.LunarPhaseEvent
import net.plumt.astro.lunar.model.LunarPhaseType
import net.plumt.astro.lunar.model.SpecialMoonType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset

/** 진입점의 인자 검사와 오류 변환, 지원 범위 경계에서 계산이 깨지지 않는지 검증. */
class LunarCalculatorValidationTest {

    private val lunar = LunarCalculator.create()

    private inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T {
        try {
            block()
        } catch (e: Throwable) {
            if (e is T) return e
            throw AssertionError("${T::class.simpleName} 대신 ${e::class.simpleName}: ${e.message}", e)
        }
        fail("${T::class.simpleName} 가 나지 않았다")
        throw AssertionError()
    }

    @Test
    fun `월이 1~12 밖이면 모든 월 단위 함수가 IllegalArgumentException 을 던진다`() {
        listOf(0, 13, -1, Int.MAX_VALUE).forEach { month ->
            val e = assertThrows<IllegalArgumentException> { lunar.phasesOfMonth(2026, month) }
            assertTrue(e.message!!.contains("month($month)"))
            assertThrows<IllegalArgumentException> { lunar.phaseEventsInMonth(2026, month, LunarPhaseType.FULL_MOON) }
            assertThrows<IllegalArgumentException> { lunar.monthCalendar(2026, month) }
        }
    }

    @Test
    fun `지원 연도 밖이면 연도를 받는 모든 함수가 IllegalArgumentException 을 던진다`() {
        val below = LunarCalculator.MIN_YEAR - 1
        val above = LunarCalculator.MAX_YEAR + 1
        listOf(below, above, -5000, 100_000, Int.MIN_VALUE, Int.MAX_VALUE).forEach { year ->
            assertThrows<IllegalArgumentException> { lunar.phasesOfMonth(year, 1) }
            assertThrows<IllegalArgumentException> { lunar.phaseEventsInMonth(year, 1, LunarPhaseType.NEW_MOON) }
            assertThrows<IllegalArgumentException> { lunar.monthCalendar(year, 1) }
            assertThrows<IllegalArgumentException> { lunar.specialFullMoons(year) }
            assertThrows<IllegalArgumentException> { lunar.specialMoonsOfYear(year) }
            assertThrows<IllegalArgumentException> { lunar.solarTermsOfYear(year) }
        }
        listOf(below, above).forEach { year ->
            val date = LocalDate.of(year, 6, 1)
            assertThrows<IllegalArgumentException> { lunar.phaseAt(date.atStartOfDay()) }
            assertThrows<IllegalArgumentException> { lunar.lunarDayOf(date) }
            assertThrows<IllegalArgumentException> { lunar.moonToday(date.atStartOfDay()) }
            assertThrows<IllegalArgumentException> { lunar.distanceKm(date.atStartOfDay()) }
            assertThrows<IllegalArgumentException> { lunar.riseSet(date) }
            assertThrows<IllegalArgumentException> { lunar.riseSetsOfMonth(YearMonth.of(year, 6)) }
            assertThrows<IllegalArgumentException> {
                lunar.isBlueMoon(LunarPhaseEvent(LunarPhaseType.FULL_MOON, date.atStartOfDay()))
            }
        }
        assertThrows<IllegalArgumentException> { lunar.phaseAt(LocalDateTime.MAX) }
        assertThrows<IllegalArgumentException> { lunar.lunarDayOf(LocalDate.MIN) }
        assertThrows<IllegalArgumentException> { lunar.distanceKm(LocalDateTime.MAX) }
    }

    @Test
    fun `지원 범위 양 끝에서 모든 함수가 정상 값을 낸다`() {
        val zones = listOf(LunarCalculatorConfig.KST, ZoneOffset.UTC, ZoneId.of("Pacific/Kiritimati"), ZoneId.of("Pacific/Pago_Pago"))
        zones.map { LunarCalculator.create(LunarCalculatorConfig(zone = it)) }.forEach { calc ->
            listOf(
                LocalDateTime.of(LunarCalculator.MIN_YEAR, 1, 1, 0, 0),
                LocalDateTime.of(LunarCalculator.MAX_YEAR, 12, 31, 23, 59, 59),
            ).forEach { at ->
                val y = at.year
                val m = at.monthValue
                assertTrue(calc.phasesOfMonth(y, m).isNotEmpty())
                assertTrue(calc.phaseAt(at) in 0.0..1.0)
                assertTrue(calc.lunarDayOf(at.toLocalDate()) in 1..30)
                assertTrue(calc.moonToday(at).lunarDay in 1..30)
                assertEquals(at.toLocalDate().lengthOfMonth(), calc.monthCalendar(y, m).size)
                calc.phaseEventsInMonth(y, m, LunarPhaseType.FULL_MOON).forEach { calc.isBlueMoon(it) }
                calc.specialMoonsOfYear(y)
                assertTrue(calc.distanceKm(at) in 350_000.0..410_000.0)
                calc.riseSet(at.toLocalDate())
                assertEquals(at.toLocalDate().lengthOfMonth(), calc.riseSetsOfMonth(YearMonth.from(at)).size)
                assertEquals(24, calc.solarTermsOfYear(y).size)
            }
        }
    }

    @Test
    fun `관측 지점 좌표가 범위 밖이거나 유한하지 않으면 만들 수 없다`() {
        listOf(90.0001, -90.0001, 999.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { lat ->
            assertThrows<IllegalArgumentException> { GeoLocation(lat, 0.0) }
        }
        listOf(180.0001, -180.0001, 720.0, Double.NaN, Double.NEGATIVE_INFINITY).forEach { lon ->
            assertThrows<IllegalArgumentException> { GeoLocation(0.0, lon) }
        }
    }

    @Test
    fun `극점과 날짜변경선을 포함한 모든 위도·경도에서 월출 계산이 깨지지 않는다`() {
        val date = LocalDate.of(2026, 6, 21)
        for (lat in -90..90 step 15) {
            for (lon in -180..180 step 45) {
                val riseSet = lunar.riseSet(date, GeoLocation(lat.toDouble(), lon.toDouble()))
                listOfNotNull(riseSet.rise, riseSet.set).forEach { assertEquals(date, it.toLocalDate()) }
            }
        }
    }

    @Test
    fun `임계값이 양의 유한한 수가 아니면 기준을 만들 수 없다`() {
        listOf(Double.NaN, -1.0, 0.0, Double.NEGATIVE_INFINITY).forEach { v ->
            assertThrows<IllegalArgumentException> { DistanceThresholdCriteria(supermoonMaxKm = v) }
        }
        listOf(Double.NaN, Double.POSITIVE_INFINITY).forEach { v ->
            assertThrows<IllegalArgumentException> { DistanceThresholdCriteria(minimoonMinKm = v) }
        }
    }

    @Test
    fun `교체한 판정 기준이 던진 예외는 원인을 담은 LunarCalculationException 이 된다`() {
        val boom = IllegalStateException("boom")
        val failing = LunarCalculator.create(LunarCalculatorConfig(specialMoonCriteria = { throw boom }))
        val e = assertThrows<LunarCalculationException> { failing.specialFullMoons(2024) }
        assertSame(boom, e.cause)
        assertThrows<LunarCalculationException> { failing.specialMoonsOfYear(2024) }
    }

    @Test
    fun `교체한 판정 기준이 정상이면 예외 없이 분류한다`() {
        val onlyMini = LunarCalculator.create(LunarCalculatorConfig(specialMoonCriteria = { SpecialMoonType.MINIMOON }))
        assertTrue(onlyMini.specialFullMoons(2024).all { it.type == SpecialMoonType.MINIMOON })
    }

    @Test
    fun `율리우스일 변환은 하루 끝 직전에도 24시를 만들지 않고 유한하지 않은 값은 거부한다`() {
        val endOfDay = AstroTime.utcToJd(LocalDateTime.of(2026, 1, 1, 0, 0)) - 1e-9
        assertEquals(LocalDateTime.of(2025, 12, 31, 23, 59, 59), AstroTime.jdToUtc(endOfDay))
        assertThrows<IllegalStateException> { AstroTime.jdToUtc(Double.NaN) }
        assertThrows<IllegalStateException> { AstroTime.jdToUtcRounded(Double.POSITIVE_INFINITY) }
    }
}
