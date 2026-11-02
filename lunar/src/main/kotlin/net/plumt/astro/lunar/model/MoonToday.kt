package net.plumt.astro.lunar.model

/**
 * 한 시각 기준 "오늘의 달" 요약. 화면 문구는 담지 않는다.
 *
 * @property phase 위상값 0.0(삭) ~ 0.5(망) ~ 1.0
 * @property lunarDay 음력 일자
 * @property illuminationPercent 조도(%). 위상에 비례하는 직선 근사다 — 망 100, 삭 0
 * @property todayEventType 오늘 날짜에 든 위상 이벤트 종류. 없으면 null
 * @property upcomingEvents 지금 이후 이번 달 포함 3개월치 위상 이벤트(시간순)
 * @property isBlueMoonComing 오늘 보름달이 블루문이거나 다음 보름달이 블루문이면 true
 */
public data class MoonToday(
    val phase: Double,
    val lunarDay: Int,
    val illuminationPercent: Int,
    val todayEventType: LunarPhaseType?,
    val upcomingEvents: List<LunarPhaseEvent>,
    val daysUntilFullMoon: Int,
    val daysUntilNewMoon: Int,
    val isBlueMoonComing: Boolean,
) {
    public val nextFullMoon: LunarPhaseEvent? get() = upcomingEvents.firstOrNull { it.type == LunarPhaseType.FULL_MOON }
    public val nextNewMoon: LunarPhaseEvent? get() = upcomingEvents.firstOrNull { it.type == LunarPhaseType.NEW_MOON }
}
