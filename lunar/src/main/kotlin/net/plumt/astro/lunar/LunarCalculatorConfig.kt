package net.plumt.astro.lunar

import java.time.ZoneId

/**
 * [LunarCalculator] 설정.
 *
 * @property zone 입출력 시각의 시간대. 기본은 한국 표준시([KST])다
 * @property specialMoonCriteria 슈퍼문·미니문 판정 기준. 기본은 [DistanceThresholdCriteria]
 */
public data class LunarCalculatorConfig(
    val zone: ZoneId = KST,
    val specialMoonCriteria: SpecialMoonCriteria = DistanceThresholdCriteria(),
) {
    public companion object {
        /** 한국 표준시(Asia/Seoul). */
        public val KST: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
