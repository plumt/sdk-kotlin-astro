# sdk-kotlin-astro

네트워크 없이 천문 값을 계산하는 Kotlin 라이브러리 모음이다. 순수 JVM(jar)이라 Android·서버·데스크톱에서 같이 쓴다.

| 모듈 | 좌표 | 상태 |
|---|---|---|
| `lunar` | `net.plumt.astro:lunar` | [![Maven Central](https://img.shields.io/maven-central/v/net.plumt.astro/lunar)](https://central.sonatype.com/artifact/net.plumt.astro/lunar) |

## lunar

달 위상(삭·상현·망·하현), 음력 일자, 블루문·슈퍼문·미니문, 지구-달 거리, 월출·월몰, 24절기를 계산한다.
Jean Meeus 『Astronomical Algorithms』(2nd Ed.) 기반이고, 위상 시각은 한국천문연구원(KASI) 발표값 대비 ±2~3분이다.

```kotlin
dependencies {
    implementation("net.plumt.astro:lunar:0.1.0")
}
```

```kotlin
import net.plumt.astro.lunar.LunarCalculator
import net.plumt.astro.lunar.LunarCalculatorConfig
import net.plumt.astro.lunar.model.LunarPhaseType

val lunar = LunarCalculator.create()                 // 기본 시간대: 한국 표준시
lunar.phasesOfMonth(2026, 11)                        // 그 달의 위상(삭·상현·망·하현)
lunar.specialMoonsOfYear(2026)                       // 슈퍼문·미니문·블루문
lunar.solarTermsOfYear(2026)                         // 24절기
lunar.riseSet(LocalDate.of(2026, 11, 10))            // 서울 월출·월몰

val utc = LunarCalculator.create(LunarCalculatorConfig.builder().zone(ZoneOffset.UTC).build())

val label = when (lunar.moonToday(LocalDateTime.now()).todayEventType) {   // 위상은 닫힌 집합 — else 가 필요 없다
    LunarPhaseType.NewMoon -> "삭"
    LunarPhaseType.FirstQuarter -> "상현"
    LunarPhaseType.FullMoon -> "보름"
    LunarPhaseType.LastQuarter -> "하현"
    null -> ""
}
```

- 진입점은 `LunarCalculator` 하나다(`create()`·`create(config)`). 상태가 없어 앱에 하나만 두고 공유한다
- 설정 `LunarCalculatorConfig` 는 불변이다 — `builder()` 로 만들고 `toBuilder()` 로 고친 사본을 만든다
- 함수는 모두 바로 끝나는 순수 계산이라 일반(동기) 함수다. 네트워크·저장소를 쓰지 않는다
- 슈퍼문·미니문에는 공식 정의가 없어 판정 기준(`SpecialMoonCriteria`)을 바꿔 끼울 수 있다. 기본은 `DistanceThresholdCriteria`
- 연도는 1900~2100 을 받는다(정확도는 2005~2050 이 가장 좋다). 틀린 인자는 `IllegalArgumentException`, 계산 실패는 `LunarCalculationException` 이다
- 앱 단위 테스트에는 `FakeLunarCalculator` 를 넘긴다. 값은 실제 계산과 같고, 부른 함수를 기록하며, `failWith` 로 실패를 낸다
- 오류와 실패 신호는 [ERRORS.md](ERRORS.md), 변경 이력은 [CHANGELOG.md](CHANGELOG.md)

### 0.0.1 에서 옮기기

좌표(`net.plumt.astro:lunar`)와 패키지는 그대로다. 0.1.0 은 공개 API 를 SDK 규칙에 맞게 바로 바꿨다(사용 중단 단계 없음).

| 0.0.1 | 0.1.0 |
|---|---|
| `LunarCalculatorConfig(zone = z, specialMoonCriteria = c)` | `LunarCalculatorConfig.builder().zone(z).specialMoonCriteria(c).build()` |
| `config.copy(zone = z)` | `config.toBuilder().zone(z).build()` |
| `LunarCalculator.create(config = ...)` 기본값 인자 | `create()` · `create(config)` |
| `riseSet(date, location = SEOUL)`·`riseSetsOfMonth(ym, location = SEOUL)` | `riseSet(date)`·`riseSet(date, location)`, `riseSetsOfMonth(ym)`·`riseSetsOfMonth(ym, location)` |
| `LunarPhaseType.NEW_MOON`·`FIRST_QUARTER`·`FULL_MOON`·`LAST_QUARTER` | `LunarPhaseType.NewMoon`·`FirstQuarter`·`FullMoon`·`LastQuarter` (sealed) |
| `SpecialMoonType.SUPERMOON`·`MINIMOON` | `SpecialMoonType.Supermoon`·`Minimoon` (sealed) |
| `SpecialMoonEventType.SUPERMOON`·`MINIMOON`·`BLUE_MOON` | `SpecialMoonEventType.Supermoon`·`Minimoon`·`BlueMoon` (sealed) |
| `SolarTermType.MINOR_COLD` 등 24개 | 이름 그대로(상수 클래스). `when` 에 `else` 가 필요하다 |
| `XxxType.entries`·`values()`·`valueOf(..)`·`ordinal` | `XxxType.values`(정의 순서 목록). 이름은 `SolarTermType.name`, 위상 종류는 `toString()` |
| 모델의 `copy(...)`·구조 분해 `val (a, b) = x` | 생성자로 새로 만들고 프로퍼티로 읽는다. `DistanceThresholdCriteria` 는 `withSupermoonMaxKm`·`withMinimoonMinKm` |
| `DistanceThresholdCriteria(supermoonMaxKm = x)` | `DistanceThresholdCriteria().withSupermoonMaxKm(x)` 또는 `DistanceThresholdCriteria(x, y)` |
| `LunarCalculationException(message)` 기본값 인자 | 생성자 `(message)`·`(message, cause)` |

## API 레벨별 동작

Android 에서 쓸 때의 차이다. 계산 자체는 API 레벨과 무관하게 같다.

| API 레벨 | 동작 | 앱이 할 일 |
|---|---|---|
| 23~25 | 공개 API 가 `java.time`(`LocalDateTime`·`ZoneId` 등)을 주고받는데, 이 버전의 기기에는 `java.time` 이 없다 | 앱 모듈에서 core library desugaring 을 켠다(아래) |
| 26 이상 | 기기의 `java.time` 을 그대로 쓴다 | 없음 |

```kotlin
android {
    compileOptions { isCoreLibraryDesugaringEnabled = true }
}
dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
```

R8(난독화·축소)용 규칙은 따로 필요 없다 — 리플렉션을 쓰지 않는다.

## 알려진 한계

- minSdk 26 미만 앱은 desugaring 을 켜지 않으면 첫 호출에서 `NoClassDefFoundError`(`java.time`)로 멈춘다
- 함수는 동기 함수뿐이다(`suspend`·콜백 형태 없음). 한 해·한 달 단위 계산(`specialMoonsOfYear`·`riseSetsOfMonth` 등)은 수 ms~수십 ms 걸리므로 많이 부를 때는 메인 스레드 밖에서 부른다
- 위상·슈퍼문 종류(`LunarPhaseType`·`SpecialMoonType`·`SpecialMoonEventType`)는 sealed 라 Java 에서는 `LunarPhaseType.NewMoon.INSTANCE` 로 쓴다
- 정확도는 2005~2050 년이 가장 좋다. ΔT 추정식이 그 구간에 맞춰져 있어 멀어질수록 거리·출몰·절기 시각의 오차가 커진다
- 위상 시각은 근사 급수라 KASI 발표값 대비 ±2~3분이고, 그보다 좁혀지지 않는다
- 슈퍼문·미니문에는 공식 정의가 없다. 기본 기준(360,000 km 이하·405,000 km 이상)은 매체 목록과 다를 수 있다
- 월출·월몰은 하루를 10분 간격으로 훑어 찾는다. 10분 안에 뜨고 지는 극지방의 짧은 출몰은 놓칠 수 있다

## 버전

모듈마다 [SemVer](https://semver.org/lang/ko/) 를 따로 쓰고 태그는 `<모듈>-v<버전>`(예: `lunar-v0.1.0`)이다.
1.0.0 전까지는 마이너 버전에서도 공개 API 가 바뀔 수 있다. 공개 API 는 `<모듈>/api/*.api` 로 고정하고 CI 가 대조한다.

## 라이선스

[Apache License 2.0](LICENSE)
