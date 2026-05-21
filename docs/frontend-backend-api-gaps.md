# Frontend Backend API Gaps

This document lists frontend features that need backend support beyond the endpoints currently described in `api_docs.json`.

## Missing Backend Endpoints

### Password Reset

The login page exposes a "Forgot password?" affordance, but the backend only provides login, logout, refresh, change-password, and account activation.

Needed endpoints:

- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`

Suggested behavior:

- Request a reset link or token for a tenant user email.
- Validate and consume the reset token.
- Set a new password without requiring an existing session.

### Notifications

The top bar shows a notification bell, but there are no notification endpoints.

Needed endpoints:

- `GET /api/v1/notifications`
- `GET /api/v1/notifications/unread-count`
- `PATCH /api/v1/notifications/{id}/read`
- `PATCH /api/v1/notifications/read-all`

Suggested behavior:

- Return role-scoped clinical/system notifications.
- Support unread counts for the bell badge.
- Mark individual or all notifications as read.

### Global Search

The top bar search says "Search patients, MRN". The backend provides `GET /api/v1/patients/search`, which covers patient search only. If the product search is meant to include users, wards, teams, tasks, rounds, or notes, a broader endpoint is needed.

Needed endpoint:

- `GET /api/v1/search?q={query}`

Suggested behavior:

- Return grouped results with type, id, title, subtitle, and route target.
- Respect tenant and role permissions.

### Operational Reports

`src/pages/supervisor/Reports.tsx` needs time-series reporting data, but the backend only exposes role dashboard summaries.

Needed endpoints:

- `GET /api/v1/reports/task-completion?wardId={wardId}&from={date}&to={date}`
- `GET /api/v1/reports/overdue-tasks?wardId={wardId}&from={date}&to={date}`
- `GET /api/v1/reports/patient-flow?wardId={wardId}&from={date}&to={date}`
- `GET /api/v1/reports/round-history?wardId={wardId}&from={date}&to={date}`

Suggested behavior:

- Return chart-ready labels and values.
- Include round duration, patient count, lead doctor, and status for round history.

### Shift Listing

The shift assignment and handover workflows need pending, active, and upcoming shifts. The backend currently exposes only `GET /api/v1/shifts/current/{wardId}` and `PUT /api/v1/shifts/{id}/assign`.

Needed endpoint:

- `GET /api/v1/shifts?wardId={wardId}&status={status}&from={date-time}&to={date-time}`

Suggested behavior:

- Return generated shifts for a ward/date range.
- Allow the frontend to find the incoming shift required by `POST /api/v1/handovers`.

### Hospital Tenant Details Update

The hospital settings page can display hospital name, contact email, and address, but the backend only exposes `GET /api/v1/hospitals/me`.

Needed endpoint:

- `PUT /api/v1/hospitals/me`

Suggested behavior:

- Update hospital display name, address, contact email, and contact phone.
- Restrict to tenant admins.

### Vitals-Linked Notes

The nurse vitals workflow has an optional note field after recording vitals. The backend can create clinical notes, but vitals records do not accept a note or expose a vitals-note association.

Possible endpoint options:

- Add `note` to `POST /api/v1/patients/{patientId}/vitals`
- Or add `vitalsId` to `POST /api/v1/clinical-notes`

Suggested behavior:

- Preserve clinical context around a vitals-triggered escalation.
- Keep a traceable relationship between the vitals recording and the note.

## Backend Exists But Frontend Wiring Was Incomplete

These are not backend gaps. The API exists and the frontend should call it:

- Shift schedule creation: `POST /api/v1/shift-schedules`
- On-call rotation creation: `POST /api/v1/oncall`
- Medical team creation: `POST /api/v1/teams`
- Team ward assignment: `POST /api/v1/teams/{teamId}/wards`
- Care task creation: `POST /api/v1/care-tasks`
- Handover patient notes: `POST /api/v1/handovers/{handoverId}/patient-notes`
- Handover completion: `POST /api/v1/handovers/{handoverId}/complete`

Handover initiation is only partially wireable until the backend exposes shift listing, because `POST /api/v1/handovers` requires both outgoing and incoming shift ids.
