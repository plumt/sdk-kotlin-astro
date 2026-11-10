package net.plumt.astro.lunar

import net.plumt.astro.lunar.model.LunarPhaseEvent
import net.plumt.astro.lunar.model.LunarPhaseType
import net.plumt.astro.lunar.model.SpecialFullMoon
import net.plumt.astro.lunar.model.SpecialMoonType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

/**
 * 파생 프로퍼티를 가진 모델([LunarPhaseEvent], [SpecialFullMoon])과
 * 분류 임계값을 갖는 enum([LunarPhaseType])의 불변식 검증 테스트.
 * 순수 data class 는 파생 로직이 없어 제외한다.
 */
class LunarModelTest {

    // ─── LunarPhaseEvent 파생 프로퍼티 ─────────────────────────────

    @Test
    fun `given 이벤트 시각이면 when 파생 프로퍼티 then 연월일시분으로 분해된다`() {
        // given
        val event = LunarPhaseEvent(
            type = LunarPhaseType.FullMoon,
            dateTime = LocalDateTime.of(2026, 1, 3, 19, 3),
        )

        // when / then
        assertEquals(2026, event.year)
        assertEquals(1, event.month)
        assertEquals(3, event.day)
        assertEquals(19, event.hour)
        assertEquals(3, event.minute)
    }

    // ─── SpecialFullMoon 파생 프로퍼티 ─────────────────────────────

    @Test
    fun `given 슈퍼문이면 when 파생 프로퍼티 then 연월일로 분해된다`() {
        // given
        val moon = SpecialFullMoon(
            dateTime = LocalDateTime.of(2026, 11, 5, 22, 19),
            distanceKm = 356_980.0,
            type = SpecialMoonType.Supermoon,
        )

        // when / then
        assertEquals(2026, moon.year)
        assertEquals(11, moon.month)
        assertEquals(5, moon.day)
    }

    // ─── LunarPhaseType 임계값(kFraction) ──────────────────────────

    @Test
    fun `given 위상 타입이면 when kFraction 확인 then 위상 순서대로 0 025 05 075다`() {
        // then
        assertEquals(0.0, LunarPhaseType.NewMoon.kFraction, 0.0)
        assertEquals(0.25, LunarPhaseType.FirstQuarter.kFraction, 0.0)
        assertEquals(0.5, LunarPhaseType.FullMoon.kFraction, 0.0)
        assertEquals(0.75, LunarPhaseType.LastQuarter.kFraction, 0.0)
    }

    @Test
    fun `given 위상 타입 enum이면 when 개수 확인 then 4종이다`() {
        // then
        assertEquals("위상은 삭/상현/망/하현 4종", 4, LunarPhaseType.values.size)
    }
}
