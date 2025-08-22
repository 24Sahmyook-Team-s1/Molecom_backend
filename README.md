# DICOM 웹 뷰어 (병원 내부망 전용)

> **범위**: 병원 내부망에서 동작하는 의료영상(DICOM) 열람 및 리포트 워크플로우 시스템. **멀티 데이터베이스 구조: Oracle**(PACS/메타데이터) + **MySQL**(애플리케이션/인증/감사).

---

## 1) 시스템 개요

* **목표**: 의사/판독의를 위한 안전한 웹 기반 DICOM 조회·판독·리포트 시스템 제공
* **사용자**: 판독의, 임상의, 방사선사, 관리자, 감사 담당자
* **핵심 기능**

  * 환자/스터디 검색(DICOM 태그 기반)
  * 썸네일·시리즈 뷰어(OHIF 연동) – WW/WL, Zoom, Pan, (선택) MPR
  * 리포트 작성/템플릿/버전 관리
  * 접근 감사 로그(법적 근거 만족)
  * 협업 노트/검토 요청(풀리퀘 유사 흐름)
  * 역할 기반 접근제어(RBAC)
  * CIFS/NAS의 DICOM 파일 접근(jcifs-ng)
  * 대용량 CT/MR에 대한 스트리밍/캐시(옵션)

---

## 2) 네트워크·배포(병원 내부망)

* **존 구성**

  * **클라이언트 VLAN**: 병동/영상의학과 PC
  * **애플리케이션 VLAN**: 웹/앱 서버
  * **데이터 VLAN**: DB(Oracle/MySQL), NAS/CIFS
* **외부 유입 차단**: 공인망 미개방, 병원 SSO/VPN을 통한 내부 접근만 허용
* **TLS**: 내부 CA 인증서 사용(종단 간 HTTPS)
* **mTLS(선택)**: 앱↔뷰어↔API↔DB 구간
* **NTP**: 전 노드 시간 동기화(감사 무결성)
* **대용량 전송**: HTTP Range/청크 전송 지원

---

## 3) 아키텍처

```
[브라우저]
   │  HTTPS(TLS1.2+)
   ▼
[Web/App (Spring Boot)] ──► [OHIF Viewer]
   │         │
   │         ├─ REST: DICOM 메타 API → (Oracle)
   │         ├─ REST: 앱/인증/리포트 → (MySQL)
   │         └─ CIFS/NAS 읽기(jcifs-ng) → DICOM 파일
   │
   └─ 감사 로그 싱크 → (MySQL 감사 테이블 + Append-only 파일)
```

* **프론트엔드**: OHIF(내장/임베드) – 백엔드 JSON 엔드포인트 소비
* **백엔드**: Spring Boot 3.x, 멀티 DataSource(Oracle + MySQL)
* **스토리지**: DICOM 파일은 NAS/CIFS에 저장, 경로/UID는 Oracle에 보관
* **캐시/프록시(옵션)**: NGINX/Node 기반 바이트 범위 캐시

---

## 4) 데이터 책임(이중 DB 역할)

| 도메인                                      | DB                          | 사유                    |
| ---------------------------------------- | --------------------------- | --------------------- |
| DICOM 태그(환자/스터디/시리즈/이미지), 파일 경로, SOP UID | **Oracle**                  | PACS 연계, 기존 스키마, 고가용성 |
| 사용자/역할, 세션(JWT), 리프레시 토큰                 | **MySQL**                   | 애플리케이션 인증/RBAC 민첩성    |
| 리포트/소견/템플릿                               | **MySQL**                   | 앱 비즈니스 데이터            |
| 접근·감사 로그                                 | **MySQL**(+ Append-only 파일) | 법적 추적성, 빠른 조회         |
| 협업(노트, 리뷰/할당)                            | **MySQL**                   | 앱 기능 데이터              |

> **주의**: PHI는 중복 저장 최소화. 교차 키는 `study_uid/series_uid/sop_uid` 중심. 환자 인적사항은 Oracle에서 온디맨드 조회.

---

## 5) 최소 스키마(논리)

### Oracle (PACS/메타)

* `PATIENT`(patient\_pk, patient\_id, name, birth\_date, sex)
* `STUDY`(study\_pk, patient\_pk, study\_uid, study\_date, accession, modality, desc)
* `SERIES`(series\_pk, study\_pk, series\_uid, modality, body\_part, desc)
* `IMAGE`(image\_pk, series\_pk, sop\_instance\_uid, instance\_no, file\_path, file\_size)
* 인덱스: `*_uid`, `patient_id`, `study_date`

### MySQL (앱/인증/감사)

* `user`(id, email, display\_name, dept, role, status, created\_at)
* `auth_session`(id, user\_id, jwt\_id, issued\_at, expires\_at, ip, ua)
* `report`(id, study\_uid, author\_id, status, title, findings, impression, created\_at, updated\_at)
* `report_template`(id, name, body\_md, modality, owner\_id, is\_shared)
* `note`(id, study\_uid, author\_id, body\_md, created\_at)
* `review_request`(id, study\_uid, requester\_id, assignee\_id, priority, status, due\_at)
* `audit_access`(id, user\_id, action, object\_type, object\_id, reason\_code, ip, ts)
* `audit_error`(id, level, code, message, stack, ts)
* `dicom_ai_mark`(id, study\_uid, series\_uid, sop\_uid, label, bbox\_json, score, source)

---

## 6) 계정 생성·인증 정책

* **회원가입 방식**: *사용자 자가 회원가입 금지*. **관리자(어드민) 계정이 신규 계정을 생성**합니다.
* **기본 관리자 계정**: 아이디 `admin` / 비밀번호 `1`

  * 배포 직후 **최초 로그인 시 비밀번호 변경 강제**(보안 권고)
  * 관리자만 사용자 생성/비활성화/권한 변경 가능
* **역할(Roles)**: `ADMIN`, `RAD`(판독의), `MD`(임상의), `TECH`(방사선사), `AUDITOR`
* **인증**: JWT(Access + Refresh), HttpOnly + Secure, SameSite=Lax(내부 단일 도메인 기준)
* **세션 관리**: 짧은 만료(Access), 긴 만료(Refresh), IP/UA 감사 저장

---

## 7) 멀티 데이터소스 설정(Spring Boot)

### `application.yml` (발췌)

```yaml
spring:
  datasource:
    oracle:
      url: jdbc:oracle:thin:@//ORACLE_HOST:1521/ORCLPDB1
      username: ${ORACLE_USER}
      password: ${ORACLE_PASS}
      driver-class-name: oracle.jdbc.OracleDriver
    mysql:
      url: jdbc:mysql://MYSQL_HOST:3306/dicom_app?useSSL=true&serverTimezone=Asia/Seoul
      username: ${MYSQL_USER}
      password: ${MYSQL_PASS}
      driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate.format_sql: true
      hibernate.jdbc.time_zone: Asia/Seoul
```

### Java 구성(개요)

```java
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {
  @Bean @Primary
  @ConfigurationProperties("spring.datasource.mysql")
  public DataSource mysqlDs() { return DataSourceBuilder.create().build(); }
  @Bean
  @ConfigurationProperties("spring.datasource.oracle")
  public DataSource oracleDs() { return DataSourceBuilder.create().build(); }
  // DB별 EntityManagerFactory/TxManager 분리, 패키지 분리 운영
}
```

* 패키지 예: `com.pacs.app.*`(MySQL), `com.pacs.pacsmeta.*`(Oracle)
* 교차 트랜잭션은 가급적 회피(XA 지양), 필요 시 사가 패턴 적용

---

## 8) 파일 접근(CIFS/NAS)

* 라이브러리: **jcifs-ng**(SMB2/3, Kerberos 또는 NTLMv2)
* 접근 정책: 읽기 전용 서비스 계정, 공유 루트는 내부망에서만 노출
* 경로 규칙: `\\nas\dicom\{study_uid}\{series_uid}\{sop_uid}.dcm`
* 스트리밍: Range 읽기, 스트림당 메모리 상한

---

## 9) 뷰어 연동(OHIF)

* 운영 방식: 독립 실행 또는 임베디드
* 백엔드 엔드포인트

  * `/api/studies/search` – 스터디/시리즈/이미지 요약(JSON)
  * `/api/dicom/{sopUid}` – DICOM 바이트 스트림(Range)
  * `/api/thumbnail/{sopUid}` – JPEG/PNG 썸네일
  * `/api/ai/marks?studyUid=...` – AI 주석(JSON)

---

## 10) 보안·컴플라이언스

* **RBAC** 최소 권한 적용
* **HTTPS** 전 구간, 약한 암호군 비활성화
* **관리자 엔드포인트**: 관리 VLAN/허용 IP만 접근
* **감사 불변성**: 누가/무엇을/언제/왜 조회했는지 기록, 사유 코드 필수
* **PHI 최소화**: Oracle에서 필요 시 조회, 앱 DB에 민감정보 미복제
* **백업**: MySQL 일일 백업, Oracle RMAN 정책 준수, 월 1회 복구 리허설
* **로그**: 중앙 수집(ELK 등), PHI 마스킹, 보존 주기 정책 반영
* **한국 법령 준수**: 의료정보 접근 기록 보관(예: ≥ 6개월, 기관 정책에 따름)

---

## 11) 대표 API

### 검색

* `GET /api/studies?patientId&name&dateFrom&dateTo&modality&page&size`
* `GET /api/studies/{studyUid}` – 스터디/시리즈 요약

### 뷰어 데이터

* `GET /api/series/{seriesUid}/images`
* `GET /api/dicom/{sopUid}`
* `GET /api/thumbnail/{sopUid}`

### 리포트

* `POST /api/reports`(studyUid, title, findings, impression)
* `PUT /api/reports/{id}` / `GET /api/reports/{id}` / `GET /api/reports?studyUid=`

### 협업·감사

* `POST /api/notes` / `POST /api/reviews`
* `GET /api/audit?user&studyUid&from&to`

---

## 12) 운영 플레이북

* **프로파일**: `dev`, `stage`, `prod-intranet`
* **헬스체크**: `/actuator/health`, `/actuator/metrics`(Prometheus)
* **용량/성능**: 동시 스트림 제한, 메모리 70% 이하 목표
* **DR**: MySQL 복제, Oracle Data Guard(가능 시), RTO/RPO 명시
* **마이그레이션**: MySQL(Flyway), Oracle(DBA 스크립트/비파괴 원칙)

---

## 13) 빌드·실행

```bash
# Java 17, Spring Boot 3.5+
./gradlew clean bootJar

# ENV
export ORACLE_USER=...
export ORACLE_PASS=...
export MYSQL_USER=...
export MYSQL_PASS=...
export SPRING_PROFILES_ACTIVE=prod-intranet

java -jar build/libs/dicom-app.jar
```

* Docker(선택): 로컬 개발용 Oracle/MySQL 컨테이너 구성, `docker-compose.yml` 제공

---

## 14) 디렉터리 구조(백엔드)

```
/src/main/java/com/pacs
  /app        # MySQL(인증/리포트/감사)
  /pacsmeta   # Oracle(DICOM 메타 read-only)
  /infra      # CIFS, 보안, 설정
  /api        # 컨트롤러
```

---

## 15) 모니터링·알림

* 5xx 비율, CIFS 읽기 지연, DB 커넥션 풀 포화, GC 일시정지
* 이상 징후 알림: 인증 실패 급증, 감사 기록 실패, CIFS 마운트 에러, 캐시 미스 폭증

---

## 16) 비범위(1단계)

* 완전한 WADO-RS/UPS, 고급 3D, 인터넷 공개, PHI 분석

---

## 17) 로드맵

* WADO-RS 호환 레이어
* AI 오버레이 검토 UX(승인/반려, 버전)
* DICOM SR(구조화 리포트)
* HL7/FHIR(오더/결과 연계)
* 병원 SSO(Keycloak/SAML) 연동

---

## 18) 컴플라이언스 체크리스트

* [ ] 내부 CA 기반 TLS 전구간 적용
* [ ] RBAC 최소권한
* [ ] 불변 접근 감사
* [ ] PHI 최소 보관
* [ ] 백업/복구 리허설 완료
* [ ] NTP 시간 동기화
* [ ] 로그 보존정책 적용

---

## 19) 접근 사유 코드 예시

`TREATMENT`, `CONSULT`, `QA`, `RESEARCH_APPROVED`, `ADMIN_MAINT`, `OTHER(자유 입력)`

---

**관리자 계정 기본값**: `admin / 1`
**담당자**: @backend, @infra, @viewer
