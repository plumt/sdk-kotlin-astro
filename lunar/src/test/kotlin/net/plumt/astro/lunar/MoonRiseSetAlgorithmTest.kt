package net.plumt.astro.lunar

import net.plumt.astro.lunar.internal.algorithm.MoonRiseSetAlgorithm
import net.plumt.astro.lunar.model.GeoLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * KASI 역서(astro.kasi.re.kr 월별 해/달 출몰시각, 서울 · 동경 126°59′ 북위 37°34′) 공식값으로 고정한다.
 *
 * 역서는 분 단위로 반올림해 적는다. 계산값을 같은 방식으로 반올림해 분까지 같아야 한다.
 * 반올림 경계(xx분 25~35초)에 걸린 날은 표본에서 뺐다 — 몇 초 차이로 분이 갈린다.
 */
class MoonRiseSetAlgorithmTest {

    private val algorithm = MoonRiseSetAlgorithm(LunarCalculatorConfig.KST)

    private val kasiSeoul = listOf(
        Triple("2024-01-15", "10:29", "22:04"),
        Triple("2024-04-01", "01:02", "10:06"),
        Triple("2024-07-10", "09:13", "22:42"),
        Triple("2024-10-20", "19:31", "09:59"),
        Triple("2025-02-14", "19:51", "08:15"),
        Triple("2025-08-15", "22:32", "12:12"),
        Triple("2025-10-06", "17:32", "05:07"),
        Triple("2025-12-25", "11:07", "22:18"),
        Triple("2026-03-03", "18:18", "06:48"),
        Triple("2026-09-25", "17:34", "04:33"),
    )

    @Test
    fun `서울 월출·월몰은 KASI 역서와 분 단위로 같다`() {
        kasiSeoul.forEach { (date, rise, set) ->
            val result = algorithm.riseSet(LocalDate.parse(date), GeoLocation.SEOUL)
            assertEquals("$date 월출", rise, result.rise?.let(::roundToMinute))
            assertEquals("$date 월몰", set, result.set?.let(::roundToMinute))
        }
    }

    @Test
    fun `그날 지지 않는 달은 월몰이 null 이다`() {
        // KASI 역서 서울 2026-06-21: 월출 11:50 · 월몰 ----
        val result = algorithm.riseSet(LocalDate.of(2026, 6, 21), GeoLocation.SEOUL)

        assertEquals("11:50", result.rise?.let(::roundToMinute))
        assertNull(result.set)
    }

    @Test
    fun `결과 시각은 요청한 날짜 안에 있다`() {
        val date = LocalDate.of(2025, 10, 2)
        val result = algorithm.riseSet(date, GeoLocation.SEOUL)

        listOfNotNull(result.rise, result.set).forEach { assertEquals(date, it.toLocalDate()) }
    }

    private fun roundToMinute(at: LocalDateTime): String =
        at.plusSeconds(30).toLocalTime().withSecond(0).withNano(0).let { t: LocalTime -> "%02d:%02d".format(t.hour, t.minute) }
}
