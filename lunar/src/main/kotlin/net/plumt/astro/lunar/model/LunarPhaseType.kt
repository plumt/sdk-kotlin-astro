package net.plumt.astro.lunar.model

/**
 * 달의 위상 4종. 닫힌 집합이다 — `when` 에 `else` 없이 네 경우를 다룬다.
 *
 * @property koreanName 한자를 병기한 한국어 이름
 */
public sealed class LunarPhaseType(
    internal val kFraction: Double,
    public val koreanName: String,
) {
    /** 삭(新月). 위상값 0.0. */
    public data object NewMoon : LunarPhaseType(0.0, "삭(朔)")

    /** 상현. 위상값 0.25. */
    public data object FirstQuarter : LunarPhaseType(0.25, "상현(上弦)")

    /** 망(보름). 위상값 0.5. */
    public data object FullMoon : LunarPhaseType(0.5, "망(望)")

    /** 하현. 위상값 0.75. */
    public data object LastQuarter : LunarPhaseType(0.75, "하현(下弦)")

    /** 값 목록. */
    public companion object {
        /** 네 위상(삭 → 상현 → 망 → 하현 순서). */
        @JvmStatic
        public val values: List<LunarPhaseType> get() = listOf(NewMoon, FirstQuarter, FullMoon, LastQuarter)
    }
}
