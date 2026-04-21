# Codex Project Notes

## Project overview

Main project path:
- `C:\Users\GRATI\Desktop\edunet\E-Learning-platform-main`

Frontend:
- Angular
- path: `C:\Users\GRATI\Desktop\edunet\E-Learning-platform-main\elearning-frontend`

Backend:
- Spring Boot
- path: `C:\Users\GRATI\Desktop\edunet\E-Learning-platform-main\elearningplatform\elearning-api`

Database:
- MySQL
- expected database: `elearning_db`
- backend config file: `elearningplatform/elearning-api/src/main/resources/application.properties`

## What was done

### 1. Initial analysis

- Deep analysis of the project structure and main risks.
- Identified critical issues around backend authorization, inconsistent statuses, config/secrets, weak tests, and missing frontend guards.

### 2. Database and project setup

- Prepared the MySQL local setup for the backend.
- Verified Spring Boot could connect to MySQL and generate/use the schema.
- Restarted frontend and backend several times during the work when needed.

### 3. Angular template integration

Integrated the template from:
- `C:\Users\GRATI\Desktop\edunet\educal-online-learning-and-education-angular-template`

Applied safely into the existing Angular app without replacing business logic.

Main integration work:
- added global public layout
- added Educal-style header/footer
- added improved homepage
- preserved services, API calls, routing, models, forms, auth flow
- avoided copying demo/template fake logic into the real app

Important frontend files touched during template integration:
- `elearning-frontend/src/app/app-routing-module.ts`
- `elearning-frontend/src/app/app-module.ts`
- `elearning-frontend/src/styles.scss`
- `elearning-frontend/src/theme/educal-theme.scss`
- `elearning-frontend/src/app/pages/home/*`
- `elearning-frontend/src/app/pages/login/*`
- `elearning-frontend/src/app/pages/register/*`
- `elearning-frontend/src/app/pages/teacher-application/*`
- `elearning-frontend/src/app/pages/teacher-pending/*`
- shared layout components under `elearning-frontend/src/app/shared/layout/`

### 4. Student flow improvements

- Student login now redirects to `/home` instead of going directly to the old student page.
- Student registration now redirects to a new onboarding page where the student chooses interests/skills.
- Added interest-based behavior to the student experience.

Main student improvements:
- dashboard design improved
- `Mes dernieres inscriptions` is clickable
- clicking recent enrollment opens the course if valid
- available formations section now excludes courses already enrolled by the student
- recommendations are prioritized based on student interests
- catalogue shows all available published formations with search/filter

Main files:
- `elearning-frontend/src/app/pages/login/login.ts`
- `elearning-frontend/src/app/pages/register/register.ts`
- `elearning-frontend/src/app/pages/student-onboarding/*`
- `elearning-frontend/src/app/pages/student/student.ts`
- `elearning-frontend/src/app/pages/student/student.html`
- `elearning-frontend/src/app/pages/student/student.scss`

### 5. Teacher/formateur section fixes

Fixed several issues in the teacher area without changing the existing course API contract.

What was improved:
- safer course form payload normalization
- category/subcategory IDs normalized before submit
- add/edit course validation improved
- PDF upload validation improved
- separate add/edit PDF state handling
- teacher application validation improved

Main files:
- `elearning-frontend/src/app/pages/teacher/teacher.ts`
- `elearning-frontend/src/app/pages/teacher/teacher.html`
- `elearning-frontend/src/app/pages/teacher-application/teacher-application.ts`

### 6. Teacher profile real persistence

Originally, teacher profile editing only changed local frontend state / localStorage.

This was fixed by adding a real backend update flow:
- added frontend service method for profile update
- added backend DTO
- added backend controller endpoint
- added backend service method
- connected teacher profile page to the new backend endpoint

Main files:
- `elearning-frontend/src/app/services/formateur.ts`
- `elearning-frontend/src/app/pages/teacher-profile/teacher-profile.ts`
- `elearning-frontend/src/app/pages/teacher-profile/teacher-profile.html`
- `elearningplatform/elearning-api/src/main/java/com/elearning/elearning_api/dto/request/FormateurProfileUpdateRequest.java`
- `elearningplatform/elearning-api/src/main/java/com/elearning/elearning_api/controller/FormateurController.java`
- `elearningplatform/elearning-api/src/main/java/com/elearning/elearning_api/service/FormateurService.java`

### 7. Seed/demo data enrichment

Enriched the backend seed data so the site has visible content.

Added richer data in:
- `elearningplatform/elearning-api/src/main/java/com/elearning/elearning_api/config/DataInitializer.java`

Seed improvements:
- more categories
- more subcategories
- more courses
- more lessons
- more quizzes/questions/choices
- more demo students
- multiple enrollment statuses: `VALIDE`, `EN_ATTENTE`, `REFUSE`

Last verified seeded counts through the real API:
- `7` categories
- `13` sous-categories
- `14` courses
- `5` demo student enrollments for `student.demo@edunet.local`

## Demo accounts

Admin:
- email: `admin@gmail.com`
- password: `admin123`

Teacher active:
- email: `teacher.demo@edunet.local`
- password: `teacher123`

Teacher pending:
- email: `teacher.pending@edunet.local`
- password: `teacher123`

Student demo:
- email: `student.demo@edunet.local`
- password: `student123`

Additional students:
- `student.product@edunet.local` / `student123`
- `student.growth@edunet.local` / `student123`

## Verification done

Frontend:
- `npm.cmd run build` passed
- `npm.cmd test -- --watch=false` passed

Backend:
- `.\mvnw.cmd test` passed after MySQL/Docker was available

Real API checks:
- login checked for demo student and demo teacher
- categories/sous-categories/courses counts verified through authenticated API calls
- backend responded successfully on `/v3/api-docs`

## Current runtime status

Checked on `2026-04-15`:
- port `4200`: not listening at the moment
- port `8081`: not listening at the moment

So the frontend and backend are **not currently running right now**.

## How to run the project

### 1. Start Docker Desktop / MySQL

Make sure Docker Desktop is running, then start the MySQL container if needed:

```powershell
docker start elearning-mysql
```

### 2. Run backend

From:
- `C:\Users\GRATI\Desktop\edunet\E-Learning-platform-main\elearningplatform\elearning-api`

Command:

```powershell
.\mvnw.cmd spring-boot:run
```

Expected URL:
- `http://localhost:8081/swagger-ui.html`

### 3. Run frontend

From:
- `C:\Users\GRATI\Desktop\edunet\E-Learning-platform-main\elearning-frontend`

Command:

```powershell
npm.cmd start
```

Expected URL:
- `http://localhost:4200`

## Important notes

- The backend currently expects MySQL at `localhost:3306` with user `root` and empty password.
- JWT secret is still hardcoded in the backend and should be externalized later.
- There is still a non-blocking Angular CSS budget warning on:
  - `elearning-frontend/src/app/pages/student/student.scss`

## Recommended next steps

1. Add guards/role guards on sensitive frontend routes.
2. Secure backend ownership checks for teacher actions on courses/candidatures.
3. Externalize DB/JWT config into env variables or profiles.
4. Add real automated tests for business flows, not just smoke tests.
5. Continue improving admin/teacher/student dashboards visually with shared reusable UI components.
