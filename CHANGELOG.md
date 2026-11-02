# Changelog

모듈별로 적는다. 형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/), 버전은 [SemVer](https://semver.org/lang/ko/) 이다.

## lunar

### [Unreleased]

### [0.0.1] - 미발행

#### 추가
- `LunarCalculator` 진입점과 `LunarCalculatorConfig`(시간대, 기본 한국 표준시)
- 위상(삭·상현·망·하현)·음력 일자·오늘의 달·월간 달력
- 블루문·슈퍼문·미니문. 판정 기준은 `SpecialMoonCriteria` 로 교체 가능, 기본 `DistanceThresholdCriteria`(360,000 km / 405,000 km)
- 지구-달 거리, 월출·월몰(관측 지점 지정), 24절기
- 지원 연도 `MIN_YEAR`(1900)..`MAX_YEAR`(2100), 모든 진입점의 인자 검사(`IllegalArgumentException`)
- 계산 실패를 알리는 `LunarCalculationException` — 교체한 판정 기준의 예외도 원인을 담아 감싼다
