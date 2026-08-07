# HRFlow

HRFlow is a Human Resources Management application built with **Spring Boot**. It covers the full
employee lifecycle: authentication, HR record management, leave management, attendance, simplified
payroll, and a statistics dashboard.

> Personal portfolio project — designed to run on free-tier hosting for the backend, database, and
> file storage.

---

## Table of contents

- [Tech stack](#tech-stack)
- [Feature modules](#feature-modules)
- [Architecture](#architecture)
- [Notable technical choices](#notable-technical-choices)
- [Configuration & environment variables](#configuration--environment-variables)
- [Running locally](#running-locally)
- [Project structure](#project-structure)
- [Known limitations](#known-limitations)
- [Possible improvements](#possible-improvements)

---

## Tech stack

| Area | Choice |
|---|---|
| Language / Framework | Java 21, Spring Boot 3 |
| Security | Spring Security, JWT |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| File storage | S3-compatible (MinIO locally, Cloudflare R2 / Backblaze B2 in production) |
| Email templating | Thymeleaf (HTML emails) |
| PDF generation | OpenPDF |
| API documentation | springdoc-openapi (Swagger) |
| Build | Maven |

---

## Feature modules

### 1. Authentication
- Two account creation flows:
  - **Self-registration** (`/api/auth/register`): the user sets their own password, email verified
    through a single-use link.
  - **Third-party creation** (e.g. HR creating an employee): account created without a password,
    activated through an email link (24h-valid token).
- JWT-based login (access token + refresh token).
- Password reset (1h-valid token).
- Sensitive tokens (activation, verification, reset) are **hashed at rest**, never stored in plain text.
- Anti-enumeration responses on `forgot-password` / `resend-*` (same generic response whether the
  account exists or not).

### 2. Employee management
- Create, view, update, archive, and soft-delete employees.
- Profile photo upload (to S3-compatible object storage).
- Department and position assignment.
- Change history (position, department, status).
- Paginated list with search by name, department, position, status.
- Export the list as CSV or PDF.

### 3. Leave management
- 4 leave types: Annual, Sick, Maternity/Paternity, Unpaid.
- Employee submission, with overlapping-dates detection.
- Automatic calculation of requested working days (weekends excluded).
- Approval / rejection by the direct manager (or HR/Admin), with a mandatory comment on rejection.
- Leave balance calculated and reserved at submission time (not only on approval).
- Cancellation allowed as long as the leave hasn't started yet.
- Monthly team calendar view and full history per employee.

### 4. Attendance management
- Check-in / check-out (server-side timestamp, never supplied by the client).
- Automatic late detection (against the employee's expected start time, with a configurable grace
  period).
- Monthly report: worked hours vs. contractual hours, present / late / absent / on-leave days.
- Unjustified absences are **computed on demand** (a working day with no check-in and no approved
  leave), never stored as an event.

### 5. Payroll (simplified)
- Base salary per employee.
- Automatic deduction prorated to unjustified absence days (daily rate = salary ÷ working days in the
  month).
- PDF payslip generation, stored privately.
- Controlled download through an application endpoint (bucket URLs are never exposed publicly).
- Payslip history per employee.

### 6. Dashboard & statistics
- Active headcount and month-over-month evolution.
- Leave requests pending approval, with urgent requests flagged separately.
- Employee distribution by department (pie chart).
- 12-month headcount trend (line chart).
- Monthly attendance rate by department (bar chart).
- Alerts: birthdays today, contracts expiring within 30 days.
- New hires widget for the current month.

---

## Architecture

![ ](hrflow-backend/docs/images/HRFlow_Architecture.png)


## Configuration & environment variables

Configuration is split by Spring profiles (`dev` / `prod`). No secrets are committed — a `.env.example`
file documents the expected variables.

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` or `prod` |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection |
| `JWT_SECRET` | JWT signing key |
| `STORAGE_ENDPOINT` | S3-compatible endpoint (MinIO locally, R2/B2 in production) |
| `STORAGE_ACCESS_KEY` / `STORAGE_SECRET_KEY` | Object storage credentials |
| `STORAGE_BUCKET` | Bucket name |
| `STORAGE_PUBLIC_URL` | Public base URL of the bucket (public files only) |
| `STORAGE_REGION` | Region (`auto` for R2) |
| `FRONTEND_URL` | Frontend URL, used in links sent by email |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` | Email sending configuration |

Copy `.env.example` to `.env` and fill in the values before running the project.

---

## Running locally

### Prerequisites
- Java 21+
- Maven
- Docker (for PostgreSQL and MinIO)

### Steps

```bash
# 1. Clone the project
git clone <repo-url>
cd hrflow-backend

# 2. Copy and fill in environment variables
cp .env.example .env

# 3. Start infrastructure services (PostgreSQL + MinIO)
docker compose up -d

# 4. Run the application
./mvnw spring-boot:run
```

The API is then available at `http://localhost:8080`, the Swagger documentation at
`http://localhost:8080/swagger-ui.html`, and the MinIO console at `http://localhost:9001`.

---

## Project structure

```
src/main/java/com/hrflow/hrflow_backend/
├── config/            Spring configuration (security, typed properties, S3, etc.)
├── controller/          REST endpoints
├── dto/                  API input/output objects
│   ├── attendance/
│   ├── auth/
│   ├── dashboard/
│   ├── department/
│   ├── employee/
│   ├── leaves/
│   └── payslip/
├── entity/               JPA entities
├── enums/                 Business enums (roles, statuses, types)
├── exceptionHandler/       Business exceptions + centralized error handling
├── mapper/                 Entity → DTO conversion
├── repository/             Data access (Spring Data JPA)
├── security/                 JWT, authentication filters
├── service/                   Business logic
├── storage/                    File storage abstraction (S3-compatible)
└── utils/                       Utilities (working-days calculation, etc.)
```

---

## Known limitations

Some simplifications were deliberately made to keep the project within the scope of a portfolio piece:

- **No salary history**: only the current salary is stored; a mid-year salary change is not reflected
  retroactively on already-generated payslips.
- **Public holidays not handled**: only weekends are excluded from working-day calculations (leave,
  attendance, payroll).
- **A single department per employee** for now (the model can evolve to a many-to-many relationship if
  needed).
- **Single timezone**: check-in/check-out timestamps use the server's local time, no multi-timezone
  support.
- **Department attendance rate**: computed on a simplifying assumption (active headcount × working
  days in the month), without finely adjusting for hires/departures mid-month.

---

## Possible improvements

- Salary history (`SalaryHistory`) for retroactively accurate payslips.
- Public holiday management by country/year.
- Support for multiple departments per employee (already anticipated in the design choices).
- Time-limited signed URLs for all private files (currently handled via an application proxy for
  payslips).
- Real-time notifications (WebSocket) for leave approvals.

---

## License

Personal project for demonstration purposes (portfolio). Not intended for production use without
further security review.