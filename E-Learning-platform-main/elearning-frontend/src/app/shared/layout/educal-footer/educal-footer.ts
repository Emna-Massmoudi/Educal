import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth';
// OU chemin relatif depuis le fichier :
// import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-educal-footer',
  templateUrl: './educal-footer.html',
  standalone: true,
  imports: [RouterModule],  
})
export class FooterComponent {
  currentYear = new Date().getFullYear();

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

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
}