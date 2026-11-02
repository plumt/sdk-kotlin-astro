package net.plumt.astro.lunar.internal

import net.plumt.astro.lunar.LunarCalculationException
import net.plumt.astro.lunar.LunarCalculator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/** 진입점 인자 검사. 실패하면 받은 값과 허용 범위를 담은 [IllegalArgumentException] 을 던진다. */
internal object Preconditions {

    private val YEARS = LunarCalculator.MIN_YEAR..LunarCalculator.MAX_YEAR

    fun year(year: Int, name: String = "year") {
        require(year in YEARS) { "$name($year) 는 $YEARS 범위여야 한다" }
    }

    fun yearMonth(year: Int, month: Int) {
        year(year)
        require(month in 1..12) { "month($month) 는 1..12 범위여야 한다" }
    }

    fun yearMonth(yearMonth: YearMonth) {
        year(yearMonth.year, "yearMonth.year")
    }

    fun date(date: LocalDate, name: String = "date") {
        year(date.year, "$name.year")
    }

    fun dateTime(dateTime: LocalDateTime, name: String = "dateTime") {
        year(dateTime.year, "$name.year")
    }
}

/**
 * 계산 본문을 감싼다. 인자 검사를 통과한 뒤의 예외는 모두 [LunarCalculationException] 으로 바꿔
 * 호출부가 "인자 오류" 와 "계산 실패" 두 가지만 구분하면 되게 한다.
 */
internal inline fun <T> calculating(what: String, block: () -> T): T =
    try {
        block()
    } catch (e: LunarCalculationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: RuntimeException) {
        throw LunarCalculationException("$what 계산에 실패했다: ${e.message}", e)
    }

/** 계산 결과가 유한한 수인지 확인한다. NaN·무한대가 호출부로 새 나가지 않게 한다. */
internal fun Double.requireFinite(what: String): Double {
    if (!isFinite()) throw LunarCalculationException("$what 계산 결과가 유한하지 않다: $this")
    return this
}
