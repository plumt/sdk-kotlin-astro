package net.plumt.astro.lunar.model

import java.time.LocalDateTime
import java.util.Objects

/** 한 해의 특별한 달 종류. 닫힌 집합이다 — `when` 에 `else` 없이 세 경우를 다룬다. */
public sealed class SpecialMoonEventType {
    /** 슈퍼문([SpecialMoonType.Supermoon]). */
    public data object Supermoon : SpecialMoonEventType()

    /** 미니문([SpecialMoonType.Minimoon]). */
    public data object Minimoon : SpecialMoonEventType()

    /** 한 양력 달의 두 번째 보름달. */
    public data object BlueMoon : SpecialMoonEventType()

    /** 값 목록. */
    public companion object {
        /** 세 종류(슈퍼문 → 미니문 → 블루문 순서). */
        @JvmStatic
        public val values: List<SpecialMoonEventType> get() = listOf(Supermoon, Minimoon, BlueMoon)
    }
}

/**
 * 한 해의 특별한 달 하나.
 *
 * @property type 종류
 * @property dateTime 보름달 시각(현지 시각)
 * @property distanceKm 지구-달 거리. 블루문은 거리로 고르지 않아 null
 */
public class SpecialMoonEvent(
    public val type: SpecialMoonEventType,
    public val dateTime: LocalDateTime,
    public val distanceKm: Double?,
) {
    override fun equals(other: Any?): Boolean =
        other is SpecialMoonEvent && type == other.type && dateTime == other.dateTime && distanceKm == other.distanceKm

    override fun hashCode(): Int = Objects.hash(type, dateTime, distanceKm)

    override fun toString(): String = "SpecialMoonEvent(type=$type, dateTime=$dateTime, distanceKm=$distanceKm)"
}
