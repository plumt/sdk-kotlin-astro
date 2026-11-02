package net.plumt.astro.lunar.internal

import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.model.LunarPhaseType
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * 임의 시각의 0~1 위상값과 음력 일자를 Meeus 삭 시각에서 낸다.
 *
 * 직전 삭부터 지난 시간을 그 주기(직전 삭 ~ 다음 삭)의 길이로 나눈다. 삭에서 0, 다음 삭 직전에 1 이다.
 */
internal object MoonAgeCalculator {

    /** 앞뒤로 훑을 달 수. 한 삭망월(약 29.5일)은 두 달보다 짧아 ±2 면 경계를 다 덮는다. */
    private const val SEARCH_MONTHS = 2

    fun phaseAt(dateTime: LocalDateTime, algorithm: MeeusChapter49Algorithm): Double =
        phaseFrom(dateTime, newMoonsAround(dateTime, algorithm))

    /** 그 달 하루하루 0시의 위상값. 날짜마다 삭을 다시 찾지 않도록 한 번만 모은다. */
    fun phasesForMonth(year: Int, month: Int, algorithm: MeeusChapter49Algorithm): List<Pair<LocalDateTime, Double>> {
        val newMoons = newMoonsAround(LocalDateTime.of(year, month, 1, 0, 0), algorithm)
        return (1..YearMonth.of(year, month).lengthOfMonth()).map { day ->
            val at = LocalDateTime.of(year, month, day, 0, 0)
            at to phaseFrom(at, newMoons)
        }
    }

    /**
     * 음력 일자(1~30). 삭이 든 날을 1일로 세는 음력 달력 규칙이다.
     *
     * 위상값에 삭망월 길이를 곱한 "월령" 은 하루 안에서도 변하고 날짜 경계가 삭 시각에 걸려,
     * 달력의 음력 날짜와 대부분 하루 어긋난다 — 그래서 날짜끼리 센다.
     * 직전 삭을 못 찾으면 1 을 돌려준다.
     */
    fun lunarDayOf(date: LocalDate, algorithm: MeeusChapter49Algorithm): Int {
        val newMoonDate = newMoonsAround(date.atStartOfDay(), algorithm)
            .map { it.toLocalDate() }
            .lastOrNull { !it.isAfter(date) }
            ?: return 1
        return (date.toEpochDay() - newMoonDate.toEpochDay()).toInt() + 1
    }

    private fun phaseFrom(at: LocalDateTime, newMoons: List<LocalDateTime>): Double {
        val previous = newMoons.lastOrNull { !it.isAfter(at) } ?: return 0.0
        val next = newMoons.firstOrNull { it.isAfter(at) } ?: return 0.0
        val lunation = Duration.between(previous, next).toMillis().toDouble()
        if (lunation <= 0.0) return 0.0
        return (Duration.between(previous, at).toMillis() / lunation).coerceIn(0.0, 1.0)
    }

    private fun newMoonsAround(around: LocalDateTime, algorithm: MeeusChapter49Algorithm): List<LocalDateTime> {
        val base = YearMonth.of(around.year, around.monthValue)
        return (-SEARCH_MONTHS..SEARCH_MONTHS)
            .map { base.plusMonths(it.toLong()) }
            .flatMap { algorithm.eventsOfTypeInMonth(it.year, it.monthValue, LunarPhaseType.NEW_MOON) }
            .map { it.dateTime }
            .distinct()
            .sorted()
    }
}
