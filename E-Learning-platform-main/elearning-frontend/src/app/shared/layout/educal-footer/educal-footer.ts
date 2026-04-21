import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth';

@Component({
  selector: 'app-educal-footer',
  templateUrl: './educal-footer.html',
  standalone: true,
  imports: [CommonModule, RouterModule],
})
export class FooterComponent {
  currentYear = new Date().getFullYear();

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  get isAuthenticated(): boolean {
    return this.authService.isLoggedIn();
  }

  naviguerConnexion(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    const role = this.authService.getRole();

    if (role === 'ADMIN') {
      this.router.navigate(['/admin']);
    } else if (role === 'TEACHER') {
      this.router.navigate(['/teacher']);
    } else {
      this.router.navigate(['/student']);
    }
  }

  changeSection(section: string): void {
    const role = this.authService.getRole();

    if (role === 'ADMIN') {
      switch (section) {
        case 'dashboard':
          this.router.navigate(['/admin']);
          break;
        case 'catalogue':
          this.router.navigate(['/home'], { fragment: 'courses' });
          break;
        case 'mesCours':
          this.router.navigate(['/admin-cours']);
          break;
        case 'profil':
          this.router.navigate(['/admin']);
          break;
      }
    } else if (role === 'TEACHER') {
      switch (section) {
        case 'dashboard':
          this.router.navigate(['/teacher']);
          break;
        case 'catalogue':
          this.router.navigate(['/home'], { fragment: 'courses' });
          break;
        case 'mesCours':
          this.router.navigate(['/teacher']);
          break;
        case 'profil':
          this.router.navigate(['/teacher-profile']);
          break;
      }
    } else {
      switch (section) {
        case 'dashboard':
          this.router.navigate(['/student']);
          break;
        case 'catalogue':
          this.router.navigate(['/home'], { fragment: 'courses' });
          break;
        case 'mesCours':
          this.router.navigate(['/student']);
          break;
        case 'profil':
          this.router.navigate(['/student']);
          break;
      }
    }
  }
}