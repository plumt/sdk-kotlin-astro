package net.plumt.astro.lunar.internal

import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.model.MoonDay

/** 한 달 달력의 날짜별 달 계산. */
internal object MonthlyMoonCalendar {

    fun days(year: Int, month: Int, algorithm: MeeusChapter49Algorithm): List<MoonDay> {
        val eventByDay = algorithm.calculate(year, month).associateBy { it.dateTime.dayOfMonth }
        val blueMoonDay = BlueMoonDetector.findBlueMoon(year, month, algorithm)?.day
        return MoonAgeCalculator.phasesForMonth(year, month, algorithm).map { (at, phase) ->
            MoonDay(
                dateTime = at,
                phase = phase,
                lunarDay = MoonAgeCalculator.lunarDayOf(at.toLocalDate(), algorithm),
                event = eventByDay[at.dayOfMonth],
                isBlueMoon = at.dayOfMonth == blueMoonDay,
            )
        }
    }
}
