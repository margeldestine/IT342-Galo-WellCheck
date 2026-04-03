# WellCheck — Guidance Booking & Appointment System

WellCheck is a specialized guidance booking and appointment system designed to modernize the way students access mental health and wellness support within academic institutions. It provides a centralized platform for students to schedule guidance check-ins online, streamlining the booking process for both students and counselors.

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA |
| Database | PostgreSQL 14+ (Supabase) |
| Web Frontend | React 18, Axios |
| Mobile | Kotlin, XML-Based UI, Retrofit |
| Build Tools | Maven (Backend), npm (Web), Gradle (Android) |

---

## 👥 User Roles

- **Student** — Register, browse counselors, book appointments, manage their own appointments
- **Counselor** — Manage availability slots, approve/reject appointment requests
- **Admin** — Approve/reject counselor registrations, monitor system activity

---

## ✨ Features

### Authentication
- Email/password registration and login
- Google OAuth2 sign in/sign up
- JWT-based authentication
- Role-based access control (Student, Counselor, Admin)

### Student
- Browse and search counselors by name or specialization
- View counselor availability slots
- Multi-step appointment booking
- View and cancel appointments
- Student profile management with School ID upload

### Counselor
- Create, edit, and delete availability slots
- View and manage appointment requests (approve/reject)
- Dashboard with appointment statistics

### Admin
- Approve or reject counselor registrations
- Deactivate counselor accounts
- Monitor all system appointments and users

---

## 📁 Project Structure
```
IT342-Galo-WellCheck/
├── backend/         # Spring Boot REST API
│   └── wellcheck/
├── web/             # React Web Application
└── mobile/          # Android (Kotlin) Application
```

---

## 👩‍💻 Developer

**Margel Destine Krizia F. Galo**

IT342 — System Integration and Architecture
