# 오류와 실패 신호

라이브러리가 던지는 예외와, 예외 대신 값으로 알리는 실패 신호를 모듈별로 적는다.

## lunar

예외는 두 종류뿐이다. 인자가 틀리면 `IllegalArgumentException`, 인자는 맞는데 계산이 끝나지 못하면
`LunarCalculationException`(원인은 `cause`)이다. 그 밖의 "없음" 은 예외가 아니라 값(빈 목록·null·0)으로 알린다.

### 예외

| 예외 | 언제 | 호출부가 할 일 |
|---|---|---|
| `IllegalArgumentException` | 연도(날짜·시각의 연도 포함)가 `LunarCalculator.MIN_YEAR`(1900)..`MAX_YEAR`(2100) 밖일 때 | 호출 전에 범위를 맞춘다 |
| `IllegalArgumentException` | 월이 1..12 밖일 때 | 호출 전에 범위를 맞춘다 |
| `IllegalArgumentException` | `GeoLocation` 위도가 -90..90, 경도가 -180..180 밖이거나 NaN·무한대일 때 (생성 시) | 좌표를 바로잡는다 |
| `IllegalArgumentException` | `DistanceThresholdCriteria` 임계값이 양의 유한한 수가 아니거나 슈퍼문 임계가 미니문 임계 이상일 때 (생성 시) | 임계값을 바로잡는다 |
| `LunarCalculationException` | 교체한 `SpecialMoonCriteria` 가 예외를 던졌을 때 — 원래 예외가 `cause` 다 | 기준 구현을 고친다 |
| `LunarCalculationException` | 내부 계산이 끝나지 못했을 때(지원 범위 안에서는 나지 않아야 한다) | 입력값과 함께 이슈로 알린다 |

예외 메시지에는 받은 값과 허용 범위가 들어간다. 메시지 문구는 바뀔 수 있으니 분기는 예외 타입으로 한다.

### 값으로 알리는 신호

| 신호 | 언제 | 호출부가 할 일 |
|---|---|---|
| 위상 4건 미만 | 그 달에 들지 않는 위상이 있을 때 | 인접 월도 함께 조회한다 |
| `rise`·`set` 이 null | 그날 안에 달이 뜨지(지지) 않을 때 — 한 달에 하루쯤 생긴다. 극지방에서 달이 종일 떠 있거나 종일 진 날은 둘 다 null | "없음" 으로 표시한다 |
| `nextFullMoon`·`nextNewMoon` 이 null | 3개월 안에 그 위상이 없을 때 | "없음" 으로 표시한다 |
| 카운트다운 0 | 오늘 그 위상이 들었거나, 다음 달까지 그 위상이 없을 때 | 정확한 시각은 `nextFullMoon`·`nextNewMoon` 으로 본다 |
