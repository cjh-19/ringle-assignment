<div align="center">
    <h2>"학생 튜터 1:1 수업 서비스 - 수강 신청 API"</h2>
</div>

<br/>

<details>
  <summary><b>[ ⚒️ 설계 배경 ]</b> </summary>

# ⚒️ 설계 배경 & 프로덕트 이해
본 프로젝트는 튜터와 학생 간의 1:1 온라인 수업 매칭을 위한 **수강 신청**의 핵심 기능을 API로 설계한 것입니다. 이 시스템은 **튜터의 수업 가능 시간 설정**부터 **학생의 수업 신청 및 조회**, 그리고 **수업 예약의 경쟁 상황 처리**까지 전반적인 플로우로 구성했습니다.

---

## 📌 핵심 개요

### [목표]
- 수업을 원하는 학생과, 수업을 제공할 수 있는 튜터 간의 매칭을 효율적으로 지원
- 사용자가 직관적으로 수업을 예약하고, 튜터는 유연하게 일정을 조율할 수 있도록 설계

---

## 👥 사용자 시나리오 기반 API 구성

### ✅ 튜터용 API

#### 1. 수업 가능한 시간대 생성 & 삭제 & 생성한 수업 조회 API

- **생성**
    - 튜터는 수업 가능 시간을 **30분 단위 슬롯**으로 설정할 수 있습니다.
    - 예시: `12:00~12:30` (→ 30분 수업 전용), 또는 연속된 두 슬롯(`12:00~13:00`)으로 60분 수업도 가능

- **삭제**
    - 튜터는 자신이 설정한 수업 가능 시간대를 자유롭게 삭제할 수 있습니다.

- **조회**
    - 튜터는 자신이 등록한 수업 가능 시간을 언제든지 조회할 수 있습니다.

---

### ✅ 학생용 API

#### 2. 신청한 수업 조회 API
- 사용자가 예약한 수업 정보를 확인
- 수업이 없다면 **빈 리스트**, 있다면 **수업 정보 + 튜터 정보** 반환

<img width="70%" src="https://github.com/user-attachments/assets/1ff11ef9-b8f6-435e-95bc-9258389659ee" alt="신청한 수업 조회"/>

#### 3. 날짜 & 수업 길이 별 현재 수업 가능 시간대 조회 API
- 학생은 특정 날짜(오늘 포함 이후)와 수업 길이(30분 or 60분)를 선택하여 가능한 시간대를 확인
- 시스템은 선택 날짜 및 시각(오늘일 때) 기준 이후의 30분 단위 슬롯을 기준으로 수업 가능 여부를 판단 (`true`/`false`)

<img width="70%" src="https://github.com/user-attachments/assets/279db4a6-749c-44fd-b436-7a6007255556" alt="현재 수업 가능 시간 조회"/>

#### 4. 날짜 & 수업 길이로 튜터별 수업 가능 시간대 조회 API
- 선택한 날짜에 대해 가능한 튜터 리스트와 시간대를 조회
- 수업 가능한 시간 순으로 정렬된 튜터 리스트 제공

<img width="70%" src="https://github.com/user-attachments/assets/aa640eac-6e6f-4931-8de6-8bba7cf004a5" alt="튜터별 수업 가능 시간 조회"/>

#### 5. 시간대 & 수업 길이 & 튜터로 새로운 수업 신청 API
- 선택한 시간대/수업 길이/튜터 정보 기반으로 수업을 신청
- **경쟁 상황 발생 시**, 다음 옵션 중 하나 선택 가능(true/false):
    - **다른 튜터 제안 받기**
        - 동일한 시간대에 가능한 다른 튜터가 있으면 자동으로 재매칭
    - **수업 취소**
        - 매칭 실패 시 자동으로 예약 취소

<img width="70%" src="https://github.com/user-attachments/assets/34292452-3b8f-471b-8998-19a84fd8d07c" alt="수업 신청"/>



- ⚠️ 동시성 해결 로직
    - 여러 명의 학생이 같은 시간대를 신청할 수 있으므로, 분산 락에 의해 가장 먼저 락을 걸고 들어온 학생만 신청

---

### 💡 향후 확장 아이디어

#### 🔸 수업 취소 기능
- 학생이 신청한 수업을 취소할 수 있는 기능
- 취소 사유, 시간, 사용자 정보를 별도 로그 테이블에 저장하여 히스토리 관리 가능

#### 🔸 튜터 평점/리뷰 기능
- 수업 종료 후 리뷰 기능을 붙이기 위해 `lesson_reviews` 테이블을 설계할 수 있음
- 향후 튜터의 신뢰도 및 추천 시스템에 활용 가능

---

</details>


<details>
  <summary><b>[ 🚀 실행 방법 ]</b> </summary>

# 🚀 실행 방법

이 프로젝트는 Docker 기반으로 구성되어 있어, 단 한 줄의 명령어로 전체 서비스를 실행할 수 있습니다.

---

### ✅ 사전 준비 사항

- [Docker](https://www.docker.com/products/docker-desktop) 및 [Docker Compose](https://docs.docker.com/compose/) 설치 필요
- 다음 명령어로 설치 여부를 확인해주세요.

```bash
docker --version
docker-compose --version
```

---

### 📂 디렉터리 구성

```csharp
📁 ringle/
├── 📄 docker-compose.yml
├── 📄 init.sql               # MySQL 초기화 스크립트
├── 📁 src/                   # Spring Boot 애플리케이션
├── 📄 README.md              # 실행 가이드 문서
```

---

### 🧩 포함된 서비스
| 서비스 이름        | 설명                                             |
|--------------------|--------------------------------------------------|
| **MySQL**          | 사용자 및 데이터베이스 초기화 포함 (`init.sql`) |
| **Redis**          | 분산 락 등을 위한 캐시 서버                      |
| **Spring Boot App**| 백엔드 서버 (포트: `8080`)                        |

---

### 🛠 실행 명령어

- `docker-compose.yml` 파일이 있는 디렉토리에서 아래의 명령어를 실행합니다.
```bash
docker-compose up -d
```
- 모든 서비스가 백그라운드(`-d`)에서 실행됩니다.
- 내부적으로는 다음 순서로 초기화됩니다
    1. `MySQL` 및 `Redis` 컨테이너 기동
    2. `healthcheck`를 통해 `MySQL` 및 `Redis` 서버가 준비될 때까지 대기
    3. 준비 완료 시, Spring Boot 앱 자동 시작

---

### 📌 포트 정보

| 서비스     | 컨테이너 포트 | 호스트 포트 |
|------------|----------------|---------------|
| MySQL      | 3306           | 3307          |
| Redis      | 6379           | 6378          |
| Spring App | 8080           | 8080          |
- 각 로컬 환경에서 3306과 6379를 사용하고 있을 수 있으므로 호스트 포트를 3307, 6378로 설정

---

### 🧪 접속 테스트

- Spring 서버 접속: [http://localhost:8080](http://localhost:8080)
- Swagger API 문서 접속 테스트(추천): [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)

---
### 🧹 컨테이너 정리
- 실행 중인 컨테이너 중지 및 정리

```bash
docker-compose down
```

<br />

</details>

<details>
  <summary><b>[ ⭐ 서비스 기능 ]</b> </summary>

## ⭐ 서비스 기능

- 서비스 기능 테스트는 [스웨거 문서](http://localhost:8080/swagger-ui/index.html#/)에 접속해서 테스트를 진행할 수 있습니다.

### [회원가입]

    - 회원가입 정보를 작성합니다.
    - 비밀번호는 8자 이상, 대소문자 구분, 특수문자, 숫자를 포함하여 작성합니다.
    - role 값은 [STUDENT, TUTOR]를 통해 학생 계정, 튜터 계정으로 생성합니다.

<img width="100%" src="https://github.com/user-attachments/assets/2f33e710-fe28-4847-ac4c-1f6c52829381" alt="회원가입"/>

### [로그인]

    - 회원가입한 이메일, 비밀번호로 로그인을 진행한 뒤,
    - 응답 데이터로 오는 JWT 토큰을 자물쇠를 눌러 기입 후 인증하여 로그인합니다.

<img width="100%" src="https://github.com/user-attachments/assets/00951adc-e267-46cc-ac8f-59e05f3f197e" alt="로그인"/>
<img width="100%" src="https://github.com/user-attachments/assets/5364b6eb-8598-46a1-bb88-83f100720f6c" alt="로그인"/>
<img width="100%" src="https://github.com/user-attachments/assets/ad86adb4-7be3-4b20-8728-ec806e10f341" alt="로그인"/>

### [튜터 - 수업 생성 & 삭제 & 조회]

    - 튜터는 0분 or 30분씩 30분 단위로 수업을 생성할 수 있습니다. (아니라면 예외처리)
    - 수업 길이인 duration은 [THIRTY, SIXTY]의 값으로 설정되며, SIXTY의 경우 30분 수업 슬롯이 두 칸 생성됩니다.
    - 조회를 하게 되면, 지금까지 생성한 수업 시간대와 예약 유무(true, false)의 정보를 받습니다.
    - 수업 삭제는 조회를 했을 때, 각 수업마다 주어지는 id 값을 이용하여 삭제합니다. (아래 그림에서는 2번, 즉 16:30 수업 삭제)

<img width="100%" src="https://github.com/user-attachments/assets/4a791371-3200-4e83-9a80-a5f7e7dd08cc" alt="수업 생성"/>
<img width="100%" src="https://github.com/user-attachments/assets/6c5cbf87-e685-487a-8f89-6aba4b11ee9f" alt="수업 조회"/>
<img width="100%" src="https://github.com/user-attachments/assets/ebbebd53-b9e0-442d-869b-87b0f329c082" alt="수업 삭제"/>

### [학생 - 날짜 & 수업 길이 별 수업 가능 시간대 조회]

    - 원하는 날짜와 수업 길이[THIRTY, SIXTY]를 선택하고 조회를 하면,
    - 수업 길이에 맞게 해당 날짜의 수업 가능한 시간대가 조회된다.
    - 단, 과거는 조회할 수 없다. (예외처리)
    - 또한, 오늘을 선택하는 경우, 현재 시각을 기준으로 가까운 0분 or 30분부터 시간대가 조회된다
    - 미래 날짜는 풀타임으로 시간대가 조회된다.

<img width="100%" src="https://github.com/user-attachments/assets/1b53da90-841a-43fe-8878-97fd3437281a" alt="수업 가능 시간 조회"/>
<img width="100%" src="https://github.com/user-attachments/assets/d843d9ef-2e1d-44c4-baf1-49d65e918775" alt="수업 가능 시간 조회"/>

### [학생 - 날짜 & 수업 길이 & 튜터 별 수업 가능한 시간대 조회]

    - 날짜를 입력하게 되면, 해당 날짜에 맞게 튜터마다 수강 신청 가능한 시간대가 조회된다.
    - 날짜에 대한 조건은 위 기능과 동일하다.

<img width="100%" src="https://github.com/user-attachments/assets/db299dd8-c753-43be-ab6b-224b384f2ead" alt="튜터별 수업 가능 시간 조회"/>
<img width="100%" src="https://github.com/user-attachments/assets/56f35190-0610-4698-9ec7-9b4500146667" alt="튜터별 수업 가능 시간 조회"/>

### [학생 - 수업 신청]

    - 튜터 별 수업 가능한 시간대 조회 API를 통해서 얻은
    - 튜터 id와 원하는 시작 시간, 수업 길이, 예약 실패 시 다른 튜터를 추천 받을지 여부를 입력한다.
    - 입력 후, 실행하게 되면 예약 성공 및 예약 실패가 발생한다.
    - 여기서 예약 실패의 경우는 아래의 경우이다.
    - 1. 타 튜터 추천을 받지 않은 상태에서 예약에 밀리는 경우
    - 2. 원하는 시간대에 대해서 신청 가능한 타 튜터가 없는 경우
    - 3. 원하는 튜터 & 원하는 시간대 모두 맞지 않는 경우

<img width="100%" src="https://github.com/user-attachments/assets/2f3d244b-78d0-42bc-b7e7-5832e5388866" alt="수업 신청"/>
<img width="100%" src="https://github.com/user-attachments/assets/1075dec2-c26e-4bd2-a1b3-9ee5fc73e5d6" alt="수업 신청"/>
<img width="100%" src="https://github.com/user-attachments/assets/eb7e9455-dd5f-473e-9c07-0883b6ec726a" alt="수업 신청"/>

### [학생 - 신청한 수업 조회]

    - 학생 수업 조회 요청을 통해 신청한 수업을 조회한다.
    - 신청한 수업 id, 시작 시간, 끝 시간, 수업 길이, 튜터 정보를 응답받는다.

<img width="100%" src="https://github.com/user-attachments/assets/53d24de2-385d-4ea6-9e3e-8c165a4d7cb1" alt="신청한 수업 조회"/>
<img width="100%" src="https://github.com/user-attachments/assets/4ca0f119-03ee-4aae-a71c-3678735a5b27" alt="신청한 수업 조회"/>

<br/>

</details>

<details>
  <summary><b>[ 💡 고민한 점 ]</b> </summary>

## 💡 고민한 점

### ⚠️ 문제 상황

수강 신청 API에서는 여러 사용자가 동시에 같은 시간대와 튜터로 수업을 예약할 수 있습니다. 이 경우, 동일한 자원(튜터의 시간)을 두 명 이상이 동시에 점유하려는 **동시성 문제**가 발생할 수 있습니다.

### 🔍 고려한 해결 전략

동시성 문제를 방지하기 위해 다음과 같은 락 전략을 비교했습니다

| 전략 유형          | 설명                                                                   | 장점                                          | 단점                                                          |
|-------------------|------------------------------------------------------------------------|-----------------------------------------------|---------------------------------------------------------------|
| **비관적 락**       | DB 수준에서 `SELECT ... FOR UPDATE`를 사용해 레코드를 즉시 잠금            | 충돌을 원천 차단, 데이터 일관성 보장            | 트랜잭션 대기 및 성능 저하 가능, 락 순서에 따라 데드락 발생 위험     |
| **낙관적 락**       | 버전 필드(`@Version`)를 통해 충돌 여부를 감지하고 충돌 시 재시도 수행       | 성능 우수, 충돌이 드문 환경에서 유리             | 충돌 시 재시도 로직 필요, 충돌 빈번 시 부적합                    |
| **분산 락 (Redis)** | `Redisson` 등을 통해 Redis 기반의 락을 획득하고 해제하며 동시성 제어         | 경량 락 구현, 속도 빠름, 다중 인스턴스 환경에 적합 | TTL 만료, 네트워크 이슈 등으로 인한 락 해제 실패 가능, Redis 의존성 |


### ✅ 최종 선택: Redis 기반 분산 락 (`Redisson`)

위 전략들 중 `Redisson`을 이용한 **Redis 기반 분산 락**을 아래와 같은 이유로 선택했습니다.

- Spring Boot와의 호환성이 좋고 redis 환경만 구축하면 사용법이 간단합니다.
- 애플리케이션이 확장되어 **멀티 인스턴스**로 운영될 경우에도 락이 일관성을 유지할 수 있습니다.
- Redis는 시스템에서 이후에 캐시 서버로도 사용할 수 있으므로, **추가 인프라 비용 없이 재사용**할 수 있습니다.
- 락 설정 및 타임아웃 조절 등 유연한 제어가 가능합니다.

### 💡 구현 방식 요약

- `RedissonClient`를 통해 Redis에 연결
- 예약 처리 핵심 로직에서 `RLock` 획득
- 처리 완료 후 `unlock()`으로 락 해제

```java
RLock lock = redissonClient.getLock("lesson:lock:" + lessonId);
try {
    if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
        // 수업 예약 처리 로직
    }
} finally {
    lock.unlock();
}
```

- 고민한 결과 Redis 분산 락을 통해 높은 트래픽 환경에서도 안정적인 예약 처리를 보장해주는 환경을 구축했습니다.

</details>

<details>
  <summary><b>[ 📜 ERD & 테스트 방법 ]</b> </summary>

## 📜 ERD

<img width="100%" src="https://github.com/user-attachments/assets/e54bca35-efd7-45f7-b1e5-4cd92c70cfbc" alt="ERD"/>

## ✅ 테스트 방법

본 프로젝트는 기능의 신뢰성과 안정성을 확보하기 위해 **단위 테스트**, **통합 테스트**, **Swagger 기반의 API 테스트**를 병행하여 수행했습니다.

---

### [단위 테스트 (Unit Test)]

- **목표**: 각 서비스/컴포넌트의 내부 로직이 의도대로 동작하는지 검증
- **프레임워크**: `JUnit5` + `Mockito`
- **적용 범위**
  - `UserService`의 인증/인가 로직
  - `LessonService`의 예약 로직 및 대체 튜터 매칭 로직
  - `AvailabilityService`의 수업 가능 시간 등록/삭제 기능 등
- **예외 케이스에 대한 철저한 검증 포함**
  - 예약 불가능 시간
  - 중복 예약 시도
  - 존재하지 않는 사용자 등

---

### [통합 테스트 (Integration Test)]

- **목표**: 실제 환경과 유사하게 DB, Redis 등 외부 의존성과 함께 전체 흐름 테스트
- **환경**: `@SpringBootTest` + 테스트용 H2 DB 또는 로컬 MySQL/Redis
- **주요 시나리오**
  - 사용자가 수업 가능 시간을 등록하고, 학생이 이를 조회하여 수업을 예약하는 전 과정 테스트
  - Redis 분산 락 적용 시 동시 예약에서도 데이터 일관성 유지 확인

---

### [Swagger를 이용한 테스트]

- 주소: `http://localhost:8080/swagger-ui/index.html`
- API를 직접 실행하며 실제 요청/응답 흐름 시각적으로 확인 가능
- **테스트 시나리오 예시**
  1. 회원가입
  2. 로그인 후 토큰 발급
  3. 튜터 수업 시간 등록
  4. 학생 수업 가능 시간 조회
  5. 수업 예약
  6. 예약 목록 조회
  7. 예외 발생 케이스 확인 (인증 누락, 잘못된 입력 등)

---

### 🧰 주요 테스트

- **Redis Lock 충돌 테스트**: 동일 시간대 동시 예약 요청 → 단 1건만 성공해야 함
- **대체 튜터 매칭 테스트**: 예약 실패 상황에서 대체 튜터가 자동 매칭되는지 확인
- **Swagger 문서 기반 테스트**: API 명세와 실제 구현의 일치 여부 검증

---

<img width="100%" src="https://github.com/user-attachments/assets/6973e0df-ce28-402c-b066-34cde39847ee" alt="테스트 코드"/>

- 현재 코드는 도커 환경으로 맞춰져 있어 코드의 주소들를 컨테이너명에서 localhost로 바꿔줘야 모두 통과합니다.

<br/>

</details>
