package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.algorithm.SolarTermAlgorithm
import net.plumt.astro.lunar.model.SolarTerm
import net.plumt.astro.lunar.model.SolarTermType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Duration

/**
 * SolarTermAlgorithm(24절기) 검증 테스트.
 *
 * 날짜를 하드코딩하지 않고, 천문학적으로 확정적인 범위(분점/지점/입춘)와
 * 구조적 성질(24개, 황경 배치, 인접 간격 14~16일)을 검증한다.
 */
class SolarTermAlgorithmTest {

    private lateinit var algorithm: SolarTermAlgorithm

    @Before
    fun setUp() {
        algorithm = SolarTermAlgorithm(LunarCalculatorConfig.KST, applyDeltaT = true)
    }

    private fun term(terms: List<SolarTerm>, type: SolarTermType): SolarTerm =
        terms.first { it.type == type }

    @Test
    fun `한 해 절기는 정확히 24개다`() {
        val terms2024 = algorithm.solarTermsOfYear(2024)
        val terms2025 = algorithm.solarTermsOfYear(2025)
        assertEquals("2024년 절기 24개", 24, terms2024.size)
        assertEquals("2025년 절기 24개", 24, terms2025.size)
        // 24개 절기 종류가 모두 한 번씩 등장
        assertEquals(24, terms2024.map { it.type }.toSet().size)
    }

    @Test
    fun `절기는 시각 오름차순으로 정렬된다`() {
        val terms = algorithm.solarTermsOfYear(2024)
        for (i in 1 until terms.size) {
            assertTrue(
                "정렬 위반: ${terms[i - 1].type} > ${terms[i].type}",
                !terms[i].dateTime.isBefore(terms[i - 1].dateTime),
            )
        }
    }

    @Test
    fun `인접 절기 간격은 약 14~16일이다`() {
        val terms = algorithm.solarTermsOfYear(2025)
        for (i in 1 until terms.size) {
            val days = Duration.between(terms[i - 1].dateTime, terms[i].dateTime).toHours() / 24.0
            assertTrue(
                "간격 이상: ${terms[i - 1].type}→${terms[i].type} = ${"%.2f".format(days)}일",
                days in 13.5..16.5,
            )
        }
    }

    @Test
    fun `춘분은 3월 20~21일 범위다`() {
        listOf(2024, 2025).forEach { year ->
            val t = term(algorithm.solarTermsOfYear(year), SolarTermType.SPRING_EQUINOX)
            assertEquals("$year 춘분 3월", 3, t.month)
            assertTrue("$year 춘분 ${t.day}일", t.day in 20..21)
        }
    }

    @Test
    fun `하지는 6월 21~22일 범위다`() {
        listOf(2024, 2025).forEach { year ->
            val t = term(algorithm.solarTermsOfYear(year), SolarTermType.SUMMER_SOLSTICE)
            assertEquals("$year 하지 6월", 6, t.month)
            assertTrue("$year 하지 ${t.day}일", t.day in 21..22)
        }
    }

    @Test
    fun `추분은 9월 22~23일 범위다`() {
        listOf(2024, 2025).forEach { year ->
            val t = term(algorithm.solarTermsOfYear(year), SolarTermType.AUTUMN_EQUINOX)
            assertEquals("$year 추분 9월", 9, t.month)
            assertTrue("$year 추분 ${t.day}일", t.day in 22..23)
        }
    }

    @Test
    fun `동지는 12월 21~22일 범위다`() {
        listOf(2024, 2025).forEach { year ->
            val t = term(algorithm.solarTermsOfYear(year), SolarTermType.WINTER_SOLSTICE)
            assertEquals("$year 동지 12월", 12, t.month)
            assertTrue("$year 동지 ${t.day}일", t.day in 21..22)
        }
    }

    @Test
    fun `입춘은 2월 3~5일 범위다`() {
        listOf(2024, 2025).forEach { year ->
            val t = term(algorithm.solarTermsOfYear(year), SolarTermType.START_OF_SPRING)
            assertEquals("$year 입춘 2월", 2, t.month)
            assertTrue("$year 입춘 ${t.day}일", t.day in 3..5)
        }
    }
}
