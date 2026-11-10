package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.MonthlyMoonCalendar
import net.plumt.astro.lunar.internal.MoonTodayCalculator
import net.plumt.astro.lunar.internal.SpecialMoonCalendar
import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.model.LunarPhaseType
import net.plumt.astro.lunar.model.SpecialMoonEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class MoonCalendarsTest {

    private val meeus = MeeusChapter49Algorithm(LunarCalculatorConfig.KST)

    @Test
    fun `조도는 망 100, 삭 0, 상현 50 이다`() {
        assertEquals(0, MoonTodayCalculator.illuminationPercent(0.0))
        assertEquals(50, MoonTodayCalculator.illuminationPercent(0.25))
        assertEquals(100, MoonTodayCalculator.illuminationPercent(0.5))
        assertEquals(50, MoonTodayCalculator.illuminationPercent(0.75))
    }

    @Test
    fun `이번 달에 남은 위상이 없으면 다음 달 첫 위상까지 센다`() {
        // 2026년 10월 보름달은 26일이라 28일에는 이번 달에 남은 보름달이 없다
        val now = LocalDateTime.of(2026, 10, 28, 12, 0)
        val nextFullMoonDay = meeus.calculate(2026, 11).first { it.type == LunarPhaseType.FullMoon }.day
        assertEquals((31 - 28) + nextFullMoonDay, MoonTodayCalculator.at(now, meeus).daysUntilFullMoon)
    }

    @Test
    fun `다가오는 이벤트는 지금 이후만 시간순으로 모은다`() {
        val now = LocalDateTime.of(2026, 10, 6, 12, 0)
        val upcoming = MoonTodayCalculator.at(now, meeus).upcomingEvents
        assertTrue(upcoming.isNotEmpty())
        assertTrue(upcoming.all { it.dateTime.isAfter(now) })
        assertEquals(upcoming.sortedBy { it.dateTime }, upcoming)
    }

    @Test
    fun `2023년 8월 달력은 31일만 블루문이다`() {
        val days = MonthlyMoonCalendar.days(2023, 8, meeus)
        assertEquals(31, days.size)
        assertEquals(listOf(31), days.filter { it.isBlueMoon }.map { it.dateTime.dayOfMonth })
        assertEquals(LunarPhaseType.FullMoon, days[30].event?.type)
        assertFalse(days[0].isBlueMoon)
    }

    @Test
    fun `특별한 달은 거리로 고른 달과 블루문을 시간순으로 합친다`() {
        val specials = SpecialMoonCalendar.ofYear(2023, emptyList(), meeus)
        assertEquals(listOf(SpecialMoonEventType.BlueMoon), specials.map { it.type })
        assertEquals(null, specials.single().distanceKm)
        assertEquals(31, specials.single().dateTime.dayOfMonth)
    }
}
