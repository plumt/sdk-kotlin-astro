package net.plumt.astro.lunar

import net.plumt.astro.lunar.model.SpecialMoonType
import java.time.LocalDateTime

/**
 * 보름달 하나를 슈퍼문·미니문·일반으로 가르는 기준.
 *
 * 슈퍼문·미니문에는 천문학의 공식 정의가 없다. 매체마다 고정 거리, 근지점 대비 비율 등 서로 다른 기준을 쓰므로
 * 기준을 바꿔 끼울 수 있게 둔다. 기본 구현은 [DistanceThresholdCriteria] 다.
 */
public fun interface SpecialMoonCriteria {
    /** 슈퍼문·미니문이면 그 종류, 일반 보름달이면 null. */
    public fun classify(fullMoon: FullMoonSample): SpecialMoonType?
}

/**
 * 판정에 쓰는 보름달 정보.
 *
 * @property dateTime 보름달(망) 시각(현지 시각)
 * @property distanceKm 그 시각의 지구-달 중심 거리(km)
 */
public data class FullMoonSample(
    val dateTime: LocalDateTime,
    val distanceKm: Double,
)

/**
 * 망 시점 지구-달 거리를 고정 임계값과 비교한다.
 *
 * 기본값은 매체에서 흔히 인용하는 수치다 — 슈퍼문 360,000 km 이하, 미니문 405,000 km 이상.
 */
public class DistanceThresholdCriteria(
    private val supermoonMaxKm: Double = DEFAULT_SUPERMOON_MAX_KM,
    private val minimoonMinKm: Double = DEFAULT_MINIMOON_MIN_KM,
) : SpecialMoonCriteria {

    init {
        require(supermoonMaxKm < minimoonMinKm) { "supermoonMaxKm($supermoonMaxKm) 는 minimoonMinKm($minimoonMinKm) 보다 작아야 한다" }
    }

    override fun classify(fullMoon: FullMoonSample): SpecialMoonType? = when {
        fullMoon.distanceKm <= supermoonMaxKm -> SpecialMoonType.SUPERMOON
        fullMoon.distanceKm >= minimoonMinKm -> SpecialMoonType.MINIMOON
        else -> null
    }

    public companion object {
        public const val DEFAULT_SUPERMOON_MAX_KM: Double = 360_000.0
        public const val DEFAULT_MINIMOON_MIN_KM: Double = 405_000.0
    }
}
