<div align="center">

# 🎧 오늘의 소리 · Backend

시각장애인을 위한 맞춤형 정보 구독 알림 서비스의 API 서버입니다.
구독한 웹페이지의 새 글을 크롤러가 수집·요약하면, 이 서버가 저장하고 푸시로 내보냅니다.

<img src="https://raw.githubusercontent.com/Today-s-Sound/.github/main/docs/screenshots/feed.jpg" width="200"/>

[![App Store](https://img.shields.io/badge/App_Store-다운로드-0D96F6?style=flat-square&logo=appstore&logoColor=white)](https://apps.apple.com/kr/app/%EC%98%A4%EB%8A%98%EC%9D%98-%EC%86%8C%EB%A6%AC/id6756462316)
[![전체 문서](https://img.shields.io/badge/프로젝트_전체_문서-181717?style=flat-square&logo=github&logoColor=white)](https://github.com/Today-s-Sound)

</div>

<br/>

## 1️⃣ 기술 스택

| 구분       | 사용 기술                                                                    |
| ---------- | ---------------------------------------------------------------------------- |
| 언어·런타임 | Java 17 (Corretto / Temurin), Gradle                                          |
| 프레임워크 | Spring Boot 3.5.6, Spring Web, Spring Security, Spring Data JPA, QueryDSL 5.0 |
| 데이터     | MySQL 8.0, Flyway 11, p6spy                                                   |
| 알림       | Firebase Admin SDK 9.2 (FCM)                                                  |
| 문서화     | Spring REST Docs (Asciidoctor), springdoc-openapi 2.7                         |
| 관측       | Actuator, Micrometer Prometheus, Micrometer Tracing(Brave) + Zipkin, Loki(logstash-logback-encoder), Sentry |
| 테스트     | JUnit 5, Spring Boot Test, Testcontainers(MySQL)                              |
| 인프라     | Docker, AWS EC2 / RDS / S3 / Route53, Terraform Cloud, GitHub Actions         |

<br/>

## 2️⃣ 인프라 구성

![AWS 인프라](https://raw.githubusercontent.com/Today-s-Sound/.github/main/docs/images/infra.png)

- **main EC2** — Spring Boot(:8080)와 Grafana Alloy 컨테이너를 `docker compose`로 운영합니다.
- **RDS MySQL 8.0** — private 서브넷에 두고 db subnet group으로 묶었습니다. 스키마는 Flyway가 관리합니다.
- **S3** — 서비스 자산 버킷(버저닝·CORS)과 로그 버킷(Loki 청크 저장소)을 분리했습니다.
- **관측** — Alloy가 `/actuator/prometheus` 메트릭, 도커 로그, OTLP 트레이스를 각각 Prometheus · Loki · Tempo로 보내고 Grafana에서 함께 봅니다.
- 모든 리소스는 [`terraform-infra`](https://github.com/Today-s-Sound/terraform-infra)에서 Terraform Cloud VCS 워크플로로 관리합니다.

<br/>

## 3️⃣ CI/CD 파이프라인

![CI/CD 파이프라인](https://raw.githubusercontent.com/Today-s-Sound/.github/main/docs/images/cicd.png)

**CI — `.github/workflows/ci.yml`** (`dev`·`main` 대상 PR)

1. JDK 17 세팅 + Gradle 캐시
2. `./gradlew compileJava compileTestJava`
3. `SPRING_PROFILES_ACTIVE=ci` 로 `./gradlew test`
4. 테스트 결과를 PR 체크로 게시, `bootJar`로 빌드 가능 여부까지 확인

**CD** (`dev` 브랜치 push)

1. `bootJar` 빌드 후 이미지 생성, Docker Hub에 `latest`와 커밋 SHA 7자리 태그로 push
2. Terraform Cloud API에서 `main_server_ip` · `rds_address` · `s3_bucket_name` 등 출력값 조회
3. EC2에 SSH 접속 → `.env` 주입 → `docker compose pull app && docker compose up -d app` → 미사용 이미지 정리
4. `/actuator/health` 를 5초 간격 15회까지 확인, 실패하면 컨테이너 로그를 남기고 배포 실패 처리

<br/>

## 4️⃣ 크롤링 · 요약 아키텍처

구독한 웹페이지를 주기적으로 확인해 새 글만 요약하고, 알림까지 만들어 내는 흐름입니다.
크롤러 코드는 [`crawler`](https://github.com/Today-s-Sound/crawler) 저장소에 있고, 이 서버는 `/internal/**` API로 연결됩니다.

![크롤링 파이프라인](https://raw.githubusercontent.com/Today-s-Sound/.github/main/docs/images/crawler.png)

| 단계 | 동작                                                                                             |
| ---- | ------------------------------------------------------------------------------------------------ |
| ①    | 크롤러가 `GET /internal/subscriptions` 로 구독 목록(사이트 URL, 키워드, `last_seen_post_id`)을 받음 |
| ②    | 사이트별 크롤러(동국대 SW교육원 · 에이블뉴스 · 한국시각장애인복지관)로 분기해 목록·본문 수집       |
| ③    | `last_seen_post_id` 이후에 올라온 글만 추림. 첫 실행이면 알림 없이 기준점만 저장                  |
| ④    | 구독에 걸린 키워드가 제목·본문에 있는지 확인. 같은 글은 본문·요약 캐시로 재사용                    |
| ⑤    | Gemini 2.5 Flash가 제목·시간·장소 중심으로 요약. 호출 실패 시 본문 앞부분을 잘라 폴백             |
| ⑥    | `POST /internal/alerts` 로 요약·알림 생성, `PATCH /internal/subscriptions/{id}/last_seen` 으로 기준점 갱신 |
| ⑦    | 서버가 `summaries`에 저장하고 FCM으로 푸시 발송 → iOS 앱이 음성으로 재생                          |

<br/>

## 5️⃣ 프로젝트 구조

```
src/main/java/com/todaysound/todaysound_server
├─ domain
│  ├─ user           # 익명 가입·기기 식별, FCM 토큰
│  ├─ url            # 구독 대상 웹페이지 주소
│  ├─ subscription   # 구독 생성·수정, 키워드, last_seen 기준점
│  ├─ summary        # 크롤러가 만든 요약 저장·조회
│  ├─ feed           # 구독처별 피드 조회
│  └─ alarm          # 알림 목록, 내부 알림 생성 API
└─ global
   ├─ config         # Security, Web, FCM, QueryDSL 등 설정
   ├─ entity         # 공통 BaseEntity
   ├─ exception      # 전역 예외 처리
   ├─ response       # 공통 응답 포맷
   ├─ presentation   # FCM 발송 컨트롤러
   └─ utils
```

각 도메인은 `controller / service / repository / entity / dto / exception` 계층으로 나눕니다.

<br/>

## 6️⃣ 로컬 실행

```bash
# 1) 실행 (기본 프로필: local)
./gradlew bootRun

# 2) 테스트 - Testcontainers로 MySQL을 띄우므로 Docker가 필요합니다
./gradlew test

# 3) 전체 빌드 (컴파일 + 테스트 + bootJar)
./gradlew build

# 4) REST Docs HTML 생성
./gradlew asciidoctor
```

- 프로필은 `local` / `ci` / `prod` 세 가지입니다. 기본값은 `local`.
- 로컬 DB 접속 정보는 `.env` 또는 환경변수로 덮어씁니다. (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- 스키마는 Hibernate가 아닌 Flyway가 관리합니다(`ddl-auto: none`). 변경이 필요하면 `V{번호}__{설명}.sql` 파일을 추가하세요.

<br/>

## 7️⃣ API 문서

| 문서            | 경로                                                    |
| --------------- | ------------------------------------------------------- |
| Spring REST Docs | `/docs/index.html` (`bootJar` 시 정적 리소스로 포함)   |
| Swagger UI      | `/swagger-ui.html`                                      |
| OpenAPI JSON    | `/v3/api-docs`                                          |

API 동작이 바뀌면 REST Docs 스니펫을 갱신하고 PR 본문에 함께 적어 주세요.

<br/>

## 8️⃣ 브랜치 · 커밋 컨벤션

- 기본 브랜치는 `dev`, 배포 기준 브랜치는 `main`입니다.
- 작업 브랜치는 `dev`에서 분기하고, PR 대상은 항상 `dev`입니다.
- 커밋 메시지는 `feat: ...`, `fix: ...` 같은 짧은 접두사를 사용합니다.
- PR에는 목적, 수행한 테스트(`./gradlew test`), 스키마 변경 시 Flyway 마이그레이션 링크를 적습니다.

<br/>

## 9️⃣ 관련 저장소

| 저장소                                                                  | 설명                          |
| ----------------------------------------------------------------------- | ----------------------------- |
| [FE](https://github.com/Today-s-Sound/FE)                               | iOS 앱 (Swift · SwiftUI)      |
| [crawler](https://github.com/Today-s-Sound/crawler)                     | 구독 사이트 크롤러 (Python)   |
| [terraform-infra](https://github.com/Today-s-Sound/terraform-infra)     | AWS 인프라 IaC (Terraform)    |
