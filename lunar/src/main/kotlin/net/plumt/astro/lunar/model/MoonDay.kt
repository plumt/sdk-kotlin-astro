package net.plumt.astro.lunar.model

import java.time.LocalDateTime
import java.util.Objects

/**
 * 달력 한 칸(하루)의 달. 화면 문구는 담지 않는다.
 *
 * @property dateTime 그날 0시(현지 시각)
 * @property phase 그 시각의 위상값
 * @property lunarDay 음력 일자
 * @property event 그날 든 위상 이벤트. 하루에 둘이면 뒤의 것이다
 * @property isBlueMoon 그날이 그 달의 블루문(두 번째 보름달)이면 true
 */
public class MoonDay(
    public val dateTime: LocalDateTime,
    public val phase: Double,
    public val lunarDay: Int,
    public val event: LunarPhaseEvent?,
    public val isBlueMoon: Boolean,
) {
    override fun equals(other: Any?): Boolean = other is MoonDay &&
        dateTime == other.dateTime &&
        phase.compareTo(other.phase) == 0 &&
        lunarDay == other.lunarDay &&
        event == other.event &&
        isBlueMoon == other.isBlueMoon

    override fun hashCode(): Int = Objects.hash(dateTime, phase, lunarDay, event, isBlueMoon)

    override fun toString(): String =
        "MoonDay(dateTime=$dateTime, phase=$phase, lunarDay=$lunarDay, event=$event, isBlueMoon=$isBlueMoon)"
}
