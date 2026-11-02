package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.BlueMoonDetector
import net.plumt.astro.lunar.internal.MonthlyMoonCalendar
import net.plumt.astro.lunar.internal.MoonAgeCalculator
import net.plumt.astro.lunar.internal.MoonTodayCalculator
import net.plumt.astro.lunar.internal.Preconditions
import net.plumt.astro.lunar.internal.SpecialMoonCalendar
import net.plumt.astro.lunar.internal.SupermoonDetector
import net.plumt.astro.lunar.internal.calculating
import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.internal.algorithm.MoonDistanceAlgorithm
import net.plumt.astro.lunar.internal.algorithm.MoonRiseSetAlgorithm
import net.plumt.astro.lunar.internal.algorithm.SolarTermAlgorithm
import net.plumt.astro.lunar.internal.requireFinite
import net.plumt.astro.lunar.internal.algorithm.riseSetsForMonth
import net.plumt.astro.lunar.model.GeoLocation
import net.plumt.astro.lunar.model.LunarPhaseEvent
import net.plumt.astro.lunar.model.LunarPhaseType
import net.plumt.astro.lunar.model.MoonDay
import net.plumt.astro.lunar.model.MoonRiseSet
import net.plumt.astro.lunar.model.MoonToday
import net.plumt.astro.lunar.model.SolarTerm
import net.plumt.astro.lunar.model.SpecialFullMoon
import net.plumt.astro.lunar.model.SpecialMoonEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

/**
 * [LunarCalculator] 기본 구현. 알고리즘 인스턴스를 묶어 위임만 한다.
 *
 * 위상은 ΔT 보정 없이(KASI 발표 시각과 맞춘 값), 거리·출몰·절기는 ΔT 보정을 켜고 계산한다.
 *
 * 함수마다 인자를 먼저 검사하고([Preconditions]), 본문은 [calculating] 으로 감싸 내부 예외를
 * [LunarCalculationException] 으로 바꾼다.
 */
internal class DefaultLunarCalculator(config: LunarCalculatorConfig) : LunarCalculator {

    override val zone: ZoneId = config.zone

    private val criteria = config.specialMoonCriteria
    private val phase = MeeusChapter49Algorithm(zone)
    private val distance = MoonDistanceAlgorithm(zone)
    private val riseSetAlgorithm = MoonRiseSetAlgorithm(zone)
    private val solarTerm = SolarTermAlgorithm(zone)

    override fun phasesOfMonth(year: Int, month: Int): List<LunarPhaseEvent> {
        Preconditions.yearMonth(year, month)
        return calculating("위상") { phase.calculate(year, month) }
    }

    override fun phaseEventsInMonth(year: Int, month: Int, type: LunarPhaseType): List<LunarPhaseEvent> {
        Preconditions.yearMonth(year, month)
        return calculating("위상") { phase.eventsOfTypeInMonth(year, month, type) }
    }

    override fun phaseAt(dateTime: LocalDateTime): Double {
        Preconditions.dateTime(dateTime)
        return calculating("위상값") { MoonAgeCalculator.phaseAt(dateTime, phase).requireFinite("위상값") }
    }

    override fun lunarDayOf(date: LocalDate): Int {
        Preconditions.date(date)
        return calculating("음력 일자") { MoonAgeCalculator.lunarDayOf(date, phase) }
    }

    override fun moonToday(now: LocalDateTime): MoonToday {
        Preconditions.dateTime(now, "now")
        return calculating("오늘의 달") { MoonTodayCalculator.at(now, phase) }
    }

    override fun monthCalendar(year: Int, month: Int): List<MoonDay> {
        Preconditions.yearMonth(year, month)
        return calculating("월간 달력") { MonthlyMoonCalendar.days(year, month, phase) }
    }

    override fun isBlueMoon(event: LunarPhaseEvent): Boolean {
        Preconditions.dateTime(event.dateTime, "event.dateTime")
        return calculating("블루문") { BlueMoonDetector.isBlueMoon(event, phase) }
    }

    override fun specialFullMoons(year: Int): List<SpecialFullMoon> {
        Preconditions.year(year)
        return calculating("슈퍼문·미니문") { SupermoonDetector.classify(year, phase, distance, criteria) }
    }

    override fun specialMoonsOfYear(year: Int): List<SpecialMoonEvent> {
        Preconditions.year(year)
        return calculating("특별한 달") {
            SpecialMoonCalendar.ofYear(year, SupermoonDetector.classify(year, phase, distance, criteria), phase)
        }
    }

    override fun distanceKm(dateTime: LocalDateTime): Double {
        Preconditions.dateTime(dateTime)
        return calculating("거리") { distance.distanceKm(dateTime).requireFinite("거리") }
    }

    override fun riseSet(date: LocalDate, location: GeoLocation): MoonRiseSet {
        Preconditions.date(date)
        return calculating("월출·월몰") { riseSetAlgorithm.riseSet(date, location) }
    }

    override fun riseSetsOfMonth(yearMonth: YearMonth, location: GeoLocation): List<MoonRiseSet> {
        Preconditions.yearMonth(yearMonth)
        return calculating("월출·월몰") { riseSetAlgorithm.riseSetsForMonth(yearMonth, location) }
    }

    override fun solarTermsOfYear(year: Int): List<SolarTerm> {
        Preconditions.year(year)
        return calculating("24절기") { solarTerm.solarTermsOfYear(year) }
    }
}
