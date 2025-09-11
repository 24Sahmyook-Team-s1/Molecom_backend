# DICOM 웹 뷰어 (MoleComs Backend)

> **범위**: 병원 내부망 전용 웹 기반 의료영상(DICOM) 조회 및 리포트 워크플로우 시스템  
> **아키텍처**: 멀티 데이터베이스 (**Oracle = PACS 메타데이터/읽기 전용**, **MySQL = 앱·인증·로그**)  
> **보안 모델**: JWT 기반 단일 세션 관리 + RBAC 최소 권한 원칙

---

## 1) 시스템 개요
- **목표**: RAD/MD/TECH/ADMIN/AUDITOR가 안전하게 의료 영상을 열람·판독·협업·리포트할 수 있는 웹 환경 제공
- **주요 기능**
    - 환자·스터디 검색 (Oracle UID 기반 LIKE)
    - 시리즈/이미지 썸네일 뷰어 (OHIF 연동)
    - 리포트 업서트, 템플릿/버전 관리
    - 협업 노트/검토 요청
    - DICOM Range 스트리밍 (CIFS/NAS)
    - AI 오버레이 마킹
    - **Silent Refresh & Session Rotation** (단일 세션 강제 + 무중단 인증 연장)

---

## 2) 아키텍처
```
[브라우저]
   │ HTTPS + JWT Cookie
   ▼
[Spring Boot Backend] ──► [OHIF Viewer]
   │        │
   │        ├─ Oracle DB (PACS 메타 조회, Read-only)
   │        ├─ MySQL DB (인증/리포트/로그)
   │        └─ CIFS/NAS Range Streaming
   │
   ├─ 감사 로그 저장
   └─ 세션 관리 (JwtSessionGuardFilter + SessionRotationService)
```

---

## 3) 데이터베이스 스키마

### 🔹 Oracle (PACS 메타, Read-only)
- **patienttab**: 환자 정보
- **studytab**: 스터디 메타 (studykey, studyinsuid, modality 등)
- **seriestab**: 시리즈 메타 (serieskey, seriesinsuid, desc 등)
- **imagetab**: 이미지 메타 (sopinstanceuid, path, fname 등)

---

### 🔹 MySQL (App/Auth/Logs)

#### `auth_session`
- **id** (bigint, PK)
- **user_id** (bigint)
- **session_id** (varchar(36))
- **active** (bit(1))
- **access_expire_at** (datetime(6))
- **refresh_expire_at** (datetime(6))
- **access_jti** (varchar(36))
- **refresh_jti** (varchar(36))
- **revoked_at** (datetime(6), nullable)
- **revoked_reason** (varchar(32), nullable)
- **created_at** (datetime(6))
- **updated_at** (datetime(6))

#### `dicom_logs`
- **id** (bigint, PK)
- **actor_id** (bigint)
- **action** (enum('OPEN_FILE','OPEN_IMAGE','OPEN_SERIES','OPEN_STUDY'))
- **target_uid** (varchar(128))
- **created_at** (datetime(6))

#### `report`
- **id** (bigint, PK)
- **study_key** (bigint)
- **author_id** (bigint)
- **content** (text)
- **created_at** (datetime(6))
- **updated_at** (datetime(6))

#### `report_logs`
- **id** (bigint, PK)
- **report_id** (bigint)
- **user_id** (bigint)
- **action** (enum('CREATE','DELETE','UPDATE','VIEW'))
- **log_action** (enum('CREATE','DELETE','UPDATE','VIEW'))
- **content** (varchar(200))
- **created_at** (datetime(6))

#### `user_logs`
- **id** (bigint, PK)
- **actor_id** (bigint)
- **target_id** (bigint)
- **db** (enum('AUTH_SESSION','LOGS','USERS'))
- **log_action** (enum('CREATE','DELETE','HARD_DELETE','LOGIN','LOGOUT','READ','READ_LIST','UPDATE'))
- **user_log_action** (enum('CREATE','DELETE','HARD_DELETE','LOGIN','LOGOUT','READ','READ_LIST','UPDATE'))
- **created_at** (datetime(6))

#### `users`
- **id** (bigint, PK)
- **email** (varchar(255), UNIQUE)
- **password** (varchar(255))
- **display_name** (varchar(100))
- **dept** (varchar(100))
- **role** (enum('ADMIN','DOCTOR','GUEST','NURSE','STAFF'))
- **status** (enum('ACTIVE','DELETED','INACTIVE','LOCKED','SUSPENDED'))
- **created_at** (datetime(6))

---

## 4) 유저 삭제 정책 (Soft Delete by `status`)
- 물리 삭제 금지 → `users.status` 컬럼으로 논리 삭제 처리
- 상태 값
    - `ACTIVE`: 정상
    - `INACTIVE`: 비활성화(로그인 불가, 복구 가능)
    - `DELETED`: 삭제(접근 불가, 로그만 유지)
- API 동작
    - `DELETE /api/users/{email}` → `status='DELETED'`, `deleted_at=NOW()`
    - 모든 세션(`auth_session`) 무효화
    - `user_logs` 에 `USER_DELETE` 기록
- 조회 API는 기본적으로 `status != 'DELETED'`만 반환
- 관리자 API에서 `INACTIVE` ↔ `ACTIVE` 전환 가능

---

## 5) 보안/세션 관리
- **JWT**: Access(짧게) + Refresh(길게), 쿠키 기반
- **JwtSessionGuardFilter**: Access 만료 시 Refresh로 무중단 갱신
- **SessionRotationService**: DB PESSIMISTIC_WRITE 기반 단일 세션 강제
- **RBAC**: 최소 권한
- **로그 정책**
    - 모든 열람에 `reasonCode` 필수 (`TREATMENT`, `CONSULT`, `QA` 등)
    - 로그는 MySQL + Append-only 파일 이중 보관
- **법령 준수**
    - MySQL에는 UID만 보관 (PHI 최소화)
    - 로그 6개월 이상 보존 (ELK 연동 가능)

---

## 6) API 엔드포인트

### 👤 UserController
- `PUT /api/users/{id}` – 유저 수정
- `PUT /api/users/pw` – 비밀번호 변경
- `GET /api/users` – 유저 전체 조회
- `POST /api/users` – 유저 생성
- `POST /api/users/logout` – 로그아웃
- `POST /api/users/login` – 로그인
- `GET /api/users/email` – 유저 단건 조회
- `DELETE /api/users/{email}` – 유저 삭제 (논리 삭제)
- `GET /api/users/me` – 본인 정보 조회

### 📝 ReportController
- `GET /api/reports/studies/{studyKey}` – 리포트 조회
- `PUT /api/reports/studies/{studyKey}` – 리포트 업서트

### 📊 LogController
- `GET /api/users/logAll`
- `GET /api/reports/logAll`
- `GET /api/dicom/logAll`
- `GET /api/combined`

### 🔎 DicomQueryController
- `GET /api/dicom/studies` – STUDY 목록 조회
- `GET /api/dicom/studies/{studyInsUid}` – Series 조회
- `GET /api/dicom/series/{seriesInsUid}/images` – 이미지 조회

### 📂 DicomFileController
- `GET /api/dicom/studies/{studyKey}/series/{seriesKey}/images/{imageKey}/stream` – DICOM Range 스트리밍

---

## 7) 운영/예외 처리
- **LazyInitializationException 방지**: DTO 변환을 서비스 계층 트랜잭션 내 처리
- **전역 예외 처리**
    - `MolecomsException` → ErrorCode 매핑
    - `Exception` → INTERNAL_ERROR 안전망
- **모니터링**: DB 풀, CIFS 지연, 인증 실패 급증
- **DR 구성**: MySQL 복제 + Oracle Data Guard 고려

---

## 8) 환경설정 (application.yml 예시)
```yaml
spring:
  datasource: # MySQL Primary
  jpa:
    open-in-view: false
app:
  datasource:
    oracle: # Oracle Read-only
jwt:
  secret: ${JWT_SECRET:...}
  security:
    cors:
      allowed-origins: "http://localhost:5173,https://frontend.example.com"
cifs:
  base-url: smb://210.94.241.9/sts
```

---

## 9) 로드맵
- WADO-RS 호환 API
- AI 오버레이 승인/반려 UX
- DICOM SR 지원
- HL7/FHIR 연동
- 병원 SSO(Keycloak/SAML)

---

✅ **관리자 기본 계정**: `admin / 1` (최초 로그인 시 변경 필수)  
📌 **담당자 태그**: @backend, @infra, @viewer  
