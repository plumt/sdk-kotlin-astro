package net.plumt.astro.lunar.model

import java.time.LocalDateTime
import java.util.Objects

/**
 * 절기 하나의 발생 시각.
 *
 * @property type 절기 종류
 * @property dateTime 발생 시각. 계산기에 설정한 시간대의 현지 시각이다
 */
public class SolarTerm(
    public val type: SolarTermType,
    public val dateTime: LocalDateTime,
) {
    /** 해당 절기에 대응하는 태양 황경(도). */
    public val eclipticLongitude: Double get() = type.eclipticLongitude

    /** [dateTime] 의 연도. */
    public val year: Int get() = dateTime.year

    /** [dateTime] 의 월(1~12). */
    public val month: Int get() = dateTime.monthValue

    /** [dateTime] 의 일. */
    public val day: Int get() = dateTime.dayOfMonth

    /** [dateTime] 의 시(0~23). */
    public val hour: Int get() = dateTime.hour

    /** [dateTime] 의 분. */
    public val minute: Int get() = dateTime.minute

    override fun equals(other: Any?): Boolean = other is SolarTerm && type == other.type && dateTime == other.dateTime

    override fun hashCode(): Int = Objects.hash(type, dateTime)

    override fun toString(): String = "SolarTerm(type=$type, dateTime=$dateTime)"
}
