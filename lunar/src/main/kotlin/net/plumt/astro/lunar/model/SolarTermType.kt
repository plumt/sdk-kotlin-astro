package net.plumt.astro.lunar.model

import java.util.Collections

/**
 * 24절기(節氣) 종류. 같은 절기는 언제나 같은 인스턴스라 `==`·`===` 로 비교한다.
 * 열거형이 아니다 — 앱의 `when` 에는 `else` 를 둔다.
 *
 * 태양의 겉보기 황경(apparent ecliptic longitude)이 15°의 배수가 되는 순간이 각 절기다.
 * 춘분(春分)을 황경 0°로 두며, 청명 15°, ... 동지 270°, 소한 285° 순으로 15°씩 증가한다.
 * [values] 는 양력 한 해 안에서 드는 순서(소한 → 동지)다. 그래서 [eclipticLongitude] 는 285° 에서 시작해
 * 345° 다음 0° 로 돌아간다.
 *
 * @property name 상수 이름. 로그·저장에 쓴다
 * @property eclipticLongitude 해당 절기에 대응하는 태양 황경(도, deg)
 * @property koreanName 절기의 한국어 이름
 */
public class SolarTermType private constructor(
    public val name: String,
    public val eclipticLongitude: Double,
    public val koreanName: String,
) {
    override fun toString(): String = name

    /** 정의된 값과 목록. */
    public companion object {
        private val all = ArrayList<SolarTermType>()
        private fun of(name: String, longitude: Double, korean: String) =
            SolarTermType(name, longitude, korean).also { all += it }

        /** 소한(小寒), 황경 285°. */
        @JvmField public val MINOR_COLD: SolarTermType = of("MINOR_COLD", 285.0, "소한(小寒)")

        /** 대한(大寒), 황경 300°. */
        @JvmField public val MAJOR_COLD: SolarTermType = of("MAJOR_COLD", 300.0, "대한(大寒)")

        /** 입춘(立春), 황경 315°. */
        @JvmField public val START_OF_SPRING: SolarTermType = of("START_OF_SPRING", 315.0, "입춘(立春)")

        /** 우수(雨水), 황경 330°. */
        @JvmField public val RAIN_WATER: SolarTermType = of("RAIN_WATER", 330.0, "우수(雨水)")

        /** 경칩(驚蟄), 황경 345°. */
        @JvmField public val AWAKENING_OF_INSECTS: SolarTermType = of("AWAKENING_OF_INSECTS", 345.0, "경칩(驚蟄)")

        /** 춘분(春分), 황경 0°. */
        @JvmField public val SPRING_EQUINOX: SolarTermType = of("SPRING_EQUINOX", 0.0, "춘분(春分)")

        /** 청명(淸明), 황경 15°. */
        @JvmField public val PURE_BRIGHTNESS: SolarTermType = of("PURE_BRIGHTNESS", 15.0, "청명(淸明)")

        /** 곡우(穀雨), 황경 30°. */
        @JvmField public val GRAIN_RAIN: SolarTermType = of("GRAIN_RAIN", 30.0, "곡우(穀雨)")

        /** 입하(立夏), 황경 45°. */
        @JvmField public val START_OF_SUMMER: SolarTermType = of("START_OF_SUMMER", 45.0, "입하(立夏)")

        /** 소만(小滿), 황경 60°. */
        @JvmField public val GRAIN_FULL: SolarTermType = of("GRAIN_FULL", 60.0, "소만(小滿)")

        /** 망종(芒種), 황경 75°. */
        @JvmField public val GRAIN_IN_EAR: SolarTermType = of("GRAIN_IN_EAR", 75.0, "망종(芒種)")

        /** 하지(夏至), 황경 90°. */
        @JvmField public val SUMMER_SOLSTICE: SolarTermType = of("SUMMER_SOLSTICE", 90.0, "하지(夏至)")

        /** 소서(小暑), 황경 105°. */
        @JvmField public val MINOR_HEAT: SolarTermType = of("MINOR_HEAT", 105.0, "소서(小暑)")

        /** 대서(大暑), 황경 120°. */
        @JvmField public val MAJOR_HEAT: SolarTermType = of("MAJOR_HEAT", 120.0, "대서(大暑)")

        /** 입추(立秋), 황경 135°. */
        @JvmField public val START_OF_AUTUMN: SolarTermType = of("START_OF_AUTUMN", 135.0, "입추(立秋)")

        /** 처서(處暑), 황경 150°. */
        @JvmField public val LIMIT_OF_HEAT: SolarTermType = of("LIMIT_OF_HEAT", 150.0, "처서(處暑)")

        /** 백로(白露), 황경 165°. */
        @JvmField public val WHITE_DEW: SolarTermType = of("WHITE_DEW", 165.0, "백로(白露)")

        /** 추분(秋分), 황경 180°. */
        @JvmField public val AUTUMN_EQUINOX: SolarTermType = of("AUTUMN_EQUINOX", 180.0, "추분(秋分)")

        /** 한로(寒露), 황경 195°. */
        @JvmField public val COLD_DEW: SolarTermType = of("COLD_DEW", 195.0, "한로(寒露)")

        /** 상강(霜降), 황경 210°. */
        @JvmField public val FROST_DESCENT: SolarTermType = of("FROST_DESCENT", 210.0, "상강(霜降)")

        /** 입동(立冬), 황경 225°. */
        @JvmField public val START_OF_WINTER: SolarTermType = of("START_OF_WINTER", 225.0, "입동(立冬)")

        /** 소설(小雪), 황경 240°. */
        @JvmField public val MINOR_SNOW: SolarTermType = of("MINOR_SNOW", 240.0, "소설(小雪)")

        /** 대설(大雪), 황경 255°. */
        @JvmField public val MAJOR_SNOW: SolarTermType = of("MAJOR_SNOW", 255.0, "대설(大雪)")

        /** 동지(冬至), 황경 270°. */
        @JvmField public val WINTER_SOLSTICE: SolarTermType = of("WINTER_SOLSTICE", 270.0, "동지(冬至)")

        /** 정의된 24절기(소한 → 동지 순서). */
        @JvmStatic
        public val values: List<SolarTermType> get() = Collections.unmodifiableList(ArrayList(all))
    }
}
