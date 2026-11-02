package net.plumt.astro.lunar.model

import java.time.LocalDateTime

/**
 * 절기 하나의 발생 시각.
 *
 * @property dateTime 발생 시각. 계산기에 설정한 시간대의 현지 시각이다
 */
public data class SolarTerm(
    val type: SolarTermType,
    val dateTime: LocalDateTime,
) {
    /** 해당 절기에 대응하는 태양 황경(도) */
    public val eclipticLongitude: Double get() = type.eclipticLongitude

    public val year: Int get() = dateTime.year
    public val month: Int get() = dateTime.monthValue
    public val day: Int get() = dateTime.dayOfMonth
    public val hour: Int get() = dateTime.hour
    public val minute: Int get() = dateTime.minute
}
