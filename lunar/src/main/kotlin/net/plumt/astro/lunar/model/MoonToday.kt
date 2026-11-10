package net.plumt.astro.lunar.model

import java.util.Objects

/**
 * 한 시각 기준 "오늘의 달" 요약. 화면 문구는 담지 않는다.
 *
 * @property phase 위상값 0.0(삭) ~ 0.5(망) ~ 1.0
 * @property lunarDay 음력 일자
 * @property illuminationPercent 조도(%). 위상에 비례하는 직선 근사다 — 망 100, 삭 0
 * @property todayEventType 오늘 날짜에 든 위상 이벤트 종류. 없으면 null
 * @property upcomingEvents 지금 이후 이번 달 포함 3개월치 위상 이벤트(시간순)
 * @property daysUntilFullMoon 다음 보름까지 남은 날 수. 오늘이거나 다음 달까지 없으면 0
 * @property daysUntilNewMoon 다음 삭까지 남은 날 수. 오늘이거나 다음 달까지 없으면 0
 * @property isBlueMoonComing 오늘 보름달이 블루문이거나 다음 보름달이 블루문이면 true
 */
public class MoonToday(
    public val phase: Double,
    public val lunarDay: Int,
    public val illuminationPercent: Int,
    public val todayEventType: LunarPhaseType?,
    public val upcomingEvents: List<LunarPhaseEvent>,
    public val daysUntilFullMoon: Int,
    public val daysUntilNewMoon: Int,
    public val isBlueMoonComing: Boolean,
) {
    /** [upcomingEvents] 의 첫 보름달. 없으면 null. */
    public val nextFullMoon: LunarPhaseEvent? get() = upcomingEvents.firstOrNull { it.type == LunarPhaseType.FullMoon }

    /** [upcomingEvents] 의 첫 삭. 없으면 null. */
    public val nextNewMoon: LunarPhaseEvent? get() = upcomingEvents.firstOrNull { it.type == LunarPhaseType.NewMoon }

    override fun equals(other: Any?): Boolean = other is MoonToday &&
        phase.compareTo(other.phase) == 0 &&
        lunarDay == other.lunarDay &&
        illuminationPercent == other.illuminationPercent &&
        todayEventType == other.todayEventType &&
        upcomingEvents == other.upcomingEvents &&
        daysUntilFullMoon == other.daysUntilFullMoon &&
        daysUntilNewMoon == other.daysUntilNewMoon &&
        isBlueMoonComing == other.isBlueMoonComing

    override fun hashCode(): Int = Objects.hash(
        phase, lunarDay, illuminationPercent, todayEventType, upcomingEvents, daysUntilFullMoon, daysUntilNewMoon, isBlueMoonComing,
    )

    override fun toString(): String =
        "MoonToday(phase=$phase, lunarDay=$lunarDay, illuminationPercent=$illuminationPercent, todayEventType=$todayEventType, " +
            "upcomingEvents=$upcomingEvents, daysUntilFullMoon=$daysUntilFullMoon, daysUntilNewMoon=$daysUntilNewMoon, " +
            "isBlueMoonComing=$isBlueMoonComing)"
}
