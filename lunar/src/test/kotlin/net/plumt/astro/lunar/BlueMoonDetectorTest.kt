package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.BlueMoonDetector
import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.model.LunarPhaseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * BlueMoonDetector 검증.
 *
 * 기준 사례: 2023년 8월은 양력 8월 안에 보름달이 두 번(8/1, 8/31, KST) 떠서 8월 31일이 블루문이다.
 */
class BlueMoonDetectorTest {

    private val algorithm = MeeusChapter49Algorithm(LunarCalculatorConfig.KST, applyDeltaT = true)

    @Test
    fun `2023년 8월은 두 번째 보름달만 블루문이다`() {
        val fullMoons = algorithm.eventsOfTypeInMonth(2023, 8, LunarPhaseType.FullMoon)
        assertEquals(2, fullMoons.size)
        assertFalse(BlueMoonDetector.isBlueMoon(fullMoons.first(), algorithm))
        assertTrue(BlueMoonDetector.isBlueMoon(fullMoons.last(), algorithm))
        assertEquals(31, fullMoons.last().day)
    }

    @Test
    fun `보름달이 아닌 위상은 블루문이 아니다`() {
        val newMoon = algorithm.eventsOfTypeInMonth(2023, 8, LunarPhaseType.NewMoon).first()
        assertFalse(BlueMoonDetector.isBlueMoon(newMoon, algorithm))
    }

    @Test
    fun `findBlueMoon - 2023년 8월은 8월 31일을 돌려준다`() {
        val blueMoon = BlueMoonDetector.findBlueMoon(2023, 8, algorithm)!!
        assertEquals(8, blueMoon.month)
        assertEquals(31, blueMoon.day)
    }

    @Test
    fun `findBlueMoon - 보름달이 한 번뿐인 달은 null`() {
        assertNull(BlueMoonDetector.findBlueMoon(2026, 1, algorithm))
    }

    @Test
    fun `eventsOfTypeInMonth 는 그 연월 보름달만 시각순으로 돌려준다`() {
        val fullMoons = algorithm.eventsOfTypeInMonth(2026, 1, LunarPhaseType.FullMoon)
        assertTrue(fullMoons.isNotEmpty())
        assertTrue(fullMoons.all { it.year == 2026 && it.month == 1 && it.type == LunarPhaseType.FullMoon })
        assertEquals(fullMoons.sortedBy { it.dateTime }, fullMoons)
    }

    @Test
    fun `한 해 전체에서 보름달이 두 번인 달만 블루문을 돌려준다`() {
        (1..12).forEach { month ->
            val fulls = algorithm.eventsOfTypeInMonth(2026, month, LunarPhaseType.FullMoon)
            val blueMoon = BlueMoonDetector.findBlueMoon(2026, month, algorithm)
            if (fulls.size >= 2) assertEquals(fulls.last().dateTime, blueMoon?.dateTime) else assertNull(blueMoon)
        }
    }
}
