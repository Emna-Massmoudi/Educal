// cSpell:ignore formateur profil

import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm, NgModel } from '@angular/forms';
import { Router } from '@angular/router';
import {
  FormateurProfileUpdateRequest,
  FormateurResponse,
  FormateurService,
} from '../../services/formateur';

@Component({
  selector: 'app-teacher-profile',
  standalone: false,
  templateUrl: './teacher-profile.html',
  styleUrl: './teacher-profile.scss',
})
export class TeacherProfile implements OnInit {
  profil: FormateurResponse | null = null;
  loading = true;
  errorMessage = '';
  successMessage = '';

  editMode = false;
  saveLoading = false;
  editNom = '';
  editSpecialite = '';
  editBio = '';
  editPortfolio = '';

  nomFormateur = localStorage.getItem('nom') ?? 'Formateur';
  emailFormateur = localStorage.getItem('email') ?? '';
  formateurId = Number(localStorage.getItem('userId'));

  constructor(
    private readonly formateurService: FormateurService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chargerProfil();
  }

  chargerProfil(): void {
    this.loading = true;
    this.formateurService.getFormateurById(this.formateurId).subscribe({
      next: (data: FormateurResponse) => {
        this.profil = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Erreur chargement profil:', err);
        this.errorMessage = 'Erreur lors du chargement du profil';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  ouvrirEdition(): void {
    if (!this.profil) return;
    this.editNom = this.profil.nom;
    this.editSpecialite = this.profil.specialite ?? '';
    this.editBio = this.profil.bio ?? '';
    this.editPortfolio = this.profil.portfolio ?? '';
    this.editMode = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  fermerEdition(): void {
    this.editMode = false;
    this.errorMessage = '';
  }

  sauvegarderProfil(form: NgForm): void {
    if (form.invalid) {
      form.control.markAllAsTouched();
      this.errorMessage = 'Merci de corriger les champs signales avant de continuer.';
      return;
    }

    const nom = this.editNom.trim();
    const specialite = this.editSpecialite.trim();
    const bio = this.editBio.trim();
    const portfolio = this.editPortfolio.trim();

    if (!nom) {
      this.errorMessage = 'Le nom est obligatoire.';
      return;
    }

    if (portfolio && !this.isValidUrl(portfolio)) {
      this.errorMessage = 'Le portfolio doit etre une URL valide.';
      return;
    }

    const payload: FormateurProfileUpdateRequest = {
      nom,
      specialite,
      bio,
      portfolio,
    };

    this.saveLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.formateurService.updateFormateurProfile(this.formateurId, payload).subscribe({
      next: (profil: FormateurResponse) => {
        this.profil = profil;
        this.nomFormateur = profil.nom;
        localStorage.setItem('nom', profil.nom);
        this.successMessage = 'Profil mis a jour avec succes.';
        this.editMode = false;
        this.saveLoading = false;
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = '';
          this.cdr.detectChanges();
        }, 3000);
      },
      error: (err: unknown) => {
        console.error('Erreur mise a jour profil:', err);
        this.errorMessage = 'Erreur lors de la mise a jour du profil.';
        this.saveLoading = false;
        this.cdr.detectChanges();
      }
    });
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

  getPortfolioError(control: NgModel | null): string {
    if (!control?.errors) {
      return '';
    }
    if (control.errors['pattern']) {
      return 'Utilise une URL valide qui commence par http:// ou https://.';
    }
    return 'Verifie ce lien puis reessaie.';
  }

  getInitiales(nom: string): string {
    if (!nom?.trim()) return '?';
    return nom.trim().split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2);
  }

  getStatutLabel(status: string): string {
    const labels: Record<string, string> = {
      EN_ATTENTE: 'En attente de validation',
      ACTIVE: 'Compte actif',
      REFUSE: 'Candidature refusee',
      BLOQUE: 'Compte bloque',
    };
    return labels[status] ?? status;
  }

  getStatutClass(status: string): string {
    const classes: Record<string, string> = {
      EN_ATTENTE: 'attente',
      ACTIVE: 'actif',
      REFUSE: 'refuse',
      BLOQUE: 'bloque',
    };
    return classes[status] ?? '';
  }

  getFileUrl(path: string | null | undefined): string {
    if (!path) return '#';
    return `http://localhost:8081${path}`;
  }

  getPortfolioLinks(portfolio: string | null | undefined): string[] {
    if (!portfolio?.trim()) return [];

    return portfolio
      .split('|')
      .map((item) => item.trim())
      .filter((item) => item.length > 0);
  }

  getPortfolioLabel(link: string, index: number): string {
    try {
      const hostname = new URL(link).hostname.replace('www.', '');
      return hostname || `Lien ${index + 1}`;
    } catch {
      return `Lien ${index + 1}`;
    }
  }

  retourDashboard(): void {
    this.router.navigate(['/teacher']);
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
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
