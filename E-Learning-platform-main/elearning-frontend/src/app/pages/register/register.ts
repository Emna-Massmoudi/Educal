import { Component } from '@angular/core';
import { NgForm, NgModel } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, RegisterRequest } from '../../services/auth';

@Component({
  selector: 'app-register',
  templateUrl: './register.html',
  styleUrl: './register.scss',
  standalone: false,
})
export class Register {
  nom = '';
  email = '';
  motDePasse = '';
  confirmationMotDePasse = '';
  role = 'ETUDIANT';
  portfolio = '';
  showPassword = false;
  showConfirmPassword = false;

  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  onRegister(form: NgForm) {
    this.errorMessage = '';
    this.successMessage = '';

    if (form.invalid) {
      form.control.markAllAsTouched();
      this.errorMessage = 'Merci de corriger les champs signales avant de continuer.';
      return;
    }

    if (this.motDePasse.trim().length < 6) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 6 caracteres.';
      return;
    }

    if (this.motDePasse !== this.confirmationMotDePasse) {
      this.errorMessage = 'Les deux mots de passe ne correspondent pas.';
      return;
    }

    if (this.role === 'FORMATEUR' && this.portfolio.trim() && !this.isValidUrl(this.portfolio.trim())) {
      this.errorMessage = 'Le portfolio doit etre une URL valide.';
      return;
    }

    this.loading = true;

    const payload: RegisterRequest = {
      nom: this.nom.trim(),
      email: this.email.trim(),
      motDePasse: this.motDePasse,
      role: this.role,
      portfolio: this.role === 'FORMATEUR' ? this.portfolio.trim() : '',
    };

    this.authService.register(payload).subscribe({
      next: (response) => {
        this.loading = false;

        localStorage.setItem('userId', response.id.toString());
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('email', response.email);
        localStorage.setItem('nom', response.nom);
        localStorage.setItem('status', response.role === 'FORMATEUR' ? 'EN_ATTENTE' : 'ACTIVE');

        this.successMessage = 'Compte cree avec succes.';

        setTimeout(() => {
          if (response.role === 'FORMATEUR') {
            this.router.navigate(['/teacher-application']);
          } else {
            this.router.navigate(['/student-onboarding']);
          }
        }, 1000);
      },
      error: (error) => {
        this.loading = false;
        console.error('Erreur register :', error);

        if (error?.error?.motDePasse) {
          this.errorMessage = error.error.motDePasse;
        } else if (error?.error?.nom) {
          this.errorMessage = error.error.nom;
        } else if (error?.error?.email) {
          this.errorMessage = error.error.email;
        } else if (error?.error?.message) {
          this.errorMessage = error.error.message;
        } else {
          this.errorMessage = "Impossible de creer le compte pour le moment. Merci de reessayer.";
        }
      },
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  shouldShowError(control: NgModel | null, form: NgForm): boolean {
    return Boolean(control && control.invalid && (control.touched || form.submitted));
  }

  getNameError(control: NgModel | null): string {
    if (!control?.errors) {
      return '';
    }
    if (control.errors['required']) {
      return 'Le nom complet est obligatoire.';
    }
    if (control.errors['minlength']) {
      return 'Saisis au moins 3 caracteres.';
    }
    return 'Verifie ce champ puis reessaie.';
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

  getConfirmPasswordError(control: NgModel | null): string {
    if (!control) {
      return '';
    }
    if (!control.value?.trim()) {
      return 'Confirme ton mot de passe.';
    }
    if (this.motDePasse !== this.confirmationMotDePasse) {
      return 'Les deux mots de passe doivent etre identiques.';
    }
    return '';
  }

  getPortfolioError(control: NgModel | null): string {
    if (!control?.errors) {
      return '';
    }
    if (control.errors['pattern']) {
      return 'Utilise une URL valide qui commence par http:// ou https://.';
    }
    return 'Verifie ce lien puis reessaie.';
  }

  shouldShowConfirmPasswordError(control: NgModel | null, form: NgForm): boolean {
    return Boolean(control && (control.touched || form.submitted) && (!control.value?.trim() || this.motDePasse !== this.confirmationMotDePasse));
  }

  private isValidUrl(value: string): boolean {
    try {
      const url = new URL(value);
      return ['http:', 'https:'].includes(url.protocol);
    } catch {
      return false;
    }
  }
}
