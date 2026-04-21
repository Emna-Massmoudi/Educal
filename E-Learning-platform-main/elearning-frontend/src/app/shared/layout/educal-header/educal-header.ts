import { CommonModule } from '@angular/common';
import { Component, HostListener } from '@angular/core';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-educal-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './educal-header.html',
  styleUrl: './educal-header.scss',
})
export class EducalHeaderComponent {
  headerSticky = false;
  showSidebar = false;
  currentPath = '/home';
  currentFragment = '';

  constructor(private readonly router: Router) {
    this.syncRouteState();
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.showSidebar = false;
        this.syncRouteState();
      });
  }

  @HostListener('window:scroll')
  onScroll(): void {
    this.headerSticky = window.scrollY > 12;
  }

  get isAuthenticated(): boolean {
    return Boolean(localStorage.getItem('token'));
  }

  get currentRole(): string {
    return localStorage.getItem('role') ?? '';
  }

  get displayName(): string {
    return localStorage.getItem('nom') ?? 'Mon espace';
  }

  get userInitials(): string {
    return this.displayName
      .split(' ')
      .filter(Boolean)
      .map((part) => part[0]?.toUpperCase() ?? '')
      .join('')
      .slice(0, 2) || 'ME';
  }

  get roleLabel(): string {
    switch (this.currentRole) {
      case 'ADMIN':
        return 'Administration';
      case 'FORMATEUR':
        return 'Formateur';
      case 'ETUDIANT':
        return 'Etudiant';
      default:
        return 'Utilisateur';
    }
  }

  get dashboardRoute(): string {
    switch (this.currentRole) {
      case 'ADMIN':
        return '/admin';
      case 'FORMATEUR':
        return '/teacher';
      case 'ETUDIANT':
        return '/student';
      default:
        return '/login';
    }
  }

  get teacherRoute(): string {
    return this.currentRole === 'FORMATEUR' ? '/teacher-application' : '/register';
  }

  get dashboardLabel(): string {
    switch (this.currentRole) {
      case 'ADMIN':
        return 'Espace admin';
      case 'FORMATEUR':
        return 'Espace formateur';
      case 'ETUDIANT':
        return 'Espace etudiant';
      default:
        return 'Mon espace';
    }
  }

  get shouldShowTeacherCta(): boolean {
    return !this.isAuthenticated;
  }

  isNavItemActive(path: string, fragment?: string): boolean {
    if (this.currentPath !== path) {
      return false;
    }

    if (!fragment) {
      return !this.currentFragment;
    }

    return this.currentFragment === fragment;
  }

  isTeacherCtaActive(): boolean {
    return this.currentPath === this.teacherRoute;
  }

  toggleSidebar(): void {
    this.showSidebar = !this.showSidebar;
  }

  closeSidebar(): void {
    this.showSidebar = false;
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  private syncRouteState(): void {
    const tree = this.router.parseUrl(this.router.url);
    const primarySegments = tree.root.children['primary']?.segments ?? [];
    const path = primarySegments.map((segment) => segment.path).join('/');
    this.currentPath = `/${path || 'home'}`;
    this.currentFragment = tree.fragment ?? '';
  }
}
