package net.plumt.astro.lunar.internal.algorithm

import net.plumt.astro.lunar.model.GeoLocation
import net.plumt.astro.lunar.model.MoonRiseSet
import java.time.LocalDate
import java.time.Duration
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * 월출·월몰 시각([zone] 현지 시각).
 *
 * ## 알고리즘
 * - 달의 지심 황경·황위·거리: Meeus Ch.47 전체 급수([MOON_TABLE_47A]·[MOON_TABLE_47B]) + 보정항
 * - 장동: Meeus Ch.22 저정밀식(Δψ 0.5″ · Δε 0.1″)
 * - 적경·적위: 진황경(λ+Δψ)과 진황도경사(ε0+Δε)로 변환 (Ch.13)
 * - 항성시: Meeus Eq.12.4 평균항성시 + Δψ·cosε (Ch.12)
 * - 출몰 기준 고도 h0 = 0.7275·π − 0°34′ (Ch.15). π 는 달의 지평시차, 34′ 는 대기차다.
 *   반지름·시차·대기차를 다 넣은 "달 윗가장자리가 지평선에 닿는 순간" 이다.
 *
 * ## 탐색
 * [zone] 기준 그날 0시부터 다음 날 0시까지 [STEP_MINUTES] 간격으로 (고도 − h0) 의 부호가 바뀌는 구간을 찾고,
 * 이분법으로 1초 안까지 좁힌다. 음→양이 월출, 양→음이 월몰이다.
 */
internal class MoonRiseSetAlgorithm(
    private val zone: ZoneId,
    private val applyDeltaT: Boolean = true,
) {

    fun riseSet(date: LocalDate, location: GeoLocation): MoonRiseSet {
        val start = date.atStartOfDay(zone)
        val startJd = AstroTime.utcToJd(AstroTime.zoneToUtc(start.toLocalDateTime(), zone))
        val deltaT = if (applyDeltaT) AstroTime.estimateDeltaT(date.year) else 0.0
        // 일광절약시간이 바뀌는 날은 하루가 23·25시간이다
        val steps = (Duration.between(start, date.plusDays(1).atStartOfDay(zone)).toMinutes() / STEP_MINUTES).toInt()

        var rise: Double? = null
        var set: Double? = null
        var prevJd = startJd
        var prev = altitudeAboveHorizon(prevJd, deltaT, location)
        for (i in 1..steps) {
            val jd = startJd + i * STEP_MINUTES / MINUTES_PER_DAY
            val now = altitudeAboveHorizon(jd, deltaT, location)
            if (prev < 0.0 && now >= 0.0 && rise == null) rise = bisect(prevJd, jd, deltaT, location, rising = true)
            if (prev >= 0.0 && now < 0.0 && set == null) set = bisect(prevJd, jd, deltaT, location, rising = false)
            prevJd = jd
            prev = now
        }
        return MoonRiseSet(date = date, rise = rise?.let(::toLocal), set = set?.let(::toLocal))
    }

    /** (지심 고도 − h0) 도. 양수면 달 윗가장자리가 지평선 위에 있다. */
    internal fun altitudeAboveHorizon(jdUt: Double, deltaTSeconds: Double, location: GeoLocation): Double {
        val jde = jdUt + deltaTSeconds / SECONDS_PER_DAY
        val t = (jde - J2000) / JULIAN_CENTURY
        val moon = moonEcliptic(t)
        val (deltaPsi, deltaEps) = nutation(t)
        val eps = Math.toRadians(meanObliquity(t) + deltaEps)
        val lambda = Math.toRadians(moon.lonDeg + deltaPsi)
        val beta = Math.toRadians(moon.latDeg)

        val ra = atan2(sin(lambda) * cos(eps) - tan(beta) * sin(eps), cos(lambda))
        val dec = asin(sin(beta) * cos(eps) + cos(beta) * sin(eps) * sin(lambda))

        val theta = apparentSiderealDeg(jdUt, deltaPsi, eps)
        val hourAngle = Math.toRadians(theta + location.longitudeDeg) - ra
        val phi = Math.toRadians(location.latitudeDeg)
        val altitude = Math.toDegrees(asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(hourAngle)))

        val parallaxDeg = Math.toDegrees(asin(EARTH_RADIUS_KM / moon.distanceKm))
        val h0 = PARALLAX_FACTOR * parallaxDeg - REFRACTION_DEG
        return altitude - h0
    }

    private fun bisect(lo0: Double, hi0: Double, deltaT: Double, location: GeoLocation, rising: Boolean): Double {
        var lo = lo0
        var hi = hi0
        repeat(BISECTION_ITERATIONS) {
            val mid = (lo + hi) / 2.0
            val above = altitudeAboveHorizon(mid, deltaT, location) >= 0.0
            if (above == rising) hi = mid else lo = mid
        }
        return (lo + hi) / 2.0
    }

    private data class Ecliptic(val lonDeg: Double, val latDeg: Double, val distanceKm: Double)

    /** Meeus Ch.47. 평균 춘분점 기준 지심 황경·황위(도)와 거리(km). */
    private fun moonEcliptic(t: Double): Ecliptic {
        val lp = poly(t, 218.3164477, 481267.88123421, -0.0015786, 1.0 / 538841.0, -1.0 / 65194000.0)
        val d = poly(t, 297.8501921, 445267.1114034, -0.0018819, 1.0 / 545868.0, -1.0 / 113065000.0)
        val m = poly(t, 357.5291092, 35999.0502909, -0.0001536, 1.0 / 24490000.0, 0.0)
        val mp = poly(t, 134.9633964, 477198.8675055, 0.0087414, 1.0 / 69699.0, -1.0 / 14712000.0)
        val f = poly(t, 93.2720950, 483202.0175233, -0.0036539, -1.0 / 3526000.0, 1.0 / 863310000.0)
        val a1 = 119.75 + 131.849 * t
        val a2 = 53.09 + 479264.29 * t
        val a3 = 313.45 + 481266.484 * t
        val e = 1.0 - 0.002516 * t - 0.0000074 * t * t

        var sumL = 3958.0 * sinDeg(a1) + 1962.0 * sinDeg(lp - f) + 318.0 * sinDeg(a2)
        var sumR = 0.0
        var sumB = -2235.0 * sinDeg(lp) + 382.0 * sinDeg(a3) + 175.0 * sinDeg(a1 - f) +
            175.0 * sinDeg(a1 + f) + 127.0 * sinDeg(lp - mp) - 115.0 * sinDeg(lp + mp)
        for (row in MOON_TABLE_47A) {
            val arg = row[0] * d + row[1] * m + row[2] * mp + row[3] * f
            val ef = eccentricityFactor(row[1], e)
            sumL += row[4] * ef * sinDeg(arg)
            sumR += row[5] * ef * cosDeg(arg)
        }
        for (row in MOON_TABLE_47B) {
            val arg = row[0] * d + row[1] * m + row[2] * mp + row[3] * f
            sumB += row[4] * eccentricityFactor(row[1], e) * sinDeg(arg)
        }
        return Ecliptic(
            lonDeg = lp + sumL / 1_000_000.0,
            latDeg = sumB / 1_000_000.0,
            distanceKm = 385000.56 + sumR / 1000.0,
        )
    }

    /** 태양 평균근점이각 M 의 계수가 ±1 이면 E, ±2 면 E² 를 곱한다. */
    private fun eccentricityFactor(mCoef: Int, e: Double): Double = when (kotlin.math.abs(mCoef)) {
        1 -> e
        2 -> e * e
        else -> 1.0
    }

    /** Meeus Ch.22 저정밀 장동(도). (Δψ, Δε) */
    private fun nutation(t: Double): Pair<Double, Double> {
        val omega = 125.04452 - 1934.136261 * t
        val l = 280.4665 + 36000.7698 * t
        val lp = 218.3165 + 481267.8813 * t
        val dPsi = -17.20 * sinDeg(omega) - 1.32 * sinDeg(2 * l) - 0.23 * sinDeg(2 * lp) + 0.21 * sinDeg(2 * omega)
        val dEps = 9.20 * cosDeg(omega) + 0.57 * cosDeg(2 * l) + 0.10 * cosDeg(2 * lp) - 0.09 * cosDeg(2 * omega)
        return dPsi / ARCSEC_PER_DEG to dEps / ARCSEC_PER_DEG
    }

    /** Meeus Eq.22.2 평균 황도경사(도). */
    private fun meanObliquity(t: Double): Double =
        23.0 + 26.0 / 60.0 + 21.448 / ARCSEC_PER_DEG +
            (-46.8150 * t - 0.00059 * t * t + 0.001813 * t * t * t) / ARCSEC_PER_DEG

    /** Meeus Eq.12.4 그리니치 평균항성시 + 분점차(Δψ·cosε). 도. */
    private fun apparentSiderealDeg(jdUt: Double, deltaPsiDeg: Double, epsRad: Double): Double {
        val t = (jdUt - J2000) / JULIAN_CENTURY
        val mean = 280.46061837 + 360.98564736629 * (jdUt - J2000) + 0.000387933 * t * t - t * t * t / 38710000.0
        return mean + deltaPsiDeg * cos(epsRad)
    }

    private fun toLocal(jdUt: Double): LocalDateTime = AstroTime.utcToZone(AstroTime.jdToUtcRounded(jdUt), zone)

    private fun poly(t: Double, c0: Double, c1: Double, c2: Double, c3: Double, c4: Double): Double =
        c0 + t * (c1 + t * (c2 + t * (c3 + t * c4)))

    private fun sinDeg(deg: Double): Double = sin(Math.toRadians(deg % 360.0))

    private fun cosDeg(deg: Double): Double = cos(Math.toRadians(deg % 360.0))

    private companion object {
        const val STEP_MINUTES = 10.0
        const val MINUTES_PER_DAY = 1440.0
        const val BISECTION_ITERATIONS = 20
        const val SECONDS_PER_DAY = 86400.0
        const val J2000 = 2451545.0
        const val JULIAN_CENTURY = 36525.0
        const val ARCSEC_PER_DEG = 3600.0
        const val EARTH_RADIUS_KM = 6378.14

        /** Meeus Ch.15: h0 = 0.7275π − 0°34′ */
        const val PARALLAX_FACTOR = 0.7275
        const val REFRACTION_DEG = 34.0 / 60.0
    }
}

/** 그 달 1일부터 말일까지 날짜별 월출·월몰(인덱스 0 이 1일). */
internal fun MoonRiseSetAlgorithm.riseSetsForMonth(yearMonth: YearMonth, location: GeoLocation): List<MoonRiseSet> =
    (1..yearMonth.lengthOfMonth()).map { day -> riseSet(yearMonth.atDay(day), location) }
