# sdk-kotlin-astro

네트워크 없이 천문 값을 계산하는 Kotlin 라이브러리 모음이다. 순수 JVM(jar)이라 Android·서버·데스크톱에서 같이 쓴다.

| 모듈 | 좌표 | 상태 |
|---|---|---|
| `lunar` | `net.plumt.astro:lunar` | 준비 중 (0.0.1) |

## lunar

달 위상(삭·상현·망·하현), 음력 일자, 블루문·슈퍼문·미니문, 지구-달 거리, 월출·월몰, 24절기를 계산한다.
Jean Meeus 『Astronomical Algorithms』(2nd Ed.) 기반이고, 위상 시각은 한국천문연구원(KASI) 발표값 대비 ±2~3분이다.

```kotlin
dependencies {
    implementation("net.plumt.astro:lunar:0.0.1")
}
```

```kotlin
import net.plumt.astro.lunar.LunarCalculator
import net.plumt.astro.lunar.LunarCalculatorConfig

val lunar = LunarCalculator.create()                 // 기본 시간대: 한국 표준시
lunar.phasesOfMonth(2026, 11)                        // 그 달의 위상 4종
lunar.specialMoonsOfYear(2026)                       // 슈퍼문·미니문·블루문
lunar.solarTermsOfYear(2026)                         // 24절기

val utc = LunarCalculator.create(LunarCalculatorConfig(zone = ZoneOffset.UTC))
```

- 진입점은 `LunarCalculator` 하나다. 상태가 없어 앱에 하나만 두고 공유한다
- 슈퍼문·미니문에는 공식 정의가 없어 판정 기준(`SpecialMoonCriteria`)을 바꿔 끼울 수 있다
- 연도는 1900~2100 을 받는다(정확도는 2005~2050 이 가장 좋다). 틀린 인자는 `IllegalArgumentException`, 계산 실패는 `LunarCalculationException` 이다
- 오류와 실패 신호는 [ERRORS.md](ERRORS.md), 변경 이력은 [CHANGELOG.md](CHANGELOG.md)

## 버전

모듈마다 [SemVer](https://semver.org/lang/ko/) 를 따로 쓰고 태그는 `<모듈>-v<버전>`(예: `lunar-v0.0.1`)이다.
1.0.0 전까지는 마이너 버전에서도 공개 API 가 바뀔 수 있다. 공개 API 는 `<모듈>/api/*.api` 로 고정하고 CI 가 대조한다.

## 라이선스

[Apache License 2.0](LICENSE)
