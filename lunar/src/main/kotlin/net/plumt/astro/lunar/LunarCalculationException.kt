package net.plumt.astro.lunar

/**
 * 인자는 올바른데 계산이 끝나지 못했을 때 던진다.
 *
 * 내부 계산의 예외와 교체한 [SpecialMoonCriteria] 가 던진 예외는 이 타입으로 감싸 [cause] 에 원인을 담는다.
 * 인자가 틀린 경우는 이 타입이 아니라 [IllegalArgumentException] 이다.
 */
public class LunarCalculationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
