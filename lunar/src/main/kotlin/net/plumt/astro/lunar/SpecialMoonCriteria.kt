package net.plumt.astro.lunar

import net.plumt.astro.lunar.model.SpecialMoonType
import java.time.LocalDateTime
import java.util.Objects

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
public class FullMoonSample(
    public val dateTime: LocalDateTime,
    public val distanceKm: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is FullMoonSample && dateTime == other.dateTime && distanceKm.compareTo(other.distanceKm) == 0

    override fun hashCode(): Int = Objects.hash(dateTime, distanceKm)

    override fun toString(): String = "FullMoonSample(dateTime=$dateTime, distanceKm=$distanceKm)"
}

/**
 * 망 시점 지구-달 거리를 고정 임계값과 비교한다.
 *
 * 기본값은 매체에서 흔히 인용하는 수치다 — 슈퍼문 [DEFAULT_SUPERMOON_MAX_KM](360,000 km) 이하,
 * 미니문 [DEFAULT_MINIMOON_MIN_KM](405,000 km) 이상.
 * 두 값은 양의 유한한 수이고 슈퍼문 임계가 미니문 임계보다 작아야 하며, 아니면 [IllegalArgumentException] 을 던진다.
 *
 * @property supermoonMaxKm 이 거리(km) 이하인 보름달은 슈퍼문
 * @property minimoonMinKm 이 거리(km) 이상인 보름달은 미니문
 */
public class DistanceThresholdCriteria(
    public val supermoonMaxKm: Double,
    public val minimoonMinKm: Double,
) : SpecialMoonCriteria {

    /** 기본 임계값(360,000 km / 405,000 km)의 기준. */
    public constructor() : this(DEFAULT_SUPERMOON_MAX_KM, DEFAULT_MINIMOON_MIN_KM)

    init {
        require(supermoonMaxKm.isFinite() && supermoonMaxKm > 0.0) { "supermoonMaxKm($supermoonMaxKm) 는 양의 유한한 수여야 한다" }
        require(minimoonMinKm.isFinite() && minimoonMinKm > 0.0) { "minimoonMinKm($minimoonMinKm) 는 양의 유한한 수여야 한다" }
        require(supermoonMaxKm < minimoonMinKm) { "supermoonMaxKm($supermoonMaxKm) 는 minimoonMinKm($minimoonMinKm) 보다 작아야 한다" }
    }

    /** 슈퍼문 임계만 바꾼 사본. */
    public fun withSupermoonMaxKm(km: Double): DistanceThresholdCriteria = DistanceThresholdCriteria(km, minimoonMinKm)

    /** 미니문 임계만 바꾼 사본. */
    public fun withMinimoonMinKm(km: Double): DistanceThresholdCriteria = DistanceThresholdCriteria(supermoonMaxKm, km)

    override fun classify(fullMoon: FullMoonSample): SpecialMoonType? = when {
        fullMoon.distanceKm <= supermoonMaxKm -> SpecialMoonType.Supermoon
        fullMoon.distanceKm >= minimoonMinKm -> SpecialMoonType.Minimoon
        else -> null
    }

    override fun equals(other: Any?): Boolean = other is DistanceThresholdCriteria &&
        supermoonMaxKm.compareTo(other.supermoonMaxKm) == 0 &&
        minimoonMinKm.compareTo(other.minimoonMinKm) == 0

    override fun hashCode(): Int = Objects.hash(supermoonMaxKm, minimoonMinKm)

    override fun toString(): String = "DistanceThresholdCriteria(supermoonMaxKm=$supermoonMaxKm, minimoonMinKm=$minimoonMinKm)"

    /** 기본 임계값. */
    public companion object {
        /** 기본 슈퍼문 임계(km). */
        public const val DEFAULT_SUPERMOON_MAX_KM: Double = 360_000.0

        /** 기본 미니문 임계(km). */
        public const val DEFAULT_MINIMOON_MIN_KM: Double = 405_000.0
    }
}
