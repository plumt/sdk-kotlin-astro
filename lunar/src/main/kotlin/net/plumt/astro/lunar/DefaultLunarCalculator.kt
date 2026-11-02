package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.BlueMoonDetector
import net.plumt.astro.lunar.internal.MonthlyMoonCalendar
import net.plumt.astro.lunar.internal.MoonAgeCalculator
import net.plumt.astro.lunar.internal.MoonTodayCalculator
import net.plumt.astro.lunar.internal.SpecialMoonCalendar
import net.plumt.astro.lunar.internal.SupermoonDetector
import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.internal.algorithm.MoonDistanceAlgorithm
import net.plumt.astro.lunar.internal.algorithm.MoonRiseSetAlgorithm
import net.plumt.astro.lunar.internal.algorithm.SolarTermAlgorithm
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
 */
internal class DefaultLunarCalculator(config: LunarCalculatorConfig) : LunarCalculator {

    override val zone: ZoneId = config.zone

    private val criteria = config.specialMoonCriteria
    private val phase = MeeusChapter49Algorithm(zone)
    private val distance = MoonDistanceAlgorithm(zone)
    private val riseSetAlgorithm = MoonRiseSetAlgorithm(zone)
    private val solarTerm = SolarTermAlgorithm(zone)

    override fun phasesOfMonth(year: Int, month: Int): List<LunarPhaseEvent> = phase.calculate(year, month)

    override fun phaseEventsInMonth(year: Int, month: Int, type: LunarPhaseType): List<LunarPhaseEvent> =
        phase.eventsOfTypeInMonth(year, month, type)

    override fun phaseAt(dateTime: LocalDateTime): Double = MoonAgeCalculator.phaseAt(dateTime, phase)

    override fun lunarDayOf(date: LocalDate): Int = MoonAgeCalculator.lunarDayOf(date, phase)

    override fun moonToday(now: LocalDateTime): MoonToday = MoonTodayCalculator.at(now, phase)

    override fun monthCalendar(year: Int, month: Int): List<MoonDay> = MonthlyMoonCalendar.days(year, month, phase)

    override fun isBlueMoon(event: LunarPhaseEvent): Boolean = BlueMoonDetector.isBlueMoon(event, phase)

    override fun specialFullMoons(year: Int): List<SpecialFullMoon> =
        SupermoonDetector.classify(year, phase, distance, criteria)

    override fun specialMoonsOfYear(year: Int): List<SpecialMoonEvent> =
        SpecialMoonCalendar.ofYear(year, specialFullMoons(year), phase)

    override fun distanceKm(dateTime: LocalDateTime): Double = distance.distanceKm(dateTime)

    override fun riseSet(date: LocalDate, location: GeoLocation): MoonRiseSet = riseSetAlgorithm.riseSet(date, location)

    override fun riseSetsOfMonth(yearMonth: YearMonth, location: GeoLocation): List<MoonRiseSet> =
        riseSetAlgorithm.riseSetsForMonth(yearMonth, location)

    override fun solarTermsOfYear(year: Int): List<SolarTerm> = solarTerm.solarTermsOfYear(year)
}
