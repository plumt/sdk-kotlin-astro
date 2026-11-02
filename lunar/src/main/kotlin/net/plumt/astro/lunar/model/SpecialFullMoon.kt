package net.plumt.astro.lunar.model

import java.time.LocalDateTime

/** 거리로 가르는 특별한 보름달 종류. 기준은 [net.plumt.astro.lunar.SpecialMoonCriteria] 가 정한다. */
public enum class SpecialMoonType(public val koreanName: String) {
    /** 지구와 가까운 보름달 */
    SUPERMOON("슈퍼문"),

    /** 지구와 먼 보름달 */
    MINIMOON("미니문"),
}

/**
 * 슈퍼문·미니문으로 분류된 보름달.
 *
 * @property dateTime 보름달(망) 시각. 계산기에 설정한 시간대의 현지 시각이다
 * @property distanceKm 그 시각의 지구-달 중심 거리(km)
 */
public data class SpecialFullMoon(
    val dateTime: LocalDateTime,
    val distanceKm: Double,
    val type: SpecialMoonType,
) {
    public val year: Int get() = dateTime.year
    public val month: Int get() = dateTime.monthValue
    public val day: Int get() = dateTime.dayOfMonth
}
