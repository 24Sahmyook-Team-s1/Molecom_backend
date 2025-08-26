DICOM 웹 뷰어 (병원 내부망 전용)

범위: 병원 내부망에서 동작하는 의료영상(DICOM) 열람 및 리포트 워크플로우 시스템
멀티 데이터베이스 구조: Oracle (PACS/메타데이터) + MySQL (애플리케이션/인증/감사)

1) 시스템 개요

목표: 판독의/임상의에게 안전한 웹 기반 DICOM 조회·판독·리포트 시스템 제공

사용자: 판독의(RAD), 임상의(MD), 방사선사(TECH), 관리자(ADMIN), 감사(AUDITOR)

주요 기능

환자/스터디 검색 (Oracle, UID 기반 LIKE 검색)

시리즈/이미지 썸네일 뷰어 (OHIF 연동)

리포트 작성/템플릿/버전 관리

협업 노트·검토 요청

접근 감사 로그 기록 (법령 준수)

CIFS/NAS 기반 DICOM 파일 접근

대용량 CT/MR Range 스트리밍

2) 아키텍처
[브라우저]
   │ HTTPS
   ▼
[Spring Boot App] ──► [OHIF Viewer]
   │        │
   │        ├─ REST: DICOM 메타 API → Oracle
   │        ├─ REST: 인증/리포트/협업 → MySQL
   │        └─ CIFS/NAS Range Streaming
   │
   └─ 감사 로그 → MySQL + Append-only 파일

3) 데이터베이스 스키마
🔹 Oracle (PACS 메타, Read-only)

patienttab

PK: pid

컬럼: pid, pname, psex, pbirthdate

studytab

PK: studykey

UK: studyinsuid

FK: pid → patienttab(pid)

컬럼:
studykey, studyinsuid, pid,
studydate, accessnum, studyid, studydesc,
modality, bodypart, seriescnt, imagecnt, delflag

seriestab

PK: (studykey, serieskey)

UK: seriesinsuid

FK: studykey → studytab(studykey)

컬럼:
serieskey, seriesinsuid, seriesnum,
modality, bodypart, seriesdesc, imagecnt, delflag

imagetab

PK: (studykey, serieskey, imagekey)

UK: sopinstanceuid

FK: (studykey, serieskey) → seriestab

컬럼:
imagekey, sopinstanceuid, sopclassuid, path, fname, delflag

🔹 MySQL (애플리케이션/인증/감사)

usertab: 사용자 계정 (이메일, 부서, 역할, 상태)

authsessiontab: JWT 세션 (발급/만료, IP, UserAgent)

reporttab: 판독 리포트 (studyinsuid FK, findings, impression)

reporttemplatetab: 리포트 템플릿 (name, bodymd, modality, 공유여부)

notetab: 협업 노트 (studyinsuid, authorid, bodymd)

reviewrequesttab: 검토 요청 (assignee, priority, status, dueat)

auditaccesstab: 접근 로그 (userid, action, objecttype, objectid, reasoncode, ts)

auditerrortab: 에러 로그 (level, code, message, stack, ts)

dicomaimarktab: AI 판독 결과 (studyinsuid, seriesinsuid, sopinstanceuid, label, bboxjson, score, source)

4) API 엔드포인트
검색/조회 (Oracle)

GET /api/studies?patientId&dateFrom&dateTo&modality&page&size

GET /api/studies/{studyinsuid}

GET /api/series/{seriesinsuid}/images

뷰어 데이터

GET /api/dicom/{sopinstanceuid} – DICOM 스트리밍 (Range)

GET /api/thumbnail/{sopinstanceuid} – 썸네일 JPEG/PNG

GET /api/ai/marks?studyinsuid=...&seriesinsuid=... – AI 오버레이

리포트

POST /api/reports

PUT /api/reports/{id}

GET /api/reports/{id}

GET /api/reports?studyinsuid=...

템플릿

POST /api/report-templates

GET /api/report-templates?modality=CT

PUT /api/report-templates/{id}

DELETE /api/report-templates/{id}

협업

POST /api/notes

GET /api/notes?studyinsuid=...

POST /api/reviews

GET /api/reviews?status=OPEN

PUT /api/reviews/{id}

감사/로그

GET /api/audit/access?userid=...&studyinsuid=...&from=...&to=...

GET /api/audit/errors?level=ERROR&from=...&to=...

인증

POST /api/auth/login

POST /api/auth/refresh

POST /api/auth/logout

5) 보안·컴플라이언스

RBAC 최소 권한

JWT 쿠키 인증: Access(짧게), Refresh(길게)

접근 로그: 이유 코드(TREATMENT, CONSULT, QA 등) 필수

PHI 최소 저장: Oracle 중심, MySQL에는 UID만 보관

백업/복구: MySQL 매일, Oracle RMAN 정책 준수

로그 보존: 중앙 수집(ELK), 최소 6개월 이상

6) 운영

프로파일: dev, stage, prod-intranet

모니터링: DB 풀 포화, CIFS 지연, 인증 실패 급증

DR: MySQL 복제, Oracle Data Guard 가능 시

헬스체크: /actuator/health, /actuator/metrics

7) 로드맵

WADO-RS 호환 레이어

AI 오버레이 승인/반려 UX

DICOM SR(구조화 리포트)

HL7/FHIR 연동

병원 SSO(Keycloak/SAML) 연동

관리자 기본 계정: admin / 1 (최초 로그인 시 비밀번호 변경 필수)
