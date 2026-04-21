import { Component } from '@angular/core';
import { NgForm, NgModel } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, LoginRequest } from '../../services/auth';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrl: './login.scss',
  standalone: false,
})
export class Login {
  email = '';
  motDePasse = '';
  showPassword = false;
  rememberMe = false;

  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  onLogin(form: NgForm) {
    this.errorMessage = '';
    this.successMessage = '';

    if (form.invalid) {
      form.control.markAllAsTouched();
      this.errorMessage = 'Merci de corriger les champs signales avant de continuer.';
      return;
    }

    this.loading = true;

    const payload: LoginRequest = {
      email: this.email.trim(),
      motDePasse: this.motDePasse,
    };

    this.authService.login(payload).subscribe({
      next: (response) => {
        this.loading = false;

        localStorage.setItem('userId', response.id.toString());
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('email', response.email);
        localStorage.setItem('nom', response.nom);
        localStorage.setItem('status', response.status);

        this.successMessage = 'Connexion reussie';

        if (response.role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else if (response.role === 'ETUDIANT') {
          this.router.navigate(['/home']);
        } else if (response.role === 'FORMATEUR') {
          if (response.status === 'EN_ATTENTE') {
            this.router.navigate(['/teacher-pending']);
          } else if (response.status === 'ACTIVE') {
            this.router.navigate(['/teacher']);
          } else if (response.status === 'REFUSE') {
            this.router.navigate(['/teacher-pending']);
          } else {
            this.router.navigate(['/teacher-pending']);
          }
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Erreur login :', error);

        if (error?.error?.message) {
          this.errorMessage = error.error.message;
        } else {
          this.errorMessage = 'Email ou mot de passe incorrect. Verifie tes informations puis reessaie.';
        }
      },
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  shouldShowError(control: NgModel | null, form: NgForm): boolean {
    return Boolean(control && control.invalid && (control.touched || form.submitted));
  }

  getEmailError(control: NgModel | null): string {
    if (!control?.errors) {
      return '';
    }
    if (control.errors['required']) {
      return "L'email est obligatoire.";
    }
    if (control.errors['email']) {
      return 'Saisis une adresse email valide.';
    }
    return 'Verifie ce champ puis reessaie.';
  }

  getPasswordError(control: NgModel | null): string {
    if (!control?.errors) {
      return '';
    }
    if (control.errors['required']) {
      return 'Le mot de passe est obligatoire.';
    }
    if (control.errors['minlength']) {
      return 'Le mot de passe doit contenir au moins 6 caracteres.';
    }
    return 'Verifie ce champ puis reessaie.';
  }
}
