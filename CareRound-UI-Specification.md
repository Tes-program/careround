# CareRound — UI/UX Specification
### Frontend Guideline for Web and Mobile

---

## Table of Contents

1. [Design Principles](#1-design-principles)
2. [Tech Stack](#2-tech-stack)
3. [Shared Patterns](#3-shared-patterns)
4. [Authentication Flow](#4-authentication-flow)
5. [Admin Dashboard (Web Only)](#5-admin-dashboard-web-only)
6. [Doctor Dashboard (Web + Mobile)](#6-doctor-dashboard-web--mobile)
7. [Nurse Dashboard (Web + Mobile)](#7-nurse-dashboard-web--mobile)
8. [Supervisor Dashboard (Web Only)](#8-supervisor-dashboard-web-only)
9. [Shared Patient Detail Page](#9-shared-patient-detail-page)
10. [Mobile-Specific Notes](#10-mobile-specific-notes)
11. [Component Inventory](#11-component-inventory)

---

## 1. Design Principles

**Land on the primary action immediately.** Every role logs in and arrives at the screen they need most — not a generic home or dashboard. The doctor lands on the patient list. The nurse lands on her task list. There is no intermediate step.

**Clinical screens are built for people on their feet.** Doctors and nurses use the mobile app while moving between patients. Every interactive element must be large enough to tap with one hand. Voice is the primary input for doctors — typing is the fallback, not the default.

**The most important action on every screen is the most visually prominent element.** The Record button on a patient's detail page must be impossible to miss. The Mark Complete button on a nurse's task card must be the thing the eye goes to first.

**Colour is clinical information, not decoration.** RED, AMBER, and GREEN carry specific clinical meaning throughout the app. They should be used consistently and never for anything else.

**Never surprise a user with an irreversible action.** Confirmations use modals. A modal for saving a clinical note follows the same pattern as a modal for deleting a user — intentional, not alarming, but requiring a deliberate second tap.

---

## 2. Tech Stack

### Web
- **Framework:** React 18 + Vite + TypeScript
- **Styling:** Tailwind CSS
- **Component library:** shadcn/ui (for forms, tables, modals, tabs, badges)
- **State management:** Redux (auth store, role-aware routing)
- **Data fetching:** Redux toolkit with JWT interceptor + silent refresh on 401
- **Charts:** Recharts (vitals trend line, supervisor hourly bar chart)
- **Audio recording:** Web Audio API + MediaRecorder API

### Mobile
- **Framework:** React Native + Expo (SDK 51+)
- **Navigation:** Expo Router (file-based, tab layout)
- **Styling:** NativeWind (Tailwind classes on React Native)
- **State management:** Zustand — mobile developer may substitute with TanStack Query + Zustand if preferred
- **Data fetching:** Axios with JWT interceptor
- **Audio:** `expo-av` for recording
- **Storage:** `expo-secure-store` for JWT tokens, `@react-native-async-storage/async-storage` for non-sensitive preferences
- **Push notifications:** `expo-notifications` + Firebase Cloud Messaging

> **Note to mobile developer:** The tooling above is a starting recommendation. If you have a preferred alternative that you are more productive with (e.g. React Navigation over Expo Router, react-query over Zustand for server state), use it. The screens, flows, and interactions described in this document are the requirement — not the specific library.

---

## 3. Shared Patterns

### Acuity Colour System
Used everywhere a patient's status is shown. Never use these colours for anything else.

| Colour | Meaning | Tailwind class (web) | Visual |
|--------|---------|---------------------|--------|
| RED | High acuity — needs immediate attention | `bg-red-600`, `text-red-600`, `border-red-600` | Solid red |
| AMBER | Moderate acuity — monitor closely | `bg-amber-500`, `text-amber-500`, `border-amber-500` | Solid amber |
| GREEN | Stable | `bg-green-500`, `text-green-500`, `border-green-500` | Solid green |

### API Response Handling
All responses follow `{ success: boolean, message: string, data: T }`. Loading state and error state must be handled on every async operation — no blank screens, no silent failures.

### JWT Authentication
On every 401 response, the Axios interceptor silently calls `POST /auth/refresh` with the stored refresh token and retries the original request. If refresh also fails, the user is logged out and redirected to login.

### Confirmation Modal Pattern
Used before any action that cannot be easily undone. Two variants:

**Save confirmation** (for clinical note confirm, prescription confirm):
- Heading: action name (e.g. "Save consultation note?")
- Body: compact summary of what will be saved
- Buttons: Cancel (left, outlined) + Save / Confirm (right, filled primary)

**Destructive confirmation** (for deactivate user, discontinue medication):
- Heading: "Are you sure?"
- Body: one sentence explaining the consequence
- Buttons: Cancel (left, outlined) + Deactivate / Discontinue (right, filled red)

### Modal Pattern (all forms)
All create/edit forms open in a centred modal with a dark backdrop overlay. Clicking outside or pressing Escape closes and discards changes. Cancel button (left, outlined) and Save button (right, filled primary) at the bottom.

### Empty States
Every list that can be empty must show a meaningful empty state — an icon, a short message, and where appropriate a call-to-action button. Examples: "No patients in this ward yet", "No tasks for today", "No notes recorded".

### Error States
All async failures show an inline error message (not a toast for critical operations, toast for non-critical). Include a Retry button where retrying makes sense.

---

## 4. Authentication Flow

### Login Page (Web + Mobile)
**URL (web):** `/login`  
**Applies to:** All roles

**Layout:**
- Centred card on a neutral background
- CareRound logo at the top of the card
- Three fields stacked vertically:
  1. **Hospital Code** — text input, uppercase-forced, placeholder "e.g. STMARYS"
  2. **Email** — email input
  3. **Password** — password input with show/hide toggle
- **Sign In** button — full width, primary colour, below the fields
- No registration link, no social login, no "forgot password" for MVP

**Behaviour:**
- On submit: `POST /auth/login` with `{ hospitalCode, email, password }`
- On success: JWT stored (SecureStore on mobile, memory + cookie on web), redirect based on role:
  - ADMIN → `/admin/dashboard`
  - DOCTOR → `/doctor/patients`
  - NURSE → `/nurse/tasks`
  - SUPERVISOR → `/supervisor/dashboard`
- On failure: inline error below the Sign In button — never clear the hospital code or email fields, only clear password

**Validation:**
- All three fields required
- Email must be a valid email format
- Errors shown inline beneath each field, not as toasts

---

## 5. Admin Dashboard (Web Only)

The Admin is a desktop user. They configure a hospital once and then manage it occasionally. The interface is information-dense and table-driven — complexity is acceptable because the Admin is seated, not rushing through a ward.

### Layout
Persistent left sidebar with:
- CareRound logo at the top
- Navigation links: Dashboard, Patients, Wards, Users, Settings
- Active link highlighted with left border accent and background tint
- Hospital name and Admin's name at the bottom of the sidebar

Main content area to the right of the sidebar.

---

### 5.1 Admin — Dashboard
**URL:** `/admin/dashboard`

**Content:**

Four stat cards in a row across the top:
- Total Wards (count)
- Total Doctors (count of active DOCTOR role users)
- Total Nurses (count of active NURSE role users)
- Patients Currently Admitted (count of patients with status ADMITTED)

Each stat card: large number, label underneath, neutral card background.

Below the stat cards: a table of recent account activity with columns: Name, Role, Action (Created / Deactivated), Date. Shows the last 20 entries. Read-only.

**Quick actions** in the top-right of the page: "Register Patient" button and "Add User" button side by side. These are the two most frequent admin tasks.

---

### 5.2 Admin — Patients
**URL:** `/admin/patients`

**Content:**

Page heading "Patients" with a "Register Patient" button top-right.

Table with columns: Hospital No., Full Name, Ward, Bed, Age, Status (Admitted / Discharged), Registered On, Actions.
- Actions: View (opens patient detail read-only), Edit (opens Edit Patient modal for correcting demographic info)
- Filter bar above the table: Ward selector, Status selector (All / Admitted / Discharged), search by name or hospital number

**Register Patient modal:**

This is the largest form in the admin interface. It collects all demographic and intake information. Organised into three sections within the modal with section headings.

**Section 1 — Identity**
- First Name (required)
- Last Name (required)
- Date of Birth (date picker, required)
- Gender (radio or select: Male / Female / Other, required)
- Hospital Number (required, must be unique — auto-generated suggestion shown but editable)
- Phone Number (optional)
- Address (textarea, optional)

**Section 2 — Medical Background**
- Previous Conditions (textarea — e.g. "Hypertension, Type 2 Diabetes, Asthma", free text)
- Current Medications (textarea — medications the patient was taking before admission, free text)
- Allergies (textarea — e.g. "Penicillin, Aspirin", free text)

**Section 3 — Admission**
- Ward (select from active wards)
- Bed Number (text, optional — can be assigned later)
- Admission Type (select: Emergency / Elective / Transfer)
- Admission Date (datetime, defaults to now)

**Emergency Contact** (below the three sections):
- Emergency Contact Name
- Emergency Contact Phone

Modal buttons: Cancel + Register Patient. On success: patient appears in the Patients table and is immediately visible to clinical staff.

**Important:** Patient registration is strictly an Admin action. Doctors and nurses cannot create or modify patient demographic records. Doctors update clinical details (diagnosis, discharge) through a separate, role-restricted endpoint.

---

### 5.3 Admin — Wards
**URL:** `/admin/wards`

**Content:**

Page heading "Wards" with an "Add Ward" button top-right.

Table with columns: Ward Name, Specialty, Total Beds, Status (Active / Inactive), Actions.
- Actions column: Edit button (opens Edit Ward modal), and a toggle for active/inactive status with a confirmation modal

**Add Ward modal:**
- Fields: Ward Name (text, required), Specialty (text, optional), Total Beds (number, required, min 1)
- Save creates the ward and closes the modal
- New ward appears at the bottom of the table

**Edit Ward modal:**
- Same fields as Add Ward, pre-filled with current values

---

### 5.4 Admin — Users
**URL:** `/admin/users`

**Content:**

Tab bar at the top filtering the table: All | Doctors | Nurses | Supervisors

"Add User" button top-right.

Table columns: Full Name, Email, Role, Status (Active badge / Inactive badge), Date Added, Actions.
- Actions column: Edit button (opens Edit User modal), Deactivate button (destructive confirmation modal — "This user will no longer be able to log in")
- Deactivated users show in the table with a greyed-out Inactive badge — they are not deleted
- A "Reactivate" button replaces the Deactivate button for inactive users

**Add User modal:**
- Fields: First Name (required), Last Name (required), Email (required, must be unique within hospital), Role (select: Doctor / Nurse / Supervisor — Admin cannot create another Admin), Temporary Password (required, min 8 chars)
- On create: account is active immediately, user can log in with these credentials

**Edit User modal:**
- Fields: First Name, Last Name, Email, Role — same as Add but pre-filled
- Password is NOT editable here — users change their own password via the Change Password endpoint

---

### 5.5 Admin — Settings
**URL:** `/admin/settings`

**Content:**

Page heading "Hospital Settings".

One section only (acuity thresholds are not configurable — the Vitals Health Index uses fixed clinical scoring):

**Task Overdue Settings**
- Label: "Medication Task Overdue Windows"
- Two number inputs:
  - "Send nurse reminder after (minutes)" — default 10
  - "Show as critically overdue on supervisor dashboard after (minutes)" — default 20
- Brief explanatory text under each input

**Save button** at the bottom of the page — full width, primary colour.
Changes do not apply until Save is tapped. A success toast confirms when saved.

> The Vitals Health Index uses fixed, clinically validated scoring thresholds: 0–2 = Stable (GREEN), 3–4 = Watch (AMBER), 5+ = Critical (RED). These are not adjustable per hospital.

---

## 6. Doctor Dashboard (Web + Mobile)

The doctor uses the app while standing at a patient's bedside. Every screen must work one-handed. The Record button is the most important UI element the doctor interacts with.

### Navigation

**Web:** Top navigation bar with links: Patients, Profile. The CareRound logo on the left links back to the patient list.

**Mobile:** Bottom tab bar with two tabs: Patients (with a ward icon), Profile (with a person icon).

---

### 6.1 Doctor — Patient List
**URL (web):** `/doctor/patients`  
**Mobile screen:** `Patients` tab

**This is the landing screen after login.**

**Layout:**
A filter row directly below the page heading with three acuity filter buttons: All | RED | AMBER | GREEN. A search bar sits to the right of the filters (secondary — sort is the primary navigation).

Below the filter row: a vertical list of patient cards.

**Patient Card:**
Each card spans the full width. On the left edge: a solid vertical colour strip (6px wide) in the patient's acuity colour (RED / AMBER / GREEN). This is the first thing the eye goes to.

Inside the card:
- **Top left:** Patient full name — large, bold
- **Below name:** Bed number as a small grey badge (e.g. "Bed 4")
- **Below bed:** Primary diagnosis in regular weight, slightly smaller, grey text — truncated to one line
- **Top right:** VHI status badge — coloured pill showing the status label and score (e.g. "STABLE · 1" in green, "WATCH · 3" in amber, "CRITICAL · 6" in red). This is the clinical number at a glance.
- **Below VHI badge:** Last vitals timestamp in small grey text (e.g. "Vitals 2h ago")
- **Below vitals time:** Active medication count — small grey text (e.g. "3 active meds") with a pill icon
- **Bottom right:** A small circular indicator — filled teal circle if a clinical note was recorded today, empty circle outline if not.

**Sort order:** RED patients first, then AMBER, then GREEN. Within each colour group, the patient with the highest VHI score appears first. Within the same VHI score, the patient with the oldest clinical note timestamp appears first (longest since last seen).

**Tapping a card** navigates to that patient's detail page.

---

### 6.2 Doctor — Recording Flow

The recording flow is initiated by tapping the floating Record button (microphone icon in a teal circle, bottom-right) from any tab on the Patient Detail page. It is a 3-screen flow that replaces the note-writing experience entirely.

---

#### Screen 1 — Recording

**Full-screen takeover.** No navigation. Dark background.

- **Patient context** (small, top-left): patient name and bed number — so the doctor always knows who they are recording for.
- **Cancel** link — top-right, small, plain text. Tapping shows a brief "Discard recording?" confirmation before exiting.
- **Waveform animation** — centred on screen. When the doctor is speaking: typical waveform bars animating at varying heights. When silent: a flat line with a small slow-pulsing dot confirming the recording is still active. This reassures the doctor that silence between patient interactions has not stopped the recording.
- **Elapsed timer** — below the waveform, format `00:00`, counting up from the moment recording started.
- **Two buttons at the bottom:**
  - **Pause** (left) — two vertical bar icon. Pauses the recording without saving. The waveform freezes. Button changes to a Resume play icon.
  - **Stop and Save** (right, larger, primary colour) — stops the recording and advances to Screen 2.

**Behaviour:** The doctor records the entire patient consultation in one take — including silence while examining, pausing to speak to a colleague, and any gaps. They do not stop and restart between topics. One recording per patient visit.

---

#### Screen 2 — Processing

**Full-screen.** Shown while the AI service is working. Should feel calm, not anxious.

- **Patient context** at the top (same as Screen 1)
- **Three-step progress track** in the centre, shown as a horizontal row of three labelled steps connected by lines:
  1. Transcribing
  2. Structuring note
  3. Extracting prescriptions

  Each step is a circle that fills with the primary teal colour as it completes. The active step pulses softly.
- **Supporting message** below the track: "Processing your consultation — longer recordings may take up to 30 seconds."
- No back button. No cancel. The process runs to completion.

---

#### Screen 3 — Review

The doctor reviews and edits the AI output before anything is saved to the patient record. This is the most important screen in the application.

**Layout — scrollable page with a sticky bottom action bar.**

**Top:** Small patient context bar (name, bed) — does not scroll away.

**Section 1 — Raw Transcription** (collapsible, collapsed by default):
- A labelled accordion row: "Raw Transcription" with a chevron icon
- Expanding it shows the verbatim Whisper output as plain scrollable text
- Purpose: lets the doctor check the source if a structured section looks wrong

**Section 2 — Clinical Note:**
- Label: "Clinical Note"
- Four labelled text areas stacked vertically:
  - **Subjective** — editable, pre-filled by AI
  - **Objective** — editable, pre-filled by AI
  - **Assessment** — editable, pre-filled by AI
  - **Plan** — editable, pre-filled by AI
- Each text area auto-expands to show its full content without internal scroll
- The doctor taps any section to edit inline

**Section 3 — Prescriptions** (only shown if the AI extracted at least one prescription):
- Label: "Prescriptions"
- Each prescription is a card:
  - **Header:** Drug name (bold, large) — dose + route on the same line — frequency string (e.g. "every 6 hours")
  - **Administration times row:** A horizontal scrollable row of small pill badges, one per scheduled dose. Each badge shows the time in HH:MM format. Badges are grey (unfilled) since none have been administered yet.
  - **Edit button** (pencil icon, top-right of card): opens an inline editing state within the card — fields for drug name, dose, route, frequency hours, total doses, start time. Saving recalculates the time badges.
  - **Remove button** (trash icon, top-right of card): removes this prescription with a brief "Remove this prescription?" inline confirmation.
- **"+ Add Prescription"** button below the last card — opens a blank prescription card in edit state for manually adding one the AI missed.

**Sticky bottom bar:**
- "Confirm and Save" button — full width, primary teal, disabled until the doctor has scrolled past the prescriptions section (ensures they have reviewed). On tap: opens the **Save Confirmation Modal**.

**Save Confirmation Modal:**
- Heading: "Save consultation note?"
- Body: compact summary — patient name, today's date, "1 clinical note" and "2 prescriptions" (or however many)
- Buttons: Cancel (outlined) + Save (filled teal)
- On Save: POST to `/clinical-notes/confirm`, on success navigate back to Patient Detail

---

## 7. Nurse Dashboard (Web + Mobile)

The nurse's entire working day is structured around her task list. The task list IS the primary interface. Everything else is secondary.

### Navigation

**Web:** Top navigation bar: Tasks, Patients, Profile.

**Mobile:** Bottom tab bar: Tasks (with overdue count badge), Patients, Profile.

---

### 7.1 Nurse — Task List
**URL (web):** `/nurse/tasks`  
**Mobile screen:** `Tasks` tab (landing screen after login)

**This is the landing screen after login.**

**Layout:**

**Summary bar** at the very top, below the page heading: three numbers in a horizontal row:
- "Total today: 12" — neutral colour
- "Completed: 7" — green colour
- "Overdue: 2" — red colour, bold

Three sections below the summary bar:

---

**Section 1 — OVERDUE** (red section heading)
Always visible, always expanded. Never collapsible. If there are no overdue tasks this section does not render at all — it does not show as an empty state.

Each overdue task card:
- **Left edge:** solid red border (6px)
- **Top of card:** overdue duration in bold red text — "22 min overdue"
- **Patient info:** Patient full name + bed number (e.g. "Mr. Okafor — Bed 4")
- **Drug info:** Drug name, dose, route (e.g. "Amoxicillin 500mg oral")
- **Scheduled time** in small grey text: "Scheduled 10:00"
- **Mark Complete button** — large, full-width at the bottom of the card, outlined red styling that fills solid red on press-hold to confirm. This requires a deliberate press-hold (approximately 1 second) to prevent accidental completions. A brief fill animation during the hold provides visual feedback.

---

**Section 2 — DUE SOON** (amber section heading)
Tasks due within the next 30 minutes. Same card format as OVERDUE but with amber styling.

Each card:
- **Left edge:** solid amber border
- **Top of card:** time until due in amber text — "Due in 18 min"
- Same patient and drug info as overdue card
- **Mark Complete button** — same press-hold interaction, amber styling

---

**Section 3 — UPCOMING** (neutral section heading, collapsed by hour)
All remaining PENDING tasks for the rest of the shift, grouped into collapsible hourly sections.

Section header example: "14:00 — 15:00 (3 tasks)" with a chevron to expand/collapse. The current hour's group is expanded by default.

Each card in Upcoming:
- Smaller visual weight than OVERDUE / DUE SOON
- Left edge: thin grey border
- Patient name + bed, drug, scheduled time
- No Mark Complete button — tasks cannot be completed before their window (completing a task before it is due opens a brief "Are you sure? This task is not due yet" confirmation first)

---

**Tapping anywhere on a task card** (except the Mark Complete button) navigates to that patient's detail page.

**Section 4 — COMPLETED** (neutral section heading, collapsible)

Positioned at the very bottom of the page, below UPCOMING. Collapsed by default. Header shows: "Completed today (7)" with a chevron to expand.

When expanded: each completed task card shows:
- Green left border
- Patient name + bed
- Drug name + **actual dose administered** (may differ from prescribed if nurse adjusted)
- "Given at [HH:MM]" — the actual completion time, not the scheduled time
- Nurse name who administered it (shown as "by [first name]")

This gives incoming shift nurses a quick reference of what was already given and when.

**When Mark Complete is triggered:**

The task transitions to COMPLETED and the corresponding time chip on the patient's Medications tab updates immediately (or on next poll) to show:
- Green background, checkmark icon
- Tapping the chip opens a popover: "Administered by [nurse full name] at [actual time] — [dose given]"

This popover is the single source of truth for medication administration records visible to all roles.

**Notification behaviour:**
- **5 minutes before** scheduled time: push notification fires — "Medication due in 5 minutes: [drug] for [patient] — Bed [N]"
- **5 minutes after** scheduled time (if still PENDING): push notification fires — "Medication overdue: [drug] for [patient] — Bed [N]", task status changes to OVERDUE and moves to the OVERDUE section

---

### 7.2 Nurse — Vitals Recording

Accessed from the Patient Detail page via a "Record Vitals" button. Opens as a full-screen page on mobile, a modal on web.

**Fields stacked vertically (exactly six inputs):**
1. **Pulse** (bpm) — numeric keyboard
2. **Systolic BP** (mmHg) — numeric keyboard
3. **Diastolic BP** (mmHg) — numeric keyboard, labelled "(Stored for reference — not used in risk score)"
4. **Respiratory Rate** (breaths/min) — numeric keyboard
5. **Temperature** (°C) — numeric keyboard, decimal allowed
6. **SpO₂** (%) — numeric keyboard, decimal allowed, label "Oxygen Saturation"

**Live Vitals Health Index preview** — shown prominently at the top of the form, updating in real time as the nurse fills each field:

A card showing:
- The label "Vitals Health Index"
- A large coloured status badge updating live: "STABLE" (green) / "WATCH" (amber) / "CRITICAL" (red)
- The numeric score beside the badge (e.g. "Score: 3")
- A one-line subtext that matches the badge: "Routine monitoring." / "Inform the floor doctor or re-check in 2 hours." / "Urgent medical attention required immediately."

The preview only shows once at least two fields are filled in. Before that, it shows a neutral "— Fill in vitals to see score" placeholder.

If the computed status is CRITICAL, an inline warning banner appears below the badge in a red-tinted box: "This patient will be flagged as Critical. A supervisor alert will be sent." This prepares the nurse before she saves.

The VHI preview is read-only and is for the nurse's reference only — it does not affect what gets saved. The score is computed and stored server-side on save.

Diastolic BP feeds into the permanent clinical record but shows 0 points next to it in any score breakdown view, so nurses understand why it is collected even though it is not scored.

**Save button** at the bottom. After saving: return to patient detail, new vitals row appears at the top of the Vitals tab history. The patient card acuity colour in any list view updates immediately.

---

### 7.3 Nurse — Recording Flow

Same as the Doctor Recording Flow (Section 6.2) with two differences:

**Screen 2 — Processing:** Shows a single step: "Transcribing your note" — no structuring or extraction steps.

**Screen 3 — Review:**
- **No SOAP sections.** Just a note type selector and a single text area.
- **Note type selector** (at the top): two options — "Handover Note" (pre-selected) or "Nursing Report"
- **Single text area** containing the Whisper transcription — fully editable, free text
- **Confirm and Save** button opens the same confirmation modal pattern (heading: "Save note?", summary: patient name + note type)

---

## 8. Supervisor Dashboard (Web Only)

The supervisor has one screen. No navigation, no sidebar. This IS the application for their role.

**URL:** `/supervisor/dashboard`  
**This is the landing screen after login.**

---

### 8.1 Dashboard Header

Fixed at the top, does not scroll.

- **Left:** Ward name (large heading), hospital name (smaller, below)
- **Right:** Current date and time updating live. Below that: a status indicator showing when data was last refreshed — "Updated 8 seconds ago" in small grey text. A small dot pulses briefly when a refresh is in progress.

If the supervisor manages more than one ward (future consideration), a ward selector appears in the header.

---

### 8.2 Stat Cards Row

Four cards in a horizontal row immediately below the header.

| Card | Content | Colour |
|------|---------|--------|
| Total Patients | Count of admitted patients in this ward | Neutral |
| Tasks Completed | "7 / 12" fraction | Green if ≥ 80%, amber if 50–79%, red if < 50% |
| Overdue Tasks | Count of OVERDUE tasks | Red text + red card background if > 0, neutral if 0 |
| Completion Rate | Percentage — "58%" | Green if ≥ 80%, amber if 50–79%, red if < 50% |

---

### 8.3 Overdue Alert Panel

**Only rendered when there is at least one overdue task.** Positioned immediately below the stat cards.

Red background, white text. The supervisor's eye must go here first when overdue tasks exist.

Heading: "⚠ Overdue Medication Tasks" with the count in brackets.

Each overdue task as a single row:
- Patient name + bed number
- Drug name + dose
- How long overdue: "22 min overdue" in bold
- Nurse assigned (first name + last initial)

**No action buttons.** The supervisor contacts the nurse through existing hospital communication channels (phone, bleep). CareRound's job is to surface the information, not replace communication.

---

### 8.4 Patient Grid

Below the alert panel. A grid of patient cards — 3 columns on desktop, 2 columns on tablet.

**Patient card:**
- **Left edge:** Solid vertical colour strip in acuity colour
- Patient full name, bold
- Bed number below the name as a small grey badge
- **VHI badge** — numeric score + status label (e.g. "4 WATCH") in the matching colour. This is the most actionable clinical number at a glance.
- Active medications: compact comma-separated list of drug names in small text
- Task status summary for today: "4 of 5 done" in green, or "1 overdue" in red, or "All complete ✓" in green
- Last vitals timestamp: "Vitals 1h ago" in small grey text

**Sort order:** RED acuity first, then patients with any overdue task, then AMBER, then GREEN. Within each colour group, highest VHI score first.

Tapping a patient card opens their detail page in read-only mode (Supervisor cannot write anything to patient records).

---

### 8.5 Hourly Completion Chart

Below the patient grid. A simple bar chart (Recharts).

- X-axis: Hours of the current shift (e.g. 07:00 through 19:00 for a day shift)
- Y-axis: Completion rate (0–100%)
- Each bar: filled with the same colour logic as the stat card — green ≥ 80%, amber 50–79%, red < 50%
- The current hour's bar is outlined with a dashed border to indicate it is in progress
- Tooltip on hover: "14:00 — 15:00: 3 of 4 tasks completed (75%)"

---

### 8.6 Polling Behaviour

The dashboard polls `GET /supervisor/dashboard?wardId=` every 10 seconds.

- Refresh is silent — no full-page reload, no flash
- If a refresh takes more than 2 seconds, a subtle spinner appears in the header next to the "Updated X seconds ago" text
- If a refresh fails, the timestamp turns amber and shows "Update failed — retrying"

---

## 9. Shared Patient Detail Page

All roles (Doctor, Nurse, Supervisor) access this page. The layout is the same across roles. What differs is which actions are available and which tabs show editable content.

**URL (web):** `/patients/:id`  
**Mobile:** Push screen from patient list

---

### Fixed Header (does not scroll)

| Element | Detail |
|---------|--------|
| Patient full name | Large, bold |
| Age + gender | e.g. "54 years, Male" — smaller, grey |
| Bed number | Small badge |
| Admission date | Small grey text — "Admitted 3 days ago" |
| Acuity badge | Coloured pill badge: RED / AMBER / GREEN |

**Floating Record button** (Doctor and Nurse only): teal circle with a microphone icon, fixed at the bottom-right of the screen. Visible on all 4 tabs. This is always accessible — the doctor or nurse never has to navigate away from what they are reading to start recording.

---

### Tabs

Four tabs below the fixed header: **Overview | Notes | Medications | Vitals**

---

#### Tab 1 — Overview

Purpose: give the doctor or nurse a 30-second brief before examining the patient.

**Admission Summary section:**
- Primary diagnosis
- Admission type (Emergency / Elective / Transfer)
- Attending doctor name

**Latest Vitals section** (always expanded, never collapsed):
Six stat mini-cards in a horizontal row: Pulse, Systolic BP, Diastolic BP, Respiratory Rate, Temp, SpO₂. Each mini-card shows the value and unit. The five scored inputs (all except Diastolic BP) show a small coloured dot in the corner indicating their individual contribution: green dot (0 points), amber dot (1–2 points), red dot (3 points).

Below the vitals row: a **Vitals Health Index card** — the total VHI score as a large number, the status badge (STABLE / WATCH / CRITICAL) in the matching colour, and the one-line guidance text. This is the most prominent clinical summary on the overview tab.

Below the VHI card: "Recorded [timestamp] by [nurse name]". If no vitals have been recorded today: amber banner — "No vitals recorded today".

**Most Recent Clinical Note section** (always expanded, never collapsed):
Shows the most recent note regardless of type. If it is an AI-generated ward round note, the full SOAP structure is rendered with labelled sections. If it is a free-text nurse note, it renders as a single block of text. Author name, role, and timestamp shown at the top of the note.

**Active Medications summary** (compact):
A flat horizontal list of active drug names as small pill badges. Tapping this area switches to the Medications tab.

---

#### Tab 2 — Notes

Purpose: the complete written record of every clinical interaction since admission, from all roles.

**A single scrollable timeline.** Oldest note at the top, newest at the bottom. The page auto-scrolls to the bottom when this tab is first opened so the most recent note is immediately visible.

**Each note is a content card.** Cards stack vertically with clear visual separation (16px gap).

Card anatomy:
- **Top-left:** Author full name + role (e.g. "Dr. Adeyemi — Doctor")
- **Top-right:** Date and time the note was created
- **Below header:** Type badge — a small coloured pill:
  - Ward Round Note — teal left border on the card
  - Progress Note — teal left border
  - Admission Note — blue left border
  - Discharge Note — purple left border
  - Handover Note — amber left border
  - Nursing Report — amber left border
- **Body:**
  - If AI-generated ward round/progress/admission/discharge note: four labelled content blocks — **Subjective**, **Objective**, **Assessment**, **Plan** — each preceded by its bold label
  - If manually typed note (any type): single block of plain text
  - If nurse handover/report note: single block of plain text

**Add Note button** — top-right of the tab, visible to Doctor and Nurse.
- Opens a modal with two fields:
  1. **Note type selector:** Doctor sees Ward Round Note / Progress Note / Admission Note / Discharge Note. Nurse sees Handover Note / Nursing Report.
  2. **Single text area** — free text, no imposed structure. The doctor writes however they choose. No SOAP fields here — manually typed notes are free form.
- Modal buttons: Cancel + Save. Saving appends the note to the bottom of the timeline.

**Record button** (floating, bottom-right) — visible to Doctor and Nurse. See Section 6.2 (Doctor) and Section 7.3 (Nurse) for the respective recording flows.

---

#### Tab 3 — Medications

Purpose: show what medications are prescribed, when each dose is scheduled, and the completion status of each dose. Anyone looking at this tab should understand the full picture within 10 seconds.

**One card per active prescription.** Stacked vertically.

**Card header:**
- **Top-left:** Drug name in large bold text
- **Below drug name:** Dose + route on the same line (e.g. "500mg — oral")
- **Below dose:** Frequency string (e.g. "Every 6 hours — 4 doses total")
- **Top-right (small grey text):** Prescribed by [doctor name] on [date]
- **Right edge:** Status badge — ACTIVE (green) / DISCONTINUED (grey)

**Administration times row:**
A horizontally scrollable row of administration time chips, one per scheduled dose.

Each chip shows:
- Time in HH:MM
- Status indicator on a second line

Chip states:
| State | Appearance | Tap behaviour |
|-------|-----------|---------------|
| Pending (not yet due) | Grey background | No action |
| Due soon (< 30 min) | Amber background | No action |
| Completed | Green background, checkmark | Popover: "Administered by [nurse full name] at [actual time] — [dose given]" |
| Overdue | Red background, exclamation | Popover: "Not administered — was due at [scheduled time]" |

**Nurse actions** (visible only to NURSE role):
- **Discontinue** button at the bottom-right of the card — opens destructive confirmation modal: "Discontinue [drug name]? This will cancel all future scheduled doses." On confirm: card moves to a greyed-out DISCONTINUED section at the bottom of the tab.
- Discontinued prescriptions remain visible — the clinical record must show what was previously prescribed.

**"+ Add Medication" button** (Nurse only) — top-right of the tab. Opens a modal with fields: Drug Name, Dose, Route, Frequency (hours between doses), Total Doses, Start Time. Saving creates a new prescription and chart entry and generates tasks — same async flow as doctor-confirmed prescriptions.

---

#### Tab 4 — Vitals

Purpose: trend visibility and individual reading reference.

**Chart section:**

A multi-line chart (Recharts on web, Victory Native or react-native-chart-kit on mobile) showing vitals over time. Each of the five scored vitals gets its own line (Diastolic BP is excluded from the chart — it is available in the table below).

- X-axis: date and time
- Y-axis: adapts to the range of values shown
- Lines: Pulse, Systolic BP, Respiratory Rate, Temperature, SpO₂ — each a distinct colour with a legend
- Background: three horizontal colour bands — green (VHI 0–2), amber (VHI 3–4), red (VHI 5+) — so clinical trends are visible without computing the score
- Default range: Last 48 hours
- Range selector below the chart: Last 24h | Last 48h | Last 7 days | Full admission

**Recordings table:**

Below the chart. Reverse-chronological order (newest at top).

Columns: Time | Pulse | Sys. BP | Dia. BP | Resp. Rate | Temp | SpO₂ | VHI | Recorded By

- **VHI column:** Shows both the numeric score and the colour badge together (e.g. "3 WATCH" in amber). More informative than colour alone.
- **Abnormal values:** Any individual input that scored 2 or 3 points is shown in the matching amber or red colour. Inputs scoring 0 points use standard text colour. Diastolic BP always displays in standard colour since it is not scored. This lets the reader see at a glance which value drove the VHI without computing it themselves.
- **Recorded By:** Nurse first name + last initial (e.g. "Sarah O.")

**"Record Vitals" button** (Nurse only) — top-right of the tab. Opens the Vitals Recording form (Section 7.2).

---

## 10. Mobile-Specific Notes

### Android APK for Demo
The demo uses an Android device. Distribute via APK (not Google Play) for the demo:
```bash
eas build --platform android --profile preview
# Produces an APK that can be installed directly via ADB or file transfer
```

### Audio Permissions
`expo-av` requires microphone permission. The permission prompt should be triggered the first time the doctor or nurse taps the Record button — not on app launch. Handle the denied state: show an inline message with a button linking to the device settings.

### Push Notifications
FCM token registration happens once after login for the Nurse role:
1. Call `expo-notifications.getExpoPushTokenAsync()` (or FCM token directly)
2. `PUT /users/me/device-token` with the token
3. Store the token in AsyncStorage to avoid re-registering on every login unless the token has changed

Handle foreground notifications (app is open) and background notifications (app is closed or backgrounded) separately — foreground can be shown as an in-app banner, background uses the system notification.

### Navigation Structure (Mobile)

**Doctor:**
```
(tabs)
├── patients/             ← landing after login
│   ├── index.tsx         ← Patient list
│   └── [id]/
│       ├── index.tsx     ← Patient detail (tabbed)
│       └── record.tsx    ← Recording flow (full-screen, no tabs)
└── profile/
    └── index.tsx
```

**Nurse:**
```
(tabs)
├── tasks/                ← landing after login
│   └── index.tsx         ← Task list
├── patients/
│   ├── index.tsx         ← Patient list
│   └── [id]/
│       ├── index.tsx     ← Patient detail (tabbed)
│       └── record.tsx    ← Recording flow
└── profile/
    └── index.tsx
```

### Offline / Poor Connectivity
The app does not need full offline support for the MVP, but it must fail gracefully. If a request fails due to network error:
- Show an inline "No connection — check your network" message
- Do not lose any data the user has entered (forms should retain their state through a network failure)
- The recording screen should still allow recording even if the network is unavailable — the audio is uploaded when connectivity is restored or the user retries

---

## 11. Component Inventory

A reference list of the reusable components to build. Build these first — the pages compose them.

### Shared (Web + Mobile where applicable)

| Component | Description |
|-----------|-------------|
| `VHIBadge` | Coloured badge showing VHI score + status. Props: `score: number`, `status: 'STABLE' \| 'WATCH' \| 'CRITICAL'`, `size`. Renders e.g. "3 WATCH" in amber. |
| `AcuityStrip` | Vertical left-edge colour strip for patient cards. Props: `color` |
| `PatientCard` | Full-width card with acuity strip and VHI badge. Used in patient lists. |
| `StatCard` | Number + label card. Used in Admin dashboard, Supervisor dashboard. |
| `ConfirmModal` | Reusable modal for confirmations. Props: `title`, `body`, `confirmLabel`, `confirmVariant` (primary / destructive), `onConfirm`, `onCancel` |
| `FormModal` | Reusable modal wrapper for forms. Props: `title`, `onClose`, `children` |
| `NoteCard` | Timeline note card. Props: `author`, `role`, `timestamp`, `noteType`, `content`, `isAiGenerated` |
| `MedicationCard` | Prescription card with time chips. Props: `prescription`, `nurseView` |
| `TimeChip` | Single administration time badge. Props: `time`, `status` (pending / due-soon / completed / overdue), `completedBy` |
| `EmptyState` | Empty list placeholder. Props: `icon`, `message`, `actionLabel`, `onAction` |
| `LoadingSpinner` | Inline loading indicator |
| `ErrorMessage` | Inline error with optional retry button |

### Web-Specific

| Component | Description |
|-----------|-------------|
| `AdminSidebar` | Left sidebar with navigation links and hospital name footer |
| `TopNav` | Top navigation bar for Doctor and Nurse roles |
| `VitalsChart` | Multi-line Recharts chart with VHI colour band background (green/amber/red zones) |
| `HourlyBarChart` | Supervisor hourly completion rate bar chart |
| `SupervisorHeader` | Fixed header with ward name, clock, and refresh indicator |
| `OverdueAlertPanel` | Red panel showing overdue task rows |

### Mobile-Specific

| Component | Description |
|-----------|-------------|
| `TabBar` | Bottom tab bar with optional badge |
| `RecordButton` | Floating teal microphone button |
| `Waveform` | Audio waveform animation. Active state (varying bars) and silent state (flat line + dot). |
| `ProcessingSteps` | Three-step progress indicator for the AI processing screen |
| `PressHoldButton` | Button requiring a press-hold to confirm. Props: `label`, `onConfirm`, `holdDuration` (ms), `variant` |
| `VitalsForm` | Six-field vitals input form (Pulse, Systolic BP, Diastolic BP, Resp. Rate, Temp, SpO₂) with live VHI score preview. Updates STABLE / WATCH / CRITICAL badge in real time. |
| `MobileVitalsChart` | React Native compatible vitals trend chart with VHI colour band background |