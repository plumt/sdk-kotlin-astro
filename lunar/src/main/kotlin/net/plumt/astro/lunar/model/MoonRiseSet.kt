package net.plumt.astro.lunar.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Objects

/**
 * 관측 지점. 경도는 동경이 양수다.
 *
 * 위도는 -90..90, 경도는 -180..180 범위의 유한한 수여야 하고, 아니면 생성할 때 [IllegalArgumentException] 을 던진다.
 *
 * [SEOUL] 은 KASI 역서 "서울" 지점 좌표(동경 126°59′ · 북위 37°34′)다.
 *
 * @property latitudeDeg 위도(도). 북위가 양수
 * @property longitudeDeg 경도(도). 동경이 양수
 */
public class GeoLocation(
    public val latitudeDeg: Double,
    public val longitudeDeg: Double,
) {
    init {
        require(latitudeDeg.isFinite() && latitudeDeg in -90.0..90.0) { "latitudeDeg($latitudeDeg) 는 -90..90 범위여야 한다" }
        require(longitudeDeg.isFinite() && longitudeDeg in -180.0..180.0) {
            "longitudeDeg($longitudeDeg) 는 -180..180 범위여야 한다"
        }
    }

    override fun equals(other: Any?): Boolean = other is GeoLocation &&
        latitudeDeg.compareTo(other.latitudeDeg) == 0 &&
        longitudeDeg.compareTo(other.longitudeDeg) == 0

    override fun hashCode(): Int = Objects.hash(latitudeDeg, longitudeDeg)

    override fun toString(): String = "GeoLocation(latitudeDeg=$latitudeDeg, longitudeDeg=$longitudeDeg)"

    /** 미리 정한 지점. */
    public companion object {
        /** 서울(KASI 역서 지점). 월출·월몰의 기본 지점이다. */
        @JvmField
        public val SEOUL: GeoLocation = GeoLocation(latitudeDeg = 37.566666, longitudeDeg = 126.983333)
    }
}

/**
 * 하루(현지 0시~24시)의 월출·월몰 시각(현지 시각, 초 단위).
 *
 * 달은 하루에 약 50분씩 늦게 떠서, 한 달에 하루쯤은 그날 안에 뜨지 않거나 지지 않는다 — 그때 null 이다.
 * 극지방처럼 달이 하루 내내 떠 있거나 내내 지평선 아래인 날은 둘 다 null 이다.
 *
 * @property date 그날
 * @property rise 월출 시각. 그날 뜨지 않으면 null
 * @property set 월몰 시각. 그날 지지 않으면 null
 */
public class MoonRiseSet(
    public val date: LocalDate,
    public val rise: LocalDateTime?,
    public val set: LocalDateTime?,
) {
    override fun equals(other: Any?): Boolean =
        other is MoonRiseSet && date == other.date && rise == other.rise && set == other.set

    override fun hashCode(): Int = Objects.hash(date, rise, set)

    override fun toString(): String = "MoonRiseSet(date=$date, rise=$rise, set=$set)"
}
