package net.plumt.astro.lunar.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 관측 지점. 경도는 동경이 양수다.
 *
 * [SEOUL] 은 KASI 역서 "서울" 지점 좌표(동경 126°59′ · 북위 37°34′)다.
 */
public data class GeoLocation(
    val latitudeDeg: Double,
    val longitudeDeg: Double,
) {
    public companion object {
        public val SEOUL: GeoLocation = GeoLocation(latitudeDeg = 37.566666, longitudeDeg = 126.983333)
    }
}

/**
 * 하루(현지 0시~24시)의 월출·월몰 시각(현지 시각, 초 단위).
 *
 * 달은 하루에 약 50분씩 늦게 떠서, 한 달에 하루쯤은 그날 안에 뜨지 않거나 지지 않는다 — 그때 null 이다.
 */
public data class MoonRiseSet(
    val date: LocalDate,
    val rise: LocalDateTime?,
    val set: LocalDateTime?,
)
