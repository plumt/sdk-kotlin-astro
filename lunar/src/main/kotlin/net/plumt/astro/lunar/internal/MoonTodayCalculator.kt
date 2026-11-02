package net.plumt.astro.lunar.internal

import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.model.LunarPhaseEvent
import net.plumt.astro.lunar.model.LunarPhaseType
import net.plumt.astro.lunar.model.MoonToday
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

/** "오늘의 달" 계산. 시각은 알고리즘에 설정한 시간대의 현지 시각이다. */
internal object MoonTodayCalculator {

    /** 다가오는 이벤트를 모으는 개월 수(이번 달 포함). */
    private const val UPCOMING_MONTHS = 3

    fun at(now: LocalDateTime, algorithm: MeeusChapter49Algorithm): MoonToday {
        val today = now.toLocalDate()
        val phase = MoonAgeCalculator.phaseAt(now, algorithm)
        val thisMonthEvents = algorithm.calculate(today.year, today.monthValue)
        val todayEvent = thisMonthEvents.firstOrNull { it.dateTime.dayOfMonth == today.dayOfMonth }
        val upcoming = upcomingEvents(now, algorithm)
        val nextFullMoon = upcoming.firstOrNull { it.type == LunarPhaseType.FULL_MOON }

        // 오늘 든 보름달이 블루문이거나, 다가오는 보름달이 블루문이면 참
        val blueMoonComing = listOfNotNull(todayEvent, nextFullMoon).any { BlueMoonDetector.isBlueMoon(it, algorithm) }

        return MoonToday(
            phase = phase,
            lunarDay = MoonAgeCalculator.lunarDayOf(today, algorithm),
            illuminationPercent = illuminationPercent(phase),
            todayEventType = todayEvent?.type,
            upcomingEvents = upcoming,
            daysUntilFullMoon = daysUntil(today, LunarPhaseType.FULL_MOON, thisMonthEvents, algorithm),
            daysUntilNewMoon = daysUntil(today, LunarPhaseType.NEW_MOON, thisMonthEvents, algorithm),
            isBlueMoonComing = blueMoonComing,
        )
    }

    /** 조도(%). 위상에 비례하는 직선 근사다 — 망 1.0, 삭 0.0. */
    fun illuminationPercent(phase: Double): Int {
        val illumination = if (phase <= 0.5) phase * 2.0 else (1.0 - phase) * 2.0
        return (illumination * 100).roundToInt()
    }

    private fun upcomingEvents(now: LocalDateTime, algorithm: MeeusChapter49Algorithm): List<LunarPhaseEvent> =
        (0 until UPCOMING_MONTHS)
            .map { offset -> now.toLocalDate().plusMonths(offset.toLong()) }
            .flatMap { algorithm.calculate(it.year, it.monthValue) }
            .filter { it.dateTime.isAfter(now) }
            .distinctBy { it.dateTime }
            .sortedBy { it.dateTime }

    /**
     * [today] 부터 [type] 위상까지 남은 날 수. 이번 달에서 오늘 이후 가장 빠른 것, 없으면 다음 달 첫 것까지 센다.
     * 다음 달에도 없으면 0 이다.
     */
    private fun daysUntil(
        today: LocalDate,
        type: LunarPhaseType,
        thisMonthEvents: List<LunarPhaseEvent>,
        algorithm: MeeusChapter49Algorithm,
    ): Int {
        val todayDay = today.dayOfMonth
        val soonest = thisMonthEvents
            .filter { it.type == type && it.dateTime.dayOfMonth >= todayDay }
            .minByOrNull { it.dateTime.dayOfMonth }
        if (soonest != null) return soonest.dateTime.dayOfMonth - todayDay

        val nextMonth = today.plusMonths(1)
        val nextEvent = algorithm.calculate(nextMonth.year, nextMonth.monthValue).firstOrNull { it.type == type }
        return nextEvent?.let { (today.lengthOfMonth() - todayDay) + it.dateTime.dayOfMonth } ?: 0
    }
}
