package net.plumt.astro.lunar.model

import java.time.LocalDateTime

/** 한 해의 특별한 달 종류. */
public enum class SpecialMoonEventType { SUPERMOON, MINIMOON, BLUE_MOON }

/**
 * 한 해의 특별한 달 하나.
 *
 * @property dateTime 보름달 시각(현지 시각)
 * @property distanceKm 지구-달 거리. 블루문은 거리로 고르지 않아 null
 */
public data class SpecialMoonEvent(
    val type: SpecialMoonEventType,
    val dateTime: LocalDateTime,
    val distanceKm: Double?,
)
