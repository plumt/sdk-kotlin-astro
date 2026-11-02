package net.plumt.astro.lunar

import net.plumt.astro.lunar.model.LunarPhaseType
import net.plumt.astro.lunar.model.SpecialMoonType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset

/** 진입점 [LunarCalculator] 의 설정·위임 검증. 알고리즘 정확도는 알고리즘별 테스트가 본다. */
class LunarCalculatorTest {

    private val kst = LunarCalculator.create()
    private val utc = LunarCalculator.create(LunarCalculatorConfig(zone = ZoneOffset.UTC))

    @Test
    fun `기본 시간대는 한국 표준시다`() {
        assertEquals(LunarCalculatorConfig.KST, kst.zone)
    }

    @Test
    fun `한 달 위상은 4종이 하나씩 시각순으로 나온다`() {
        val events = kst.phasesOfMonth(2026, 1)
        assertEquals(LunarPhaseType.entries.toSet(), events.map { it.type }.toSet())
        assertEquals(events.sortedBy { it.dateTime }, events)
    }

    @Test
    fun `시간대를 바꾸면 같은 순간이 그 시간대 현지 시각으로 나온다`() {
        // 2026년 1월 보름달은 월 경계에서 멀어 두 시간대 모두 1월에 든다
        val kstFull = kst.phaseEventsInMonth(2026, 1, LunarPhaseType.FULL_MOON).single()
        val utcFull = utc.phaseEventsInMonth(2026, 1, LunarPhaseType.FULL_MOON).single()
        assertEquals(Duration.ofHours(9), Duration.between(utcFull.dateTime, kstFull.dateTime))
    }

    @Test
    fun `월출은 시간대마다 그 시간대의 하루 안에서 찾는다`() {
        val date = LocalDate.of(2025, 10, 2)
        listOf(kst, utc).forEach { calculator ->
            val riseSet = calculator.riseSet(date)
            listOfNotNull(riseSet.rise, riseSet.set).forEach { assertEquals(date, it.toLocalDate()) }
        }
        assertNotEquals(kst.riseSet(date).rise, utc.riseSet(date).rise)
    }

    @Test
    fun `기본 기준에서 슈퍼문은 360000km 이하, 미니문은 405000km 이상이다`() {
        val specials = kst.specialFullMoons(2024)
        assertTrue(specials.any { it.type == SpecialMoonType.SUPERMOON })
        assertTrue(specials.any { it.type == SpecialMoonType.MINIMOON })
        specials.forEach {
            when (it.type) {
                SpecialMoonType.SUPERMOON -> assertTrue(it.distanceKm <= DistanceThresholdCriteria.DEFAULT_SUPERMOON_MAX_KM)
                SpecialMoonType.MINIMOON -> assertTrue(it.distanceKm >= DistanceThresholdCriteria.DEFAULT_MINIMOON_MIN_KM)
            }
            assertEquals(2024, it.year)
        }
        assertEquals(specials.sortedBy { it.dateTime }, specials)
    }

    @Test
    fun `판정 기준을 바꿔 끼우면 그 기준으로 분류한다`() {
        val everySuper = LunarCalculator.create(LunarCalculatorConfig(specialMoonCriteria = { SpecialMoonType.SUPERMOON }))
        val fullMoons = (1..12).flatMap { kst.phaseEventsInMonth(2024, it, LunarPhaseType.FULL_MOON) }
        val specials = everySuper.specialFullMoons(2024)
        assertEquals(fullMoons.size, specials.size)
        assertTrue(specials.all { it.type == SpecialMoonType.SUPERMOON })
    }

    @Test
    fun `임계값 기준을 좁히면 슈퍼문이 줄어든다`() {
        val strict = LunarCalculator.create(
            LunarCalculatorConfig(specialMoonCriteria = DistanceThresholdCriteria(supermoonMaxKm = 357_000.0)),
        )
        val defaultCount = kst.specialFullMoons(2024).count { it.type == SpecialMoonType.SUPERMOON }
        val strictCount = strict.specialFullMoons(2024).count { it.type == SpecialMoonType.SUPERMOON }
        assertTrue(strictCount < defaultCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `슈퍼문 임계가 미니문 임계보다 크면 만들 수 없다`() {
        DistanceThresholdCriteria(supermoonMaxKm = 410_000.0, minimoonMinKm = 405_000.0)
    }

    @Test
    fun `특별한 달 목록에는 블루문이 거리 없이 들어간다`() {
        val blueMoon = kst.specialMoonsOfYear(2023).single { it.distanceKm == null }
        assertEquals(LocalDate.of(2023, 8, 31), blueMoon.dateTime.toLocalDate())
    }

    @Test
    fun `24절기는 한 해에 24개가 시각순으로 나온다`() {
        val terms = kst.solarTermsOfYear(2026)
        assertEquals(24, terms.size)
        assertEquals(terms.sortedBy { it.dateTime }, terms)
    }
}
