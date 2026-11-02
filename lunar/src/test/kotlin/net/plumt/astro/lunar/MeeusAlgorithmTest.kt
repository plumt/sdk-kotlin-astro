package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.model.LunarPhaseEvent
import net.plumt.astro.lunar.model.LunarPhaseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * MeeusChapter49Algorithm 정확도 검증 테스트.
 *
 * ## 기준 데이터
 * KASI(한국천문연구원) 천문우주지식정보 (astro.kasi.re.kr) 기준 KST 시각.
 *
 * ## 허용 오차
 * - 날짜: ±0일 (날짜 일치 필수)
 * - 시각: ±5분 (알고리즘 구조적 한계 ±2~3분 + 여유 2분)
 */
class MeeusAlgorithmTest {

    private lateinit var algorithm: MeeusChapter49Algorithm

    // KASI 기준 데이터 (KST)
    private val kasiReference: Map<Pair<Int, Int>, List<Pair<LunarPhaseType, LocalDateTime>>> = mapOf(
        (2026 to 1) to listOf(
            LunarPhaseType.FULL_MOON to LocalDateTime.of(2026, 1, 3, 19, 3),
            LunarPhaseType.LAST_QUARTER to LocalDateTime.of(2026, 1, 11, 0, 48),
            LunarPhaseType.NEW_MOON to LocalDateTime.of(2026, 1, 19, 4, 52),
            LunarPhaseType.FIRST_QUARTER to LocalDateTime.of(2026, 1, 26, 13, 47),
        ),
        (2026 to 2) to listOf(
            LunarPhaseType.FULL_MOON to LocalDateTime.of(2026, 2, 2, 7, 9),
            LunarPhaseType.LAST_QUARTER to LocalDateTime.of(2026, 2, 9, 21, 43),
            LunarPhaseType.NEW_MOON to LocalDateTime.of(2026, 2, 17, 21, 1),
            LunarPhaseType.FIRST_QUARTER to LocalDateTime.of(2026, 2, 24, 21, 27),
        ),
        (2026 to 3) to listOf(
            LunarPhaseType.FULL_MOON to LocalDateTime.of(2026, 3, 3, 20, 38),
            LunarPhaseType.LAST_QUARTER to LocalDateTime.of(2026, 3, 11, 18, 38),
            LunarPhaseType.NEW_MOON to LocalDateTime.of(2026, 3, 19, 10, 23),
            LunarPhaseType.FIRST_QUARTER to LocalDateTime.of(2026, 3, 26, 4, 18),
        ),
        (2026 to 4) to listOf(
            LunarPhaseType.FULL_MOON to LocalDateTime.of(2026, 4, 2, 11, 14),
            LunarPhaseType.LAST_QUARTER to LocalDateTime.of(2026, 4, 10, 13, 54),
            LunarPhaseType.NEW_MOON to LocalDateTime.of(2026, 4, 17, 20, 54),
            LunarPhaseType.FIRST_QUARTER to LocalDateTime.of(2026, 4, 24, 11, 34),
        ),
        (2026 to 12) to listOf(
            LunarPhaseType.NEW_MOON to LocalDateTime.of(2026, 12, 9, 9, 52),
            LunarPhaseType.FIRST_QUARTER to LocalDateTime.of(2026, 12, 17, 14, 42),
            LunarPhaseType.FULL_MOON to LocalDateTime.of(2026, 12, 24, 10, 28),
            LunarPhaseType.LAST_QUARTER to LocalDateTime.of(2026, 12, 31, 3, 59),
        ),
    )

    @Before
    fun setUp() {
        algorithm = MeeusChapter49Algorithm(LunarCalculatorConfig.KST)
    }

    // ─── 날짜 정확도 테스트 ────────────────────────────────────────

    @Test
    fun `2026년 1월 모든 이벤트 날짜 일치`() = assertDatesMatch(2026, 1)

    @Test
    fun `2026년 2월 모든 이벤트 날짜 일치`() = assertDatesMatch(2026, 2)

    @Test
    fun `2026년 3월 모든 이벤트 날짜 일치`() = assertDatesMatch(2026, 3)

    @Test
    fun `2026년 4월 모든 이벤트 날짜 일치`() = assertDatesMatch(2026, 4)

    @Test
    fun `2026년 12월 모든 이벤트 날짜 일치`() = assertDatesMatch(2026, 12)

    // ─── 시각 정확도 테스트 (±5분 허용) ─────────────────────────────

    @Test
    fun `2026년 1월 모든 이벤트 시각 5분 내 일치`() = assertTimesWithin(2026, 1, toleranceMinutes = 5)

    @Test
    fun `2026년 2월 모든 이벤트 시각 5분 내 일치`() = assertTimesWithin(2026, 2, toleranceMinutes = 5)

    @Test
    fun `2026년 3월 모든 이벤트 시각 5분 내 일치`() = assertTimesWithin(2026, 3, toleranceMinutes = 5)

    @Test
    fun `2026년 4월 모든 이벤트 시각 5분 내 일치`() = assertTimesWithin(2026, 4, toleranceMinutes = 5)

    @Test
    fun `2026년 12월 모든 이벤트 시각 5분 내 일치`() = assertTimesWithin(2026, 12, toleranceMinutes = 5)

    // ─── 이벤트 개수 테스트 ────────────────────────────────────────

    @Test
    fun `각 월에 이벤트가 4개 반환됨`() {
        listOf(1, 2, 3, 4, 12).forEach { month ->
            val results = algorithm.calculate(2026, month)
            assertEquals("2026년 ${month}월 이벤트 개수 오류", 4, results.size)
        }
    }

    // ─── 이벤트 타입 포함 여부 테스트 ─────────────────────────────

    @Test
    fun `각 월에 삭 상현 망 하현 모두 포함됨`() {
        listOf(1, 2, 3, 4, 12).forEach { month ->
            val results = algorithm.calculate(2026, month)
            LunarPhaseType.entries.forEach { type ->
                assertTrue(
                    "2026년 ${month}월에 ${type.koreanName} 누락",
                    results.any { it.type == type }
                )
            }
        }
    }

    // ─── 결과 정렬 테스트 ──────────────────────────────────────────

    @Test
    fun `이벤트가 날짜 오름차순으로 정렬됨`() {
        val results = algorithm.calculate(2026, 1)
        for (i in 0 until results.size - 1) {
            assertTrue(
                "정렬 오류: ${results[i].dateTime} > ${results[i + 1].dateTime}",
                !results[i].dateTime.isAfter(results[i + 1].dateTime)
            )
        }
    }

    // ─── 오차 리포트 (디버깅용) ────────────────────────────────────

    @Test
    fun `2026년 전체 오차 리포트 출력`() {
        kasiReference.forEach { (yearMonth, references) ->
            val (year, month) = yearMonth
            val results = algorithm.calculate(year, month)
            println("\n── ${year}년 ${month}월 ─────────────────")
            references.forEach { (type, kasiTime) ->
                val calculated = results.firstOrNull { it.type == type }
                if (calculated != null) {
                    val diffMin = ChronoUnit.MINUTES.between(kasiTime, calculated.dateTime)
                    println("  ${type.koreanName}: KASI=${kasiTime.toHHmm()}, 계산=${calculated.dateTime.toHHmm()}, 오차=${diffMin}분")
                } else {
                    println("  ${type.koreanName}: 이벤트 없음 (오류)")
                }
            }
        }
    }

    // ─── 헬퍼 함수 ────────────────────────────────────────────────

    private fun assertDatesMatch(year: Int, month: Int) {
        val references = kasiReference[year to month] ?: return
        val results = algorithm.calculate(year, month)

        references.forEach { (type, kasiTime) ->
            val calculated = results.firstOrNull { it.type == type }
            checkNotNull(calculated) { "${type.koreanName} 이벤트 없음" }
            assertEquals(
                "${type.koreanName} 날짜 불일치 (계산: ${calculated.dateTime}, KASI: $kasiTime)",
                kasiTime.dayOfMonth,
                calculated.day
            )
        }
    }

    private fun assertTimesWithin(year: Int, month: Int, toleranceMinutes: Long) {
        val references = kasiReference[year to month] ?: return
        val results = algorithm.calculate(year, month)

        references.forEach { (type, kasiTime) ->
            val calculated = results.firstOrNull { it.type == type }
            checkNotNull(calculated) { "${type.koreanName} 이벤트 없음" }
            val diffMin = abs(ChronoUnit.MINUTES.between(kasiTime, calculated.dateTime))
            assertTrue(
                "${type.koreanName} 시각 오차 ${diffMin}분 (허용: ${toleranceMinutes}분) | KASI: ${kasiTime.toHHmm()}, 계산: ${calculated.dateTime.toHHmm()}",
                diffMin <= toleranceMinutes
            )
        }
    }

    private fun LocalDateTime.toHHmm() = "%02d:%02d".format(hour, minute)

    private fun LunarPhaseEvent.toKsiStr() =
        "${month}월 ${day}일 ${"%02d:%02d".format(hour, minute)}"
}
