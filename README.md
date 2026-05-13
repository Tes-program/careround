# CareRound — Digital Ward Management System

> **Coding Agent Context Document** — This README contains every finalized architectural decision, data model, workflow, and implementation detail for the CareRound system. Read this fully before writing any code.

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Architecture Decision](#2-architecture-decision)
3. [Tech Stack](#3-tech-stack)
4. [Project Structure](#4-project-structure)
5. [Domain Modules](#5-domain-modules)
6. [Database Topology](#6-database-topology)
7. [All Entities](#7-all-entities)
8. [All Enums](#8-all-enums)
9. [Actors and Permissions](#9-actors-and-permissions)
10. [Workflows](#10-workflows)
11. [Kafka Event Catalogue](#11-kafka-event-catalogue)
12. [API Endpoints](#12-api-endpoints)
13. [Business Rules](#13-business-rules)
14. [Coding Standards](#14-coding-standards)
15. [Local Environment](#15-local-environment)
16. [Production Concerns](#16-production-concerns)

---

## 1. System Overview

CareRound is a **multi-tenant, production-grade digital ward management platform** for hospitals. It digitises the full lifecycle of inpatient care: patient admission and team assignment, shift management and handovers, daily ward rounds with patient prioritisation, nursing care tasks, clinical documentation, patient deterioration detection via NEWS2 score, and automated next-of-kin notification.

**Multi-tenancy:** Every hospital is an independent tenant. All data is scoped by `hospitalId`. A user from Hospital A cannot access data from Hospital B under any circumstance.

**Domain context:** The system models real clinical workflows. Patients are assigned to consultant-led medical teams (firms) based on specialty and on-call rotations. Ward rounds are prioritised by patient acuity and NEWS score. Shifts are auto-created from schedules. Handovers are first-class entities because shift change is the highest-risk moment in patient care.

---

## 2. Architecture Decision

### Final Decision: Modular Monolith — NOT microservices

This decision is final and must not be reversed. The reasoning:

- Two-person team, eleven-day build timeline, live demo format
- Domain is tightly coupled — a ward round touches shift, patient, review, note, and care task in one operation
- Hospital ward management operates at human scale, not social media scale
- A complete, observable, well-tested monolith is more impressive than a half-finished distributed system
- The presentation argument: "We evaluated microservices and rejected them deliberately. The domain coupling would have required five synchronous service calls per round creation. Instead we built a modular monolith with the same reliability patterns: Transactional Outbox, Quartz JDBC clustering, Redis rate limiting, DLT handling, and clean module boundaries that are microservices-extractable in 2–3 days if scale demands it."

### What is NOT included

- No API Gateway (Spring Security handles JWT directly)
- No CQRS with separate read store (not justified by read/write patterns)
- No Event Sourcing (Transactional Outbox + Audit Consumer already provides the audit trail)
- No service mesh, gRPC, Saga orchestrator, or Debezium
- No Elasticsearch (MySQL with indexes handles all search at this scale)

### Why Notification and Audit are separate processes

These two have a legitimate reason to be independent:
- `careround-notification` calls external APIs (email, SMS) that fail and throttle — a slow email provider must not affect round creation response times. DLT handling is isolated here.
- `careround-audit` writes compliance records independently — audit log writes must never block clinical operations.
- Both are pure Kafka consumers with no HTTP API surface.

---

## 3. Tech Stack

| Concern | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.5 |
| Security | Spring Security | 6.x (via Spring Boot) |
| ORM | Spring Data JPA / Hibernate | via Spring Boot |
| Database | MySQL | 8.0 |
| Schema Migrations | Flyway | 10.x (via Spring Boot) |
| Connection Pool | HikariCP | via Spring Boot |
| Messaging | Apache Kafka | 3.7 (KRaft mode) |
| Scheduled Jobs | Quartz Scheduler (JDBC clustered) | via spring-boot-starter-quartz |
| Cache / Rate Limiting | Redis | 7 |
| Circuit Breakers | Resilience4j | via spring-cloud-starter-circuitbreaker-resilience4j |
| Auth | JWT | jjwt 0.12.5 |
| Metrics | Micrometer + Prometheus | via spring-boot-starter-actuator |
| Dashboards | Grafana | latest |
| Logging | Logback JSON encoder | logstash-logback-encoder 7.4 |
| Build | Maven | 3.9+ |
| Boilerplate | Lombok | 1.18.34 |
| Mapping | MapStruct | 1.5.5.Final |
| Frontend | Not present in this repository | Backend API only |

---

## 4. Project Structure

### Monorepo layout

```
careround/
├── pom.xml                          ← parent POM (dependencyManagement only)
├── docker-compose.yml               ← infrastructure: MySQL, Redis, Kafka, Grafana
├── .env                             ← environment variables (never committed)
├── .env.example                     ← committed with dummy values
├── docker/                          ← MySQL init, Prometheus, Grafana provisioning
├── infra/mysql/seed.sql             ← optional demo seed data
├── careround-core/                  ← main Spring Boot application (port 8080)
├── careround-notification/          ← Kafka consumer service (port 8081)
└── careround-audit/                 ← Kafka consumer service (port 8082)
```

### Parent POM responsibility

The parent POM uses `<dependencyManagement>` only. It pins versions for: Spring Boot BOM, jjwt, Lombok, MapStruct. All three child projects inherit from it. This prevents version drift between projects, which is critical because they share Kafka topics and must use the same serialiser versions.

### careround-core internal structure

```
careround-core/src/main/java/com/careround/
├── CareRoundApplication.java
│
├── auth/                            ← Domain Module 1
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── UserController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   └── UserService.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── entity/
│   │   └── User.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── LoginResponse.java
│       ├── RefreshTokenRequest.java
│       ├── CreateUserRequest.java
│       └── UserResponse.java
│
├── hospital/                        ← Domain Module 2
│   ├── ward/
│   │   ├── controller/WardController.java
│   │   ├── service/WardService.java
│   │   ├── repository/WardRepository.java
│   │   ├── entity/Ward.java
│   │   └── dto/CreateWardRequest.java, WardResponse.java, WardDashboardResponse.java
│   ├── department/
│   │   └── (same structure)
│   ├── medicalteam/
│   │   ├── controller/MedicalTeamController.java
│   │   ├── service/MedicalTeamService.java
│   │   ├── repository/MedicalTeamRepository.java, MedicalTeamInviteRepository.java
│   │   ├── entity/MedicalTeam.java, MedicalTeamWard.java, MedicalTeamMember.java, MedicalTeamInvite.java
│   │   └── dto/...
│   ├── shift/
│   │   └── (Shift, Handover, PatientHandoverNote)
│   ├── handover/
│   ├── oncall/
│   │   └── (OnCallRotation, ShiftSchedule)
│   └── hospital/
│       └── (Hospital, SystemConfiguration)
│
├── patient/                         ← Domain Module 3
│   ├── patient/
│   │   ├── controller/PatientController.java
│   │   ├── service/PatientService.java
│   │   ├── repository/PatientRepository.java
│   │   ├── entity/Patient.java
│   │   └── dto/...
│   ├── vitals/
│   │   └── (PatientVitals + NewsScoreService)
│   ├── round/
│   │   └── (Round, PatientRoundReview)
│   ├── caretask/
│   │   └── (CareTask)
│   ├── clinicalnote/
│   │   └── (ClinicalNote)
│   ├── escalation/
│   │   └── (Escalation)
│   └── nextofkin/
│       └── (NextOfKin)
│
├── scheduler/                       ← Quartz jobs (inside core, not separate JVM)
│   ├── config/QuartzConfig.java
│   ├── jobs/
│   │   ├── OutboxPollerJob.java
│   │   ├── ShiftCreationJob.java
│   │   ├── TaskOverdueJob.java
│   │   ├── EscalationUnacknowledgedJob.java
│   │   ├── InviteExpiryJob.java
│   │   └── RefreshTokenCleanupJob.java
│   └── service/JobSchedulingService.java
│
└── shared/                          ← Cross-cutting infrastructure
    ├── config/
    │   ├── SecurityConfig.java
    │   ├── KafkaProducerConfig.java
    │   ├── KafkaTopicConfig.java
    │   └── RedisConfig.java
    ├── entity/
    │   └── BaseEntity.java          ← @MappedSuperclass: id, createdAt, updatedAt
    ├── event/
    │   ├── OutboxEvent.java
    │   ├── OutboxEventRepository.java
    │   └── events/                  ← Kafka event POJOs (Java records)
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── ResourceNotFoundException.java
    │   ├── AccessDeniedException.java
    │   └── BusinessRuleException.java
    ├── security/
    │   ├── JwtAuthFilter.java
    │   ├── JwtService.java
    │   └── HospitalContextHolder.java
    ├── service/
    │   └── OutboxService.java
    ├── dto/
    │   └── ApiResponse.java
    └── validation/
        └── EnumValidator.java
```

### careround-notification structure

```
careround-notification/src/main/java/com/careround/notification/
├── NotificationApplication.java
├── config/
│   └── KafkaConsumerConfig.java     ← DefaultErrorHandler + DLT KafkaTemplate
├── client/
│   ├── CoreLookupClient.java
│   ├── HttpCoreLookupClient.java
│   └── NextOfKinContact.java
├── consumer/
│   ├── RoundCompletedConsumer.java
│   ├── PatientDeteriorationConsumer.java
│   ├── PatientDischargedConsumer.java
│   ├── TaskOverdueConsumer.java
│   ├── ShiftCreatedConsumer.java
│   ├── NotificationFactory.java
│   └── NotificationIdempotencyGuard.java
├── notification/
│   ├── Notification.java            ← entity (PENDING, SENT, FAILED)
│   ├── NotificationRepository.java
│   └── NotificationStatus.java       ← enum
├── dlt/
│   ├── NotificationDltConsumer.java  ← consumes failed messages, saves failed_notifications
│   ├── entity/FailedNotification.java
│   └── repository/FailedNotificationRepository.java
├── provider/
│   ├── EmailNotificationProvider.java
│   └── SmsNotificationProvider.java
└── service/
    └── NotificationService.java
```

### careround-audit structure

```
careround-audit/src/main/java/com/careround/audit/
├── AuditApplication.java
├── config/
│   └── KafkaConsumerConfig.java
├── consumer/
│   └── AuditEventConsumer.java      ← single consumer, all audit topics
├── entity/
│   └── AuditLogEntry.java
└── repository/
    └── AuditLogRepository.java
```

---

## 5. Domain Modules

### Module boundary rule — CRITICAL

Within careround-core, domain modules must not import repositories from other modules. If `round/` needs patient data, it calls `PatientService` — never `PatientRepository` directly. This enforces the same boundary that would exist between microservices, making future extraction a targeted operation rather than a full refactor.

### Scheduler inside careround-core

The Quartz scheduler runs inside careround-core, not in a separate JVM. When multiple instances of careround-core run horizontally, Quartz JDBC clustered mode ensures each job fires on exactly one instance. Configure in application.yml:

```yaml
spring:
  quartz:
    job-store-type: jdbc
    jdbc:
      initialize-schema: never
    properties:
      org.quartz.jobStore.isClustered: true
      org.quartz.jobStore.clusterCheckinInterval: 10000
      org.quartz.jobStore.driverDelegateClass: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
      org.quartz.threadPool.threadCount: 5
```

The `QRTZ_*` tables in MySQL (created via Flyway V13) and the `QRTZ_LOCKS` table provide distributed locking. No ShedLock needed — Quartz handles cluster safety natively. Registered jobs include outbox polling, shift creation, overdue-task detection, unacknowledged-escalation handling, invite expiry, and hourly refresh-token cleanup.

---

## 6. Database Topology

```
MySQL 8 (single server, port 3306)
├── careround_core          ← all domain tables + QRTZ_* + outbox_event
├── careround_notification  ← notifications + failed_notifications
└── careround_audit         ← audit_log only
```

One MySQL user (`careround`) has full privileges on all three schemas. Each Spring Boot application points its datasource URL at its own schema. Each runs its own Flyway migration set. No cross-schema SQL joins ever.

### HikariCP configuration

```yaml
# careround-core
hikari:
  pool-name: CorePool
  maximum-pool-size: 10
  minimum-idle: 5
  connection-timeout: 30000
  idle-timeout: 600000
  max-lifetime: 1800000

# careround-notification and careround-audit
hikari:
  maximum-pool-size: 5
  minimum-idle: 2
```

At 3 instances of careround-core + 1 each of notification and audit:
`(3 × 10) + (1 × 5) + (1 × 5) = 40 connections` — well within MySQL's default `max_connections: 151`.

---

## 7. All Entities

All entities extend `BaseEntity` which provides: `String id` (UUID, set in `@PrePersist`), `LocalDateTime createdAt`, `LocalDateTime updatedAt`.

Use `VARCHAR(36)` for all ID columns. Use `DATETIME` for timestamps. Use `@Enumerated(EnumType.STRING)` on all enum columns. Lombok `@Getter @Setter @NoArgsConstructor` on all entities.

### Hospital

```
Table: hospital
hospitalId not needed (this IS the tenant root)
name               VARCHAR(255) NOT NULL
address            TEXT
contactEmail       VARCHAR(255) NOT NULL UNIQUE
contactPhone       VARCHAR(50)
```

### SystemConfiguration

```
Table: system_configuration
hospitalId         VARCHAR(36) NOT NULL UNIQUE   ← 1:1 with Hospital
newsAmberThreshold INT NOT NULL DEFAULT 5
newsRedThreshold   INT NOT NULL DEFAULT 7
taskOverdueGraceMinutes INT NOT NULL DEFAULT 30
roundNotificationsEnabled BOOLEAN NOT NULL DEFAULT TRUE
nokNotificationEnabled    BOOLEAN NOT NULL DEFAULT TRUE
```

### User

```
Table: users
hospitalId         VARCHAR(36) NOT NULL
firstName          VARCHAR(100) NOT NULL
lastName           VARCHAR(100) NOT NULL
email              VARCHAR(255) NOT NULL
passwordHash       VARCHAR(255) NOT NULL
role               ENUM(UserRole) NOT NULL
departmentId       VARCHAR(36) nullable
isActive           BOOLEAN NOT NULL DEFAULT TRUE

UNIQUE INDEX: (hospital_id, email)
```

### PlatformOperator

```
Table: platform_operator
firstName          VARCHAR(100) NOT NULL
lastName           VARCHAR(100) NOT NULL
email              VARCHAR(255) NOT NULL UNIQUE
passwordHash       VARCHAR(255) NOT NULL
role               ENUM(PlatformOperatorRole) NOT NULL
isActive           BOOLEAN NOT NULL DEFAULT TRUE

Platform operators are internal CareRound users. They are not tenant users, do not belong to a hospital, and authenticate through the platform auth flow.
```

### AccountActivationToken

```
Table: account_activation_token
tokenHash          VARCHAR(64) NOT NULL UNIQUE
userId             VARCHAR(36) NOT NULL
hospitalId         VARCHAR(36) NOT NULL
expiresAt          DATETIME NOT NULL
usedAt             DATETIME nullable

Activation tokens are stored hashed, expire, and are single-use.
```

### HospitalOnboardingRequest

```
Table: hospital_onboarding_request
hospitalName           VARCHAR(255) NOT NULL
countryOrRegion       VARCHAR(120) NOT NULL
contactEmail           VARCHAR(255) NOT NULL
contactPhone           VARCHAR(50) nullable
hospitalType           VARCHAR(80) NOT NULL
estimatedBeds          VARCHAR(40) nullable
primaryNeed            TEXT NOT NULL
status                 ENUM(HospitalOnboardingStatus) NOT NULL
reviewNotes            TEXT nullable
reviewedByUserId       VARCHAR(36) nullable
reviewedAt             DATETIME nullable
provisionedHospitalId  VARCHAR(36) nullable

INDEX: (status, created_at)
INDEX: (contact_email)
```

### Department

```
Table: department
hospitalId            VARCHAR(36) NOT NULL
name                  VARCHAR(255) NOT NULL
headOfDepartmentId    VARCHAR(36) nullable  ← FK to users
```

### Ward

```
Table: ward
hospitalId    VARCHAR(36) NOT NULL
name          VARCHAR(255) NOT NULL
specialty     VARCHAR(100)
totalBeds     INT NOT NULL DEFAULT 0
supervisorId  VARCHAR(36) nullable  ← FK to users
```

### MedicalTeam

```
Table: medical_team
hospitalId    VARCHAR(36) NOT NULL
name          VARCHAR(255) NOT NULL
consultantId  VARCHAR(36) NOT NULL  ← FK to users
departmentId  VARCHAR(36) NOT NULL  ← FK to department
```

### MedicalTeamWard (join table)

```
Table: medical_team_ward
Composite PK: (medical_team_id, ward_id)
medicalTeamId  VARCHAR(36) NOT NULL
wardId         VARCHAR(36) NOT NULL
assignedAt     DATETIME NOT NULL
No BaseEntity extension — composite PK only
```

### MedicalTeamMember (join table)

```
Table: medical_team_member
Composite PK: (medical_team_id, user_id)
medicalTeamId  VARCHAR(36) NOT NULL
userId         VARCHAR(36) NOT NULL
joinedAt       DATETIME NOT NULL
No BaseEntity extension — composite PK only
```

### MedicalTeamInvite

```
Table: medical_team_invite
hospitalId       VARCHAR(36) NOT NULL
medicalTeamId    VARCHAR(36) NOT NULL
invitedUserId    VARCHAR(36) NOT NULL
invitedById      VARCHAR(36) NOT NULL  ← Consultant who sent invite
status           ENUM(InviteStatus) NOT NULL DEFAULT PENDING
expiresAt        DATETIME NOT NULL
```

### OnCallRotation

```
Table: on_call_rotation
hospitalId    VARCHAR(36) NOT NULL
departmentId  VARCHAR(36) NOT NULL
wardId        VARCHAR(36) nullable     ← optional ward-level override
doctorId      VARCHAR(36) NOT NULL
role          ENUM(OnCallRole) NOT NULL
startTime     DATETIME NOT NULL
endTime       DATETIME NOT NULL
```

### ShiftSchedule

```
Table: shift_schedule
hospitalId   VARCHAR(36) NOT NULL
wardId       VARCHAR(36) nullable   ← null = applies to ALL wards in hospital
shiftType    ENUM(ShiftType) NOT NULL
startTime    TIME NOT NULL           ← e.g. 07:00:00
endTime      TIME NOT NULL           ← e.g. 19:00:00
daysOfWeek   VARCHAR(50) NOT NULL    ← comma-separated: MON,TUE,WED,THU,FRI
isActive     BOOLEAN NOT NULL DEFAULT TRUE
```

### Shift

```
Table: shift
wardId            VARCHAR(36) NOT NULL
shiftScheduleId   VARCHAR(36) nullable    ← null if manually created
type              ENUM(ShiftType) NOT NULL
startTime         DATETIME NOT NULL
endTime           DATETIME NOT NULL
leadDoctorId      VARCHAR(36) nullable    ← set by WardSupervisor on assignment
nurseInChargeId   VARCHAR(36) nullable    ← set by WardSupervisor on assignment
status            ENUM(ShiftStatus) NOT NULL DEFAULT PENDING_ASSIGNMENT
assignedAt        DATETIME nullable

UNIQUE INDEX: (ward_id, type, start_time)
```

### Handover

```
Table: handover
wardId            VARCHAR(36) NOT NULL
outgoingShiftId   VARCHAR(36) NOT NULL
incomingShiftId   VARCHAR(36) NOT NULL
conductedById     VARCHAR(36) NOT NULL
status            ENUM(HandoverStatus) NOT NULL DEFAULT PENDING
generalNotes      TEXT nullable
completedAt       DATETIME nullable
```

### PatientHandoverNote

```
Table: patient_handover_note
handoverId          VARCHAR(36) NOT NULL
patientId           VARCHAR(36) NOT NULL
statusSummary       TEXT
outstandingTaskIds  TEXT nullable    ← comma-separated CareTask UUIDs
urgencyFlag         BOOLEAN NOT NULL DEFAULT FALSE
addedById           VARCHAR(36) NOT NULL
```

### Patient

```
Table: patient
hospitalId              VARCHAR(36) NOT NULL
wardId                  VARCHAR(36) nullable   ← cleared after final discharge
bedNumber               VARCHAR(20) nullable
medicalTeamId           VARCHAR(36) NOT NULL
admittingConsultantId   VARCHAR(36) nullable
firstName               VARCHAR(100) NOT NULL
lastName                VARCHAR(100) NOT NULL
dateOfBirth             DATE NOT NULL
gender                  VARCHAR(20) nullable
hospitalNumber          VARCHAR(50) NOT NULL UNIQUE
admissionDate           DATETIME NOT NULL
admissionType           ENUM(AdmissionType) NOT NULL
primaryDiagnosis        TEXT nullable
specialtyRequired       VARCHAR(100) nullable   ← matched to Department for on-call routing
acuityLevel             ENUM(AcuityLevel) NOT NULL DEFAULT LOW
newsScore               INT NOT NULL DEFAULT 0  ← computed from latest vitals
isDischargeReady        BOOLEAN NOT NULL DEFAULT FALSE
estimatedDischargeDate  DATE nullable
status                  ENUM(PatientStatus) NOT NULL DEFAULT ADMITTED

INDEX: (hospital_id, ward_id, acuity_level, news_score)
```

### PatientVitals

```
Table: patient_vitals
patientId           VARCHAR(36) NOT NULL
recordedById        VARCHAR(36) NOT NULL
heartRate           INT nullable
respiratoryRate     INT nullable
oxygenSaturation    DECIMAL(5,2) nullable
systolicBP          INT nullable
temperature         DECIMAL(4,1) nullable
consciousnessLevel  ENUM(ConsciousnessLevel) nullable
newsScore           INT NOT NULL    ← computed server-side via NEWS2 algorithm
recordedAt          DATETIME NOT NULL

INDEX: (patient_id, recorded_at)
```

### NextOfKin

```
Table: next_of_kin
patientId               VARCHAR(36) NOT NULL
name                    VARCHAR(255) NOT NULL
relationship            VARCHAR(100) nullable
phone                   VARCHAR(50) nullable
email                   VARCHAR(255) nullable
preferredContactMethod  ENUM(ContactMethod) NOT NULL DEFAULT SMS
isEmergencyContact      BOOLEAN NOT NULL DEFAULT FALSE
notificationConsent     BOOLEAN NOT NULL DEFAULT FALSE   ← GDPR gate
```

### Escalation

```
Table: escalation
hospitalId      VARCHAR(36) NOT NULL
patientId       VARCHAR(36) NOT NULL
triggeredById   VARCHAR(36) nullable    ← null when System-triggered
triggerType     ENUM(EscalationTrigger) NOT NULL
severity        ENUM(EscalationSeverity) NOT NULL
assignedToId    VARCHAR(36) nullable
status          ENUM(EscalationStatus) NOT NULL DEFAULT OPEN
notes           TEXT nullable
resolvedAt      DATETIME nullable
```

### Round

```
Table: round
hospitalId      VARCHAR(36) NOT NULL
wardId          VARCHAR(36) NOT NULL
medicalTeamId   VARCHAR(36) NOT NULL
shiftId         VARCHAR(36) NOT NULL
roundType       ENUM(RoundType) NOT NULL
leadDoctorId    VARCHAR(36) NOT NULL
status          ENUM(RoundStatus) NOT NULL DEFAULT SCHEDULED
scheduledTime   DATETIME nullable
startedAt       DATETIME nullable
completedAt     DATETIME nullable
teamMembers     TEXT nullable   ← comma-separated User UUIDs

INDEX: (ward_id, medical_team_id, round_type, status)
```

### PatientRoundReview

```
Table: patient_round_review
roundId              VARCHAR(36) NOT NULL
patientId            VARCHAR(36) NOT NULL
reviewedById         VARCHAR(36) NOT NULL
reviewOrder          INT NOT NULL          ← 1 = first seen, driven by acuity
newsScoreAtReview    INT nullable          ← snapshot at time of review
clinicalStatus       ENUM(ClinicalStatus) NOT NULL
wasExamined          BOOLEAN NOT NULL DEFAULT FALSE
managementPlan       TEXT nullable
dischargeAssessment  ENUM(DischargeAssessment) NOT NULL DEFAULT NONE
notifiedNextOfKin    BOOLEAN NOT NULL DEFAULT FALSE
reviewedAt           DATETIME NOT NULL
```

### ClinicalNote

```
Table: clinical_note
patientId             VARCHAR(36) NOT NULL
patientRoundReviewId  VARCHAR(36) nullable    ← null for standalone notes
authorId              VARCHAR(36) NOT NULL
noteType              ENUM(NoteType) NOT NULL
content               TEXT NOT NULL
isAmended             BOOLEAN NOT NULL DEFAULT FALSE
amendedById           VARCHAR(36) nullable
amendedAt             DATETIME nullable

NOTE: ClinicalNotes are NEVER deleted. Amendments preserve original.
```

### CareTask

```
Table: care_task
hospitalId       VARCHAR(36) NOT NULL
patientId        VARCHAR(36) NOT NULL
wardId           VARCHAR(36) NOT NULL
roundId          VARCHAR(36) nullable        ← null for NURSING_CARE_PLAN tasks
createdById      VARCHAR(36) NOT NULL
assignedToId     VARCHAR(36) nullable
assignedToRole   ENUM(AssignedToRole) nullable
taskType         VARCHAR(100) NOT NULL        ← free text: "Medication", "Chest X-Ray", etc.
source           ENUM(TaskSource) NOT NULL
title            VARCHAR(255) NOT NULL
description      TEXT nullable
priority         ENUM(TaskPriority) NOT NULL DEFAULT ROUTINE
windowStart      DATETIME NOT NULL for API-created tasks
windowEnd        DATETIME NOT NULL for API-created tasks
status           ENUM(TaskStatus) NOT NULL DEFAULT PENDING
completedById    VARCHAR(36) nullable
completedAt      DATETIME nullable
escalatedAt      DATETIME nullable
workloadConflict BOOLEAN NOT NULL DEFAULT FALSE
workloadConflictReason TEXT nullable

INDEX: (hospital_id, ward_id, status, window_end)
INDEX: (hospital_id, assigned_to_id, status, window_start, window_end)
```

### OutboxEvent

```
Table: outbox_event  (in careround_core schema)
hospitalId     VARCHAR(36) NOT NULL
eventType      VARCHAR(100) NOT NULL
payload        LONGTEXT NOT NULL        ← serialised JSON
published      BOOLEAN NOT NULL DEFAULT FALSE
publishedAt    DATETIME nullable
correlationId  VARCHAR(36)

INDEX: (published, created_at)
```

### Notification (careround_notification schema)

```
Table: notifications
eventType       VARCHAR(100) NOT NULL
hospitalId      VARCHAR(36) nullable
recipientId     VARCHAR(36) nullable
recipientType   VARCHAR(20) nullable
channel         VARCHAR(20) nullable
subject         VARCHAR(255) nullable
body            TEXT nullable
correlationId   VARCHAR(100) nullable
payload         LONGTEXT nullable
status          ENUM(NotificationStatus) NOT NULL    ← PENDING, SENT, FAILED
failureReason   TEXT nullable                        ← populated only if status=FAILED
sentAt          DATETIME nullable
retryCount      INT NOT NULL DEFAULT 0
```

### AuditLogEntry (careround_audit schema)

```
Table: audit_log
eventType      VARCHAR(100) NOT NULL
hospitalId     VARCHAR(36) NOT NULL
correlationId  VARCHAR(36)
payload        LONGTEXT
processedAt    DATETIME NOT NULL

INDEX: (hospital_id, event_type, created_at)
```

---

## 8. All Enums

```java
// auth
UserRole: ADMIN, CONSULTANT, REGISTRAR, JUNIOR_DOCTOR, NURSE, WARD_SUPERVISOR
PlatformOperatorRole: PLATFORM_ADMIN

// onboarding
HospitalOnboardingStatus: PENDING_REVIEW, CONTACTED, APPROVED, REJECTED, PROVISIONED

// hospital domain
ShiftType:       DAY, NIGHT
ShiftStatus:     PENDING_ASSIGNMENT, ACTIVE, COMPLETED, HANDED_OVER
HandoverStatus:  PENDING, IN_PROGRESS, COMPLETED
OnCallRole:      REGISTRAR_ON_CALL, CONSULTANT_ON_CALL
InviteStatus:    PENDING, ACCEPTED, DECLINED, EXPIRED

// patient domain
PatientStatus:        ADMITTED, STABLE, DETERIORATING, DISCHARGE_READY, DISCHARGED
AcuityLevel:          LOW, MEDIUM, HIGH, CRITICAL
AdmissionType:        EMERGENCY, ELECTIVE, TRANSFER
ConsciousnessLevel:   ALERT, VOICE, PAIN, UNRESPONSIVE   ← AVPU scale
EscalationTrigger:    HIGH_NEWS_SCORE, TASK_OVERDUE, NURSE_CONCERN, DETERIORATION
EscalationSeverity:   AMBER, RED
EscalationStatus:     OPEN, ACKNOWLEDGED, RESOLVED
ContactMethod:        SMS, EMAIL, BOTH
RoundType:            MORNING, POST_TAKE, BOARD, EVENING, WEEKEND
RoundStatus:          SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
ClinicalStatus:       STABLE, IMPROVING, DETERIORATING, CRITICAL
DischargeAssessment:  NONE, POSSIBLE, CONFIRMED, BLOCKED_SOCIAL, BLOCKED_MEDICAL
NoteType:             ROUND_NOTE, PROGRESS_NOTE, ADMISSION_NOTE, DISCHARGE_NOTE, ESCALATION_NOTE
TaskSource:           NURSING_CARE_PLAN, POST_ROUND_JOB
TaskPriority:         ROUTINE, URGENT, EMERGENCY
TaskStatus:           PENDING, IN_PROGRESS, COMPLETED, OVERDUE, CANCELLED
AssignedToRole:       NURSE, JUNIOR_DOCTOR, REGISTRAR
```

### AcuityLevel derivation from NEWS score

```
0–4   → LOW
5–6   → MEDIUM (AMBER alert)
7+    → HIGH / CRITICAL (RED alert)
```

---

## 9. Actors and Permissions

### Role overview

| Role | Real-world equivalent | Key scope |
|---|---|---|
| ADMIN | Hospital account administrator | System config, wards, users — no clinical data |
| CONSULTANT | Attending physician | Leads medical team, all clinical authority for own team |
| REGISTRAR | Senior resident | Daily ward management, on-call, round leadership |
| JUNIOR_DOCTOR | Foundation year / resident | Documentation, jobs list, round participation |
| NURSE | Ward nurse | Care tasks, vitals recording, escalation creation |
| WARD_SUPERVISOR | Ward manager / charge nurse | Shift assignment, oversight, dashboard |
| PLATFORM_ADMIN | Internal CareRound operator | Reviews onboarding requests, provisions tenants, lists hospitals |

### PLATFORM_ADMIN - allowed actions

- Log in through `/api/v1/platform/auth/login` using the `platform_operator` table
- Review hospital onboarding requests
- Provision approved hospitals, their default configuration, and first tenant `ADMIN`
- List all hospitals on the platform through `GET /api/v1/hospitals`
- **Cannot** access tenant clinical workflows as a hospital user

The first platform admin can be bootstrapped on startup by setting `CAREROUND_PLATFORM_BOOTSTRAP_ADMIN_EMAIL` and `CAREROUND_PLATFORM_BOOTSTRAP_ADMIN_PASSWORD`. Bootstrap only runs when the `platform_operator` table is empty.

### ADMIN — allowed actions

- Configure own Hospital record
- Manage SystemConfiguration (NEWS thresholds, notification toggles)
- Create/update/deactivate Departments
- Create/update/deactivate Wards
- Create/update/deactivate User accounts (all roles)
- Create and assign MedicalTeams to Wards
- Configure OnCallRotations
- Create and manage ShiftSchedules (automated shift timing)
- **Cannot** view or modify patient records or clinical data

### CONSULTANT — allowed actions

- Create new MedicalTeam (automatically becomes `consultantId`)
- Send MedicalTeamInvites to existing hospital users
- Remove members from own MedicalTeam
- Create and lead Rounds (all types)
- **ONLY role that can confirm discharge** (`dischargeAssessment = CONFIRMED`)
- Create and amend ClinicalNotes for own team's patients
- View all patients assigned to own MedicalTeam
- Create post-round CareTasks assigned to any team role
- **Cannot** access other teams' patients

### REGISTRAR — allowed actions

- Create and lead Rounds (all types, including on-call and weekend)
- Manage on-call admissions — assign patients to MedicalTeams
- Acknowledge and resolve Escalations
- Create post-round CareTasks
- Create and amend ClinicalNotes
- **Cannot** confirm discharge — Consultant only

### JUNIOR_DOCTOR — allowed actions

- Participate in Rounds, create PatientRoundReview records
- Write ClinicalNotes (ROUND_NOTE, PROGRESS_NOTE, DISCHARGE_NOTE)
- Complete assigned post-round CareTasks
- Create PatientHandoverNotes during shift handover
- **Cannot** lead Rounds, confirm discharge, or create NURSING_CARE_PLAN tasks

### NURSE — allowed actions

- Create NURSING_CARE_PLAN CareTasks for any patient on their ward
- Execute and complete CareTasks
- Record PatientVitals — triggers NEWS2 computation
- Create Escalations (triggerType: NURSE_CONCERN)
- Create PatientHandoverNotes
- View ClinicalNotes and PatientRoundReviews (read-only)
- **Cannot** write ClinicalNotes, create Rounds, or confirm discharge

### WARD_SUPERVISOR — allowed actions

- **Assign lead doctor and nurse to PENDING_ASSIGNMENT shifts**
- Initiate and oversee Handovers
- View all CareTasks, completion rates, overdue tasks for their ward
- View all Round histories for their ward
- View all patient records (read-only)
- Receive and action escalation alerts
- View ward bed capacity and occupancy; discharged patients are removed from beds automatically
- **Cannot** write ClinicalNotes, create Rounds, or confirm discharge

---

## 10. Workflows

### 10.1 Hospital Onboarding

```
Hospital representative submits public onboarding request
  POST /api/v1/onboarding/hospital-requests
  -> HospitalOnboardingRequest status = PENDING_REVIEW
  -> Outbox publishes careround.hospital.onboarding_requested

PlatformAdmin logs in through /api/v1/platform/auth/login
  -> Reviews request: CONTACTED, APPROVED, or REJECTED
  -> Approved requests can be provisioned

PlatformAdmin provisions approved request
  POST /api/v1/onboarding/hospital-requests/:id/provision
  -> Hospital record created
  -> SystemConfiguration defaults or request values created
  -> First tenant ADMIN user created inactive
  -> Single-use activation token generated
  -> Outbox publishes careround.hospital.provisioned
  -> Outbox publishes careround.user.activation_requested

Hospital admin opens http://localhost:3000/activate?token=...
  -> Sets password through POST /api/v1/auth/activate-account
  -> Token is marked used
  -> Admin logs in normally through POST /api/v1/auth/login

After activation:
  -> Admin creates Departments
  -> Admin creates Wards and optionally assigns WardSupervisor
  -> Admin creates staff User accounts with role + hospitalId
  -> Admin creates ShiftSchedules; wardId can target one ward or be null for all wards
  -> Admin or Consultant creates MedicalTeam
  -> Consultant sends invites to Registrar and JuniorDoctors
  -> Admin or owning Consultant assigns MedicalTeam to Wards via MedicalTeamWard
  -> Admin configures OnCallRotations
  -> System is operational
```

Refresh tokens are persisted for session rotation. Logout, password change, token refresh, and expired-token usage mark tokens revoked; `RefreshTokenCleanupJob` runs hourly and deletes rows where `revoked = true` or `expires_at` is in the past.

### 10.2 Patient Admission

```
Admin, Consultant, Registrar, or WardSupervisor creates Patient record
  -> admissionType: EMERGENCY | ELECTIVE | TRANSFER
  -> wardId and medicalTeamId are supplied by the caller
  -> specialtyRequired is stored for clinical routing and reporting

If admittingConsultantId is omitted, the service resolves the on-call consultant:
  WHERE department matches specialtyRequired
  AND role = CONSULTANT_ON_CALL
  AND start_time <= NOW() AND end_time > NOW()
  -> if no match: fall back to General Medicine on-call

Admission ClinicalNote can be created separately by receiving staff
Authorized clinical staff records first PatientVitals -> newsScore computed -> acuityLevel set
Patient appears on ward list ordered by acuityLevel DESC, newsScore DESC

Outbox publishes -> PATIENT_ADMITTED event fires
```

### 10.3 Automatic Shift Creation

```
Quartz ShiftCreationJob runs at configured intervals (every minute)
-> Reads active ShiftSchedule records from database
-> For each schedule matching current day:
    Checks uniqueness: (ward_id, type, start_time) must not already exist
    Creates Shift with status = PENDING_ASSIGNMENT
    leadDoctorId = null, nurseInChargeId = null
    If schedule.wardId is null, creates one shift for every ward in the hospital
    Outbox publishes -> SHIFT_CREATED event

WardSupervisor receives notification -> assigns leadDoctorId + nurseInChargeId
-> PUT /api/v1/shifts/:id/assign
-> Shift status transitions: PENDING_ASSIGNMENT -> ACTIVE
-> Outbox publishes -> SHIFT_ACTIVATED event

Rounds cannot be created against a PENDING_ASSIGNMENT shift.
Shift must be ACTIVE before rounds begin.
```

### 10.4 MedicalTeam Creation and Invite Flow

```
Consultant creates MedicalTeam:
  POST /api/v1/teams
  -> consultantId defaults to authenticated user's ID unless supplied
  -> Must specify departmentId

Consultant sends invite:
  POST /api/v1/teams/:teamId/invites  { invitedUserId }
  Validates: same hospitalId, not already a member, no duplicate PENDING invite
  Creates MedicalTeamInvite (status: PENDING, expiresAt: now + 48h)
  Outbox publishes -> TEAM_INVITE_SENT event

Invited doctor accepts:
  POST /api/v1/teams/invites/:inviteId/accept
  Creates MedicalTeamMember record
  Outbox publishes -> TEAM_MEMBER_ADDED event

Admin or owning Consultant assigns ward:
  POST /api/v1/teams/:teamId/wards { wardId }

Quartz InviteExpiryJob: marks PENDING invites past expiresAt as EXPIRED
  Outbox publishes -> INVITE_EXPIRED event
```

### 10.5 Ward Round

**Round creation:**
```
Consultant or Registrar creates Round:
  POST /api/v1/rounds  { wardId, medicalTeamId, roundType, leadDoctorId, scheduledTime, teamMembers[] }
  
  Validates:
    - An ACTIVE shift must exist for the ward
    - No other round of the same type can be IN_PROGRESS for same wardId + medicalTeamId

  Patient queue generated ordered by:
    1. CRITICAL acuity
    2. HIGH acuity (NEWS ≥ 7)
    3. New admissions since last round
    4. MEDIUM acuity
    5. STABLE / LOW acuity

Round status: SCHEDULED
```

**Conducting the round:**
```
Lead doctor starts round: POST /api/v1/rounds/:roundId/start
  → status: SCHEDULED → IN_PROGRESS

For each patient in queue:
  JuniorDoctor presents (overnight events, vitals, results)
  Consultant examines patient

  PATCH /api/v1/rounds/:roundId/patients/:patientId
  Updates PatientRoundReview:
    clinicalStatus, managementPlan, dischargeAssessment, wasExamined
    newsScoreAtReview (snapshot of current score)

  JuniorDoctor can write a ClinicalNote (ROUND_NOTE) linked to the review
  Post-round CareTasks can be created through POST /api/v1/care-tasks
    with source: POST_ROUND_JOB and roundId set

  If dischargeAssessment = CONFIRMED:
    → Patient.isDischargeReady = true
    → Patient.status = DISCHARGE_READY
    → Auto-creates discharge CareTasks (summary, medications)
    → Outbox publishes → PATIENT_DISCHARGE_READY event

All patients reviewed:
  POST /api/v1/rounds/:roundId/complete
  Round status: IN_PROGRESS → COMPLETED
  Outbox publishes → ROUND_COMPLETED event
```

**Round type reference:**

| Type | Led by | Scope | Documentation |
|---|---|---|---|
| MORNING | Consultant | All team patients | Full ROUND_NOTE per patient, physical exam |
| POST_TAKE | Registrar | New admissions since last full round | ROUND_NOTE |
| BOARD | Registrar / JuniorDoctor | All ward patients (status check only) | Brief — no exam |
| EVENING | Registrar | Deteriorating or post-procedure | PROGRESS_NOTE |
| WEEKEND | On-call Registrar | All patients across specialty wards | Brief, escalate if needed |

### 10.6 Care Task Lifecycle

**Nursing care task:**
```
Nurse creates: POST /api/v1/care-tasks
  source: NURSING_CARE_PLAN
  taskType: free text ("Medication", "Vitals Check", "Wound Dressing", etc.)
  windowStart, windowEnd required
  assignedToRole: NURSE
  assignedToId: system-selected nurse
  status: PENDING

Auto-assignment:
  1. Assign to nurseInChargeId on the patient's ward active shift
  2. If that nurse has an overlapping PENDING or IN_PROGRESS task, assign another active nurse from a same-specialty ward
  3. If every same-specialty nurse has a clash, assign to the ward nurse in charge anyway
     -> workloadConflict = true
     -> Outbox publishes careround.care_task.workload_conflict
     -> WardSupervisor notified

At windowStart: assigned nurse notified
Nurse starts: status → IN_PROGRESS
Nurse completes: status → COMPLETED, completedAt set

If windowEnd passes and status ≠ COMPLETED:
  Quartz TaskOverdueJob fires → TASK_OVERDUE event
  CareTask.escalatedAt set
  Escalation created (triggerType: TASK_OVERDUE)
  WardSupervisor notified
```

**Post-round doctor job:**
```
Created through POST /api/v1/care-tasks with source: POST_ROUND_JOB
  assignedToRole: NURSE for API-created tasks; system-generated discharge jobs can still target JUNIOR_DOCTOR or NURSE
  priority: ROUTINE | URGENT | EMERGENCY

EMERGENCY: if not started within 30 minutes → auto-escalate to Registrar
URGENT: if overdue → escalate to Registrar
ROUTINE: if overdue → flag to WardSupervisor only

Status transitions: PENDING → IN_PROGRESS → COMPLETED (forward only, no reversal)
```

### 10.7 Patient Vitals and NEWS2 Deterioration

```
Nurse records vitals: POST /api/v1/patients/:id/vitals
  { heartRate, respiratoryRate, oxygenSaturation, systolicBP,
    temperature, consciousnessLevel }

NewsScoreService computes score via NHS NEWS2 algorithm:
  heartRate:           ≤40 or ≥131 → 3 | 41-50 or 111-130 → 2 | etc.
  respiratoryRate:     ≤8 or ≥25 → 3 | 9-11 → 1 | 12-20 → 0 | etc.
  oxygenSaturation:    ≤91% → 3 | 92-93% → 2 | 94-95% → 1 | ≥96% → 0
  systolicBP:          ≤90 or ≥220 → 3 | etc.
  temperature:         ≤35.0 or ≥39.1 → 2 | etc.
  consciousnessLevel:  ALERT → 0 | any other → 3

Patient.newsScore updated
Patient.acuityLevel updated:
  0-4 → LOW | 5-6 → MEDIUM | 7+ → HIGH / CRITICAL

If score crosses AMBER threshold (default: 5):
  Escalation created (severity: AMBER)
  Assigned to on-call REGISTRAR_ON_CALL for patient's department

If score crosses RED threshold (default: 7):
  Escalation created (severity: RED)
  Assigned to CONSULTANT_ON_CALL
  Patient.status = DETERIORATING
  Outbox publishes → PATIENT_DETERIORATION event
  NextOfKin with isEmergencyContact=true notified (if consent=true)

Quartz EscalationUnacknowledgedJob:
  If escalation not acknowledged within SystemConfiguration.taskOverdueGraceMinutes:
  → Re-assigns to next seniority level
  → Outbox publishes → ESCALATION_UNACKNOWLEDGED event
```

### 10.8 Discharge

```
During PatientRoundReview (Consultant only):
  dischargeAssessment = CONFIRMED (CONSULTANT role enforced at service layer)

Patient.isDischargeReady = true
Patient.status = DISCHARGE_READY
Outbox publishes → PATIENT_DISCHARGE_READY event

Auto-creates clinical CareTasks (source: POST_ROUND_JOB):
  "Write discharge summary"   → assignedToRole: JUNIOR_DOCTOR
  "Prepare discharge medications" → assignedToRole: NURSE

JuniorDoctor creates DISCHARGE_NOTE ClinicalNote

When all discharge CareTasks are COMPLETED:
  Patient.status = DISCHARGED
  wardId and bedNumber cleared automatically (bed freed)
  Outbox publishes → PATIENT_DISCHARGED event
  NextOfKin notified (if notificationConsent = true)

Blocked discharge cases:
  BLOCKED_SOCIAL: awaiting care package — patient stays, flagged on supervisor dashboard
  BLOCKED_MEDICAL: awaiting test result — same
```

### 10.9 Shift Handover

```
Lead doctor initiates: POST /api/v1/handovers
  { outgoingShiftId, incomingShiftId }
  Validates: outgoing shift status must be ACTIVE
  Handover status: PENDING → IN_PROGRESS

For each patient on the ward:
  POST /api/v1/handovers/:id/patient-notes
  { patientId, statusSummary, outstandingTaskIds[], urgencyFlag }

Incoming shift lead signs off:
  PUT /api/v1/handovers/:id/complete
  Handover status -> COMPLETED
  Outgoing shift -> HANDED_OVER
  Incoming shift is not auto-transitioned by handover completion
  Outstanding tasks carry forward (not auto-closed)
  Outbox publishes -> HANDOVER_COMPLETED event
```

### 10.10 Notification Fan-Out

| Trigger | Event | Content | Condition |
|---|---|---|---|
| Round completed | ROUND_COMPLETED | Confirmation to round lead doctor | leadDoctorId present |
| Shift created | SHIFT_CREATED | Email to ward supervisor | ward.supervisorId present |
| Task overdue | TASK_OVERDUE | SMS to assignee and email to ward supervisor | respective recipient IDs present |
| Care task workload conflict | CARE_TASK_WORKLOAD_CONFLICT | Email to ward supervisor | every same-specialty active nurse already has a clashing task |
| Patient deterioration | PATIENT_DETERIORATION | SMS to assigned doctor and email to ward supervisor | respective recipient IDs present |
| Patient discharged | PATIENT_DISCHARGED | NOK discharge notification | notificationConsent=true; preferredContactMethod controls EMAIL/SMS |
| User activation requested | USER_ACTIVATION_REQUESTED | Email activation URL to first hospital admin | onboarding provisioning completed |

---

## 11. Kafka Event Catalogue

All 18 topics use 3 partitions, 1 replica (local dev). All payloads include `hospitalId` and `correlationId`.

| Topic | Published by | Key payload | Consumers |
|---|---|---|---|
| `careround.patient.admitted` | PatientService | patientId, wardId, medicalTeamId | Dashboard, audit |
| `careround.shift.created` | ShiftCreationJob | shiftId, wardId, shiftType, startTime, endTime | Notification (WardSupervisor alert), audit |
| `careround.shift.activated` | ShiftService | shiftId, wardId, leadDoctorId, nurseInChargeId | Notification, audit |
| `careround.round.completed` | RoundService | roundId, wardId, medicalTeamId, shiftId, roundType, leadDoctorId, completedAt | Notification (round lead), audit |
| `careround.handover.completed` | HandoverService | handoverId, wardId, incomingShiftId | Notification, audit |
| `careround.task.overdue` | TaskOverdueJob | taskId, patientId, wardId, assignedToId, title, windowEnd | Notification, audit |
| `careround.patient.deterioration` | PatientVitalsService / EscalationService | patientId, wardId, newsScore, severity, escalationId, assignedToId | Notification, audit |
| `careround.escalation.unacknowledged` | EscalationUnacknowledgedJob | escalationId, patientId, severity | Notification, audit |
| `careround.patient.discharge-ready` | RoundService | patientId, wardId, estimatedDischargeDate | Notification, audit |
| `careround.patient.discharged` | PatientService | patientId, wardId, dischargedAt | Notification (NOK), audit |
| `careround.team.invite-sent` | MedicalTeamService | inviteId, teamId, invitedUserId | Notification, audit |
| `careround.team.member-added` | MedicalTeamService | teamId, userId | Notification, audit |
| `careround.invite.expired` | InviteExpiryJob | inviteId, teamId, invitedUserId | Notification, audit |
| `careround.hospital.onboarding_requested` | HospitalOnboardingService | hospitalId=platform, requestId, hospitalName, contactEmail | Audit |
| `careround.hospital.onboarding_reviewed` | HospitalOnboardingService | hospitalId=platform, requestId, status, reviewedByUserId | Audit |
| `careround.hospital.provisioned` | HospitalOnboardingService | requestId, hospitalId, adminUserId | Audit |
| `careround.user.activation_requested` | HospitalOnboardingService | hospitalId, userId, email, activationUrl | Notification (first admin email), audit |
| `careround.care_task.workload_conflict` | CareTaskService | taskId, wardId, patientId, assignedNurseId, wardSupervisorId, windowStart, windowEnd | Notification (ward supervisor), audit |

### Transactional Outbox Pattern — mandatory

**NEVER publish directly to Kafka from a service method.** Always:

```java
// Inside a @Transactional service method:
outboxService.publish("careround.round.completed", eventPayload, hospitalId);
// → inserts OutboxEvent row with published=false in SAME transaction

// OutboxPollerJob (Quartz, every ~1 second):
// → reads unpublished outbox rows
// → publishes to Kafka
// → marks published=true
```

This guarantees zero event loss. If Kafka is down, events queue in MySQL and are delivered when Kafka recovers.

### Dead Letter Topic (DLT) — careround-notification only

Kafka `DefaultErrorHandler` in NotificationConsumer config: 3 retries with 1-second backoff, then publish to `<topic>.DLT`. `NotificationDltConsumer` persists failed messages to `failed_notifications` with topic, payload, error message, hospitalId, correlationId, and failedAt.

All notifications (successful and failed) are persisted in the `notifications` table:
- **PENDING**: Notification row created before provider call
- **SENT**: Provider stub completed successfully
- **FAILED**: Provider call failed or the circuit breaker was open

Notification fan-out can enrich events by calling careround-core through `HttpCoreLookupClient`. It currently looks up ward supervisors for shift/task/deterioration alerts and consenting next-of-kin contacts for discharge notifications, using `CAREROUND_CORE_BASE_URL` and optional `CAREROUND_SERVICE_ACCOUNT_JWT`.

### Consumer group IDs

```
careround-notification-{eventname}-group  ← dedicated group per notification consumer
careround-notification-dlt                ← DLT inspection consumer
careround-audit-group                     ← AuditEventConsumer
```

Notification consumers are idempotent through `NotificationIdempotencyGuard`, which skips a message when its `correlationId` already has a persisted notification. Audit persists Kafka topic, partition, offset, key, and payload metadata for each consumed event.

---

## 12. API Endpoints

All endpoints are prefixed `/api/v1`. All require JWT Bearer token except public auth endpoints and public onboarding request submission. `hospitalId` is extracted from the JWT for tenant-scoped endpoints. Platform operator JWTs carry `PLATFORM_ADMIN` and are not tenant-scoped.

### Authentication — careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/auth/login` | Public |
| POST | `/auth/refresh` | Public |
| POST | `/auth/activate-account` | Public activation token |
| POST | `/auth/forgot-password` | Public |
| POST | `/auth/reset-password` | Public reset token |
| POST | `/auth/logout` | Authenticated |
| POST | `/auth/change-password` | Authenticated |

### Platform Authentication - careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/platform/auth/login` | Public; authenticates `platform_operator` |

### Hospital Onboarding - careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/onboarding/hospital-requests` | Public |
| GET | `/onboarding/hospital-requests` | PLATFORM_ADMIN |
| GET | `/onboarding/hospital-requests/:id` | PLATFORM_ADMIN |
| PUT | `/onboarding/hospital-requests/:id/review` | PLATFORM_ADMIN |
| POST | `/onboarding/hospital-requests/:id/provision` | PLATFORM_ADMIN |

### Hospital and Configuration — careround-core

| Method | Endpoint | Access |
|---|---|---|
| GET | `/hospitals` | PLATFORM_ADMIN |
| GET | `/hospitals/me` | Authenticated |
| PUT | `/hospitals/me` | ADMIN |
| GET | `/system-config` | ADMIN |
| PUT | `/system-config` | ADMIN |

### Department and Ward Management — careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/departments` | ADMIN |
| GET | `/departments` | Authenticated |
| GET | `/departments/:id` | Authenticated |
| PUT | `/departments/:id` | ADMIN |
| DELETE | `/departments/:id` | ADMIN |
| POST | `/wards` | ADMIN |
| GET | `/wards` | Authenticated |
| GET | `/wards/:id` | Authenticated |
| PUT | `/wards/:id` | ADMIN, WARD_SUPERVISOR |
| DELETE | `/wards/:id` | ADMIN |

### Dashboards — careround-core

Role dashboards expose operational summaries for the authenticated tenant: active patients, open escalations, open and overdue tasks, active shifts, rounds in progress, and role-specific counts.

| Method | Endpoint | Access |
|---|---|---|
| GET | `/dashboard/me` | Authenticated current-role dashboard |
| GET | `/dashboard/admin` | ADMIN |
| GET | `/dashboard/consultant` | CONSULTANT |
| GET | `/dashboard/doctor` | CONSULTANT, REGISTRAR, JUNIOR_DOCTOR |
| GET | `/dashboard/nurse` | NURSE |
| GET | `/dashboard/ward-supervisor` | WARD_SUPERVISOR |

### Staff Management — careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/users` | ADMIN |
| GET | `/users` | ADMIN |
| GET | `/users/me` | Authenticated |
| GET | `/users/:id` | ADMIN |
| PUT | `/users/:id/deactivate` | ADMIN |

### Medical Team Management — careround-core

Medical-team endpoints use `/teams` in the implemented API.

| Method | Endpoint | Access |
|---|---|---|
| POST | `/teams` | ADMIN, CONSULTANT |
| GET | `/teams` | Authenticated |
| GET | `/teams/:id` | Authenticated tenant user |
| POST | `/teams/:teamId/wards` | ADMIN, owning CONSULTANT |
| DELETE | `/teams/:teamId/wards/:wardId` | ADMIN, owning CONSULTANT |
| POST | `/teams/:teamId/invites` | Owning CONSULTANT |
| DELETE | `/teams/:teamId/members/:userId` | ADMIN, owning CONSULTANT |
| GET | `/teams/invites/pending` | Authenticated invited user |
| POST | `/teams/invites/:inviteId/accept` | Invited user |
| POST | `/teams/invites/:inviteId/decline` | Invited user |

### Patient Management — careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/patients` | ADMIN, CONSULTANT, REGISTRAR, WARD_SUPERVISOR |
| GET | `/patients/:patientId` | Authenticated clinical tenant user |
| GET | `/patients/ward/:wardId` | Authenticated clinical tenant user |
| GET | `/patients/search?q=...` | Authenticated clinical tenant user |
| PATCH | `/patients/:patientId/discharge-ready` | CONSULTANT |
| PATCH | `/patients/:patientId/status` | CONSULTANT, WARD_SUPERVISOR |

### Next-of-Kin — careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/patients/:patientId/next-of-kin` | ADMIN, NURSE, WARD_SUPERVISOR, CONSULTANT, REGISTRAR |
| GET | `/patients/:patientId/next-of-kin` | Authenticated clinical tenant user |
| PUT | `/patients/:patientId/next-of-kin/:nokId` | ADMIN, NURSE, WARD_SUPERVISOR, CONSULTANT, REGISTRAR |
| DELETE | `/patients/:patientId/next-of-kin/:nokId` | ADMIN, WARD_SUPERVISOR, CONSULTANT |
| PATCH | `/patients/:patientId/next-of-kin/:nokId/consent` | ADMIN, NURSE, WARD_SUPERVISOR |

### Patient Vitals — careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/patients/:patientId/vitals` | NURSE, JUNIOR_DOCTOR, REGISTRAR, CONSULTANT, WARD_SUPERVISOR |
| GET | `/patients/:patientId/vitals?limit=10` | Authenticated clinical tenant user |
| GET | `/patients/:patientId/vitals/latest` | Authenticated clinical tenant user |

`POST /patients/:patientId/vitals` accepts an optional `note` field. When present, the core service records a linked `PROGRESS_NOTE` with `vitalsId` for traceability.

### On-Call Rotation and Shift Schedules — careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/oncall` | ADMIN |
| GET | `/oncall` | Authenticated |
| GET | `/oncall/:id` | Authenticated |
| GET | `/oncall/current?departmentId=...&role=...` | Authenticated |
| DELETE | `/oncall/:id` | ADMIN |
| POST | `/shift-schedules` | ADMIN |
| GET | `/shift-schedules` | Authenticated |
| GET | `/shift-schedules/:id` | Authenticated |
| PUT | `/shift-schedules/:id/deactivate` | ADMIN |

### Shift and Handover Management — careround-core

| Method | Endpoint | Access |
|---|---|---|
| GET | `/shifts?wardId=...&status=ACTIVE&from=2026-05-13T08:00:00&to=2026-05-13T20:00:00` | Authenticated clinical tenant user |
| PUT | `/shifts/:id/assign` | ADMIN, WARD_SUPERVISOR |
| GET | `/shifts/current/:wardId` | Authenticated clinical tenant user |
| POST | `/handovers` | CONSULTANT, REGISTRAR, NURSE, WARD_SUPERVISOR |
| POST | `/handovers/:handoverId/patient-notes` | CONSULTANT, REGISTRAR, NURSE, WARD_SUPERVISOR |
| POST | `/handovers/:handoverId/complete` | CONSULTANT, REGISTRAR, NURSE, WARD_SUPERVISOR |
| GET | `/handovers/ward/:wardId` | Authenticated clinical tenant user |
| GET | `/handovers/:handoverId/patient-notes` | Authenticated clinical tenant user |

### Ward Rounds — careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/rounds` | CONSULTANT, REGISTRAR |
| POST | `/rounds/:roundId/start` | CONSULTANT, REGISTRAR |
| PATCH | `/rounds/:roundId/patients/:patientId` | CONSULTANT, REGISTRAR, JUNIOR_DOCTOR |
| POST | `/rounds/:roundId/complete` | CONSULTANT, REGISTRAR |
| GET | `/rounds?wardId=...&teamId=...` | Authenticated clinical tenant user |
| GET | `/rounds/:roundId/reviews` | Authenticated clinical tenant user |

### Clinical Notes, Care Tasks, and Escalations — careround-core

| Method | Endpoint | Access |
|---|---|---|
| POST | `/clinical-notes` | CONSULTANT, REGISTRAR, JUNIOR_DOCTOR, NURSE, WARD_SUPERVISOR |
| PATCH | `/clinical-notes/:noteId/amend` | CONSULTANT, REGISTRAR, JUNIOR_DOCTOR, NURSE, WARD_SUPERVISOR |
| GET | `/clinical-notes/patient/:patientId` | Authenticated clinical tenant user |
| POST | `/care-tasks` | CONSULTANT, REGISTRAR, NURSE, WARD_SUPERVISOR |
| PATCH | `/care-tasks/:taskId/assign` | WARD_SUPERVISOR, or NURSE who created the task |
| PATCH | `/care-tasks/:taskId/progress` | NURSE, JUNIOR_DOCTOR, REGISTRAR, WARD_SUPERVISOR |
| PATCH | `/care-tasks/:taskId/complete` | NURSE, JUNIOR_DOCTOR, REGISTRAR, WARD_SUPERVISOR |
| GET | `/care-tasks/ward/:wardId?status=PENDING` | Authenticated clinical tenant user |
| GET | `/care-tasks/patient/:patientId` | Authenticated clinical tenant user |
| POST | `/escalations` | NURSE, JUNIOR_DOCTOR, REGISTRAR, WARD_SUPERVISOR |
| GET | `/escalations/ward/:wardId` | Authenticated clinical tenant user |
| GET | `/escalations/patient/:patientId` | Authenticated clinical tenant user |
| PATCH | `/escalations/:escalationId/acknowledge` | REGISTRAR, CONSULTANT |
| PATCH | `/escalations/:escalationId/resolve` | REGISTRAR, CONSULTANT |

### Notifications, Search, and Reports - careround-core

These endpoints support the frontend shell: notification bell state, global navigation search, and operational chart views. They are tenant-scoped from the authenticated JWT.
Dashboard endpoints include `unreadNotifications` and `recentNotifications` for the authenticated user. These notification items include persisted delivery records from `careround_notification.notifications`, so a user who receives an email/SMS alert also sees the same alert in their role dashboard.

| Method | Endpoint | Access |
|---|---|---|
| GET | `/notifications` | Authenticated clinical tenant user |
| GET | `/notifications/unread-count` | Authenticated clinical tenant user |
| PATCH | `/notifications/:id/read` | Authenticated clinical tenant user |
| PATCH | `/notifications/read-all` | Authenticated clinical tenant user |
| GET | `/search?q=...` | Authenticated tenant user |
| GET | `/reports/task-completion?wardId=...&from=2026-05-01&to=2026-05-13` | Authenticated clinical tenant user |
| GET | `/reports/overdue-tasks?wardId=...&from=2026-05-01&to=2026-05-13` | Authenticated clinical tenant user |
| GET | `/reports/patient-flow?wardId=...&from=2026-05-01&to=2026-05-13` | Authenticated clinical tenant user |
| GET | `/reports/round-history?wardId=...&from=2026-05-01&to=2026-05-13` | Authenticated clinical tenant user |

---

## 13. Business Rules

These rules are enforced at the **service layer**, not the controller layer. Business rule violations throw `BusinessRuleException` (HTTP 422); authorization violations throw `AccessDeniedException`.

1. **Round active uniqueness:** A round cannot be created if another round of the same `roundType` is already `IN_PROGRESS` for the same `wardId + medicalTeamId`.

2. **Round requires active shift:** A Round cannot be created against a Shift with `status = PENDING_ASSIGNMENT`. Shift must be `ACTIVE`.

3. **Handover requires active shift:** A Handover can only be initiated if the outgoing `Shift.status = ACTIVE`.

4. **Discharge confirmation - Consultant only:** Only a user with `role = CONSULTANT` can set `dischargeAssessment = CONFIRMED` during a round review. Any other role attempting this returns `AccessDeniedException`.

5. **CareTask forward-only status:** Task status transitions: `PENDING -> IN_PROGRESS -> COMPLETED` only. Backward transitions are rejected.

6. **CareTask time window required:** API-created care tasks must include both `windowStart` and `windowEnd`, and `windowEnd` must be after `windowStart`.

7. **CareTask automatic nurse assignment:** API-created care tasks are assigned to the patient's active ward `nurseInChargeId`. If that nurse has an overlapping `PENDING` or `IN_PROGRESS` task, the service selects another active nurse from a same-specialty ward. If every same-specialty nurse clashes, the task remains assigned to the ward nurse, `workloadConflict=true`, and `careround.care_task.workload_conflict` is published.

8. **CareTask manual reassignment:** Manual assignment can be performed only by a `WARD_SUPERVISOR` or by the `NURSE` who created the task. Manual reassignment can override conflicts; nurse conflicts are recorded with `workloadConflict=true`.

9. **Patient discharge event:** A patient must be `DISCHARGE_READY` before final discharge. When status is changed to `DISCHARGED`, all care tasks for the patient must already be `COMPLETED`; the service clears `wardId` and `bedNumber`, then publishes `careround.patient.discharged` for notification and audit consumers.

10. **ClinicalNotes are immutable:** ClinicalNote records are never deleted. Amendments create a new version alongside the original with `isAmended = true`.

11. **OutboxService inside transaction:** `OutboxService.publish()` must always be called within an existing `@Transactional` context. It inserts an `OutboxEvent` row and never publishes to Kafka directly.

12. **Vitals update patient:** `Patient.newsScore` and `Patient.acuityLevel` are updated on every `PatientVitals` save, within the same transaction.

13. **Invite same hospital:** A `MedicalTeamInvite` can only be sent to a User within the same `hospitalId`.

14. **No duplicate pending invite:** A user cannot receive a duplicate `PENDING` invite to the same `MedicalTeam`.

15. **Invite ownership:** Only the Consultant whose `userId = MedicalTeam.consultantId` can send invites or remove members from that team. Admins can assign/remove team ward mappings.

16. **ShiftSchedule idempotency:** `ShiftCreationJob` checks for existing Shift records before creating. Unique constraint `(ward_id, type, start_time)` enforces this at the database level. If `ShiftSchedule.wardId` is null, the schedule applies to every ward in the hospital.

17. **Cross-tenant scoping:** `hospitalId` from the authenticated user's JWT must match the `hospitalId` of every entity being accessed. Violations return `AccessDeniedException`.

18. **NEWS thresholds from config:** Alert thresholds are read from `SystemConfiguration` per hospital, never hardcoded.

19. **Dashboard scoping:** Dashboard responses are role-specific summaries scoped to the authenticated user's `hospitalId`; ward-supervisor metrics are further scoped to wards where `ward.supervisorId = userId`.

20. **Cross-module repository access:** The current implementation keeps all repositories inside the modular monolith and uses direct repository access where workflows span bounded contexts. This is an implementation tradeoff; extractable service boundaries should be tightened before splitting modules into separate deployables.

21. **Hospital onboarding gate:** Public users can only create `HospitalOnboardingRequest` records. A live `Hospital`, `SystemConfiguration`, and first tenant `ADMIN` are created only when a `PLATFORM_ADMIN` provisions an `APPROVED` request.

22. **First admin activation:** The provisioned tenant admin starts inactive. Activation requires a valid, unexpired, unused token, sets the admin password, marks the token used, and then requires normal login through `/api/v1/auth/login`.

23. **Platform auth isolation:** Platform operators are stored in `platform_operator`, not `users`. Platform JWTs can access platform-admin endpoints but do not populate tenant `HospitalContextHolder` state.

---

## 14. Coding Standards

### Dependency injection

```java
// Always use constructor injection via Lombok
@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final OutboxService outboxService;
    // Never use @Autowired
}
```

### Transactions

```java
// All write operations
@Transactional
public PatientResponse admit(AdmitPatientRequest request) { ... }

// All read-only operations
@Transactional(readOnly = true)
public PatientResponse getById(String patientId) { ... }
```

### HospitalContextHolder usage

```java
// Populated by JwtAuthFilter from JWT claims
// Read in service layer — never from request parameters
String hospitalId = HospitalContextHolder.getHospitalId();
String userId = HospitalContextHolder.getUserId();
UserRole role = HospitalContextHolder.getRole();

// Always cleared in JwtAuthFilter finally block
// Thread-local: safe for concurrent requests
```

### Controller response format

```java
// All controllers return ResponseEntity<ApiResponse<T>>
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<PatientResponse>> getPatient(@PathVariable String id) {
    PatientResponse patient = patientService.getById(id);
    return ResponseEntity.ok(ApiResponse.ok(patient));
}
```

### Error response format

```json
{
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Patient not found",
  "path": "/api/v1/patients/abc-123",
  "correlationId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "timestamp": "2025-05-06T08:00:00Z"
}
```

### Validation

```java
// All request bodies use Jakarta Validation
public record AdmitPatientRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull LocalDate dateOfBirth,
    @NotNull AdmissionType admissionType,
    // ...
) {}
```

### DTO style

Most domain modules use Java records for immutable request and response DTOs. The existing auth-style DTO packages use Lombok classes for both requests and responses; new auth-adjacent DTOs should follow that local package convention unless the whole package is refactored together.

### Structured logging

```java
// Every significant action logs with MDC fields
log.info("action=ROUND_COMPLETED roundId={} wardId={} patientCount={} durationMs={}",
    roundId, wardId, patientCount, duration);
// MDC is populated by CorrelationIdFilter with correlationId, hospitalId, userId
```

### Micrometer custom counters

```java
// Example: escalation created
Counter.builder("careround.escalation.created")
    .tag("severity", severity.name())
    .tag("hospitalId", hospitalId)
    .register(meterRegistry)
    .increment();
```

### Kafka consumer idempotency

```java
@KafkaListener(topics = "careround.round.completed", groupId = "careround-notification")
@Transactional
public void onRoundCompleted(RoundCompletedEvent event) {
    // Check for duplicate processing
    if (notificationRepository.existsByCorrelationId(event.correlationId())) return;
    // Process...
}
```

---

## 15. Local Environment

### Prerequisites

```
Java 21 JDK (Temurin)     — https://adoptium.net
Maven 3.9+
Docker Desktop
IntelliJ IDEA Community
Git
```

### Docker Compose infrastructure

```bash
# Start all infrastructure services
docker compose up -d

# Services started:
#   MySQL 8       → localhost:3306
#   Redis 7       → localhost:6379
#   Kafka 3.7     → localhost:9094 (KRaft, no Zookeeper)
#   Kafka UI      → http://localhost:8090
#   Prometheus    → http://localhost:9090
#   Grafana       → http://localhost:3001 (admin / from .env)
```

### .env file

```env
MYSQL_ROOT_PASSWORD=careround_root
MYSQL_USER=careround
MYSQL_PASSWORD=careround_password
JWT_SECRET=<256-bit hex string — generate with: openssl rand -hex 32>
REDIS_HOST=localhost
REDIS_PORT=6379
KAFKA_BOOTSTRAP_SERVERS=localhost:9094
GRAFANA_PASSWORD=admin
CAREROUND_CORE_BASE_URL=http://localhost:8080
CAREROUND_APP_ACTIVATION_BASE_URL=http://localhost:3000/activate
CAREROUND_PLATFORM_BOOTSTRAP_ADMIN_EMAIL=platform-admin@careround.local
CAREROUND_PLATFORM_BOOTSTRAP_ADMIN_PASSWORD=<set only for first startup, then remove>
CAREROUND_SERVICE_ACCOUNT_JWT=<JWT used by notification service for core lookups>
```

### Running the applications

```bash
# Build from monorepo root
mvn clean install -DskipTests

# Run each service (in separate terminals)
cd careround-core && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd careround-notification && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd careround-audit && mvn spring-boot:run -Dspring-boot.run.profiles=dev

```

### IntelliJ setup

1. File → Open → select `careround/` root (imports all modules via parent POM)
2. File → Project Structure → SDK → Java 21
3. Settings → Build → Compiler → Annotation Processors → **Enable annotation processing** (required for Lombok + MapStruct)
4. Create Run Configurations (Spring Boot) for each service with `Active profiles: dev`
5. Add `.env` file contents as environment variables in each Run Configuration

### API documentation

Swagger UI is served by `careround-core` only:

```
http://localhost:8080/swagger-ui.html
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/swagger-ui
http://localhost:8080/docs
```

OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

`careround-notification` and `careround-audit` are Kafka consumer services and do not expose Swagger UI.

### Port allocation

```
3001    Grafana
3306    MySQL
6379    Redis
8080    careround-core
8081    careround-notification
8082    careround-audit
8090    Kafka UI
9090    Prometheus
9094    Kafka (external listener)
```

### Flyway migrations — careround-core

```
V1__create_users.sql
V2__create_hospital_config.sql
V3__create_department_ward.sql
V4__create_medical_team.sql
V5__create_on_call_shift_schedule.sql
V6__create_shift_handover.sql
V7__create_patient.sql
V8__create_vitals_nok.sql
V9__create_escalation.sql
V10__create_round_review.sql
V11__create_clinical_note_care_task.sql
V12__create_outbox.sql
V13__create_quartz_tables.sql   ← full QRTZ_* schema for MySQL
V14__create_indexes.sql         ← all composite indexes
V15__allow_discharged_patients_without_ward.sql
V16__create_onboarding_platform_activation.sql
V17__add_care_task_workload_conflict.sql
V18__add_refresh_token_cleanup_indexes.sql
```

---

## 16. Production Concerns

### Composite indexes (V14)

```sql
-- Round queue generation (most critical read path)
CREATE INDEX idx_patient_ward_acuity
  ON patient(hospital_id, ward_id, acuity_level, news_score);

-- Overdue task detection (Quartz job, every minute)
CREATE INDEX idx_care_task_overdue
  ON care_task(hospital_id, ward_id, status, window_end);

-- Outbox poller (every second)
CREATE INDEX idx_outbox_unpublished
  ON outbox_event(published, created_at);

-- Refresh token cleanup (hourly)
CREATE INDEX idx_refresh_tokens_expires_at
  ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked
  ON refresh_tokens(revoked);

-- On-call rotation lookup (every admission + escalation)
CREATE INDEX idx_on_call_dept_time
  ON on_call_rotation(department_id, start_time, end_time);

-- Current shift lookup
CREATE INDEX idx_shift_ward_status
  ON shift(ward_id, type, status, start_time);

-- Round status check
CREATE INDEX idx_round_ward_team_type
  ON round(ward_id, medical_team_id, round_type, status);

-- Vitals history
CREATE INDEX idx_vitals_patient_time
  ON patient_vitals(patient_id, recorded_at);
```

### Pagination

All list endpoints must use cursor-based pagination:
```
GET /api/v1/patients?wardId=X&limit=20&cursor=<encoded>
```
Never return unbounded lists. A ward with years of round history must not return thousands of rows.

### Graceful shutdown

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

### Circuit breakers (careround-notification)

```java
@CircuitBreaker(name = "emailProvider", fallbackMethod = "handleEmailFailure")
public void sendEmail(String to, String subject, String body) { ... }
```

Apply to all external provider calls (email, SMS). Configure: 5 consecutive failures → open for 30 seconds → allow one test call.

### Quartz JDBC clustering (careround-core)

Already covered in Section 5. Critical: `QRTZ_LOCKS` table provides distributed locking. No ShedLock needed. All registered Quartz jobs fire on exactly one instance across any number of horizontal cores.

### Redis rate limiting

```java
// In RateLimitingFilter
// Per-hospital sliding window: max 1000 requests/minute per hospitalId
// Per-user: max 100 requests/minute per userId
// Uses Redis INCR + EXPIRE commands
```

### Correlation IDs

Every request gets a `X-Correlation-Id` header (generated by `CorrelationIdFilter` if not present). Stored in MDC for Logback. Included in all Kafka event payloads. Flows from HTTP request → service method → Kafka event → notification/audit consumers → log entries.

### Horizontal scaling

careround-core scales horizontally with zero state changes because:
- JWT is stateless — each instance validates independently
- Redis is shared — rate limit counters are consistent across instances
- MySQL is shared — all instances see the same data
- Quartz JDBC clustering — jobs fire on exactly one instance

Run multiple instances behind a load balancer (round-robin, no sticky sessions needed).
