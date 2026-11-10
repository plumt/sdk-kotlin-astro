package net.plumt.astro.lunar

import java.time.ZoneId
import java.util.Objects

/**
 * [LunarCalculator] 설정. 불변이다 — [builder] 로 만들고 [toBuilder] 로 고친 사본을 만든다.
 *
 * 기본값: 시간대 [KST](Asia/Seoul), 슈퍼문·미니문 기준 [DistanceThresholdCriteria] 기본 임계값.
 *
 * ```
 * val utc = LunarCalculatorConfig.builder().zone(ZoneOffset.UTC).build()
 * ```
 *
 * @property zone 입출력 시각의 시간대
 * @property specialMoonCriteria 슈퍼문·미니문 판정 기준
 */
public class LunarCalculatorConfig private constructor(builder: Builder) {
    public val zone: ZoneId = builder.zone
    public val specialMoonCriteria: SpecialMoonCriteria = builder.criteria

    /** 이 값에서 시작하는 빌더. */
    public fun toBuilder(): Builder = Builder().zone(zone).specialMoonCriteria(specialMoonCriteria)

    override fun equals(other: Any?): Boolean =
        other is LunarCalculatorConfig && zone == other.zone && specialMoonCriteria == other.specialMoonCriteria

    override fun hashCode(): Int = Objects.hash(zone, specialMoonCriteria)

    override fun toString(): String = "LunarCalculatorConfig(zone=$zone, specialMoonCriteria=$specialMoonCriteria)"

    /** 빌더와 기본 시간대. */
    public companion object {
        /** 한국 표준시(Asia/Seoul). 기본 시간대다. */
        @JvmField
        public val KST: ZoneId = ZoneId.of("Asia/Seoul")

        /** 기본값에서 시작하는 빌더. */
        @JvmStatic
        public fun builder(): Builder = Builder()
    }

    /** [LunarCalculatorConfig] 빌더. */
    public class Builder internal constructor() {
        internal var zone: ZoneId = KST
        internal var criteria: SpecialMoonCriteria = DistanceThresholdCriteria()

        /** 입출력 시각의 시간대. 기본 [KST]. 연·월·일 경계도 이 시간대로 나눈다. */
        public fun zone(zone: ZoneId): Builder = apply { this.zone = zone }

        /** 슈퍼문·미니문 판정 기준. 기본 [DistanceThresholdCriteria] 기본 임계값. */
        public fun specialMoonCriteria(criteria: SpecialMoonCriteria): Builder = apply { this.criteria = criteria }

        /** 설정을 만든다. */
        public fun build(): LunarCalculatorConfig = LunarCalculatorConfig(this)
    }
}
