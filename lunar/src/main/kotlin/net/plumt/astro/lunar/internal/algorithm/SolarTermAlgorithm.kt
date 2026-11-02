package net.plumt.astro.lunar.internal.algorithm

import net.plumt.astro.lunar.model.SolarTerm
import net.plumt.astro.lunar.model.SolarTermType
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.sin

/**
 * 24절기(節氣) 발생 시각 계산기.
 *
 * 태양의 겉보기 황경(apparent ecliptic longitude)이 15°의 배수가 되는 순간이 각 절기다.
 * (춘분 0°, 청명 15°, ... 동지 270°, 소한 285° ...)
 *
 * ## 알고리즘
 * Jean Meeus "Astronomical Algorithms" 2nd Ed. Ch.25(태양의 위치, 저정밀도)에 더해
 * 장동(nutation)과 광행차(aberration) 보정을 적용한 겉보기 황경(λ_apparent)을 구한다.
 * 목표 황경에 대응하는 시각은, 평균 황경 진행 속도(≈0.9856°/day)로 1차 추정한 뒤
 * 뉴턴법으로 황경차가 0이 되는 율리우스력 일(JDE)을 찾는다.
 *
 * ## 정확도
 * 저정밀도 태양 위치식(±0.01° 수준) 기반이므로 절기 시각 오차는 수 분 이내다.
 * 절기 판정(날짜/계절 경계)에는 충분하다.
 *
 * ## 시간 기준
 * 내부 계산은 TT(Terrestrial Time) 기준 JDE를 사용하며, 결과 시각은 ΔT 보정 후
 * UT를 거쳐 [zone] 현지 시각으로 바꾼다. ([applyDeltaT] = false면 TT ≈ UT 근사)
 */
internal class SolarTermAlgorithm(
    private val zone: ZoneId,
    private val applyDeltaT: Boolean = true,
) {

    /**
     * 주어진 양력 연도에 발생하는 24절기 시각을 모두 계산한다.
     *
     * 절기는 양력 기준으로 한 해에 정확히 24개가 발생하며, 결과는 시각 오름차순으로 정렬된다.
     * (소한 → 대한 → 입춘 → ... → 동지 순서로 한 해를 채운다.)
     *
     * @param year 양력 연도
     * @return 해당 연도 24절기 목록(현지 시각 오름차순)
     */
    fun solarTermsOfYear(year: Int): List<SolarTerm> {
        val deltaTSeconds = if (applyDeltaT) AstroTime.estimateDeltaT(year) else 0.0

        return SolarTermType.entries
            .map { type -> SolarTerm(type, findSolarTerm(year, type, deltaTSeconds)) }
            .sortedBy { it.dateTime }
    }

    // 특정 연도의 특정 절기(목표 황경)에 대응하는 현지 시각을 구한다.
    private fun findSolarTerm(year: Int, type: SolarTermType, deltaTSeconds: Double): LocalDateTime {
        val targetLongitude = type.eclipticLongitude

        // 1차 추정: 목표 황경에 가까운 양력 날짜를 근사식으로 잡는다(Meeus Ch.27 근사).
        val jdeGuess = approximateJde(year, targetLongitude)

        // 뉴턴법으로 (겉보기황경 - 목표황경)의 360° 정규화 차이가 0이 되는 JDE를 수렴시킨다.
        var jde = jdeGuess
        var remaining = MAX_ITERATIONS
        do {
            // 태양 황경 진행 속도 ≈ 0.9856°/day → Δjde = diff / 0.9856
            val correction = normalizedDifference(apparentLongitude(jde), targetLongitude) / SUN_MEAN_DAILY_MOTION
            jde += correction
        } while (kotlin.math.abs(correction) >= CONVERGENCE_DAYS && --remaining > 0)

        return AstroTime.utcToZone(AstroTime.jdToUtc(jde - deltaTSeconds / AstroTime.SECONDS_PER_DAY), zone)
    }

    /**
     * 목표 황경에 해당하는 절기의 대략적인 JDE(TT)를 구한다.
     * Meeus "Astronomical Algorithms" Ch.27(분점·지점) 근사식을 24절기 일반으로 확장.
     *
     * 해당 절기가 **양력 [year] 연도 안에** 들어오도록, 춘분 anchor에서 황경만큼 진행시킨 뒤
     * 결과 연도가 어긋나면 ±1년(≈365.2422일)만큼 보정한다.
     * (예: 소한 285°은 춘분으로부터 약 289일 뒤 → 다음 해 1월이 되므로 1년을 뺀다)
     */
    private fun approximateJde(year: Int, targetLongitude: Double): Double {
        // 분점/지점 근사식 기준(춘분 0°, 하지 90°, 추분 180°, 동지 270°)을 기반 anchor로 사용.
        val y = (year - 2000.0) / 1000.0
        // 1996~ 기준 분점/지점 평균 JDE 근사 (Meeus Table 27.B/27.C)
        val baseEquinoxJde = 2451623.80984 + 365242.37404 * y + 0.05169 * y * y -
            0.00411 * y * y * y - 0.00057 * y * y * y * y
        // 분점(춘분)으로부터 황경이 1° 증가할 때 약 1.0146일 진행.
        val lon = ((targetLongitude % 360.0) + 360.0) % 360.0
        var jde = baseEquinoxJde + lon * DAYS_PER_DEGREE

        // 양력 연도가 목표 연도보다 크면(다음 해로 넘어감) 1년 앞으로 당긴다.
        // (소한/대한/입춘처럼 황경이 285° 이상인 절기가 1~3월에 발생하는 경우)
        while (yearOfJde(jde) > year) jde -= TROPICAL_YEAR_DAYS
        while (yearOfJde(jde) < year) jde += TROPICAL_YEAR_DAYS

        return jde
    }

    // JDE(TT 근사)의 양력 연도만 빠르게 추출(근사 추정용)
    private fun yearOfJde(jde: Double): Int = AstroTime.jdToUtc(jde).year

    /**
     * 태양의 겉보기 황경(겉보기 황도경도, deg)을 계산한다. (Meeus Ch.25 저정밀도식 + 보정)
     */
    internal fun apparentLongitude(jde: Double): Double {
        val t = (jde - 2451545.0) / 36525.0 // J2000.0 기준 율리우스 세기(TT)

        // 태양의 기하 평균 황경 L0 (deg)
        val l0 = normalizeDeg(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        // 태양의 평균 근점 이각 M (deg)
        val m = normalizeDeg(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
        val mRad = toRad(m)

        // 중심차 C (deg)
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(mRad) +
            (0.019993 - 0.000101 * t) * sin(2 * mRad) +
            0.000289 * sin(3 * mRad)

        // 태양의 진황경(true longitude)
        val trueLongitude = l0 + c

        // 겉보기 황경 보정: 장동·광행차 (Meeus Eq. 25.x)
        val omega = normalizeDeg(125.04 - 1934.136 * t)
        val apparent = trueLongitude - 0.00569 - 0.00478 * sin(toRad(omega))

        return normalizeDeg(apparent)
    }

    // (실측 황경 - 목표 황경)을 [-180, 180] 범위로 정규화한 차이(deg).
    // 0에 수렴시키면 황경이 목표각이 되는 순간.
    private fun normalizedDifference(actual: Double, target: Double): Double {
        var diff = target - actual
        diff = ((diff + 180.0) % 360.0 + 360.0) % 360.0 - 180.0
        return diff
    }

    // degrees → radians (음수 안전 정규화 포함)
    private fun toRad(degrees: Double): Double =
        Math.toRadians(normalizeDeg(degrees))

    private fun normalizeDeg(degrees: Double): Double =
        ((degrees % 360.0) + 360.0) % 360.0

    private companion object {
        // 태양 황경의 하루당 평균 진행 속도(deg/day) ≈ 360 / 365.2422
        const val SUN_MEAN_DAILY_MOTION = 0.98564736
        // 황경 1°당 일 수 ≈ 365.2422 / 360
        const val DAYS_PER_DEGREE = 1.01456152
        // 1 회귀년(tropical year) 일 수
        const val TROPICAL_YEAR_DAYS = 365.2422
        const val MAX_ITERATIONS = 50
        // 수렴 임계값(일): 1e-6일 ≈ 0.09초
        const val CONVERGENCE_DAYS = 1e-6
    }
}
