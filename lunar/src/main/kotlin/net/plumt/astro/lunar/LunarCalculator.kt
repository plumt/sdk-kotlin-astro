package net.plumt.astro.lunar

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
 * 달 위상·거리·출몰·24절기를 네트워크 없이 계산한다. 이 모듈의 유일한 진입점이다.
 *
 * 받는 시각과 돌려주는 시각은 모두 [zone] 의 현지 시각([LocalDateTime])이고, 연·월·일 경계도 그 시간대로 나눈다.
 * 구현은 상태가 없어 여러 스레드에서 함께 써도 된다.
 *
 * ## 오류
 * - 인자가 틀리면 [IllegalArgumentException] — 연도가 [MIN_YEAR]..[MAX_YEAR] 밖이거나 월이 1..12 밖일 때.
 *   메시지에 받은 값과 허용 범위가 들어간다
 * - 인자는 맞는데 계산이 끝나지 못하면 [LunarCalculationException] — 원인은 `cause` 에 있다.
 *   교체한 [SpecialMoonCriteria] 가 던진 예외도 이것으로 감싼다
 * - 그 밖의 "없음" 은 예외가 아니라 값으로 알린다(빈 목록·null). 각 함수 설명을 본다
 *
 * 정확도는 [MIN_YEAR]..[MAX_YEAR] 안에서도 2005~2050년이 가장 좋다. ΔT 추정식이 그 구간에 맞춰져 있어
 * 멀어질수록 거리·출몰·절기 시각의 오차가 커진다(위상 시각은 ΔT 를 쓰지 않는다).
 *
 * ```
 * val lunar = LunarCalculator.create()                                   // 한국 표준시
 * val utc = LunarCalculator.create(LunarCalculatorConfig(zone = ZoneOffset.UTC))
 * lunar.phasesOfMonth(2026, 11)
 * ```
 */
@Suppress("TooManyFunctions") // 기능 전부를 진입점 하나로 모은 파사드라 의도된 크기다
public interface LunarCalculator {

    /** 입출력 시각의 시간대. */
    public val zone: ZoneId

    /** 그 달의 위상 이벤트. 위상마다 하나씩, 시각 오름차순이다. 한 달에 두 번 든 위상은 뒤의 것만 남는다. */
    public fun phasesOfMonth(year: Int, month: Int): List<LunarPhaseEvent>

    /** 그 달에 든 [type] 위상 전부. 블루문처럼 한 달에 두 번 들면 둘 다 들어간다. */
    public fun phaseEventsInMonth(year: Int, month: Int, type: LunarPhaseType): List<LunarPhaseEvent>

    /** [dateTime] 의 위상값. 0.0(삭) ~ 0.5(망) ~ 1.0(다음 삭 직전). */
    public fun phaseAt(dateTime: LocalDateTime): Double

    /** [date] 의 음력 일자(1~30). 삭이 든 날을 1일로 센다. */
    public fun lunarDayOf(date: LocalDate): Int

    /** [now] 기준 오늘의 달 요약. */
    public fun moonToday(now: LocalDateTime): MoonToday

    /** 그 달 1일~말일의 날짜별 달. */
    public fun monthCalendar(year: Int, month: Int): List<MoonDay>

    /** [event] 가 그 달의 두 번째 보름달(블루문)이면 true. 보름달이 아니면 false. */
    public fun isBlueMoon(event: LunarPhaseEvent): Boolean

    /** 그해 슈퍼문·미니문. 기준은 [LunarCalculatorConfig.specialMoonCriteria] 이고 일반 보름달은 빠진다. */
    public fun specialFullMoons(year: Int): List<SpecialFullMoon>

    /** 그해 슈퍼문·미니문·블루문을 시각순으로. 같은 시각이면 슈퍼문·미니문이 앞선다. */
    public fun specialMoonsOfYear(year: Int): List<SpecialMoonEvent>

    /** [dateTime] 의 지구-달 중심 거리(km). */
    public fun distanceKm(dateTime: LocalDateTime): Double

    /** [date] 하루(현지 0시~24시)의 월출·월몰. */
    public fun riseSet(date: LocalDate, location: GeoLocation = GeoLocation.SEOUL): MoonRiseSet

    /** 그 달 1일~말일의 월출·월몰(인덱스 0 이 1일). */
    public fun riseSetsOfMonth(yearMonth: YearMonth, location: GeoLocation = GeoLocation.SEOUL): List<MoonRiseSet>

    /** 그해 24절기. 소한부터 동지까지 시각 오름차순이다. */
    public fun solarTermsOfYear(year: Int): List<SolarTerm>

    public companion object {
        /** 계산을 받는 가장 이른 연도. */
        public const val MIN_YEAR: Int = 1900

        /** 계산을 받는 가장 늦은 연도. */
        public const val MAX_YEAR: Int = 2100

        /** [config] 로 계산기를 만든다. 기본값은 한국 표준시와 거리 기준 슈퍼문·미니문이다. */
        public fun create(config: LunarCalculatorConfig = LunarCalculatorConfig()): LunarCalculator =
            DefaultLunarCalculator(config)
    }
}
