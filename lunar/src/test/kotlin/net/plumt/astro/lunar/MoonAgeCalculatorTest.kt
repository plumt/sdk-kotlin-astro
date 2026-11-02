package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.MoonAgeCalculator
import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.model.LunarPhaseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * 위상값이 Meeus 삭 시각에 정확히 걸려 있는지 고정한다.
 * 삭에서 0, 다음 삭 직전에 1, 그 사이는 단조 증가여야 화면의 달 그림이 되감기지 않는다.
 */
class MoonAgeCalculatorTest {

    private val meeus = MeeusChapter49Algorithm(LunarCalculatorConfig.KST)

    private fun newMoons(year: Int, month: Int) =
        meeus.eventsOfTypeInMonth(year, month, LunarPhaseType.NEW_MOON).map { it.dateTime }

    @Test
    fun `삭 시각의 위상은 0 이다`() {
        val newMoon = newMoons(2026, 3).first()
        assertEquals(0.0, MoonAgeCalculator.phaseAt(newMoon, meeus), 1e-12)
    }

    @Test
    fun `다음 삭 1분 전의 위상은 1 에 붙는다`() {
        val next = newMoons(2026, 4).first()
        val phase = MoonAgeCalculator.phaseAt(next.minusMinutes(1), meeus)
        assertTrue("1 직전이어야 한다: $phase", phase > 0.999)
    }

    @Test
    fun `한 주기 안에서 위상은 되돌아가지 않는다`() {
        val start = newMoons(2026, 3).first()
        val end = newMoons(2026, 4).first()
        var previous = -1.0
        var at = start
        while (at.isBefore(end)) {
            val phase = MoonAgeCalculator.phaseAt(at, meeus)
            assertTrue("$at 에서 ${previous} → $phase 로 줄었다", phase >= previous)
            previous = phase
            at = at.plusHours(1)
        }
    }

    @Test
    fun `망 시각의 위상은 절반 부근이다`() {
        // 달이 등속이 아니라 정확히 0.5 는 아니다. 절반에서 크게 벗어나면 삭을 잘못 잡은 것이다.
        val fullMoon = meeus.eventsOfTypeInMonth(2026, 3, LunarPhaseType.FULL_MOON).first().dateTime
        val phase = MoonAgeCalculator.phaseAt(fullMoon, meeus)
        assertTrue("망은 0.45~0.55 사이여야 한다: $phase", phase in 0.45..0.55)
    }

    @Test
    fun `그 달의 날짜 수만큼 위상값을 내고 전부 0과 1 사이다`() {
        for (month in 1..12) {
            val list = MoonAgeCalculator.phasesForMonth(2026, month, meeus)
            assertEquals(YearMonth.of(2026, month).lengthOfMonth(), list.size)
            assertTrue(list.all { (_, phase) -> phase in 0.0..1.0 })
            list.forEachIndexed { index, (at, _) -> assertEquals(index + 1, at.dayOfMonth) }
        }
    }

    @Test
    fun `월별 목록의 값은 같은 시각을 낱개로 물은 값과 같다`() {
        MoonAgeCalculator.phasesForMonth(2026, 3, meeus).forEach { (at, phase) ->
            assertEquals(MoonAgeCalculator.phaseAt(at, meeus), phase, 1e-12)
        }
    }

    // ========== 음력 일자 ==========

    @Test
    fun `설날은 음력 1일이고 추석은 음력 15일이다`() {
        // 한국천문연구원이 공표한 설날(음 1.1)·추석(음 8.15) 양력 날짜
        val seollal = listOf("2024-02-10", "2025-01-29", "2026-02-17", "2027-02-07")
        val chuseok = listOf("2024-09-17", "2025-10-06", "2026-09-25", "2027-09-15")

        seollal.forEach { assertEquals(it, 1, MoonAgeCalculator.lunarDayOf(LocalDate.parse(it), meeus)) }
        chuseok.forEach { assertEquals(it, 15, MoonAgeCalculator.lunarDayOf(LocalDate.parse(it), meeus)) }
    }

    @Test
    fun `삭이 든 날은 시각과 무관하게 1일이고 다음 날은 2일이다`() {
        val newMoon = meeus.eventsOfTypeInMonth(2026, 2, LunarPhaseType.NEW_MOON).first().dateTime.toLocalDate()

        assertEquals(1, MoonAgeCalculator.lunarDayOf(newMoon, meeus))
        assertEquals(2, MoonAgeCalculator.lunarDayOf(newMoon.plusDays(1), meeus))
    }

    @Test
    fun `삭 전날은 그 달의 마지막 날(29 또는 30일)이다`() {
        val newMoon = meeus.eventsOfTypeInMonth(2026, 2, LunarPhaseType.NEW_MOON).first().dateTime.toLocalDate()

        assertTrue(MoonAgeCalculator.lunarDayOf(newMoon.minusDays(1), meeus) in 29..30)
    }

    @Test
    fun `음력 일자는 2년 내내 1에서 30 사이다`() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(730) {
            assertTrue("$date", MoonAgeCalculator.lunarDayOf(date, meeus) in 1..30)
            date = date.plusDays(1)
        }
    }
}
