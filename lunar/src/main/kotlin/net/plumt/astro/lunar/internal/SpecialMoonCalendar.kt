package net.plumt.astro.lunar.internal

import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.model.SpecialFullMoon
import net.plumt.astro.lunar.model.SpecialMoonEvent
import net.plumt.astro.lunar.model.SpecialMoonEventType
import net.plumt.astro.lunar.model.SpecialMoonType

/** 한 해의 슈퍼문·미니문·블루문을 시간순으로 모은다. */
internal object SpecialMoonCalendar {

    /**
     * @param specialFullMoons 그해 슈퍼문·미니문 — [SupermoonDetector.classify] 결과
     * @return 시각 오름차순. 같은 시각이면 슈퍼문·미니문이 블루문보다 앞선다
     */
    fun ofYear(year: Int, specialFullMoons: List<SpecialFullMoon>, algorithm: MeeusChapter49Algorithm): List<SpecialMoonEvent> {
        val byDistance = specialFullMoons.map { moon ->
            val type = when (moon.type) {
                SpecialMoonType.SUPERMOON -> SpecialMoonEventType.SUPERMOON
                SpecialMoonType.MINIMOON -> SpecialMoonEventType.MINIMOON
            }
            SpecialMoonEvent(type, moon.dateTime, moon.distanceKm)
        }
        val blueMoons = (1..12)
            .mapNotNull { month -> BlueMoonDetector.findBlueMoon(year, month, algorithm) }
            .map { SpecialMoonEvent(SpecialMoonEventType.BLUE_MOON, it.dateTime, distanceKm = null) }
        return (byDistance + blueMoons).sortedBy { it.dateTime }
    }
}
