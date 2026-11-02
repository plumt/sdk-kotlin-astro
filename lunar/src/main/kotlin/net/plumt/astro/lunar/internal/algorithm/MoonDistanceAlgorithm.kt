package net.plumt.astro.lunar.internal.algorithm

import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.cos

/**
 * 특정 시각의 지구-달 중심 거리(km)를 계산하는 알고리즘.
 *
 * ## 알고리즘
 * Jean Meeus "Astronomical Algorithms" 2nd Ed. Ch.47(달의 위치)의 거리항 Σr를 사용한다.
 * 거리는 다음과 같이 구한다.
 *
 *   distance_km = 385000.56 + Σr / 1000
 *
 * 여기서 Σr는 Table 47.A의 cos 급수(거리 계수 열, 단위 0.001 km)를 합산한 값이다.
 * Table 47.A의 거리항 60항을 모두 쓰는 완전 급수다(astronomia `moonposition.js` 와 항·계수가 같다).
 *
 * ## 물리 범위
 * 근지점 ≈ 356,400~370,000 km, 원지점 ≈ 404,000~406,700 km 범위에서 변동한다.
 *
 * ## 시간 기준
 * 입력은 [zone] 의 현지 시각이며, 내부에서 UT → (옵션)TT 보정 후 JDE로 변환한다.
 */
internal class MoonDistanceAlgorithm(
    private val zone: ZoneId,
    private val applyDeltaT: Boolean = true,
) {

    /** [dateTime]([zone] 현지 시각)의 지구-달 중심 거리(km). */
    fun distanceKm(dateTime: LocalDateTime): Double {
        val deltaTSeconds = if (applyDeltaT) AstroTime.estimateDeltaT(dateTime.year) else 0.0
        val jdUt = AstroTime.utcToJd(AstroTime.zoneToUtc(dateTime, zone))
        return distanceKmFromJde(jdUt + deltaTSeconds / AstroTime.SECONDS_PER_DAY)
    }

    // Meeus Ch.47 거리항 Σr 기반 지구-달 거리(km)
    internal fun distanceKmFromJde(jde: Double): Double {
        val t = (jde - 2451545.0) / 36525.0
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t

        // 달의 평균 황경 등 기본 인수 (deg) — Meeus Eq. 47.1~47.5
        val d = normalizeDeg(297.8501921 + 445267.1114034 * t - 0.0018819 * t2 + t3 / 545868.0 - t4 / 113065000.0)
        val m = normalizeDeg(357.5291092 + 35999.0502909 * t - 0.0001536 * t2 + t3 / 24490000.0)
        val mP = normalizeDeg(134.9633964 + 477198.8675055 * t + 0.0087414 * t2 + t3 / 69699.0 - t4 / 14712000.0)
        val f = normalizeDeg(93.2720950 + 483202.0175233 * t - 0.0036539 * t2 - t3 / 3526000.0 + t4 / 863310000.0)

        // 지구 궤도 이심률 보정 인수 E (M, 2M 항에 곱함)
        val e = 1.0 - 0.002516 * t - 0.0000074 * t2
        val e2 = e * e

        val dR = toRad(d)
        val mR = toRad(m)
        val mPR = toRad(mP)
        val fR = toRad(f)

        // Table 47.A 거리 계수(cos, 단위 0.001 km) 전체 60항 완전 급수.
        // 각 항: (계수, D계수, M계수, M'계수, F계수, E지수)
        var sumR = 0.0
        for (term in DISTANCE_TERMS) {
            val arg = term.dCoef * dR + term.mCoef * mR + term.mpCoef * mPR + term.fCoef * fR
            val eFactor = when (term.eExp) {
                1 -> e
                2 -> e2
                else -> 1.0
            }
            sumR += term.coefficient * eFactor * cos(arg)
        }

        // distance_km = 385000.56 + Σr(0.001 km) / 1000
        return 385000.56 + sumR / 1000.0
    }

    private data class DistanceTerm(
        val dCoef: Int,
        val mCoef: Int,
        val mpCoef: Int,
        val fCoef: Int,
        val coefficient: Double,
        val eExp: Int,
    )

    private fun toRad(degrees: Double): Double = Math.toRadians(normalizeDeg(degrees))

    private fun normalizeDeg(degrees: Double): Double =
        ((degrees % 360.0) + 360.0) % 360.0

    private companion object {
        // Meeus Table 47.A 거리항(Σr, cos, 0.001 km) 전체 60항. E지수는 |M계수|와 같다(0→없음, 1→E, 2→E²).
        // Σr 계수가 0인 항(Meeus가 Σl만 기재한 항)도 표 순서를 맞추려고 그대로 둔다.
        // (D, M, M', F, 계수, E지수)
        val DISTANCE_TERMS = listOf(
            DistanceTerm(0, 0, 1, 0, -20905355.0, 0),
            DistanceTerm(2, 0, -1, 0, -3699111.0, 0),
            DistanceTerm(2, 0, 0, 0, -2955968.0, 0),
            DistanceTerm(0, 0, 2, 0, -569925.0, 0),
            DistanceTerm(0, 1, 0, 0, 48888.0, 1),
            DistanceTerm(0, 0, 0, 2, -3149.0, 0),
            DistanceTerm(2, 0, -2, 0, 246158.0, 0),
            DistanceTerm(2, -1, -1, 0, -152138.0, 1),
            DistanceTerm(2, 0, 1, 0, -170733.0, 0),
            DistanceTerm(2, -1, 0, 0, -204586.0, 1),
            DistanceTerm(0, 1, -1, 0, -129620.0, 1),
            DistanceTerm(1, 0, 0, 0, 108743.0, 0),
            DistanceTerm(0, 1, 1, 0, 104755.0, 1),
            DistanceTerm(2, 0, 0, -2, 10321.0, 0),
            DistanceTerm(0, 0, 1, 2, 0.0, 0),
            DistanceTerm(0, 0, 1, -2, 79661.0, 0),
            DistanceTerm(4, 0, -1, 0, -34782.0, 0),
            DistanceTerm(0, 0, 3, 0, -23210.0, 0),
            DistanceTerm(4, 0, -2, 0, -21636.0, 0),
            DistanceTerm(2, 1, -1, 0, 24208.0, 1),
            DistanceTerm(2, 1, 0, 0, 30824.0, 1),
            DistanceTerm(1, 0, -1, 0, -8379.0, 0),
            DistanceTerm(1, 1, 0, 0, -16675.0, 1),
            DistanceTerm(2, -1, 1, 0, -12831.0, 1),
            DistanceTerm(2, 0, 2, 0, -10445.0, 0),
            DistanceTerm(4, 0, 0, 0, -11650.0, 0),
            DistanceTerm(2, 0, -3, 0, 14403.0, 0),
            DistanceTerm(0, 1, -2, 0, -7003.0, 1),
            DistanceTerm(2, 0, -1, 2, 0.0, 0),
            DistanceTerm(2, -1, -2, 0, 10056.0, 1),
            DistanceTerm(1, 0, 1, 0, 6322.0, 0),
            DistanceTerm(2, -2, 0, 0, -9884.0, 2),
            DistanceTerm(0, 1, 2, 0, 5751.0, 1),
            DistanceTerm(0, 2, 0, 0, 0.0, 2),
            DistanceTerm(2, -2, -1, 0, -4950.0, 2),
            DistanceTerm(2, 0, 1, -2, 4130.0, 0),
            DistanceTerm(2, 0, 0, 2, 0.0, 0),
            DistanceTerm(4, -1, -1, 0, -3958.0, 1),
            DistanceTerm(0, 0, 2, 2, 0.0, 0),
            DistanceTerm(3, 0, -1, 0, 3258.0, 0),
            DistanceTerm(2, 1, 1, 0, 2616.0, 1),
            DistanceTerm(4, -1, -2, 0, -1897.0, 1),
            DistanceTerm(0, 2, -1, 0, -2117.0, 2),
            DistanceTerm(2, 2, -1, 0, 2354.0, 2),
            DistanceTerm(2, 1, -2, 0, 0.0, 1),
            DistanceTerm(2, -1, 0, -2, 0.0, 1),
            DistanceTerm(4, 0, 1, 0, -1423.0, 0),
            DistanceTerm(0, 0, 4, 0, -1117.0, 0),
            DistanceTerm(4, -1, 0, 0, -1571.0, 1),
            DistanceTerm(1, 0, -2, 0, -1739.0, 0),
            DistanceTerm(2, 1, 0, -2, 0.0, 1),
            DistanceTerm(0, 0, 2, -2, -4421.0, 0),
            DistanceTerm(1, 1, 1, 0, 0.0, 1),
            DistanceTerm(3, 0, -2, 0, 0.0, 0),
            DistanceTerm(4, 0, -3, 0, 0.0, 0),
            DistanceTerm(2, -1, 2, 0, 0.0, 1),
            DistanceTerm(0, 2, 1, 0, 1165.0, 2),
            DistanceTerm(1, 1, -1, 0, 0.0, 1),
            DistanceTerm(2, 0, 3, 0, 0.0, 0),
            DistanceTerm(2, 0, -1, -2, 8752.0, 0),
        )
    }
}
