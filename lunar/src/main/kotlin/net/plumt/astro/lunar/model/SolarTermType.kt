package net.plumt.astro.lunar.model

/**
 * 24절기(節氣) 종류.
 *
 * 태양의 겉보기 황경(apparent ecliptic longitude)이 15°의 배수가 되는 순간이 각 절기다.
 * 춘분(春分)을 황경 0°로 두며, 청명 15°, ... 동지 270°, 소한 285° 순으로 15°씩 증가한다.
 *
 * 아래 enum은 양력 한 해 안에서 발생하는 순서(소한 → 동지)로 나열했다.
 * 따라서 [eclipticLongitude]는 0°에서 시작해 증가하다가 동지(270°) 이후
 * 소한(285°)·대한(300°)으로 360°에 가까운 값을 가진다.
 *
 * @property eclipticLongitude 해당 절기에 대응하는 태양 황경(도, deg)
 * @property koreanName 절기의 한국어 이름
 */
public enum class SolarTermType(
    public val eclipticLongitude: Double,
    public val koreanName: String,
) {
    MINOR_COLD(285.0, "소한(小寒)"),
    MAJOR_COLD(300.0, "대한(大寒)"),
    START_OF_SPRING(315.0, "입춘(立春)"),
    RAIN_WATER(330.0, "우수(雨水)"),
    AWAKENING_OF_INSECTS(345.0, "경칩(驚蟄)"),
    SPRING_EQUINOX(0.0, "춘분(春分)"),
    PURE_BRIGHTNESS(15.0, "청명(淸明)"),
    GRAIN_RAIN(30.0, "곡우(穀雨)"),
    START_OF_SUMMER(45.0, "입하(立夏)"),
    GRAIN_FULL(60.0, "소만(小滿)"),
    GRAIN_IN_EAR(75.0, "망종(芒種)"),
    SUMMER_SOLSTICE(90.0, "하지(夏至)"),
    MINOR_HEAT(105.0, "소서(小暑)"),
    MAJOR_HEAT(120.0, "대서(大暑)"),
    START_OF_AUTUMN(135.0, "입추(立秋)"),
    LIMIT_OF_HEAT(150.0, "처서(處暑)"),
    WHITE_DEW(165.0, "백로(白露)"),
    AUTUMN_EQUINOX(180.0, "추분(秋分)"),
    COLD_DEW(195.0, "한로(寒露)"),
    FROST_DESCENT(210.0, "상강(霜降)"),
    START_OF_WINTER(225.0, "입동(立冬)"),
    MINOR_SNOW(240.0, "소설(小雪)"),
    MAJOR_SNOW(255.0, "대설(大雪)"),
    WINTER_SOLSTICE(270.0, "동지(冬至)"),
}
