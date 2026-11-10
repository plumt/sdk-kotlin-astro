package net.plumt.astro.sample

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import net.plumt.astro.lunar.DistanceThresholdCriteria
import net.plumt.astro.lunar.LunarCalculator
import net.plumt.astro.lunar.LunarCalculatorConfig
import net.plumt.astro.lunar.model.GeoLocation
import net.plumt.astro.lunar.model.LunarPhaseType
import net.plumt.astro.lunar.model.SpecialMoonType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset

/** 공개 메서드를 버튼 하나씩으로 불러 결과를 화면에 쌓는다. 계산기는 상태가 없어 하나만 둔다. */
class MainActivity : Activity() {

    private val lunar = LunarCalculator.create()
    private lateinit var log: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log = TextView(this).apply { setText(R.string.ready) }
        val now = LocalDateTime.now(lunar.zone)
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(button(R.string.phases) {
            append("phasesOfMonth: ${lunar.phasesOfMonth(now.year, now.monthValue)}")
            append("phaseEventsInMonth(망): ${lunar.phaseEventsInMonth(now.year, now.monthValue, LunarPhaseType.FullMoon)}")
        })
        column.addView(button(R.string.phase_at) {
            append("phaseAt: ${lunar.phaseAt(now)}")
            append("lunarDayOf: ${lunar.lunarDayOf(now.toLocalDate())}")
            append("distanceKm: ${lunar.distanceKm(now)}")
        })
        column.addView(button(R.string.today) {
            val today = lunar.moonToday(now)
            append("moonToday: $today")
            today.nextFullMoon?.let { append("isBlueMoon(다음 망): ${lunar.isBlueMoon(it)}") }
        })
        column.addView(button(R.string.calendar) {
            append("monthCalendar: ${lunar.monthCalendar(now.year, now.monthValue).size}일 ${lunar.monthCalendar(now.year, now.monthValue).first()}")
        })
        column.addView(button(R.string.special) {
            append("specialFullMoons: ${lunar.specialFullMoons(now.year)}")
            append("specialMoonsOfYear: ${lunar.specialMoonsOfYear(now.year)}")
        })
        column.addView(button(R.string.rise_set) {
            append("riseSet(서울): ${lunar.riseSet(now.toLocalDate())}")
            append("riseSet(부산): ${lunar.riseSet(now.toLocalDate(), GeoLocation(35.1796, 129.0756))}")
            append("riseSetsOfMonth: ${lunar.riseSetsOfMonth(YearMonth.from(now)).size}일")
        })
        column.addView(button(R.string.solar_terms) {
            append("solarTermsOfYear: ${lunar.solarTermsOfYear(now.year).map { "${it.type.koreanName} ${it.dateTime}" }}")
        })
        column.addView(button(R.string.utc) {
            val utc = LunarCalculator.create(
                LunarCalculatorConfig.builder()
                    .zone(ZoneOffset.UTC)
                    .specialMoonCriteria(DistanceThresholdCriteria().withSupermoonMaxKm(356_500.0))
                    .build(),
            )
            append("UTC phasesOfMonth: ${utc.phasesOfMonth(now.year, now.monthValue)}")
            append("UTC specialFullMoons: ${utc.specialFullMoons(now.year)}")
            // 기준을 바꾼 효과를 한 줄로 비교한다(해마다 근지점 거리가 달라 목록만으로는 차이가 안 보일 수 있다)
            val supermoons = { c: LunarCalculator -> c.specialFullMoons(now.year).count { it.type == SpecialMoonType.Supermoon } }
            append("슈퍼문 수: 기본 ${supermoons(lunar)} · 바꾼 기준 ${supermoons(utc)}")
        })
        column.addView(button(R.string.invalid) {
            expectIllegal("year 1800") { lunar.solarTermsOfYear(1800) }
            expectIllegal("month 13") { lunar.monthCalendar(2026, 13) }
            expectIllegal("latitude 91") { lunar.riseSet(LocalDate.of(2026, 1, 1), GeoLocation(91.0, 0.0)) }
        })
        column.addView(log)
        // target 35 이상은 edge-to-edge 가 강제라 시스템 바 높이만큼 비켜 둔다
        setContentView(ScrollView(this).apply { fitsSystemWindows = true; addView(column) })
    }

    private fun expectIllegal(label: String, block: () -> Unit) {
        try {
            block()
            append("$label: 던지지 않았다(오류)")
        } catch (e: IllegalArgumentException) {
            append("$label: ${e.message}")
        }
    }

    private fun append(line: String) {
        log.append("\n" + line)
    }

    private fun button(label: Int, onClick: () -> Unit) = Button(this).apply {
        setText(label)
        setOnClickListener { onClick() }
    }
}
