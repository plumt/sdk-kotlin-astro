package net.plumt.astro.lunar.model

import java.time.LocalDateTime

/**
 * 달력 한 칸(하루)의 달. 화면 문구는 담지 않는다.
 *
 * @property dateTime 그날 0시(현지 시각)
 * @property phase 그 시각의 위상값
 * @property lunarDay 음력 일자
 * @property event 그날 든 위상 이벤트. 하루에 둘이면 뒤의 것이다
 * @property isBlueMoon 그날이 그 달의 블루문(두 번째 보름달)이면 true
 */
public data class MoonDay(
    val dateTime: LocalDateTime,
    val phase: Double,
    val lunarDay: Int,
    val event: LunarPhaseEvent?,
    val isBlueMoon: Boolean,
)
