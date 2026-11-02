package net.plumt.astro.lunar.internal

import net.plumt.astro.lunar.FullMoonSample
import net.plumt.astro.lunar.SpecialMoonCriteria
import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.internal.algorithm.MoonDistanceAlgorithm
import net.plumt.astro.lunar.model.LunarPhaseType
import net.plumt.astro.lunar.model.SpecialFullMoon

/** 한 해의 보름달마다 망 시점 거리를 재 [SpecialMoonCriteria] 로 슈퍼문·미니문을 가른다. */
internal object SupermoonDetector {

    /** 슈퍼문·미니문으로 분류된 보름달(시각 오름차순). 일반 보름달은 빠진다. */
    fun classify(
        year: Int,
        phaseAlgorithm: MeeusChapter49Algorithm,
        distanceAlgorithm: MoonDistanceAlgorithm,
        criteria: SpecialMoonCriteria,
    ): List<SpecialFullMoon> =
        fullMoonSamples(year, phaseAlgorithm, distanceAlgorithm).mapNotNull { sample ->
            criteria.classify(sample)?.let { SpecialFullMoon(sample.dateTime, sample.distanceKm, it) }
        }

    /** 그해 모든 보름달(블루문 포함)과 망 시점 거리. 시각 오름차순. */
    fun fullMoonSamples(
        year: Int,
        phaseAlgorithm: MeeusChapter49Algorithm,
        distanceAlgorithm: MoonDistanceAlgorithm,
    ): List<FullMoonSample> =
        (1..12)
            .flatMap { month -> phaseAlgorithm.eventsOfTypeInMonth(year, month, LunarPhaseType.FULL_MOON) }
            .distinctBy { it.dateTime }
            .sortedBy { it.dateTime }
            .map { FullMoonSample(it.dateTime, distanceAlgorithm.distanceKm(it.dateTime)) }
}
