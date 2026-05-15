# CareRound Project Review Guide

This guide is a practical map for reviewing the backend. Use it to move from a business workflow to the files that implement it, without searching through the repository from scratch.

## How To Review

Start with `README.md` for the intended business rules, then use this guide to inspect implementation files. For each workflow, review in this order:

1. Controller: request shape, authorization, endpoint path.
2. Service: business rules, transactions, tenant scoping, events.
3. Entity and repository: persisted fields, indexes, query behavior.
4. Events/jobs/consumers: async side effects.
5. Tests: expected behavior and edge cases.

Cross-cutting files worth keeping open:

- `careround-core/src/main/java/com/careround/shared/config/SecurityConfig.java`
- `careround-core/src/main/java/com/careround/shared/security/JwtAuthFilter.java`
- `careround-core/src/main/java/com/careround/shared/security/JwtService.java`
- `careround-core/src/main/java/com/careround/shared/security/HospitalContextHolder.java`
- `careround-core/src/main/java/com/careround/shared/service/OutboxService.java`
- `careround-core/src/main/java/com/careround/shared/exception/GlobalExceptionHandler.java`
- `careround-core/src/main/resources/db/migration/`

## Repository Shape

CareRound is a Maven multi-module backend:

- `careround-core`: main API, domain logic, database migrations, Quartz jobs, outbox producer.
- `careround-notification`: Kafka consumers that create and send notifications.
- `careround-audit`: Kafka consumer that persists audit log entries.
- `README.md`: product/business architecture.
- `hospital_onboarding.md`: onboarding feature specification.
- `TODO.md`: deferred notes.

## Shared Core Concepts

### Multi-tenancy

Most domain records are scoped by `hospitalId`. The authenticated user's JWT supplies `hospitalId`, `userId`, and `role`; services read these through `HospitalContextHolder`.

Relevant files:

- `shared/security/HospitalContextHolder.java`
- `shared/security/JwtAuthFilter.java`
- `shared/security/JwtService.java`
- `shared/config/SecurityConfig.java`
- Tenant-scoped repositories across `auth/`, `hospital/`, and `patient/`

Review focus:

- Confirm services never trust request-supplied hospital IDs for tenant-scoped data.
- Confirm lookups use `findBy...AndHospitalId` or explicit hospital checks.
- Platform admin JWTs should not populate tenant context.

### Auth and Authorization

Tenant users authenticate through `/api/v1/auth/login`; platform operators authenticate separately through `/api/v1/platform/auth/login`.

Relevant files:

- `auth/controller/AuthController.java`
- `auth/service/AuthServiceImpl.java`
- `auth/service/AccountActivationService.java`
- `auth/entity/User.java`
- `auth/entity/RefreshToken.java`
- `auth/entity/AccountActivationToken.java`
- `auth/repository/UserRepository.java`
- `auth/repository/RefreshTokenRepository.java`
- `auth/repository/AccountActivationTokenRepository.java`
- `platform/controller/PlatformAuthController.java`
- `platform/service/PlatformAuthService.java`
- `platform/service/PlatformOperatorBootstrap.java`
- `platform/entity/PlatformOperator.java`
- `platform/repository/PlatformOperatorRepository.java`

Review focus:

- Login only authenticates active tenant users.
- Activation tokens are hashed, single-use, and expire.
- Account activation sets the initial password and requires normal login afterward.
- Platform operators are separate from tenant users.
- First platform admin bootstrap only runs when configured and when the table is empty.

## Hospital Onboarding

Public users cannot create hospitals directly. They submit onboarding requests. A platform admin reviews and provisions approved requests.

Workflow:

1. Public request creates `HospitalOnboardingRequest` with `PENDING_REVIEW`.
2. Platform admin marks request `CONTACTED`, `APPROVED`, or `REJECTED`.
3. Platform admin provisions only `APPROVED` requests.
4. Provisioning creates `Hospital`, `SystemConfiguration`, inactive first tenant `ADMIN`, activation token, and outbox events.
5. First admin activates account, sets password, then logs in normally.

Relevant files:

- `onboarding/controller/HospitalOnboardingController.java`
- `onboarding/service/HospitalOnboardingService.java`
- `onboarding/entity/HospitalOnboardingRequest.java`
- `onboarding/entity/HospitalOnboardingStatus.java`
- `onboarding/repository/HospitalOnboardingRequestRepository.java`
- `onboarding/dto/CreateHospitalOnboardingRequest.java`
- `onboarding/dto/ReviewHospitalOnboardingRequest.java`
- `onboarding/dto/ProvisionHospitalTenantRequest.java`
- `onboarding/dto/HospitalOnboardingResponse.java`
- `onboarding/dto/ProvisionHospitalTenantResponse.java`
- `auth/service/AccountActivationService.java`
- `shared/event/HospitalOnboardingRequestedEvent.java`
- `shared/event/HospitalOnboardingReviewedEvent.java`
- `shared/event/HospitalProvisionedEvent.java`
- `shared/event/UserActivationRequestedEvent.java`
- `careround-notification/.../UserActivationRequestedConsumer.java`
- `careround-core/src/main/resources/db/migration/V16__create_onboarding_platform_activation.sql`

Tests:

- `careround-core/src/test/java/com/careround/onboarding/controller/HospitalOnboardingControllerTest.java`

Review focus:

- Public submit endpoint must not create a hospital.
- Duplicate active onboarding requests and duplicate hospital contact emails should fail.
- Provisioning should be transactional.
- Activation URL should come from `careround.app.activation-base-url`, with localhost only as dev fallback.
- Platform-only endpoints require `PLATFORM_ADMIN`.

## Hospital and Configuration

Hospitals are tenant roots. Platform admins can list all hospitals; tenant users can fetch their own hospital. Config controls NEWS thresholds and notification toggles.

Relevant files:

- `hospital/hospital/HospitalController.java`
- `hospital/hospital/HospitalServiceImpl.java`
- `hospital/hospital/SystemConfigurationController.java`
- `hospital/hospital/SystemConfigurationServiceImpl.java`
- `hospital/entity/Hospital.java`
- `hospital/entity/SystemConfiguration.java`
- `hospital/repository/HospitalRepository.java`
- `hospital/repository/SystemConfigurationRepository.java`
- `hospital/hospital/dto/HospitalResponse.java`
- `hospital/hospital/dto/SystemConfigResponse.java`
- `hospital/hospital/dto/UpdateSystemConfigRequest.java`

Tests:

- `hospital/hospital/HospitalControllerTest.java`
- `hospital/hospital/HospitalServiceTest.java`
- `hospital/hospital/SystemConfigurationServiceTest.java`

Review focus:

- `GET /api/v1/hospitals` is platform-admin only.
- Tenant config updates must be scoped to the authenticated hospital.
- Direct public hospital creation should not exist.

## Staff Users

Tenant admins create and manage hospital users. Users have a hospital role and optional department.

Relevant files:

- `auth/controller/UserController.java`
- `auth/service/UserServiceImpl.java`
- `auth/entity/User.java`
- `auth/enums/UserRole.java`
- `auth/repository/UserRepository.java`
- `auth/dto/CreateUserRequest.java`
- `auth/dto/UserResponse.java`

Tests:

- `auth/controller/UserControllerTest.java`
- `auth/service/UserServiceTest.java`

Review focus:

- User email uniqueness is per hospital.
- User creation/deactivation requires `ADMIN`.
- Clinical roles should not gain admin-only endpoints accidentally.

## Departments and Wards

Departments and wards define hospital structure. Wards carry specialty, capacity, and supervisor.

Relevant files:

- `hospital/department/DepartmentController.java`
- `hospital/department/DepartmentServiceImpl.java`
- `hospital/entity/Department.java`
- `hospital/repository/DepartmentRepository.java`
- `hospital/ward/WardController.java`
- `hospital/ward/WardServiceImpl.java`
- `hospital/entity/Ward.java`
- `hospital/repository/WardRepository.java`

Tests:

- `hospital/department/DepartmentControllerTest.java`
- `hospital/department/DepartmentServiceTest.java`
- `hospital/ward/WardControllerTest.java`
- `hospital/ward/WardServiceTest.java`

Review focus:

- Wards are tenant-scoped.
- Ward specialty is now used by care task fallback nurse assignment.
- Ward supervisor is used for dashboard scope and workload conflict notifications.

## Medical Teams and Invites

Consultant-led teams group doctors and are assigned to wards. Consultants can invite registrars and junior doctors.

Relevant files:

- `hospital/medicalteam/MedicalTeamController.java`
- `hospital/medicalteam/MedicalTeamServiceImpl.java`
- `hospital/entity/MedicalTeam.java`
- `hospital/entity/MedicalTeamWard.java`
- `hospital/entity/MedicalTeamMember.java`
- `hospital/entity/MedicalTeamInvite.java`
- `hospital/repository/MedicalTeamRepository.java`
- `hospital/repository/MedicalTeamWardRepository.java`
- `hospital/repository/MedicalTeamMemberRepository.java`
- `hospital/repository/MedicalTeamInviteRepository.java`
- `shared/event/TeamInviteSentEvent.java`
- `shared/event/TeamMemberAddedEvent.java`

Tests:

- `hospital/medicalteam/MedicalTeamControllerTest.java`
- `hospital/medicalteam/MedicalTeamServiceTest.java`

Review focus:

- Only owning consultants or admins should change team composition/ward mappings.
- Invites must be same-hospital, non-duplicate, and expire.
- Team membership should not cross tenant boundaries.

## On-call Rotations and Shift Schedules

On-call rotations drive admission fallback and escalation assignment. Shift schedules define when shifts should be generated.

Relevant files:

- `hospital/oncall/OnCallRotationController.java`
- `hospital/oncall/OnCallRotationServiceImpl.java`
- `hospital/entity/OnCallRotation.java`
- `hospital/repository/OnCallRotationRepository.java`
- `hospital/shiftschedule/ShiftScheduleController.java`
- `hospital/shiftschedule/ShiftScheduleServiceImpl.java`
- `hospital/entity/ShiftSchedule.java`
- `hospital/repository/ShiftScheduleRepository.java`

Tests:

- `hospital/oncall/OnCallRotationServiceTest.java`
- `hospital/shiftschedule/ShiftScheduleControllerTest.java`
- `hospital/shiftschedule/ShiftScheduleServiceTest.java`

Review focus:

- Admin-only creation/update behavior.
- Shift schedules with null `wardId` apply across all wards.
- On-call lookup should match current time and department.

## Shifts and Automatic Shift Creation

Quartz creates shifts from schedules. Ward supervisors assign lead doctor and nurse in charge, activating the shift.

Relevant files:

- `hospital/shift/ShiftController.java`
- `hospital/shift/ShiftServiceImpl.java`
- `hospital/entity/Shift.java`
- `hospital/repository/ShiftRepository.java`
- `scheduler/jobs/ShiftCreationJob.java`
- `scheduler/service/ShiftCreationProcessor.java`
- `shared/event/ShiftCreatedEvent.java`
- `shared/event/ShiftActivatedEvent.java`
- `careround-notification/.../ShiftCreatedConsumer.java`

Tests:

- `hospital/shift/ShiftControllerTest.java`
- `hospital/shift/ShiftServiceTest.java`
- `scheduler/jobs/ShiftCreationJobTest.java`

Review focus:

- `ShiftCreationProcessor` must be idempotent.
- Shift uniqueness is `(ward_id, type, start_time)`.
- Rounds and care task assignment depend on active shifts.
- Care task auto-assignment uses `nurseInChargeId`.

## Patient Admission and Patient Status

Admitted patients are assigned to a ward and medical team. Admission publishes an event. Final discharge clears ward and bed.

Relevant files:

- `patient/patient/PatientController.java`
- `patient/patient/PatientServiceImpl.java`
- `patient/entity/Patient.java`
- `patient/repository/PatientRepository.java`
- `patient/patient/dto/AdmitPatientRequest.java`
- `patient/patient/dto/PatientResponse.java`
- `shared/event/PatientAdmittedEvent.java`
- `shared/event/PatientDischargedEvent.java`
- `careround-notification/.../PatientDischargedConsumer.java`

Tests:

- `patient/patient/PatientControllerTest.java`
- `patient/patient/PatientServiceTest.java`

Review focus:

- Admissions require valid hospital-scoped ward/team.
- Discharge must require `DISCHARGE_READY`.
- Final discharge must require all care tasks complete.
- Final discharge clears `wardId` and `bedNumber`.

## Ward Rounds

Rounds are created for active shifts. Consultants/registrars lead rounds. Patient reviews update clinical state and may trigger discharge readiness.

Relevant files:

- `patient/round/RoundController.java`
- `patient/round/RoundServiceImpl.java`
- `patient/entity/Round.java`
- `patient/entity/PatientRoundReview.java`
- `patient/repository/RoundRepository.java`
- `patient/repository/PatientRoundReviewRepository.java`
- `patient/round/dto/CreateRoundRequest.java`
- `patient/round/dto/ReviewPatientRequest.java`
- `shared/event/RoundCompletedEvent.java`
- `shared/event/PatientDischargeReadyEvent.java`
- `careround-notification/.../RoundCompletedConsumer.java`

Tests:

- `patient/round/RoundControllerTest.java`
- `patient/round/RoundServiceTest.java`

Review focus:

- No duplicate active round for same ward/team/type.
- Round requires active shift.
- Only consultants can confirm discharge.
- Discharge-ready creates only the intended discharge tasks.
- Discharge summary and medication tasks should align with `AssignedToRole`.

## Care Tasks and Automated Nurse Assignment

Care tasks represent nursing care plans and post-round jobs. API-created tasks require time windows and are automatically assigned to nurses.

Workflow:

1. Creator submits task with patient, source, title, priority, and time window.
2. Service validates patient is admitted and window is valid.
3. Assignment service chooses active ward nurse in charge.
4. If that nurse has an overlapping pending/in-progress task, fallback uses another active nurse from same-specialty wards.
5. If all same-specialty nurses clash, task stays with ward nurse and is marked as a workload conflict.
6. Workload conflict publishes an event and notifies ward supervisor.
7. Manual reassignment is allowed only for ward supervisors or the nurse who created the task, and can override conflicts.

Relevant files:

- `patient/caretask/CareTaskController.java`
- `patient/caretask/CareTaskServiceImpl.java`
- `patient/caretask/CareTaskAssignmentService.java`
- `patient/caretask/CareTaskAssignmentResult.java`
- `patient/entity/CareTask.java`
- `patient/repository/CareTaskRepository.java`
- `patient/caretask/dto/CreateCareTaskRequest.java`
- `patient/caretask/dto/AssignTaskRequest.java`
- `patient/caretask/dto/CareTaskResponse.java`
- `shared/event/CareTaskWorkloadConflictEvent.java`
- `careround-notification/.../CareTaskWorkloadConflictConsumer.java`
- `careround-core/src/main/resources/db/migration/V17__add_care_task_workload_conflict.sql`

Tests:

- `patient/caretask/CareTaskAssignmentServiceTest.java`
- `patient/caretask/CareTaskServiceTest.java`
- `patient/caretask/CareTaskControllerTest.java`

Review focus:

- Tasks without windows should fail validation.
- Overlap check should use `existing.windowStart < new.windowEnd` and `existing.windowEnd > new.windowStart`.
- Fallback should be same-specialty, not arbitrary hospital-wide.
- Conflict state should be visible in responses.
- Manual assignment role rules should match business rules.

## Clinical Notes

Clinical notes are immutable. Amendments create a new version rather than replacing history.

Relevant files:

- `patient/clinicalnote/ClinicalNoteController.java`
- `patient/clinicalnote/ClinicalNoteServiceImpl.java`
- `patient/entity/ClinicalNote.java`
- `patient/repository/ClinicalNoteRepository.java`
- `patient/clinicalnote/dto/CreateClinicalNoteRequest.java`
- `patient/clinicalnote/dto/AmendNoteRequest.java`
- `patient/clinicalnote/dto/ClinicalNoteResponse.java`

Tests:

- `patient/clinicalnote/ClinicalNoteControllerTest.java`
- `patient/clinicalnote/ClinicalNoteServiceTest.java`

Review focus:

- Original notes should not be deleted or overwritten.
- Amendments should preserve author, amendment metadata, and patient scope.
- Access should be tenant-scoped.

## Patient Vitals and NEWS2 Deterioration

Vitals update patient NEWS score and acuity. Threshold crossings create escalations and may notify next-of-kin.

Relevant files:

- `patient/vitals/PatientVitalsController.java`
- `patient/vitals/PatientVitalsServiceImpl.java`
- `patient/vitals/NewsScoreService.java`
- `patient/entity/PatientVitals.java`
- `patient/repository/PatientVitalsRepository.java`
- `patient/vitals/dto/RecordVitalsRequest.java`
- `patient/vitals/dto/VitalsResponse.java`
- `shared/event/PatientDeteriorationEvent.java`
- `careround-notification/.../PatientDeteriorationConsumer.java`

Tests:

- `patient/vitals/PatientVitalsControllerTest.java`
- `patient/vitals/PatientVitalsServiceTest.java`
- `patient/vitals/NewsScoreServiceTest.java`

Review focus:

- NEWS2 scoring values match business expectations.
- Patient acuity/status updates happen in the same transaction as vitals save.
- Config thresholds come from `SystemConfiguration`.
- Escalation routing matches amber/red severity.

## Escalations

Escalations are created for deterioration, task overdue, or nurse concern. Registrars/consultants acknowledge and resolve.

Relevant files:

- `patient/escalation/EscalationController.java`
- `patient/escalation/EscalationServiceImpl.java`
- `patient/entity/Escalation.java`
- `patient/repository/EscalationRepository.java`
- `patient/escalation/dto/CreateEscalationRequest.java`
- `scheduler/jobs/EscalationUnacknowledgedJob.java`
- `scheduler/service/EscalationUnacknowledgedProcessor.java`
- `shared/event/EscalationUnacknowledgedEvent.java`

Tests:

- `patient/escalation/EscalationControllerTest.java`
- `patient/escalation/EscalationServiceTest.java`
- `scheduler/jobs/EscalationUnacknowledgedJobTest.java`

Review focus:

- Escalation status moves forward.
- Only allowed roles acknowledge/resolve.
- Unacknowledged escalation job should escalate after configured grace period.
- Escalation queries should be hospital/ward scoped.

## Next-of-Kin

Next-of-kin contacts are attached to patients. Consent controls notification fan-out.

Relevant files:

- `patient/nextofkin/NextOfKinController.java`
- `patient/nextofkin/NextOfKinServiceImpl.java`
- `patient/entity/NextOfKin.java`
- `patient/repository/NextOfKinRepository.java`
- `patient/nextofkin/dto/AddNextOfKinRequest.java`
- `patient/nextofkin/dto/UpdateNextOfKinRequest.java`
- `patient/nextofkin/dto/UpdateNotificationConsentRequest.java`
- `patient/nextofkin/dto/NextOfKinResponse.java`
- `careround-notification/client/HttpCoreLookupClient.java`
- `careround-notification/client/NextOfKinContact.java`

Tests:

- `patient/nextofkin/NextOfKinControllerTest.java`
- `patient/nextofkin/NextOfKinServiceTest.java`

Review focus:

- NOK access should be scoped through the patient hospital.
- Notification consent must be respected.
- Emergency contact handling should match deterioration/discharge notification expectations.

## Shift Handovers

Handovers move patient context between shifts. Staff can add patient notes and complete handover.

Relevant files:

- `hospital/handover/HandoverController.java`
- `hospital/handover/HandoverServiceImpl.java`
- `hospital/entity/Handover.java`
- `hospital/entity/PatientHandoverNote.java`
- `hospital/repository/HandoverRepository.java`
- `hospital/repository/PatientHandoverNoteRepository.java`
- `hospital/handover/dto/InitiateHandoverRequest.java`
- `hospital/handover/dto/AddPatientHandoverNoteRequest.java`
- `shared/event/HandoverCompletedEvent.java`

Tests:

- `hospital/handover/HandoverControllerTest.java`
- `hospital/handover/HandoverServiceTest.java`

Review focus:

- Handover requires active outgoing shift.
- Patient notes should belong to patients in the relevant ward/hospital.
- Completion should update the correct shift/handover status.

## Dashboards

Dashboards aggregate role-specific operational state: active patients, open escalations, open/overdue tasks, active shifts, rounds in progress.

Relevant files:

- `dashboard/DashboardController.java`
- `dashboard/DashboardService.java`
- Repositories used by dashboard across patient, care task, escalation, shift, ward, round.

Tests:

- `dashboard/DashboardControllerTest.java`
- `dashboard/DashboardServiceTest.java`

Review focus:

- Dashboard data must be scoped to authenticated hospital.
- Ward supervisor dashboard must only include wards where `ward.supervisorId = userId`.
- Counts should match statuses used in business workflows.

## Scheduler Jobs

Quartz jobs perform recurring backend work.

Jobs:

- `OutboxPollerJob`: publishes pending outbox events to Kafka.
- `ShiftCreationJob`: creates shifts from schedules.
- `TaskOverdueJob`: marks overdue tasks/escalations.
- `EscalationUnacknowledgedJob`: escalates unacknowledged escalations.
- `InviteExpiryJob`: expires pending medical team invites.

Relevant files:

- `scheduler/config/QuartzConfig.java`
- `scheduler/service/JobSchedulingService.java`
- `scheduler/jobs/*.java`
- `scheduler/service/*Processor.java`
- `careround-core/src/main/resources/db/migration/V13__create_quartz_tables.sql`

Tests:

- `scheduler/jobs/OutboxPollerJobTest.java`
- `scheduler/jobs/ShiftCreationJobTest.java`
- `scheduler/jobs/TaskOverdueJobTest.java`
- `scheduler/jobs/EscalationUnacknowledgedJobTest.java`
- `scheduler/jobs/InviteExpiryJobTest.java`

Review focus:

- Jobs should be idempotent.
- Jobs should not bypass business rules accidentally.
- Outbox failures should not mark events as published.

## Transactional Outbox and Kafka Events

Domain services write outbox rows inside their transactions. The poller later publishes to Kafka.

Relevant files:

- `shared/service/OutboxService.java`
- `shared/event/OutboxEvent.java`
- `shared/event/OutboxEventRepository.java`
- `scheduler/service/OutboxPollerProcessor.java`
- `shared/config/KafkaProducerConfig.java`
- `shared/config/KafkaTopicConfig.java`
- `shared/event/*Event.java`
- `careround-core/src/main/resources/db/migration/V12__create_outbox.sql`

Review focus:

- Event type strings should match configured Kafka topics and consumers.
- Payload records should include `hospitalId` and `correlationId` where needed.
- Services should publish only inside transactions.
- Audit and notification consumers should subscribe to the same topic names.

## Notification Service

Notification consumes Kafka events, persists notification attempts, sends through stub email/SMS providers, and stores failures.

Relevant files:

- `careround-notification/src/main/java/com/careround/notification/consumer/*.java`
- `careround-notification/src/main/java/com/careround/notification/service/NotificationService.java`
- `careround-notification/src/main/java/com/careround/notification/notification/Notification.java`
- `careround-notification/src/main/java/com/careround/notification/notification/NotificationRepository.java`
- `careround-notification/src/main/java/com/careround/notification/consumer/NotificationIdempotencyGuard.java`
- `careround-notification/src/main/java/com/careround/notification/dlt/NotificationDltConsumer.java`
- `careround-notification/src/main/java/com/careround/notification/config/KafkaConsumerConfig.java`
- `careround-notification/src/main/java/com/careround/notification/client/HttpCoreLookupClient.java`

Tests:

- `careround-notification/src/test/java/com/careround/notification/consumer/*Test.java`
- `careround-notification/src/test/java/com/careround/notification/service/NotificationServiceTest.java`
- `careround-notification/src/test/java/com/careround/notification/dlt/NotificationDltConsumerTest.java`

Review focus:

- Consumers should be idempotent.
- DLT consumer should persist failed messages.
- Notification channels should match business expectations.
- Core lookup calls should use service credentials when needed.

## Audit Service

Audit consumes configured topics and persists immutable audit entries with Kafka metadata.

Relevant files:

- `careround-audit/src/main/java/com/careround/audit/consumer/AuditEventConsumer.java`
- `careround-audit/src/main/java/com/careround/audit/entity/AuditLogEntry.java`
- `careround-audit/src/main/java/com/careround/audit/repository/AuditLogRepository.java`
- `careround-audit/src/main/java/com/careround/audit/config/KafkaConsumerConfig.java`

Tests:

- `careround-audit/src/test/java/com/careround/audit/consumer/AuditEventConsumerTest.java`

Review focus:

- Audit topic list should include every domain event requiring audit.
- Payloads should include hospital ID, including platform-scoped onboarding events.
- Duplicate handling should not skip legitimate separate events with reused correlation IDs unless intended.

## Migrations

Core migration order:

- `V1__create_users.sql`: users and auth base.
- `V2__create_hospital_config.sql`: hospital and system config.
- `V3__create_department_ward.sql`: departments and wards.
- `V4__create_medical_team.sql`: medical team, members, invites, ward mappings.
- `V5__create_on_call_shift_schedule.sql`: on-call and shift schedules.
- `V6__create_shift_handover.sql`: shifts and handovers.
- `V7__create_patient.sql`: patient table.
- `V8__create_vitals_nok.sql`: vitals and next-of-kin.
- `V9__create_escalation.sql`: escalations.
- `V10__create_round_review.sql`: rounds and patient reviews.
- `V11__create_clinical_note_care_task.sql`: notes and care tasks.
- `V12__create_outbox.sql`: transactional outbox.
- `V13__create_quartz_tables.sql`: Quartz JDBC tables.
- `V14__create_indexes.sql`: performance indexes.
- `V15__allow_discharged_patients_without_ward.sql`: discharge can clear ward.
- `V16__create_onboarding_platform_activation.sql`: onboarding, platform operators, activation tokens.
- `V17__add_care_task_workload_conflict.sql`: care task conflict fields and assignment index.

Review focus:

- Entity fields and migration columns should match.
- New nullable/non-null constraints should match service behavior.
- Indexes should support the critical queries: patient queues, overdue tasks, outbox polling, nurse task conflict checks.

## High-Value Review Checklist

Use this checklist to identify bugs or business drift:

- Security: every controller method has the intended role restriction.
- Tenant scoping: every service verifies hospital ownership before reading or writing.
- Transactions: workflows that persist state and publish outbox events are transactional.
- Events: event topic strings match `KafkaTopicConfig`, notification consumers, and audit consumer.
- Status transitions: care tasks, escalations, rounds, handovers, invites, and shifts move forward only.
- Discharge: final discharge requires ready status, completed tasks, and clears ward/bed.
- Onboarding: public request never creates tenant; provisioned admin starts inactive.
- Care tasks: task windows required; assignment uses active shifts; conflicts are visible and notify supervisor.
- Notifications: consent and recipient lookup rules are respected.
- Tests: new business rules have both happy-path and failure-path tests.

