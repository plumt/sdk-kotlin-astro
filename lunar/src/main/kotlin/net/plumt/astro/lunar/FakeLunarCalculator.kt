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
 * 시험용 [LunarCalculator]. 앱 단위 테스트에서 기기 없이 호출을 기록하고 실패를 손으로 낸다.
 *
 * - 값은 실제 계산과 같다(계산은 네트워크·기기 없이 결정적이다). 인자 검사도 실제와 같다
 * - [failWith] 를 부르면 이후 모든 계산 함수가 인자 검사 전에 그 예외를 던진다. [succeed] 로 되돌린다
 * - 부른 함수 이름은 [calls] 에 순서대로 쌓인다([zone] 읽기는 세지 않는다)
 *
 * 스레드 안전하다.
 *
 * ```
 * val lunar = FakeLunarCalculator()
 * lunar.failWith(LunarCalculationException("시험용 실패"))
 * viewModel(lunar).load()             // 오류 화면을 확인한다
 * assertEquals(listOf("moonToday"), lunar.calls)
 * ```
 */
@Suppress("TooManyFunctions") // 진입점 인터페이스를 그대로 구현한다
public class FakeLunarCalculator(config: LunarCalculatorConfig) : LunarCalculator {

    /** 기본 설정(한국 표준시, 거리 기준 슈퍼문·미니문)의 가짜 계산기. */
    public constructor() : this(LunarCalculatorConfig.builder().build())

    private val real = DefaultLunarCalculator(config)
    private val lock = Any()
    private val recorded = ArrayList<String>()
    private var failure: RuntimeException? = null

    override val zone: ZoneId = real.zone

    /** 지금까지 부른 계산 함수 이름(부른 순서). 사본이다. */
    public val calls: List<String> get() = synchronized(lock) { ArrayList(recorded) }

    /** 이후 계산 함수가 [exception] 을 던지게 한다. */
    public fun failWith(exception: RuntimeException) {
        synchronized(lock) { failure = exception }
    }

    /** [failWith] 를 되돌린다. */
    public fun succeed() {
        synchronized(lock) { failure = null }
    }

    /** 기록한 호출을 지운다. 실패 설정은 그대로 둔다. */
    public fun clearCalls() {
        synchronized(lock) { recorded.clear() }
    }

    override fun phasesOfMonth(year: Int, month: Int): List<LunarPhaseEvent> =
        call("phasesOfMonth") { real.phasesOfMonth(year, month) }

    override fun phaseEventsInMonth(year: Int, month: Int, type: LunarPhaseType): List<LunarPhaseEvent> =
        call("phaseEventsInMonth") { real.phaseEventsInMonth(year, month, type) }

    override fun phaseAt(dateTime: LocalDateTime): Double = call("phaseAt") { real.phaseAt(dateTime) }

    override fun lunarDayOf(date: LocalDate): Int = call("lunarDayOf") { real.lunarDayOf(date) }

    override fun moonToday(now: LocalDateTime): MoonToday = call("moonToday") { real.moonToday(now) }

    override fun monthCalendar(year: Int, month: Int): List<MoonDay> =
        call("monthCalendar") { real.monthCalendar(year, month) }

    override fun isBlueMoon(event: LunarPhaseEvent): Boolean = call("isBlueMoon") { real.isBlueMoon(event) }

    override fun specialFullMoons(year: Int): List<SpecialFullMoon> =
        call("specialFullMoons") { real.specialFullMoons(year) }

    override fun specialMoonsOfYear(year: Int): List<SpecialMoonEvent> =
        call("specialMoonsOfYear") { real.specialMoonsOfYear(year) }

    override fun distanceKm(dateTime: LocalDateTime): Double = call("distanceKm") { real.distanceKm(dateTime) }

    override fun riseSet(date: LocalDate, location: GeoLocation): MoonRiseSet =
        call("riseSet") { real.riseSet(date, location) }

    override fun riseSetsOfMonth(yearMonth: YearMonth, location: GeoLocation): List<MoonRiseSet> =
        call("riseSetsOfMonth") { real.riseSetsOfMonth(yearMonth, location) }

    override fun solarTermsOfYear(year: Int): List<SolarTerm> =
        call("solarTermsOfYear") { real.solarTermsOfYear(year) }

    override fun toString(): String =
        "FakeLunarCalculator(zone=$zone, calls=${calls.size}, failure=${synchronized(lock) { failure }})"

    private inline fun <T> call(name: String, block: () -> T): T {
        val failed = synchronized(lock) {
            recorded += name
            failure
        }
        if (failed != null) throw failed
        return block()
    }
}
