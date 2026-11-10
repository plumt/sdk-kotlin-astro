package net.plumt.astro.lunar.model

import java.time.LocalDateTime
import java.util.Objects

/**
 * 거리로 가르는 특별한 보름달 종류. 기준은 [net.plumt.astro.lunar.SpecialMoonCriteria] 가 정한다.
 * 닫힌 집합이다 — `when` 에 `else` 없이 두 경우를 다룬다.
 *
 * @property koreanName 한국어 이름
 */
public sealed class SpecialMoonType(public val koreanName: String) {
    /** 지구와 가까운 보름달. */
    public data object Supermoon : SpecialMoonType("슈퍼문")

    /** 지구와 먼 보름달. */
    public data object Minimoon : SpecialMoonType("미니문")

    /** 값 목록. */
    public companion object {
        /** 두 종류(슈퍼문 → 미니문 순서). */
        @JvmStatic
        public val values: List<SpecialMoonType> get() = listOf(Supermoon, Minimoon)
    }
}

/**
 * 슈퍼문·미니문으로 분류된 보름달.
 *
 * @property dateTime 보름달(망) 시각. 계산기에 설정한 시간대의 현지 시각이다
 * @property distanceKm 그 시각의 지구-달 중심 거리(km)
 * @property type 분류
 */
public class SpecialFullMoon(
    public val dateTime: LocalDateTime,
    public val distanceKm: Double,
    public val type: SpecialMoonType,
) {
    /** [dateTime] 의 연도. */
    public val year: Int get() = dateTime.year

    /** [dateTime] 의 월(1~12). */
    public val month: Int get() = dateTime.monthValue

    /** [dateTime] 의 일. */
    public val day: Int get() = dateTime.dayOfMonth

    override fun equals(other: Any?): Boolean = other is SpecialFullMoon &&
        dateTime == other.dateTime &&
        distanceKm.compareTo(other.distanceKm) == 0 &&
        type == other.type

    override fun hashCode(): Int = Objects.hash(dateTime, distanceKm, type)

    override fun toString(): String = "SpecialFullMoon(dateTime=$dateTime, distanceKm=$distanceKm, type=$type)"
}
