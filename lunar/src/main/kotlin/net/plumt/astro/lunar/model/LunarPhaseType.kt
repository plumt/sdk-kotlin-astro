package net.plumt.astro.lunar.model

/**
 * 달의 위상 4종.
 *
 * @property koreanName 한자를 병기한 한국어 이름
 */
public enum class LunarPhaseType(
    internal val kFraction: Double,
    public val koreanName: String,
) {
    NEW_MOON(0.0, "삭(朔)"),
    FIRST_QUARTER(0.25, "상현(上弦)"),
    FULL_MOON(0.5, "망(望)"),
    LAST_QUARTER(0.75, "하현(下弦)"),
}
