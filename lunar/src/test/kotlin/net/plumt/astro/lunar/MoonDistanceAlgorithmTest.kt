package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.algorithm.MoonDistanceAlgorithm
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * MoonDistanceAlgorithm(지구-달 거리 Meeus Ch.47) 검증 테스트.
 *
 * 절대값 단언이 어려운 천문 계산이므로 물리 범위·변동성·주기성·근지점/원지점 불변식으로 검증한다.
 * 근지점 ≈ 356,400~370,000km, 원지점 ≈ 404,000~406,700km(소스 문서 기준).
 */
class MoonDistanceAlgorithmTest {

    private lateinit var algorithm: MoonDistanceAlgorithm

    @Before
    fun setUp() {
        algorithm = MoonDistanceAlgorithm(LunarCalculatorConfig.KST)
    }

    @Test
    fun `given 임의 시각 when 거리 계산 then 물리적 근지점 원지점 범위 안이다`() {
        // given: 한 삭망월(약 29.5일)을 6시간 간격으로 전수 샘플링
        var t = LocalDateTime.of(2026, 1, 1, 0, 0)
        val end = LocalDateTime.of(2026, 1, 30, 0, 0)

        // when / then
        while (t.isBefore(end)) {
            val d = algorithm.distanceKm(t)
            assertTrue(
                "$t 거리 ${d}km는 물리 범위(350,000~407,000km) 안이어야 함",
                d in 350_000.0..407_000.0,
            )
            t = t.plusHours(6)
        }
    }

    @Test
    fun `given 한 달 샘플링 when 최소 최대 then 근지점은 원지점보다 작고 충분히 변동한다`() {
        // given
        val samples = (0 until 120).map { i ->
            algorithm.distanceKm(LocalDateTime.of(2026, 1, 1, 0, 0).plusHours(i * 6L))
        }

        // when
        val min = samples.min()
        val max = samples.max()

        // then: 근지점은 370,000km 이하, 원지점은 400,000km 이상, 변동폭은 2만km 이상
        assertTrue("근지점 ${min}km는 370,000km 이하여야 함", min <= 370_000.0)
        assertTrue("원지점 ${max}km는 400,000km 이상이어야 함", max >= 400_000.0)
        assertTrue("한 달 변동폭은 20,000km 이상이어야 함", (max - min) >= 20_000.0)
    }

    @Test
    fun `given 서로 다른 시각 when 거리 계산 then 시간에 따라 값이 달라진다`() {
        // given
        val d1 = algorithm.distanceKm(LocalDateTime.of(2026, 1, 1, 0, 0))
        val d2 = algorithm.distanceKm(LocalDateTime.of(2026, 1, 8, 0, 0))

        // when / then: 일주일 차이면 거리 항이 충분히 변해 동일하지 않아야 함
        assertNotEquals("일주일 차이의 거리는 같지 않아야 함", d1, d2, 1.0)
    }

    @Test
    fun `given 같은 시각 when 두 번 계산 then 결정적으로 동일한 값을 반환한다`() {
        // given
        val time = LocalDateTime.of(2026, 6, 11, 12, 0)

        // when
        val first = algorithm.distanceKm(time)
        val second = algorithm.distanceKm(time)

        // then
        assertTrue("같은 입력은 같은 결과를 내야 함", first == second)
    }

    @Test
    fun `given deltaT 보정 on off when 같은 시각 then 결과가 가까우나 동일하지는 않다`() {
        // given
        val withDeltaT = MoonDistanceAlgorithm(LunarCalculatorConfig.KST, applyDeltaT = true)
        val withoutDeltaT = MoonDistanceAlgorithm(LunarCalculatorConfig.KST, applyDeltaT = false)
        val time = LocalDateTime.of(2026, 3, 3, 20, 38)

        // when
        val a = withDeltaT.distanceKm(time)
        val b = withoutDeltaT.distanceKm(time)

        // then: ΔT(약 75초)만큼 위상이 달라 미세하게 다르되, 차이는 수십 km 이내
        assertNotEquals("ΔT 보정 여부에 따라 미세 차이가 있어야 함", a, b, 1e-9)
        assertTrue("ΔT 보정 차이는 100km 이내여야 함", kotlin.math.abs(a - b) < 100.0)
    }
}
