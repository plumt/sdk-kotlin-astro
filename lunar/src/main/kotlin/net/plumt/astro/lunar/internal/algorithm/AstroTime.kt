package net.plumt.astro.lunar.internal.algorithm

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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

    /** 율리우스일 → UTC 시각. Meeus Ch.7 역변환이며 초 미만은 버린다. */
    fun jdToUtc(jd: Double): LocalDateTime {
        val jdp = jd + 0.5
        val z = jdp.toLong()
        val f = jdp - z

        val a = if (z < 2299161L) z else {
            val alpha = ((z - 1867216.25) / 36524.25).toLong()
            z + 1L + alpha - alpha / 4L
        }
        val b = a + 1524L
        val c = ((b - 122.1) / 365.25).toLong()
        val d = (365.25 * c).toLong()
        val e = ((b - d) / 30.6001).toLong()

        val day = (b - d - (30.6001 * e).toLong()).toInt()
        val month = if (e < 14L) (e - 1).toInt() else (e - 13).toInt()
        val year = if (month > 2) (c - 4716).toInt() else (c - 4715).toInt()

        val totalSeconds = (f * SECONDS_PER_DAY).toLong()
        val hour = (totalSeconds / 3600).toInt()
        val minute = ((totalSeconds % 3600) / 60).toInt()
        val second = (totalSeconds % 60).toInt()

        return LocalDateTime.of(year, month, day, hour, minute, second)
    }

    /** 율리우스일 → UTC 시각. 초 단위로 반올림한다. */
    fun jdToUtcRounded(jd: Double): LocalDateTime =
        LocalDateTime.ofEpochSecond(((jd - UNIX_EPOCH_JD) * SECONDS_PER_DAY).roundToLong(), 0, ZoneOffset.UTC)

    fun utcToZone(utc: LocalDateTime, zone: ZoneId): LocalDateTime =
        utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(zone).toLocalDateTime()

    fun zoneToUtc(local: LocalDateTime, zone: ZoneId): LocalDateTime =
        local.atZone(zone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
}
