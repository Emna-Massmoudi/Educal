import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Admin } from './pages/admin/admin';
import { Student } from './pages/student/student';
import { Teacher } from './pages/teacher/teacher';
import { TeacherApplication } from './pages/teacher-application/teacher-application';
import { TeacherPending } from './pages/teacher-pending/teacher-pending';
import { AdminFormateurs } from './pages/admin-formateurs/admin-formateurs';
import { Courses } from './pages/admin-cours/admin-cours';
import { TeacherProfile } from './pages/teacher-profile/teacher-profile';
import { AdminCoursDetail } from './pages/admin-cours-detail/admin-cours-detail';
import { HomeComponent } from './pages/home/home';
import { CategoryCoursesComponent } from './pages/category-courses/category-courses';
import { CourseDetailComponent } from './pages/course-detail/course-detail';
import { StudentOnboardingComponent } from './pages/student-onboarding/student-onboarding';
import { PublicShellComponent } from './shared/layout/public-shell/public-shell';
import { authGuard, roleGuard } from './shared/guards/auth-role.guard';

const routes: Routes = [
  {
    path: '',
    component: PublicShellComponent,
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: HomeComponent },
      { path: 'categories/:id', component: CategoryCoursesComponent },
      { path: 'formations/:id', component: CourseDetailComponent },
      { path: 'login', component: Login },
      { path: 'register', component: Register },
      { path: 'student-onboarding', component: StudentOnboardingComponent },
      { path: 'teacher-application', component: TeacherApplication },
      { path: 'teacher-pending', component: TeacherPending },
    ],
  },
  { path: 'admin',             component: Admin, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'student',           component: Student, canActivate: [authGuard, roleGuard(['ETUDIANT'])] },
  { path: 'teacher',           component: Teacher, canActivate: [authGuard, roleGuard(['FORMATEUR'])] },
  { path: 'admin-formateurs',  component: AdminFormateurs, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'admin-cours',       component: Courses, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'teacher-profile', component: TeacherProfile, canActivate: [authGuard, roleGuard(['FORMATEUR'])] },
  { path: 'admin/formations/:id', component: AdminCoursDetail, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: '**', redirectTo: 'home' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    anchorScrolling: 'enabled',
    scrollPositionRestoration: 'enabled',
  })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
