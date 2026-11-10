# Changelog

모듈별로 적는다. 형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/), 버전은 [SemVer](https://semver.org/lang/ko/) 이다.

## lunar

### [Unreleased]

### [0.1.0] - 2026-11-11

공개 API 를 SDK 규칙(공개 enum·data class·기본값 인자 금지)에 맞게 바로 바꿨다. 좌표·패키지·계산 결과값은 그대로다.
옮기는 법은 README "0.0.1 에서 옮기기" 표를 본다.

#### 깨짐
- `LunarCalculatorConfig`: data class → 불변 클래스 + 빌더. `LunarCalculatorConfig(zone = ..)` → `LunarCalculatorConfig.builder().zone(..).build()`, `copy` → `toBuilder()`
- `LunarCalculator.create(config = 기본)` → `create()`·`create(config)` 오버로드(Java 에서 정적 메서드)
- `riseSet`·`riseSetsOfMonth` 의 기본값 인자 `location = GeoLocation.SEOUL` → 지점 없는 오버로드(서울)와 지점 있는 오버로드
- `LunarPhaseType` enum → sealed class(`NewMoon`·`FirstQuarter`·`FullMoon`·`LastQuarter`)
- `SpecialMoonType` enum → sealed class(`Supermoon`·`Minimoon`), `SpecialMoonEventType` enum → sealed class(`Supermoon`·`Minimoon`·`BlueMoon`)
- `SolarTermType` enum → 상수 클래스(값 이름 그대로, `name`·`values`). 앱의 `when` 에 `else` 가 필요하다
- `entries`·`values()`·`valueOf`·`ordinal` → 각 타입의 `values` 목록
- 공개 data class 10개(`LunarCalculatorConfig`·`FullMoonSample`·`GeoLocation`·`LunarPhaseEvent`·`MoonDay`·`MoonRiseSet`·`MoonToday`·`SolarTerm`·`SpecialFullMoon`·`SpecialMoonEvent`) → 일반 클래스. `equals`·`hashCode`·`toString` 은 같고 `copy`·`componentN` 이 없다
- `DistanceThresholdCriteria` 기본값 인자 → `()`·`(supermoonMaxKm, minimoonMinKm)` 생성자. 두 임계값은 공개 프로퍼티다
- `LunarCalculationException(message, cause = null)` → `(message)`·`(message, cause)` 생성자
- 공개 인터페이스 기본 구현을 Java 기본 메서드로만 낸다(`jvmDefault = NO_COMPATIBILITY`, `DefaultImpls` 없음)

#### 추가
- `FakeLunarCalculator` — 앱 단위 테스트용 가짜 계산기. 값은 실제 계산과 같고, 부른 함수를 기록하며, `failWith` 로 실패를 낸다
- `DistanceThresholdCriteria.withSupermoonMaxKm`·`withMinimoonMinKm`
- README 의 API 레벨별 동작 표·알려진 한계·옮기는 법, 문서와 코드가 어긋나면 실패하는 `DocsConsistencyTest`
- R8 release 를 만드는 확인용 `sample` 앱(발행하지 않는다)

### [0.0.1] - 2026-11-02

#### 추가
- `LunarCalculator` 진입점과 `LunarCalculatorConfig`(시간대, 기본 한국 표준시)
- 위상(삭·상현·망·하현)·음력 일자·오늘의 달·월간 달력
- 블루문·슈퍼문·미니문. 판정 기준은 `SpecialMoonCriteria` 로 교체 가능, 기본 `DistanceThresholdCriteria`(360,000 km / 405,000 km)
- 지구-달 거리, 월출·월몰(관측 지점 지정), 24절기
- 지원 연도 `MIN_YEAR`(1900)..`MAX_YEAR`(2100), 모든 진입점의 인자 검사(`IllegalArgumentException`)
- 계산 실패를 알리는 `LunarCalculationException` — 교체한 판정 기준의 예외도 원인을 담아 감싼다
