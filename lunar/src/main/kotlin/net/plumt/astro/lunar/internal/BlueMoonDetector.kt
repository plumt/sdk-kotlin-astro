package net.plumt.astro.lunar.internal

import net.plumt.astro.lunar.internal.algorithm.MeeusChapter49Algorithm
import net.plumt.astro.lunar.model.LunarPhaseEvent
import net.plumt.astro.lunar.model.LunarPhaseType

/**
 * 블루문 판별.
 *
 * 한 양력 달 안에 보름달이 두 번 뜨면 그중 두 번째 보름달을 블루문이라고 부른다.
 * (예: 2023년 8월은 8월 1일과 8월 31일에 보름달이 떠서 8월 31일이 블루문.)
 */
internal object BlueMoonDetector {

    /** [event] 가 그 달의 블루문(두 번째 보름달)이면 true. 보름달이 아니면 false. */
    fun isBlueMoon(event: LunarPhaseEvent, algorithm: MeeusChapter49Algorithm): Boolean {
        if (event.type != LunarPhaseType.FullMoon) return false
        return findBlueMoon(event.year, event.month, algorithm)?.dateTime == event.dateTime
    }

    /**
     * 그 달이 블루문 달이면 두 번째 보름달, 아니면 null.
     *
     * 같은 위상이 한 달에 두 번 드는 경우를 모두 모아야 해서 [MeeusChapter49Algorithm.eventsOfTypeInMonth] 를 쓴다.
     */
    fun findBlueMoon(year: Int, month: Int, algorithm: MeeusChapter49Algorithm): LunarPhaseEvent? {
        val fullMoons = algorithm.eventsOfTypeInMonth(year, month, LunarPhaseType.FullMoon)
        return if (fullMoons.size >= 2) fullMoons.last() else null
    }
}
