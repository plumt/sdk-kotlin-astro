package net.plumt.astro.lunar.internal.algorithm

import net.plumt.astro.lunar.model.LunarPhaseEvent
import net.plumt.astro.lunar.model.LunarPhaseType
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.cos
import kotlin.math.sin

/**
 * Jean Meeus "Astronomical Algorithms" 2nd Ed., Chapter 49 구현.
 *
 * ## 정확도
 * KASI(한국천문연구원) 기준 ±2~3분. 25개 주요 보정항만 쓰는 근사 급수라 ±2분 아래로는 내려가지 않는다.
 *
 * ## ΔT (TT → UT 보정)
 * Meeus Ch.49는 TT(Terrestrial Time) 기준 JDE를 낸다. 2026년 ΔT ≈ 75초라 보정하지 않으면
 * UT 기준 KASI 값과 1~2분 더 어긋날 수 있다. [applyDeltaT] 로 보정을 켠다.
 *
 * 결과 시각은 [zone] 의 현지 시각이고, 연·월 판정도 그 시간대 기준이다.
 */
internal class MeeusChapter49Algorithm(
    private val zone: ZoneId,
    private val applyDeltaT: Boolean = false,
) {

    fun calculate(year: Int, month: Int): List<LunarPhaseEvent> {
        val decimalYear = year + (month - 0.5) / 12.0
        val kApprox = (decimalYear - 2000.0) * 12.3685

        val candidates = mutableListOf<LunarPhaseEvent>()

        for (kBase in (kApprox.toInt() - 1)..(kApprox.toInt() + 1)) {
            for (phaseType in LunarPhaseType.values) {
                val k = kBase.toDouble() + phaseType.kFraction
                val jde = computeJde(k, phaseType)
                val local = jdeToLocal(jde, if (applyDeltaT) AstroTime.estimateDeltaT(year) else 0.0)
                if (local.year == year && local.monthValue == month) {
                    candidates.add(LunarPhaseEvent(phaseType, local))
                }
            }
        }

        // 같은 위상이 둘 이상이면 뒤의 것만 남긴다 — 전월 이벤트가 시간대 보정으로 이번 달에 넘어온 경우다
        return candidates
            .groupBy { it.type }
            .mapValues { (_, events) -> events.maxByOrNull { it.dateTime }!! }
            .values
            .sortedBy { it.dateTime }
    }

    /**
     * 주어진 양력 연·월에 발생하는 [phaseType] 위상 이벤트를 **모두** 반환한다.
     *
     * [calculate]는 위상 타입별로 한 달에 1개만 남기는 정리를 거치기 때문에,
     * 한 달에 보름달이 두 번 뜨는 블루문 같은 경우를 놓친다.
     * 이 함수는 k값 탐색 범위를 충분히 넓혀(±2 주기) 같은 달 안의 동일 위상 중복을
     * 그대로 보존한 뒤, 시각 기준으로 정렬해 반환한다.
     *
     * @param year 양력 연도
     * @param month 양력 월(1~12)
     * @param phaseType 수집할 위상 종류
     * @return 해당 연·월에 속하는 [phaseType] 이벤트 목록(시간 오름차순, 중복 시 모두 포함)
     */
    fun eventsOfTypeInMonth(year: Int, month: Int, phaseType: LunarPhaseType): List<LunarPhaseEvent> {
        val decimalYear = year + (month - 0.5) / 12.0
        val kApprox = (decimalYear - 2000.0) * 12.3685

        val deltaT = if (applyDeltaT) AstroTime.estimateDeltaT(year) else 0.0
        val results = mutableListOf<LunarPhaseEvent>()

        // 한 달에 동일 위상이 두 번 들 수 있으므로 ±2 주기까지 넓게 탐색
        for (kBase in (kApprox.toInt() - 2)..(kApprox.toInt() + 2)) {
            val k = kBase.toDouble() + phaseType.kFraction
            val jde = computeJde(k, phaseType)
            val local = jdeToLocal(jde, deltaT)
            if (local.year == year && local.monthValue == month) {
                results.add(LunarPhaseEvent(phaseType, local))
            }
        }

        return results
            .distinctBy { it.dateTime }
            .sortedBy { it.dateTime }
    }

    // JDE 계산: Meeus Table 49.a (삭/망) / 49.b (상현/하현)
    internal fun computeJde(k: Double, phaseType: LunarPhaseType): Double {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t

        // 평균 JDE (Eq. 49.1)
        var jde = 2451550.09766 +
                29.530588861 * k +
                0.00015437 * t2 -
                0.000000150 * t3 +
                0.00000000073 * t4

        val e = 1.0 - 0.002516 * t - 0.0000074 * t2

        // 각도 계산 후 radians 변환 (정규화: 음수 포함 안전한 mod)
        val m = toRad(2.5534 + 29.10535670 * k - 0.0000014 * t2 - 0.00000011 * t3)
        val mP = toRad(201.5643 + 385.81693528 * k + 0.0107582 * t2 + 0.00001238 * t3 - 0.000000058 * t4)
        val f = toRad(160.7108 + 390.67050284 * k - 0.0016118 * t2 - 0.00000227 * t3 + 0.000000011 * t4)
        val omega = toRad(124.7746 - 1.56375588 * k + 0.0020672 * t2 + 0.00000215 * t3)

        jde += when (phaseType) {
            LunarPhaseType.NewMoon -> correctionNewMoon(e, m, mP, f, omega)
            LunarPhaseType.FullMoon -> correctionFullMoon(e, m, mP, f, omega)
            LunarPhaseType.FirstQuarter -> correctionQuarter(e, m, mP, f, omega) + quarterW(e, m, mP, f)
            LunarPhaseType.LastQuarter -> correctionQuarter(e, m, mP, f, omega) - quarterW(e, m, mP, f)
        }

        jde += planetaryCorrections(k, t)

        return jde
    }

    // Meeus Table 49.a - 삭(朔) 보정
    private fun correctionNewMoon(e: Double, m: Double, mP: Double, f: Double, omega: Double): Double {
        val e2 = e * e
        return -0.40720 * sin(mP) +
                0.17241 * e * sin(m) +
                0.01608 * sin(2 * mP) +
                0.01039 * sin(2 * f) +
                0.00739 * e * sin(mP - m) +
                -0.00514 * e * sin(mP + m) +
                0.00208 * e2 * sin(2 * m) +
                -0.00111 * sin(mP - 2 * f) +
                -0.00057 * sin(mP + 2 * f) +
                0.00056 * e * sin(2 * mP + m) +
                -0.00042 * sin(3 * mP) +
                0.00042 * e * sin(m + 2 * f) +
                0.00038 * e * sin(m - 2 * f) +
                -0.00024 * e * sin(2 * mP - m) +
                -0.00017 * sin(omega) +
                -0.00007 * sin(mP + 2 * m) +
                0.00004 * sin(2 * mP - 2 * f) +
                0.00004 * sin(3 * m) +
                0.00003 * sin(mP + m - 2 * f) +
                0.00003 * sin(2 * mP + 2 * f) +
                -0.00003 * sin(mP + m + 2 * f) +
                0.00003 * sin(mP - m + 2 * f) +
                -0.00002 * sin(mP - m - 2 * f) +
                -0.00002 * sin(3 * mP + m) +
                0.00002 * sin(4 * mP)
    }

    // Meeus Table 49.a - 망(望) 보정 (계수 일부 상이)
    private fun correctionFullMoon(e: Double, m: Double, mP: Double, f: Double, omega: Double): Double {
        val e2 = e * e
        return -0.40614 * sin(mP) +
                0.17302 * e * sin(m) +
                0.01614 * sin(2 * mP) +
                0.01043 * sin(2 * f) +
                0.00734 * e * sin(mP - m) +
                -0.00515 * e * sin(mP + m) +
                0.00209 * e2 * sin(2 * m) +
                -0.00111 * sin(mP - 2 * f) +
                -0.00057 * sin(mP + 2 * f) +
                0.00056 * e * sin(2 * mP + m) +
                -0.00042 * sin(3 * mP) +
                0.00042 * e * sin(m + 2 * f) +
                0.00038 * e * sin(m - 2 * f) +
                -0.00024 * e * sin(2 * mP - m) +
                -0.00017 * sin(omega) +
                -0.00007 * sin(mP + 2 * m) +
                0.00004 * sin(2 * mP - 2 * f) +
                0.00004 * sin(3 * m) +
                0.00003 * sin(mP + m - 2 * f) +
                0.00003 * sin(2 * mP + 2 * f) +
                -0.00003 * sin(mP + m + 2 * f) +
                0.00003 * sin(mP - m + 2 * f) +
                -0.00002 * sin(mP - m - 2 * f) +
                -0.00002 * sin(3 * mP + m) +
                0.00002 * sin(4 * mP)
    }

    // Meeus Table 49.b - 상현/하현 공통 보정 (W는 별도)
    private fun correctionQuarter(e: Double, m: Double, mP: Double, f: Double, omega: Double): Double {
        val e2 = e * e
        return -0.62801 * sin(mP) +
                0.17172 * e * sin(m) +
                -0.01183 * e * sin(mP + m) +
                0.00862 * sin(2 * mP) +
                0.00804 * sin(2 * f) +
                0.00454 * e * sin(mP - m) +
                0.00204 * e2 * sin(2 * m) +
                -0.00180 * sin(mP - 2 * f) +
                -0.00070 * sin(mP + 2 * f) +
                -0.00040 * sin(3 * mP) +
                -0.00034 * e * sin(2 * mP - m) +
                0.00032 * e * sin(m + 2 * f) +
                0.00032 * e * sin(m - 2 * f) +
                -0.00028 * e2 * sin(mP + 2 * m) +
                0.00027 * e * sin(2 * mP + m) +
                -0.00017 * sin(omega) +
                -0.00005 * sin(mP - m - 2 * f) +
                0.00004 * sin(2 * mP + 2 * f) +
                -0.00004 * sin(mP + m + 2 * f) +
                0.00004 * sin(mP - 2 * m) +
                0.00003 * sin(mP + m - 2 * f) +
                0.00003 * sin(3 * m) +
                0.00002 * sin(2 * mP - 2 * f) +
                0.00002 * sin(mP - m + 2 * f) +
                -0.00002 * sin(3 * mP + m)
    }

    // W: 상현(+W) / 하현(-W) 추가 보정항
    private fun quarterW(e: Double, m: Double, mP: Double, f: Double): Double {
        return 0.00306 -
                0.00038 * e * cos(m) +
                0.00026 * cos(mP) -
                0.00002 * cos(mP - m) +
                0.00002 * cos(mP + m) +
                0.00002 * cos(2 * f)
    }

    // Meeus Table 49.e - 행성 섭동 14개 항
    private fun planetaryCorrections(k: Double, t: Double): Double {
        val t2 = t * t
        return 0.000325 * sin(toRad(299.77 + 0.107408 * k - 0.009173 * t2)) +
                0.000165 * sin(toRad(251.88 + 0.016321 * k)) +
                0.000164 * sin(toRad(251.83 + 26.651886 * k)) +
                0.000126 * sin(toRad(349.42 + 36.412478 * k)) +
                0.000110 * sin(toRad(84.66 + 18.206239 * k)) +
                0.000062 * sin(toRad(141.74 + 53.303771 * k)) +
                0.000060 * sin(toRad(207.14 + 2.453732 * k)) +
                0.000056 * sin(toRad(154.84 + 7.306860 * k)) +
                0.000047 * sin(toRad(34.52 + 27.261239 * k)) +
                0.000042 * sin(toRad(207.19 + 0.121824 * k)) +
                0.000040 * sin(toRad(291.34 + 1.844379 * k)) +
                0.000037 * sin(toRad(161.72 + 24.198154 * k)) +
                0.000035 * sin(toRad(239.56 + 25.513099 * k)) +
                0.000023 * sin(toRad(331.55 + 3.592518 * k))
    }

    // JDE(TT) → 현지 시각. deltaT 는 TT − UT(초)이고 0 이면 TT ≈ UT 로 둔다
    private fun jdeToLocal(jde: Double, deltaT: Double): LocalDateTime =
        AstroTime.utcToZone(AstroTime.jdToUtc(jde - deltaT / AstroTime.SECONDS_PER_DAY), zone)

    // degrees → radians (음수 안전한 정규화 포함)
    private fun toRad(degrees: Double): Double =
        Math.toRadians(((degrees % 360.0) + 360.0) % 360.0)
}
