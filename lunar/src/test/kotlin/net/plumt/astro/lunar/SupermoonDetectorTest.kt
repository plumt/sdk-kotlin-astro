package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.SupermoonDetector
import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.internal.algorithm.MoonDistanceAlgorithm
import net.plumt.astro.lunar.model.SpecialMoonType
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * SupermoonDetector(슈퍼문/미니문) 및 MoonDistanceAlgorithm(지구-달 거리) 검증 테스트.
 *
 * 거리는 천문학적으로 확정적인 물리 범위(근지점~원지점)와
 * 2024년 슈퍼문/미니문 존재 사실로 검증한다(날짜 하드코딩 회피).
 */
class SupermoonDetectorTest {

    private lateinit var phaseAlgorithm: MeeusChapter49Algorithm
    private lateinit var distanceAlgorithm: MoonDistanceAlgorithm

    @Before
    fun setUp() {
        phaseAlgorithm = MeeusChapter49Algorithm(LunarCalculatorConfig.KST, applyDeltaT = true)
        distanceAlgorithm = MoonDistanceAlgorithm(LunarCalculatorConfig.KST, applyDeltaT = true)
    }

    @Test
    fun `보름달 거리는 항상 근지점~원지점 물리 범위 안에 든다`() {
        listOf(2024, 2025, 2026).forEach { year ->
            val distances = SupermoonDetector.fullMoonSamples(year, phaseAlgorithm, distanceAlgorithm)
            assertTrue("$year 보름달 12~13회", distances.size in 12..14)
            distances.forEach { (kst, dist) ->
                assertTrue(
                    "$year $kst 거리 ${"%.0f".format(dist)}km 범위 이탈",
                    dist in 356_000.0..407_000.0,
                )
            }
        }
    }

    @Test
    fun `2024년에 슈퍼문이 존재한다`() {
        val specials = SupermoonDetector.classify(2024, phaseAlgorithm, distanceAlgorithm, DistanceThresholdCriteria())
        val supermoons = specials.filter { it.type == SpecialMoonType.SUPERMOON }
        assertTrue("2024 슈퍼문 1회 이상", supermoons.isNotEmpty())
    }

    @Test
    fun `2024년 10월 17일 무렵 보름달은 약 357000km 슈퍼문이다`() {
        val distances = SupermoonDetector.fullMoonSamples(2024, phaseAlgorithm, distanceAlgorithm)
        // 2024-10-17 ±1일 범위의 보름달을 찾는다
        val octFullMoon = distances.firstOrNull { sample ->
            val d = sample.dateTime.toLocalDate()
            !d.isBefore(LocalDate.of(2024, 10, 16)) && !d.isAfter(LocalDate.of(2024, 10, 18))
        }
        assertTrue("2024-10-17 무렵 보름달 존재", octFullMoon != null)
        val kst = octFullMoon!!.dateTime
        val dist = octFullMoon.distanceKm
        // 약 357,000 km (±수백 km 허용, 절단 급수 여유 포함 ±2000)
        assertTrue(
            "2024-10-17 보름달 거리 ${"%.0f".format(dist)}km ($kst)",
            dist in 355_000.0..359_000.0,
        )
        assertTrue("슈퍼문 임계값(360000km) 이하", dist <= DistanceThresholdCriteria.DEFAULT_SUPERMOON_MAX_KM)
    }

    @Test
    fun `2024년에 미니문이 1회 이상 검출된다`() {
        val specials = SupermoonDetector.classify(2024, phaseAlgorithm, distanceAlgorithm, DistanceThresholdCriteria())
        val minimoons = specials.filter { it.type == SpecialMoonType.MINIMOON }
        assertTrue("2024 미니문 1회 이상", minimoons.isNotEmpty())
    }
}
