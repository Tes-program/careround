# CareRound — Digital Ward Management Platform

> **Related repositories:**
> - AI Service (careround-ai): _[https://github.com/Dave-n-tech/careround_ai]_
> - Web Frontend (careround-web): _[https://github.com/Dave-n-tech/careround_frontend]_
> - Mobile App (careround-mobile): _[https://github.com/Tes-program/careround_mobile]_

---

## Contents

**For product and clinical readers**
1. [What CareRound Is](#1-what-careround-is)
2. [The Problem It Solves](#2-the-problem-it-solves)
3. [Key Features](#3-key-features)
4. [User Roles](#4-user-roles)
5. [Core Workflows](#5-core-workflows)

**For engineering readers**
6. [Architecture](#6-architecture)
7. [Tech Stack](#7-tech-stack)
8. [Services](#8-services)
9. [Database](#9-database)
10. [API Reference](#10-api-reference)
11. [Kafka Event Catalogue](#11-kafka-event-catalogue)
12. [Security](#12-security)
13. [Local Development](#13-local-development)
14. [Testing](#14-testing)
15. [Production Notes](#15-production-notes)

---

## 1. What CareRound Is

CareRound is a **multi-tenant, production-grade digital ward management platform** for hospitals. It fills the operational gap between a hospital's electronic medical records (EMR) system and the real-time coordination happening on a ward during a shift.

Where an EMR records what happened to a patient across their clinical history, CareRound manages what needs to happen *right now*: capturing a ward round note while the doctor is still at the bedside, generating the medication schedule from that note automatically, alerting the nurse when a dose window is approaching, and giving the ward supervisor live visibility of the shift.

The platform is built around two core innovations:

1. **AI-powered voice documentation** — a doctor dictates during a consultation and receives a structured SOAP note plus extracted prescriptions, streamed back in real time, ready for review and confirmation.
2. **Automated medication task generation** — the moment a prescription is confirmed, the complete medication schedule is generated and pushed to the assigned nurse as an ordered task list, with push notification reminders.

---

## 2. The Problem It Solves

Ward coordination currently runs on verbal instructions, paper notes, and manual checking. The cost is measurable:

- **60%** of hospital adverse events involve a communication or coordination failure _(Joint Commission)_
- **70%** of deaths from medical error are linked to breakdown at shift handover _(AHRQ)_
- **15%** of total hospital expenditure results from preventable adverse events _(OECD)_

**For doctors:** Writing structured clinical notes during a ward round consumes 30–40% of round time. Documentation competes directly with examination.

**For nurses:** Medication charts are monitored manually. There is no automatic alert when a dose window is missed, and no systemic way to see what the next overdue task is without actively checking.

**For supervisors:** There is no real-time visibility of task completion across the ward until something has already gone wrong.

---

## 3. Key Features

### AI Voice Documentation
- Doctor records audio on mobile or web during a consultation
- The AI service transcribes the audio and structures it as a SOAP-format clinical note, extracting prescriptions with drug, dose, route, frequency, and calculated administration times
- Results stream back progressively via SSE — the transcription appears as soon as Whisper finishes; the structured note and prescriptions follow from the LLM
- Doctor reviews on-screen, edits any field, and confirms — nothing is saved until confirmed
- Both ward-round mode (note + prescriptions) and transcription-only mode (for nurse handover notes) are supported

### Automated Medication Task Chain
- Doctor confirms a prescription → medication chart entry created automatically (async, via Kafka)
- Chart entry → one medication task per scheduled administration time, assigned to the ward nurse
- Nurse receives a sorted task list: **OVERDUE** (red, oldest first), **DUE SOON** (amber, within 30 min), **UPCOMING** (grouped by hour), then **COMPLETED TODAY**
- 5-minute pre-task push notification: _"Amoxicillin 500mg for Bed 4 due in 5 minutes"_
- 5-minute overdue push notification: _"Amoxicillin 500mg for Bed 4 is now 5 minutes overdue"_
- Task completion records the nurse's name, timestamp, and actual dose given; the medication chart is updated automatically

### Live Supervisor Dashboard
- Per-ward view: total patients, task completion fraction, overdue count, completion rate
- Overdue alert panel surfacing patient name, bed, drug, dose, and minutes overdue
- Patient grid with acuity colour, active medications, and task summary per patient
- Polls every 10 seconds with silent background refresh

### Patient Acuity with VHI Scoring
- Nurses record six vital signs: pulse, systolic BP, respiratory rate, temperature, SpO₂, and diastolic BP (stored but not scored)
- The system computes a **Vitals Health Index (VHI)** score (0–15) from five inputs
- Score 0–2 → **GREEN** (stable), 3–4 → **AMBER** (watch), 5+ → **RED** (critical)
- Patient acuity colour updates immediately on every vitals recording, within the same transaction
- Ward lists and supervisor dashboard are always sorted: RED → AMBER → GREEN → oldest-seen first

### Clinical Documentation
- Clinical notes (SOAP, progress notes, handover notes, nursing reports) are immutable once saved
- Amendments create a side-by-side record; the original is never overwritten or deleted
- AI-generated notes store the raw transcription and a flag for audit purposes

### Multi-Hospital Platform
- Each hospital is an independent tenant; all data is scoped by `hospitalId`
- Cross-tenant access attempts return 404, never 403 (no confirmation that a resource exists)
- Hospital-specific configuration: task overdue windows, push notification toggles
- Two onboarding paths: direct self-registration (`POST /hospitals/register`) or the full platform-admin provisioning flow with review and approval

### Full Audit Trail
- All 16 event types are consumed by `careround-audit` and written to an immutable, append-only schema in a separate database
- Audit writes never block clinical operations — they are event-driven via Kafka
- Each audit record stores event type, hospitalId, correlationId, full payload, and received timestamp

---

## 4. User Roles

| Role | Who they are | Key capabilities |
|---|---|---|
| **ADMIN** | Hospital account administrator | Configure hospital and wards; create and manage user accounts; no access to patient records |
| **DOCTOR** | Attending physicians, registrars, residents | All clinical authority: ward rounds, clinical notes, prescriptions, discharge, AI voice notes |
| **NURSE** | Ward nurses | Record vitals, complete medication tasks, add handover notes, view patient records |
| **SUPERVISOR** | Ward manager / charge nurse | Live ward dashboard, task oversight, no clinical note creation |
| **PLATFORM_ADMIN** | Internal CareRound operators | Review and provision hospital onboarding requests |

**Role-based access summary:**

| Action | ADMIN | DOCTOR | NURSE | SUPERVISOR |
|---|:---:|:---:|:---:|:---:|
| Register/configure hospital | ✓ | | | |
| Manage wards and users | ✓ | | | |
| Admit patient (demographic intake) | ✓ | | | |
| Update patient clinical details | | ✓ | | |
| Discharge patient | | ✓ | | |
| Record AI voice note | | ✓ | ✓ | |
| Confirm note + prescription | | ✓ | | |
| Record vitals | | ✓ | ✓ | |
| Complete medication tasks | | | ✓ | |
| Edit medication chart | | ✓ | ✓ | |
| Add handover note | | ✓ | ✓ | |
| View patient records | | ✓ | ✓ | ✓ (read) |
| Supervisor dashboard | | | | ✓ |

---

## 5. Core Workflows

### Hospital Onboarding
A hospital representative submits a public onboarding request. A `PLATFORM_ADMIN` reviews, approves, and provisions the tenant. Provisioning creates the hospital record, default system configuration, and a first `ADMIN` user account in inactive state. A single-use activation link is generated; the admin sets their password and logs in normally.

Alternatively, hospitals can self-register instantly via `POST /api/v1/hospitals/register` without platform review — suitable for demo and development.

### Patient Admission
An `ADMIN` registers the patient's demographic details, ward assignment, and bed number. Clinical staff then create the first vitals record, which triggers VHI computation and sets the initial acuity colour. The patient appears on the ward list ordered by acuity.

### AI Ward Round
1. Doctor taps **Record** on mobile or web and begins dictating during the consultation
2. Audio is sent to `POST /api/v1/ai/process-voice-note` — core proxies it to the AI service
3. SSE events arrive progressively: `transcription_complete` (Whisper finished), then `processing_complete` (SOAP note + prescriptions)
4. Doctor reviews, edits any field, and taps **Confirm**
5. `POST /api/v1/patients/{id}/notes/confirm` (or `/api/v1/clinical-notes/confirm`) saves the note and all prescriptions atomically
6. `prescription-confirmed` event → Kafka → `PrescriptionConfirmedConsumer` → creates MedicationChart
7. `medication-chart-created` event → Kafka → `MedicationChartCreatedConsumer` → creates one MedicationTask per administration time

### Nurse Task Flow
The nurse's task list is always sorted by urgency. Overdue tasks appear at the top in red with the minutes overdue. The nurse taps **Complete**, optionally records if the actual dose differed from prescribed, and the task closes. The medication chart chip for that dose updates instantly.

Push reminders fire 5 minutes before and 5 minutes after each scheduled administration time, delivered via FCM to the nurse's mobile device.

### Shift Handover
A shift lead initiates a handover by linking the outgoing and incoming shift. For each patient a handover note is added, flagging outstanding tasks and urgency. The incoming shift lead signs off; the outgoing shift transitions to `HANDED_OVER`.

### Patient Discharge
A `DOCTOR` updates patient status to `DISCHARGED`. The ward ID and bed number are cleared, freeing the bed. A `patient-discharged` event triggers audit logging and any next-of-kin notification configured on the patient record.

---

## 6. Architecture

CareRound is a **modular monolith** for the clinical core with two lightweight event-driven satellite services and one dedicated AI service in a separate repository.

```
Clients (Web + Mobile)
          │ HTTPS
          ▼
  careround-core  ─────────────────────────────────────────────────
  (Spring Boot 4, port 8080)                                       │
  ├── Auth Module                                                   │
  ├── Hospital Module (hospital, ward, config, onboarding)         │ REST
  ├── Patient Module (patients, vitals, notes, prescriptions,      │ (sync)
  │   medication charts + tasks, handover notes)                   │
  └── Scheduler Module (Quartz JDBC — 4 jobs)                      ▼
          │                                              careround-ai
          │ Transactional Outbox → Kafka                 (Python/FastAPI, port 8000)
          ▼                                              ├── faster-whisper
      Apache Kafka                                       └── Ollama / vLLM LLM
          │
          ├──→ careround-notification  (Spring Boot, port 8081)
          │       Kafka consumers: medication-task-reminder, medication-task-overdue
          │       Sends FCM push notifications to nurse devices
          │       Dead-letter handling with exponential backoff
          │
          └──→ careround-audit  (Spring Boot, port 8082)
                  Kafka consumer: all 16 topics
                  Append-only audit log (separate DB schema)
```

### Why a Modular Monolith

The clinical workflows — prescriptions, charts, tasks, notes, vitals — require transactional integrity across entities. A doctor confirming a note must atomically save the note, all prescriptions, and an outbox event in a single database transaction. Splitting those across network-separated services would require a saga orchestrator with distributed compensation logic, adding significant complexity for zero operational benefit at this scale.

The module boundaries are enforced in code: domain modules do not import repositories from other modules. This makes them extractable to separate services in 2–3 days if scale demands it.

### Why AI and Notifications Are Separate

- `careround-ai` runs a different runtime (Python), different hardware (GPU), and has no business logic coupling to the core. It processes audio and returns structured drafts — all clinical decisions remain with the doctor.
- `careround-notification` makes outbound calls to Firebase Cloud Messaging, which can throttle and fail. Isolating FCM errors ensures a slow FCM call never affects a prescription confirmation.
- `careround-audit` writes compliance records independently. Audit writes must never block clinical operations.

### Transactional Outbox Pattern

Events are never published directly to Kafka. The flow is:

1. A service method calls `OutboxService.publish(eventType, payload, hospitalId)` inside an existing `@Transactional` context — this writes a row to `outbox_event` in the same DB transaction.
2. `OutboxPollerJob` (Quartz, every 1 second) reads unpublished rows and sends them to Kafka.
3. The row is marked `published = true`.

This guarantees zero event loss. If Kafka is unavailable, events queue in MySQL and are delivered when Kafka recovers.

### Correlation IDs

Every HTTP request receives a `X-Correlation-Id` header (generated by `CorrelationIdFilter` if not present). It is stored in MDC, included in all Kafka event payloads, and flows through to notification and audit consumers. Every log line from every service carries the same `correlationId` for a given clinical action.

---

## 7. Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security 6 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8.0 |
| Schema migrations | Flyway |
| Connection pool | HikariCP |
| Messaging | Apache Kafka 3.7 (KRaft, no Zookeeper) |
| Scheduled jobs | Quartz Scheduler (JDBC clustered) |
| Cache / rate limiting | Redis 7 |
| Auth | JWT (jjwt 0.12.5, HS256, 15-min access / 7-day refresh) |
| Push notifications | Firebase Cloud Messaging (FCM) |
| Metrics | Micrometer + Prometheus |
| Dashboards | Grafana |
| Logging | Logback with logstash-logback-encoder (structured JSON) |
| API docs | SpringDoc OpenAPI 3 (Swagger UI on careround-core) |
| Build | Maven 3.9+ (multi-module) |
| Boilerplate | Lombok |
| AI runtime | Python 3.12, FastAPI |
| Speech-to-text | faster-whisper |
| LLM | Ollama (dev/demo) or vLLM (production GPU) |
| Containers | Docker / Docker Compose |

---

## 8. Services

### careround-core (port 8080)

The clinical and administrative domain. Owns all business logic, the REST API, JWT issuance, the Transactional Outbox, and internal Kafka consumers for the prescription-to-task async chain.

**Domain modules:**

| Module | Responsibilities |
|---|---|
| `auth` | Login, token refresh, logout, account activation, password change, FCM device token |
| `hospital` | Hospital CRUD, ward management, system configuration, supervisor dashboard |
| `onboarding` | Hospital onboarding request lifecycle, tenant provisioning, activation tokens |
| `patient` | Patient admission, vitals (VHI scoring), clinical notes, prescriptions, medication charts, medication tasks, handover notes |
| `ai` | Proxies audio to careround-ai; streams SSE response back to client via WebFlux |
| `scheduler` | Quartz job registration and processors |
| `shared` | JWT filter, HospitalContextHolder, OutboxService, rate limiting, correlation ID, exception handlers |

**Quartz jobs:**

| Job | Schedule | Purpose |
|---|---|---|
| `OutboxPollerJob` | Every 1 second | Reads unpublished outbox rows, publishes to Kafka |
| `MedicationTaskReminderJob` | Every 1 minute | Two-pass: pre-task reminders (T−5 min) and overdue alerts (T+5 min) |
| `RefreshTokenCleanupJob` | Hourly | Deletes revoked and expired refresh token rows |
| `OutboxCleanupJob` | Daily at 2am | Deletes published outbox events older than 7 days |

**Prescription → Chart → Task async chain:**

```
ClinicalNoteService.confirm()  [single @Transactional]
  → saves ClinicalNote + Prescriptions + outbox event (prescription-confirmed)
       ↓  (Kafka, via OutboxPollerJob)
PrescriptionConfirmedConsumer  [careround-core-internal group]
  → creates MedicationChart + outbox event (medication-chart-created)
       ↓  (Kafka)
MedicationChartCreatedConsumer [careround-core-internal group]
  → creates one MedicationTask per administrationTime in Prescription.administrationTimes[]
```

Both consumers check `processedEventRepository.existsById(eventId)` before acting (idempotency).

**Security filter chain order:**

`CorrelationIdFilter` → `RateLimitingFilter` → `JwtAuthFilter` → `ApiRequestLoggingFilter`

Rate limiting is Redis-backed, sliding window per `hospitalId:userId`. `JwtAuthFilter` sets `hospitalId`, `userId`, and `role` in `HospitalContextHolder` (a `ThreadLocal`).

### careround-notification (port 8081)

A pure Kafka consumer service with no HTTP API surface. Listens to `medication-task-reminder` and `medication-task-overdue` topics and delivers FCM push notifications to nurse devices.

- **Error handling:** `DefaultErrorHandler` with `ExponentialBackOffWithMaxRetries(3)` at 1s / 2s / 4s backoff
- **Dead-letter handling:** After 3 failures, `DeadLetterPublishingRecoverer` routes to `<topic>.DLT`; `NotificationDltConsumer` writes failed messages to `failed_notifications`
- **FCM:** Firebase Admin SDK; device tokens registered by nurses via `PUT /api/v1/users/me/device-token` on careround-core

### careround-audit (port 8082)

A pure Kafka consumer service. `AuditEventConsumer` subscribes to all 16 event topics and writes every consumed event as an immutable row to `audit_log`. Before writing, checks `existsByEventId()` to handle Kafka redelivery idempotently. No updates or deletes ever occur in this schema.

### careround-ai (separate repository)

Python 3.12 / FastAPI service. Accepts a multipart audio upload and streams results back as Server-Sent Events. Has no database, emits no Kafka events, makes no clinical decisions.

**Modes:**
- `ward_round`: full pipeline — Whisper transcription → LLM SOAP structuring + prescription extraction → administration time calculation
- `transcription_only`: Whisper transcription only (for nurse handover notes)

**Provider configuration:**

| Variable | Value | Effect |
|---|---|---|
| `AI_PROVIDER=stub` | Dev/CI | Bypasses all models; returns deterministic fixture output immediately |
| `AI_PROVIDER=ollama` | Local/demo | Real LLM via Ollama (llama3.2:3b or mistral:7b) |
| `TRANSCRIPTION_PROVIDER=stub` | Dev/CI | Bypasses Whisper; returns fixed transcription string |
| `TRANSCRIPTION_PROVIDER=whisper` | Real | faster-whisper (base.en for dev, large-v3 for production) |

The service exposes `GET /health` returning `{ "status": "loading" | "ready" }`. careround-core checks this before forwarding requests and returns `503` to clients while the service is initialising.

---

## 9. Database

Three separate MySQL schemas. Cross-schema SQL joins never occur.

```
MySQL 8 (single server, port 3306)
├── careround_core          ← all domain tables + QRTZ_* + outbox_event
├── careround_notification  ← failed_notifications
└── careround_audit         ← audit_log (append-only)
```

### Key entities (careround_core)

**Patient** — Full demographic record: first/last name, date of birth, gender, hospital number, phone, address, previous conditions, current medications, allergies, emergency contact, ward, bed, admission type/date, primary diagnosis, acuity colour, status (ADMITTED / DISCHARGED).

**PatientVitals** — Six measurements per recording: pulse, systolic BP, diastolic BP, respiratory rate, temperature, SpO₂. System computes `vhiScore` (0–15) and `vhiStatus` (STABLE / WATCH / CRITICAL) at save time. Patient acuity colour is updated in the same transaction.

**VHI Scoring Table:**

| Measurement | 0 pts (Normal) | 1 pt (Mild) | 2 pts (Moderate) | 3 pts (Severe) |
|---|---|---|---|---|
| Pulse | 61–100 bpm | 51–60 or 101–110 | 41–50 or 111–129 | ≤40 or ≥130 |
| Systolic BP | 101–159 mmHg | 91–100 or 160–199 | 81–90 or ≥200 | ≤80 |
| Respiratory Rate | 9–14 /min | 15–20 | 21–29 | ≤8 or ≥30 |
| Temperature | 36.1–37.4 °C | 35.1–36.0 or 37.5–38.4 | 38.5–38.9 | ≤35.0 or ≥39.0 |
| SpO₂ | 96–100% | 94–95% | 92–93% | ≤91% |

Score 0–2 → GREEN, 3–4 → AMBER, 5+ → RED.

**ClinicalNote** — Immutable. Stores note type, content (SOAP as structured fields for AI-generated notes, free text otherwise), raw transcription, `isAiGenerated` flag, and `confirmedByDoctorAt`. Never deleted; amendments are new records with `isAmended = true`.

**Prescription** — Drug, dose, route, frequency, total doses, start time, and administration times (stored as JSON array of ISO 8601 datetimes). Links to clinical note. Status: ACTIVE / DISCONTINUED / COMPLETED.

**MedicationChart** — One entry per prescription. Status: ACTIVE / COMPLETED / DISCONTINUED. Holds nurse notes.

**MedicationTask** — One task per administration time. Stores `scheduledTime`, `status` (PENDING / COMPLETED / OVERDUE), `completedAt`, `completedById`, `actualDoseGiven`, `preReminderSentAt`, `overdueAlertSentAt`. Assigned to a nurse at creation time.

**OutboxEvent** — `published`, `publishedAt`, `eventType`, `payload` (JSON), `correlationId`, `hospitalId`.

### Flyway migrations (careround_core)

| Version | Contents |
|---|---|
| V1 | hospital, system_configuration, users, refresh_tokens |
| V2 | ward, patient (with full demographic fields) |
| V3 | patient_vitals (VHI scoring fields), clinical_note |
| V4 | outbox_event, processed_event, full QRTZ_* table set |
| V5 | prescription, medication_chart, medication_task |
| V6 | handover_note |
| V7 | Composite performance indexes |
| V8 | Additional patient demographic fields |
| V9 | hospital_onboarding_request, activation_token |
| V10 | ward_id column on users (nurse-to-ward assignment) |

---

## 10. API Reference

All endpoints are prefixed `/api/v1`. All require a JWT Bearer token except where noted as Public. `hospitalId` is always read from the JWT — never from the request body.

**Swagger UI (careround-core only):** `http://localhost:8080/swagger-ui.html`

### Authentication

| Method | Path | Access |
|---|---|---|
| POST | `/auth/login` | Public |
| POST | `/auth/refresh` | Public |
| POST | `/auth/logout` | Authenticated |
| POST | `/auth/activate-account` | Public (activation token) |
| POST | `/auth/change-password` | Authenticated |

### Users

| Method | Path | Access |
|---|---|---|
| POST | `/users` | ADMIN |
| GET | `/users` | Any tenant user |
| GET | `/users/me` | Authenticated |
| PUT | `/users/me` | Authenticated (own profile) |
| GET | `/users/:id` | ADMIN |
| PUT | `/users/:id` | ADMIN |
| PUT | `/users/:id/deactivate` | ADMIN |
| PUT | `/users/:id/reactivate` | ADMIN |
| PUT | `/users/:id/ward-assignment` | ADMIN |
| PUT | `/users/me/device-token` | Any authenticated user |

### Hospitals

| Method | Path | Access |
|---|---|---|
| POST | `/hospitals/register` | Public (direct registration) |
| GET | `/hospitals` | ADMIN |
| GET | `/hospitals/me` | Authenticated |
| PUT | `/hospitals/me` | ADMIN |
| GET | `/system-config` | ADMIN |
| PUT | `/system-config` | ADMIN |

### Hospital Onboarding (Platform Admin Flow)

| Method | Path | Access |
|---|---|---|
| POST | `/onboarding/hospital-requests` | Public |
| GET | `/onboarding/hospital-requests` | PLATFORM_ADMIN |
| GET | `/onboarding/hospital-requests/:id` | PLATFORM_ADMIN |
| PUT | `/onboarding/hospital-requests/:id/review` | PLATFORM_ADMIN |
| POST | `/onboarding/hospital-requests/:id/provision` | PLATFORM_ADMIN |

### Wards

| Method | Path | Access |
|---|---|---|
| POST | `/wards` | ADMIN |
| GET | `/wards` | Any tenant user |
| GET | `/wards/:id` | Any tenant user |
| PUT | `/wards/:id` | ADMIN |
| DELETE | `/wards/:id` | ADMIN |

### Patients

| Method | Path | Access |
|---|---|---|
| POST | `/patients` | ADMIN |
| GET | `/patients` | Any tenant user |
| GET | `/patients/:id` | Any tenant user |
| GET | `/patients/ward/:wardId` | Any tenant user |
| PUT | `/patients/:id` | ADMIN |
| PATCH | `/patients/:id/status` | ADMIN, DOCTOR |
| POST | `/patients/:id/notes/confirm` | DOCTOR |

### Vitals

| Method | Path | Access |
|---|---|---|
| POST | `/patients/:id/vitals` | NURSE, DOCTOR |
| PUT | `/patients/:id/vitals/:vitalsId` | NURSE, DOCTOR |
| GET | `/patients/:id/vitals?limit=10` | Any tenant user |
| GET | `/patients/:id/vitals/latest` | Any tenant user |

### Clinical Notes

| Method | Path | Access |
|---|---|---|
| POST | `/clinical-notes` | DOCTOR, NURSE |
| POST | `/clinical-notes/confirm` | DOCTOR |
| GET | `/clinical-notes/patient/:patientId` | Any tenant user |

### Prescriptions

| Method | Path | Access |
|---|---|---|
| GET | `/patients/:patientId/prescriptions` | Any tenant user |
| PUT | `/prescriptions/:id/discontinue` | DOCTOR, NURSE |

### Medication Charts

| Method | Path | Access |
|---|---|---|
| GET | `/patients/:id/medication-chart` | Any tenant user |
| PUT | `/medication-charts/:id` | NURSE, DOCTOR |
| POST | `/medication-charts/:patientId/manual` | NURSE, DOCTOR |
| PUT | `/medication-charts/:id/discontinue` | NURSE, DOCTOR |

### Medication Tasks

| Method | Path | Access |
|---|---|---|
| GET | `/medication-tasks?wardId=` | NURSE, DOCTOR, SUPERVISOR |
| PUT | `/medication-tasks/:id/complete` | NURSE |

### Handover Notes

| Method | Path | Access |
|---|---|---|
| POST | `/patients/:id/handover-notes` | NURSE, DOCTOR |
| GET | `/patients/:id/handover-notes` | Any tenant user |

### Supervisor Dashboard

| Method | Path | Access |
|---|---|---|
| GET | `/supervisor/dashboard?wardId=` | SUPERVISOR |

### AI Proxy

| Method | Path | Access | Response |
|---|---|---|---|
| POST | `/ai/process-voice-note` | DOCTOR, NURSE | `text/event-stream` (SSE) |

**SSE event sequence:**
```
event: transcription_complete   ← Whisper finished (~30–60s on CPU); empty payload
event: processing_complete      ← LLM finished; payload: rawTranscription + clinicalNote + prescriptions[]
event: done                     ← stream closed normally
event: error                    ← a stage failed; payload: { "detail": "..." }
```

---

## 11. Kafka Event Catalogue

All topics use 3 partitions, 1 replica. All payloads include `eventId`, `hospitalId`, `correlationId`, and `timestamp`.

| Topic | Produced by | Consumed by |
|---|---|---|
| `prescription-confirmed` | ClinicalNoteService | careround-core (internal chain), careround-audit |
| `prescription-discontinued` | PrescriptionService | careround-audit |
| `medication-chart-created` | PrescriptionConfirmedConsumer | careround-core (internal chain), careround-audit |
| `medication-task-reminder` | MedicationTaskReminderJob | careround-notification, careround-audit |
| `medication-task-overdue` | MedicationTaskReminderJob | careround-notification, careround-audit |
| `medication-task-completed` | MedicationTaskService | careround-audit |
| `clinical-note-saved` | ClinicalNoteService | careround-audit |
| `vitals-recorded` | PatientVitalsService | careround-audit |
| `patient-admitted` | PatientService | careround-audit |
| `patient-updated` | PatientService | careround-audit |
| `patient-discharged` | PatientService | careround-audit |
| `manual-medication-added` | MedicationChartService | careround-audit |
| `hospital-onboarding-requested` | HospitalOnboardingService | careround-audit |
| `hospital-onboarding-reviewed` | HospitalOnboardingService | careround-audit |
| `hospital-provisioned` | HospitalOnboardingService | careround-audit |
| `user-activation-requested` | HospitalOnboardingService | careround-notification, careround-audit |

**Consumer groups:**

| Group | Consumers |
|---|---|
| `careround-core-internal` | PrescriptionConfirmedConsumer, MedicationChartCreatedConsumer |
| `careround-notification` | MedicationTaskReminderConsumer, MedicationTaskOverdueConsumer, NotificationDltConsumer |
| `careround-audit-group` | AuditEventConsumer |

---

## 12. Security

### JWT

- Algorithm: HS256, 256-bit secret key
- Access token: 15-minute expiry, claims: `{ sub: userId, hospitalId, role, email }`
- Refresh token: 7-day expiry, stored in DB, rotated on every use
- All existing refresh tokens are revoked on password change

### Multi-Tenancy Enforcement

- `hospitalId` is extracted from the JWT by `JwtAuthFilter` and stored in `HospitalContextHolder` (ThreadLocal)
- Every repository call in business logic uses `findByIdAndHospitalId()` — bare `findById()` is never used in service code
- Cross-tenant access attempts return 404, never 403
- Kafka consumers read `hospitalId` from the event payload; `HospitalContextHolder` is not used in consumer threads

### Rate Limiting

Redis-backed sliding window per `hospitalId:userId`. Configured via `careround.ratelimit.*` properties.

### Password Storage

BCrypt with cost factor 10.

### CORS

All origins permitted in dev (`application-dev.yml`). Restrict to known domains in production.

---

## 13. Local Development

### Prerequisites

- Java 21 JDK (Eclipse Temurin recommended)
- Maven 3.9+
- Docker Desktop
- IntelliJ IDEA (recommended) or VS Code

### Infrastructure services

Start MySQL, Redis, Kafka, and observability tooling with Docker Compose:

```bash
docker compose up -d mysql redis kafka kafka-ui prometheus grafana
```

Spring Boot services run on the host (not in Docker) so hot reload and debugging work normally.

### Environment setup

Copy `.env.example` to `.env` and fill in the required values:

```env
MYSQL_USER=careround
MYSQL_PASSWORD=careround_password
JWT_SECRET=<256-bit hex — generate with: openssl rand -hex 32>
REDIS_HOST=localhost
REDIS_PORT=6379
KAFKA_BOOTSTRAP_SERVERS=localhost:9094
```

The `application-dev.yml` in each module supplies defaults for all other settings. `JWT_SECRET` is the only required env var for local development of `careround-core`.

### Running the services

```bash
# Build all modules (skip tests)
mvn clean package -DskipTests

# Run careround-core (in one terminal)
cd careround-core && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run careround-notification (another terminal)
cd careround-notification && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run careround-audit (another terminal)
cd careround-audit && mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Seeding the database

A demo seed script is provided at `infra/mysql/seed.sql`. It **truncates all domain tables first** and loads a complete dataset: one hospital tenant (City General Hospital, code `CGH`), three wards, seven staff accounts across all roles, and several patients at varying acuity levels.

Run it after the services have started and Flyway has applied all migrations:

```bash
# Local dev (Docker MySQL on port 3306)
mysql -h 127.0.0.1 -P 3306 -u careround -pcareround_password careround_core < infra/mysql/seed.sql

# Production / deployed MySQL (substitute your actual host and credentials)
mysql -h <MYSQL_HOST> -u <MYSQL_USER> -p<MYSQL_PASSWORD> careround_core < infra/mysql/seed.sql
```

**Password for all seeded accounts:** `Password123`

**Seeded accounts:**

| Role | Email | Name |
|---|---|---|
| ADMIN | `admin@citygeneral.nhs.uk` | Rebecca Morgan |
| SUPERVISOR | `l.walsh@citygeneral.nhs.uk` | Linda Walsh |
| DOCTOR | `s.chen@citygeneral.nhs.uk` | Sarah Chen |
| DOCTOR | `j.okafor@citygeneral.nhs.uk` | James Okafor |
| NURSE | `e.foster@citygeneral.nhs.uk` | Emily Foster |
| NURSE | `m.adeyemi@citygeneral.nhs.uk` | Michael Adeyemi |
| NURSE | `p.sharma@citygeneral.nhs.uk` | Priya Sharma |

Log in with hospital code `CGH`, the email above, and `Password123`.

**Seeded wards and patients:** Ward A (General Medicine) and Ward B (Surgery) each have patients at GREEN, AMBER, and RED acuity. ICU is seeded as an empty ward.

> The seed script is safe to re-run at any time — it truncates and reloads cleanly. Do not run it against a production database that holds real patient data.

### Running the AI service

See the `careround-ai` repository README for full setup. For local development without models, stub mode is the fastest path:

```bash
# In careround-ai directory
AI_PROVIDER=stub TRANSCRIPTION_PROVIDER=stub uvicorn main:app --reload --port 8000
```

Set `AI_SERVICE_URL=http://localhost:8000` in careround-core's `.env` or dev profile.

### IntelliJ setup

1. **File → Open** → select the `careround/` root directory (imports all modules via parent POM)
2. **Project Structure → SDK** → Java 21
3. **Settings → Build → Compiler → Annotation Processors** → enable annotation processing (required for Lombok)
4. Create a **Spring Boot Run Configuration** for each service with `Active profiles: dev`
5. Add `.env` values as **Environment variables** in each Run Configuration

### Port reference

| Port | Service |
|---|---|
| 3001 | Grafana |
| 3306 | MySQL |
| 6379 | Redis |
| 8000 | careround-ai (Python) |
| 8080 | careround-core + Swagger UI |
| 8081 | careround-notification |
| 8082 | careround-audit |
| 8090 | Kafka UI |
| 9090 | Prometheus |
| 9094 | Kafka (external listener) |

### API documentation

Swagger UI is available on careround-core only:

```
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs       ← OpenAPI JSON
```

`careround-notification` and `careround-audit` are Kafka-consumer-only services and expose no HTTP API.

---

## 14. Testing

Tests use **Mockito** (`@ExtendWith(MockitoExtension.class)`) for service and component unit tests.

Repository tests use `@DataJpaH2Test` — a custom composed annotation in `com.careround.test` that wires an in-memory H2 database in MySQL compatibility mode with `ddl-auto: create-drop` and Flyway disabled.

Controller tests use `@WebMvcTest` with mocked services.

```bash
# Run all tests
mvn test

# Run tests for one module
mvn test -pl careround-core

# Run a single test class
mvn test -pl careround-core -Dtest=PatientServiceTest

# Run a single test method
mvn test -pl careround-core -Dtest=PatientServiceTest#shouldAdmitPatient
```

---

## 15. Production Notes

### Horizontal scaling

`careround-core` scales horizontally with no state changes:
- JWT is stateless — each instance validates independently
- Redis is shared — rate limit counters are consistent across instances
- MySQL is shared — all instances see the same data
- Quartz JDBC clustered mode — each registered job fires on exactly **one** instance across all replicas (`QRTZ_LOCKS` table provides distributed locking)

### HikariCP sizing

| Service | `maximum-pool-size` | `minimum-idle` |
|---|---|---|
| careround-core | 10 | 5 |
| careround-notification | 5 | 2 |
| careround-audit | 5 | 2 |

At 3 instances of core + 1 each of notification and audit: `(3 × 10) + 5 + 5 = 40 connections` — within MySQL's default `max_connections: 151`.

### Graceful shutdown

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

### Key composite indexes (V7 migration)

```sql
-- Patient acuity list ordering (most critical read path)
INDEX ON patient(hospital_id, ward_id, acuity_color)

-- Medication task reminder + overdue detection (runs every minute)
INDEX ON medication_task(status, scheduled_time, pre_reminder_sent_at)
INDEX ON medication_task(hospital_id, status, scheduled_time)

-- Outbox poller (runs every second)
INDEX ON outbox_event(published, created_at)

-- Refresh token cleanup (hourly)
INDEX ON refresh_tokens(expires_at)
INDEX ON refresh_tokens(revoked)

-- Vitals history
INDEX ON patient_vitals(patient_id, recorded_at)

-- Audit log lookup
INDEX ON audit_log(hospital_id, event_type, received_at)
```

### AI service in production

The AI service runs on a private-subnet EC2 instance (GPU recommended: `g4dn.xlarge` for demo, `g4dn.2xlarge` for production). It must not be reachable from the public internet — careround-core reaches it over the VPC private network only. Raw transcription and clinical note content must not be logged anywhere in the pipeline.

### Environment variables (careround-core, production)

```env
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=<RDS endpoint>
MYSQL_USER=<db user>
MYSQL_PASSWORD=<db password>
REDIS_HOST=<ElastiCache endpoint>
KAFKA_BOOTSTRAP_SERVERS=<Kafka broker>
JWT_SECRET=<256-bit secret>
AI_SERVICE_URL=http://<careround-ai-private-ip>:8000
CAREROUND_APP_ACTIVATION_BASE_URL=https://<your-domain>/activate
CAREROUND_PLATFORM_BOOTSTRAP_ADMIN_EMAIL=<first platform admin>
CAREROUND_PLATFORM_BOOTSTRAP_ADMIN_PASSWORD=<set on first start only, then remove>
```

### Environment variables (careround-notification, production)

```env
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=<RDS endpoint>
MYSQL_USER=<db user>
MYSQL_PASSWORD=<db password>
KAFKA_BOOTSTRAP_SERVERS=<Kafka broker>
CAREROUND_CORE_BASE_URL=http://<core-private-ip>:8080
FCM_CREDENTIALS_JSON=<Firebase service account JSON>
```

### Environment variables (careround-audit, production)

```env
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=<RDS endpoint>
MYSQL_USER=<db user>
MYSQL_PASSWORD=<db password>
KAFKA_BOOTSTRAP_SERVERS=<Kafka broker>
```
