# Hospital Onboarding Feature

## Purpose

The public CareRound signup page is not a direct self-service account creation flow. It is a hospital tenant onboarding request flow.

Hospitals are sensitive multi-user organisations with patient data, tenant isolation requirements, role-based access, and operational configuration. For that reason, a public visitor should not be able to instantly create an active hospital tenant and clinical admin account without review.

This feature lets a hospital representative submit an onboarding request. An internal CareRound operator reviews the request, provisions a hospital tenant, creates the first hospital administrator, and sends that administrator an activation invite.

## Business Goals

- Capture interest from hospitals from the public landing page.
- Prevent unauthorised or spam tenant creation.
- Ensure every hospital is provisioned as an isolated tenant.
- Give backend engineers a clear workflow for turning a request into a live hospital account.
- Preserve auditability for tenant creation and first-admin activation.

## User Flow

1. A hospital representative (admin) opens `/signup`.
2. The representative submits hospital and contact details.
3. Backend stores the request as `PENDING_REVIEW`.
4. An internal CareRound operator reviews the request.
5. If rejected, the request is marked `REJECTED`.
6. If approved, backend provisions:
    - `Hospital`
    - default `SystemConfiguration`
    - initial hospital `ADMIN` user
    - activation token / invite
7. The hospital admin receives an activation email.
8. The admin sets their password and completes tenant setup.
9. The hospital admin can then add departments, wards, users, medical teams, schedules, and configuration.

## Recommended Module

Add an onboarding module to `careround-core`:

```txt
careround-core/src/main/java/com/careround/onboarding/
├── controller/
│   └── HospitalOnboardingController.java
├── service/
│   └── HospitalOnboardingService.java
├── repository/
│   └── HospitalOnboardingRequestRepository.java
├── entity/
│   ├── HospitalOnboardingRequest.java
│   └── HospitalOnboardingStatus.java
└── dto/
    ├── CreateHospitalOnboardingRequest.java
    ├── HospitalOnboardingResponse.java
    ├── ReviewHospitalOnboardingRequest.java
    └── ProvisionHospitalTenantRequest.java
```

## Entity: HospitalOnboardingRequest

Table: `hospital_onboarding_request`

```txt
id                    VARCHAR(36) PRIMARY KEY
hospital_name         VARCHAR(255) NOT NULL
country_or_region     VARCHAR(120) NOT NULL
contact_email         VARCHAR(255) NOT NULL
contact_phone         VARCHAR(50)
hospital_type         VARCHAR(80) NOT NULL
estimated_beds        VARCHAR(40)
primary_need          TEXT NOT NULL
status                ENUM(HospitalOnboardingStatus) NOT NULL
review_notes          TEXT
reviewed_by_user_id   VARCHAR(36)
reviewed_at           DATETIME
provisioned_hospital_id VARCHAR(36)
created_at            DATETIME NOT NULL
updated_at            DATETIME NOT NULL
```

Recommended indexes:

```sql
CREATE INDEX idx_onboarding_status_created
  ON hospital_onboarding_request(status, created_at);

CREATE INDEX idx_onboarding_contact_email
  ON hospital_onboarding_request(contact_email);
```

Optional uniqueness:

```sql
CREATE UNIQUE INDEX uk_pending_onboarding_contact_email
  ON hospital_onboarding_request(contact_email, status);
```

If the database does not support partial unique indexes, enforce duplicate pending requests in the service layer.

## Enum: HospitalOnboardingStatus

```java
public enum HospitalOnboardingStatus {
    PENDING_REVIEW,
    CONTACTED,
    APPROVED,
    REJECTED,
    PROVISIONED
}
```

Status meanings:

- `PENDING_REVIEW`: request submitted from public signup.
- `CONTACTED`: internal team has contacted the hospital.
- `APPROVED`: request is approved for tenant provisioning.
- `REJECTED`: request will not proceed.
- `PROVISIONED`: hospital tenant and first admin have been created.

## Public Endpoint

### Submit Hospital Onboarding Request

```http
POST /api/v1/onboarding/hospital-requests
Access: Public
```

Request:

```json
{
  "hospitalName": "City Teaching Hospital",
  "countryOrRegion": "Nigeria",
  "contactEmail": "admin@cityhospital.org",
  "contactPhone": "+234 801 000 0000",
  "hospitalType": "TEACHING_HOSPITAL",
  "estimatedInpatientBeds": "301_700",
  "primaryNeed": "Improve ward rounds, handover safety, and escalation response."
}
```

Response:

```json
{
  "status": 201,
  "data": {
    "id": "req_123",
    "hospitalName": "City Teaching Hospital",
    "contactEmail": "admin@cityhospital.org",
    "status": "PENDING_REVIEW",
    "createdAt": "2026-05-11T10:30:00Z"
  }
}
```

Validation:

- `hospitalName`: required, 2-255 chars.
- `countryOrRegion`: required.
- `contactEmail`: required, valid email.
- `contactPhone`: optional, max 50 chars.
- `hospitalType`: required enum/string.
- `estimatedInpatientBeds`: optional enum/string.
- `primaryNeed`: required, 10-5000 chars.

Security:

- Public endpoint.
- Apply rate limiting by IP and email.
- Add CAPTCHA or bot protection before production.
- Do not create `Hospital` or `User` records from this public endpoint.

## Internal Admin Endpoints

These endpoints are for CareRound internal operators or platform administrators, not hospital tenant admins.

### List Onboarding Requests

```http
GET /api/v1/onboarding/hospital-requests?status=PENDING_REVIEW&limit=20&cursor=...
Access: PLATFORM_ADMIN
```

Use cursor pagination.

### Get Onboarding Request

```http
GET /api/v1/onboarding/hospital-requests/{id}
Access: PLATFORM_ADMIN
```

### Review Onboarding Request

```http
PUT /api/v1/onboarding/hospital-requests/{id}/review
Access: PLATFORM_ADMIN
```

Request:

```json
{
  "status": "CONTACTED",
  "reviewNotes": "Spoke with CMIO. Pilot wards: Medicine and Cardiology."
}
```

Allowed review statuses:

- `CONTACTED`
- `APPROVED`
- `REJECTED`

### Provision Hospital Tenant

```http
POST /api/v1/onboarding/hospital-requests/{id}/provision
Access: PLATFORM_ADMIN
```

Request:

```json
{
  "hospitalName": "City Teaching Hospital",
  "address": "1 Hospital Road, Lagos",
  "contactEmail": "admin@cityhospital.org",
  "contactPhone": "+234 801 000 0000",
  "adminFirstName": "Ada",
  "adminLastName": "Okoro",
  "adminEmail": "admin@cityhospital.org",
  "newsAmberThreshold": 5,
  "newsRedThreshold": 7,
  "taskOverdueGraceMinutes": 30,
  "roundNotificationsEnabled": true,
  "nokNotificationEnabled": true
}
```

Response:

```json
{
  "status": 201,
  "data": {
    "requestId": "req_123",
    "hospitalId": "hosp_456",
    "adminUserId": "user_789",
    "status": "PROVISIONED"
  }
}
```

Provisioning must be transactional.

Within one transaction:

1. Verify request exists.
2. Verify request status is `APPROVED`.
3. Create `Hospital`.
4. Create `SystemConfiguration`.
5. Create first `User` with role `ADMIN`.
6. Generate account activation token.
7. Mark onboarding request as `PROVISIONED`.
8. Save `provisionedHospitalId`.
9. Publish an outbox event for activation email.

Do not send email directly inside the transaction. Use the existing transactional outbox pattern.

## Activation Flow

After provisioning, the first hospital admin should receive an activation email.

Recommended endpoint:

```http
POST /api/v1/auth/activate-account
Access: Public
```

Request:

```json
{
  "token": "activation-token",
  "password": "StrongPassword123!"
}
```

Backend behaviour:

- Validate token exists and is not expired.
- Set password hash for the user.
- Mark user active.
- Mark token as used.
- Return login response or require normal login after activation.

Activation token requirements:

- Random, high entropy.
- Single use.
- Expiry: 24-72 hours.
- Stored hashed, not as plaintext.

## Events

Recommended outbox events:

```txt
careround.hospital.onboarding_requested
careround.hospital.onboarding_reviewed
careround.hospital.provisioned
careround.user.activation_requested
```

Example `careround.user.activation_requested` payload:

```json
{
  "eventId": "evt_123",
  "hospitalId": "hosp_456",
  "userId": "user_789",
  "email": "admin@cityhospital.org",
  "activationUrl": "https://app.careround.com/activate?token=...",
  "correlationId": "..."
}
```

## Business Rules

1. Public onboarding request must never create a live hospital tenant directly.
2. A request must be `APPROVED` before provisioning.
3. A request can only be provisioned once.
4. A provisioned hospital must always have a default `SystemConfiguration`.
5. A provisioned hospital must always have exactly one initial active or pending-activation `ADMIN`.
6. Hospital data must be scoped by `hospitalId` immediately after provisioning.
7. Duplicate pending requests from the same contact email should be rejected or merged.
8. Rejected requests cannot be provisioned unless reopened by a platform admin.
9. Activation tokens must expire and must be single-use.
10. All review and provisioning actions must be audited.

## Error Cases

Duplicate pending request:

```json
{
  "status": 409,
  "error": "DUPLICATE_ONBOARDING_REQUEST",
  "message": "An onboarding request for this contact email is already pending."
}
```

Provisioning without approval:

```json
{
  "status": 422,
  "error": "BUSINESS_RULE_VIOLATION",
  "message": "Only approved onboarding requests can be provisioned."
}
```

Already provisioned:

```json
{
  "status": 409,
  "error": "ALREADY_PROVISIONED",
  "message": "This onboarding request has already been provisioned."
}
```

## Frontend Integration

Current public signup page route:

```txt
/signup
```

Frontend should eventually call:

```http
POST /api/v1/onboarding/hospital-requests
```

Payload mapping:

```txt
Hospital name              -> hospitalName
Country / region           -> countryOrRegion
Work email                 -> contactEmail
Phone                      -> contactPhone
Hospital type              -> hospitalType
Estimated inpatient beds   -> estimatedInpatientBeds
Primary need               -> primaryNeed
```

The frontend should show a success state after submission:

```txt
Request received. A CareRound onboarding specialist will contact your hospital.
```

The frontend should not redirect the user into the authenticated app after public signup.

## Suggested Migration

```sql
CREATE TABLE hospital_onboarding_request (
  id VARCHAR(36) PRIMARY KEY,
  hospital_name VARCHAR(255) NOT NULL,
  country_or_region VARCHAR(120) NOT NULL,
  contact_email VARCHAR(255) NOT NULL,
  contact_phone VARCHAR(50),
  hospital_type VARCHAR(80) NOT NULL,
  estimated_beds VARCHAR(40),
  primary_need TEXT NOT NULL,
  status VARCHAR(40) NOT NULL,
  review_notes TEXT,
  reviewed_by_user_id VARCHAR(36),
  reviewed_at DATETIME,
  provisioned_hospital_id VARCHAR(36),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE INDEX idx_onboarding_status_created
  ON hospital_onboarding_request(status, created_at);

CREATE INDEX idx_onboarding_contact_email
  ON hospital_onboarding_request(contact_email);
```

## Acceptance Criteria

- Public users can submit hospital onboarding requests.
- Duplicate pending requests are handled safely.
- Requests are stored with `PENDING_REVIEW`.
- Platform admins can review, approve, reject, and provision requests.
- Provisioning creates hospital tenant, config, first admin, and activation token in one transaction.
- Activation email is emitted through outbox, not sent synchronously.
- First hospital admin can activate account and log in.
- All tenant data created during provisioning is scoped by `hospitalId`.
- Type-safe DTOs and validation annotations are used.
- Cursor pagination is used for listing requests.
- Review and provisioning actions are auditable.
