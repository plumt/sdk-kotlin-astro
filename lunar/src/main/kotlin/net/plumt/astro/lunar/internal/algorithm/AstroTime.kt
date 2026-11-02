package net.plumt.astro.lunar.internal.algorithm

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.floor
import kotlin.math.roundToLong

/** 율리우스일·ΔT·시간대 변환. 알고리즘 클래스들이 함께 쓴다. */
internal object AstroTime {

    const val SECONDS_PER_DAY = 86400.0
    const val UNIX_EPOCH_JD = 2440587.5

    /** ΔT(TT − UT, 초) 추정 — Meeus Ch.10, 2005~2050년 유효. */
    fun estimateDeltaT(year: Int): Double {
        val t = year - 2000.0
        return 62.92 + 0.32217 * t + 0.005589 * t * t
    }

    /** UTC 시각 → 율리우스일(UT). */
    fun utcToJd(utc: LocalDateTime): Double =
        UNIX_EPOCH_JD + utc.toEpochSecond(ZoneOffset.UTC) / SECONDS_PER_DAY

    /** 율리우스일 → UTC 시각. 초 미만은 버린다. */
    fun jdToUtc(jd: Double): LocalDateTime = LocalDateTime.ofEpochSecond(
        floor(epochSeconds(jd)).toLong(), 0, ZoneOffset.UTC,
    )

    /** 율리우스일 → UTC 시각. 초 단위로 반올림한다. */
    fun jdToUtcRounded(jd: Double): LocalDateTime =
        LocalDateTime.ofEpochSecond(epochSeconds(jd).roundToLong(), 0, ZoneOffset.UTC)

    // 유닉스 기준 초. 에포크 초로 바꿔 만들면 시·분·초를 따로 조립하다 24시·음수 같은 값이 생기지 않는다
    private fun epochSeconds(jd: Double): Double {
        check(jd.isFinite()) { "율리우스일이 유한하지 않다: $jd" }
        return (jd - UNIX_EPOCH_JD) * SECONDS_PER_DAY
    }

    fun utcToZone(utc: LocalDateTime, zone: ZoneId): LocalDateTime =
        utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(zone).toLocalDateTime()

    fun zoneToUtc(local: LocalDateTime, zone: ZoneId): LocalDateTime =
        local.atZone(zone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
}
