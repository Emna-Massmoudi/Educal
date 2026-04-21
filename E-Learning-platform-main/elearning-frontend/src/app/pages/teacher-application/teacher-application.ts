import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormateurService } from '../../services/formateur';

@Component({
  selector: 'app-teacher-application',
  templateUrl: './teacher-application.html',
  styleUrl: './teacher-application.scss',
  standalone: false,
})
export class TeacherApplication {
  specialite = '';
  bio = '';
  portfolio = '';
  github = '';
  linkedin = '';
  motivation = '';

  cvFile: File | null = null;
  diplomeFile: File | null = null;
  certificatFile: File | null = null;
  attestationFile: File | null = null;

  loading = false;
  successMessage = '';
  errorMessage = '';
  private readonly maxPdfSize = 10 * 1024 * 1024;

  constructor(
    private readonly formateurService: FormateurService,
    private readonly router: Router,
  ) {}

  onFileSelect(event: Event, type: string): void {
    this.errorMessage = '';
    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      return;
    }

    const file = input.files[0];

    if (file.type !== 'application/pdf') {
      this.errorMessage = 'Seuls les fichiers PDF sont autorises.';
      input.value = '';
      return;
    }

    if (file.size > this.maxPdfSize) {
      this.errorMessage = 'Chaque PDF doit faire moins de 10 MB.';
      input.value = '';
      return;
    }

    if (type === 'cv') this.cvFile = file;
    if (type === 'diplome') this.diplomeFile = file;
    if (type === 'certificat') this.certificatFile = file;
    if (type === 'attestation') this.attestationFile = file;
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    const userId = Number(localStorage.getItem('userId'));

    if (!userId) {
      this.errorMessage = "Impossible de trouver l'identifiant du formateur connecte.";
      return;
    }

    if (!this.specialite.trim()) {
      this.errorMessage = 'La specialite est obligatoire.';
      return;
    }

    if (!this.bio.trim()) {
      this.errorMessage = 'La bio est obligatoire.';
      return;
    }

    if (!this.motivation.trim()) {
      this.errorMessage = 'La motivation est obligatoire.';
      return;
    }

    if (!this.cvFile) {
      this.errorMessage = 'Le CV en PDF est obligatoire pour completer la candidature.';
      return;
    }

    const links = [this.portfolio.trim(), this.github.trim(), this.linkedin.trim()].filter((value) => value);
    const invalidLink = links.find((value) => !this.isValidUrl(value));

    if (invalidLink) {
      this.errorMessage = 'Les liens portfolio, GitHub et LinkedIn doivent etre des URL valides.';
      return;
    }

    const formData = new FormData();
    formData.append('specialite', this.specialite.trim());
    formData.append('bio', this.bio.trim());
    formData.append('portfolio', links.join(' | '));
    formData.append('motivation', this.motivation.trim());

    if (this.cvFile) formData.append('cv', this.cvFile);
    if (this.diplomeFile) formData.append('diplome', this.diplomeFile);
    if (this.certificatFile) formData.append('certificat', this.certificatFile);
    if (this.attestationFile) formData.append('attestation', this.attestationFile);

    this.loading = true;

    this.formateurService.uploadCandidature(userId, formData).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Votre candidature a ete envoyee avec succes.';

        setTimeout(() => {
          this.router.navigate(['/teacher-pending']);
        }, 900);
      },
      error: (error) => {
        this.loading = false;
        console.error(error);

        if (error?.error?.message) {
          this.errorMessage = error.error.message;
        } else {
          this.errorMessage = "Une erreur est survenue lors de l'envoi de la candidature.";
        }
      },
    });
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
