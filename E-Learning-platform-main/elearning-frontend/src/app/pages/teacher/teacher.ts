// cSpell:ignore ATTENTE VALIDATION BROUILLON SUPPRIME formateur cours categorie

import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { NgForm, NgModel } from '@angular/forms';
import { Router } from '@angular/router';
import { Cours, CoursRequest, CoursService, EtatCours } from '../../services/cours';
import { InscriptionResponse, InscriptionService } from '../../services/inscription';
import { Categorie, CategoryService, SousCategorie } from '../../services/category';
import { PaiementService, WalletFormateurResponse } from '../../services/paiement';

@Component({
  selector: 'app-teacher',
  standalone: false,
  templateUrl: './teacher.html',
  styleUrl: './teacher.scss',
})
export class Teacher implements OnInit {
  currentYear: number = new Date().getFullYear();
  activeSection: 'formations' | 'etudiants' | 'quiz' = 'formations';
  mobileMenuOpen = false;

  inscriptions: InscriptionResponse[] = [];
  inscLoading = false;
  inscFilter = '';
  inscActionLoading = false;

  nomFormateur = localStorage.getItem('nom') ?? 'Formateur';
  emailFormateur = localStorage.getItem('email') ?? '';
  initialesFormateur = this.getInitialesFromNom(localStorage.getItem('nom') ?? '');

  pdfUploading = false;
  newPdfNomFichier = '';
  editPdfNomFichier = '';

  mesCours: Cours[] = [];
  categories: Categorie[] = [];
  sousCategories: SousCategorie[] = [];
  loading = false;
  errorMessage = '';
  wallet: WalletFormateurResponse = {
    formateurId: Number(localStorage.getItem('userId')),
    totalGagne: 0,
    totalCommissionPlateforme: 0,
    paiementsApprouves: 0,
  };
  walletLoading = false;

  searchQuery = '';
  filterStatut = '';
  viewMode: 'grid' | 'list' = 'grid';

  showAddForm = false;
  addLoading = false;
  addError = '';
  addFormSubmitted = false;
  newCours: CoursRequest = this.createEmptyCourseRequest();

  showEditForm = false;
  editLoading = false;
  editError = '';
  editFormSubmitted = false;
  editCours: CoursRequest = this.createEmptyCourseRequest();
  itemToEdit: Cours | null = null;

  showDeleteConfirm = false;
  itemToDelete: Cours | null = null;
  deleteLoading = false;

  formationFeedbackMessage = '';
  formationFeedbackTone: 'success' | 'error' | 'info' = 'success';
  etudiantFeedbackMessage = '';
  etudiantFeedbackTone: 'success' | 'error' | 'info' = 'info';

  constructor(
    private readonly coursService: CoursService,
    private readonly inscriptionService: InscriptionService,
    private readonly categoryService: CategoryService,
    private readonly paiementService: PaiementService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chargerMesCours();
    this.chargerCategories();
    this.chargerWallet();
  }

  chargerMesCours(): void {
    this.loading = true;
    this.errorMessage = '';
    const formateurId = Number(localStorage.getItem('userId'));

    this.coursService.getCoursByFormateur(formateurId).subscribe({
      next: (data: Cours[]) => {
        this.mesCours = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Erreur chargement cours:', err);
        this.errorMessage = 'Erreur lors du chargement de vos cours';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerCategories(): void {
    this.categoryService.getAll().subscribe({
      next: (data: Categorie[]) => {
        this.categories = data;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => console.error('Erreur categories:', err)
    });
  }

  chargerWallet(): void {
    const formateurId = Number(localStorage.getItem('userId'));
    if (!formateurId) {
      return;
    }

    this.walletLoading = true;
    this.paiementService.getWalletFormateur(formateurId).subscribe({
      next: (wallet: WalletFormateurResponse) => {
        this.wallet = wallet;
        this.walletLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Erreur chargement wallet:', err);
        this.walletLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerSousCategories(categorieId: number): void {
    this.sousCategories = [];
    if (!categorieId) return;

    this.categoryService.getSousCategoriesByCategorieId(categorieId).subscribe({
      next: (data: SousCategorie[]) => {
        this.sousCategories = data;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => console.error('Erreur sous-categories:', err)
    });
  }

  filteredCours(): Cours[] {
    return this.mesCours.filter((cours) => {
      const q = this.searchQuery.toLowerCase().trim();
      const matchSearch =
        !q ||
        cours.titre.toLowerCase().includes(q) ||
        (cours.categorieNom ?? '').toLowerCase().includes(q) ||
        (cours.sousCategorieNom ?? '').toLowerCase().includes(q);
      const matchStatut = !this.filterStatut || cours.etatPublication === (this.filterStatut as EtatCours);
      return matchSearch && matchStatut;
    });
  }

  getCountByStatut(etat: EtatCours): number {
    return this.mesCours.filter((cours) => cours.etatPublication === etat).length;
  }

  openAddForm(): void {
    this.newCours = this.createEmptyCourseRequest();
    this.sousCategories = [];
    this.addError = '';
    this.addFormSubmitted = false;
    this.newPdfNomFichier = '';
    this.showAddForm = true;
  }

  closeAddForm(): void {
    this.showAddForm = false;
    this.addError = '';
    this.pdfUploading = false;
  }

  onCategorieChangeAdd(): void {
    this.addError = '';
    this.newCours.categorieId = Number(this.newCours.categorieId);
    this.newCours.sousCategorieId = 0;
    this.chargerSousCategories(this.newCours.categorieId);
  }

  soumettreCours(form: NgForm): void {
    this.addFormSubmitted = true;

    if (form.invalid) {
      form.control.markAllAsTouched();
      this.addError = 'Merci de corriger les champs signales avant de soumettre la formation.';
      return;
    }

    const validationError = this.validateCourseForm(this.newCours);
    if (validationError) {
      this.addError = validationError;
      return;
    }

    this.addLoading = true;
    this.addError = '';
    const payload = this.buildCoursPayload(this.newCours);

    this.coursService.createCours(payload).subscribe({
      next: (created: Cours) => {
        this.mesCours = [created, ...this.mesCours];
        this.addLoading = false;
        this.closeAddForm();
        this.showFormationFeedback('Formation soumise avec succes. Elle apparait maintenant dans ta liste.', 'success');
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Erreur creation:', err);
        this.addError = "Impossible de creer la formation pour le moment. Merci de reessayer.";
        this.addLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  openEditForm(item: Cours): void {
    this.itemToEdit = item;

    const categorieId =
      item.categorieId ||
      this.categories.find((cat) => cat.nom?.toLowerCase() === (item.categorieNom ?? '').toLowerCase())?.id ||
      0;

    this.editCours = {
      titre: item.titre ?? '',
      description: item.description ?? '',
      formateurId: item.formateurId ?? Number(localStorage.getItem('userId')),
      categorieId,
      sousCategorieId: item.sousCategorieId ?? 0,
      duree: item.duree ?? '',
      niveau: item.niveau ?? 'debutant',
      videoUrl: item.videoUrl ?? '',
      imageUrl: item.imageUrl ?? '',
      pdfUrl: item.pdfUrl ?? '',
      prix: Number(item.prix ?? 0),
    };

    this.editPdfNomFichier = this.getPdfFileName(item.pdfUrl);
    this.editError = '';
    this.editFormSubmitted = false;
    if (categorieId) {
      this.chargerSousCategories(categorieId);
    }
    this.showEditForm = true;
  }

  closeEditForm(): void {
    this.showEditForm = false;
    this.itemToEdit = null;
    this.editError = '';
    this.pdfUploading = false;
  }

  onCategorieChangeEdit(): void {
    this.editError = '';
    this.editCours.categorieId = Number(this.editCours.categorieId);
    this.editCours.sousCategorieId = 0;
    this.chargerSousCategories(this.editCours.categorieId);
  }

  mettreAJourCours(form: NgForm): void {
    if (!this.itemToEdit?.id) return;

    this.editFormSubmitted = true;

    if (form.invalid) {
      form.control.markAllAsTouched();
      this.editError = 'Merci de corriger les champs signales avant d enregistrer les modifications.';
      return;
    }

    const validationError = this.validateCourseForm(this.editCours);
    if (validationError) {
      this.editError = validationError;
      return;
    }

    this.editLoading = true;
    this.editError = '';
    const payload = this.buildCoursPayload(this.editCours);

    this.coursService.updateCours(this.itemToEdit.id, payload).subscribe({
      next: (updated: Cours) => {
        this.mesCours = this.mesCours.map((cours) => cours.id === updated.id ? updated : cours);
        this.editLoading = false;
        this.closeEditForm();
        this.showFormationFeedback('Formation mise a jour avec succes.', 'success');
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Erreur modification:', err);
        this.editError = 'Impossible d enregistrer les modifications pour le moment.';
        this.editLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  confirmDelete(item: Cours): void {
    this.itemToDelete = item;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm(): void {
    this.showDeleteConfirm = false;
    this.itemToDelete = null;
  }

  supprimerCours(): void {
    if (!this.itemToDelete?.id) return;

    this.deleteLoading = true;
    const id = this.itemToDelete.id;

    this.coursService.supprimerCours(id).subscribe({
      next: () => {
        this.mesCours = this.mesCours.filter((cours) => cours.id !== id);
        this.deleteLoading = false;
        this.closeDeleteConfirm();
        this.showFormationFeedback('La formation a ete supprimee.', 'success');
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Erreur suppression:', err);
        this.errorMessage = 'Impossible de supprimer cette formation pour le moment.';
        this.showFormationFeedback(this.errorMessage, 'error');
        this.deleteLoading = false;
        this.closeDeleteConfirm();
        this.cdr.detectChanges();
      }
    });
  }

  getStatutLabel(statut: string): string {
    const labels: Record<string, string> = {
      BROUILLON: 'Brouillon',
      EN_ATTENTE_VALIDATION: 'En attente',
      PUBLIE: 'Publie',
      SUPPRIME: 'Supprime',
    };
    return labels[statut] ?? statut;
  }

  getStatutClass(statut: string): string {
    const classes: Record<string, string> = {
      BROUILLON: 'brouillon',
      EN_ATTENTE_VALIDATION: 'attente',
      PUBLIE: 'publie',
      SUPPRIME: 'supprime',
    };
    return classes[statut] ?? '';
  }

  getInitialesFromNom(nom: string): string {
    if (!nom?.trim()) return '?';
    return nom.trim().split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2);
  }

  getIcon(categorieNom: string): string {
    const nom = (categorieNom ?? '').toLowerCase();
    if (nom.includes('informatique') || nom.includes('programmation') || nom.includes('developpement')) return '💻';
    if (nom.includes('langue') || nom.includes('anglais') || nom.includes('communication')) return '🌐';
    if (nom.includes('science') || nom.includes('math') || nom.includes('data')) return '🔬';
    if (nom.includes('design')) return '🎨';
    if (nom.includes('ia') || nom.includes('intelligence')) return '🤖';
    if (nom.includes('marketing')) return '📣';
    return '📚';
  }

  getBannerGradient(categorieId: number): string {
    const gradients = [
      'linear-gradient(135deg,#6366f1,#8b5cf6)',
      'linear-gradient(135deg,#8b5cf6,#6366f1)',
      'linear-gradient(135deg,#4f46e5,#7c3aed)',
      'linear-gradient(135deg,#7c3aed,#4f46e5)',
      'linear-gradient(135deg,#6366f1,#4f46e5)',
    ];
    return gradients[(categorieId ?? 0) % gradients.length];
  }

  get inscriptionsEnAttente(): number {
    return this.inscriptions.filter((inscription) => String(inscription.statut).trim() === 'EN_ATTENTE').length;
  }

  get totalInscriptions(): number {
    return this.inscriptions.length;
  }

  chargerInscriptions(): void {
    this.inscLoading = true;
    const formateurId = Number(localStorage.getItem('userId'));
    const coursIds = this.mesCours.map((cours) => cours.id);

    if (coursIds.length === 0) {
      this.coursService.getCoursByFormateur(formateurId).subscribe({
        next: (data: Cours[]) => {
          this.mesCours = data;
          const ids = data.map((cours) => cours.id);
          if (ids.length === 0) {
            this.inscLoading = false;
            this.cdr.detectChanges();
            return;
          }
          this.chargerInscriptionsPourCours(ids);
        },
        error: () => {
          this.inscLoading = false;
          this.cdr.detectChanges();
        }
      });
      return;
    }

    this.chargerInscriptionsPourCours(coursIds);
  }

  filteredInscriptions(): InscriptionResponse[] {
    if (!this.inscFilter) return this.inscriptions;
    return this.inscriptions.filter((inscription) => String(inscription.statut).trim() === String(this.inscFilter).trim());
  }

  accepterInscription(id: number): void {
    this.inscActionLoading = true;
    this.inscriptionService.updateStatut(id, 'VALIDE').subscribe({
      next: (updated: InscriptionResponse) => {
        this.inscriptions = this.inscriptions.map((inscription) => inscription.id === id ? updated : inscription);
        this.inscActionLoading = false;
        this.showEtudiantFeedback('Inscription acceptee avec succes.', 'success');
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
        this.inscActionLoading = false;
        this.showEtudiantFeedback("Impossible d accepter cette inscription pour le moment.", 'error');
        this.cdr.detectChanges();
      }
    });
  }

  refuserInscription(id: number): void {
    this.inscActionLoading = true;
    this.inscriptionService.updateStatut(id, 'REFUSE').subscribe({
      next: (updated: InscriptionResponse) => {
        this.inscriptions = this.inscriptions.map((inscription) => inscription.id === id ? updated : inscription);
        this.inscActionLoading = false;
        this.showEtudiantFeedback('Inscription refusee.', 'info');
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
        this.inscActionLoading = false;
        this.showEtudiantFeedback("Impossible de refuser cette inscription pour le moment.", 'error');
        this.cdr.detectChanges();
      }
    });
  }

  getInscStatutClass(statut: string): string {
    const map: Record<string, string> = {
      EN_ATTENTE: 'attente',
      VALIDE: 'valide',
      REFUSE: 'refuse',
      ANNULE: 'annule'
    };
    return map[statut] ?? '';
  }

  getInscStatutLabel(statut: string): string {
    const map: Record<string, string> = {
      EN_ATTENTE: 'En attente',
      VALIDE: 'Acceptee',
      REFUSE: 'Refusee',
      ANNULE: 'Annulee'
    };
    return map[statut] ?? statut;
  }

  formatPrice(value: number | undefined): string {
    const amount = Number(value ?? 0);
    return amount > 0 ? `${amount.toFixed(2)} TND` : 'Gratuit';
  }

  onPdfChange(event: Event, mode: 'new' | 'edit'): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];
    const targetError = mode === 'new' ? 'addError' : 'editError';

    this[targetError] = '';

    if (file.type !== 'application/pdf') {
      this[targetError] = 'Seuls les fichiers PDF sont autorises.';
      input.value = '';
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      this[targetError] = 'PDF trop volumineux (max 10 MB).';
      input.value = '';
      return;
    }

    this.pdfUploading = true;

    const reader = new FileReader();
    reader.onload = () => {
      const url = URL.createObjectURL(file);
      if (mode === 'new') {
        this.newCours.pdfUrl = url;
        this.newPdfNomFichier = file.name;
      } else {
        this.editCours.pdfUrl = url;
        this.editPdfNomFichier = file.name;
      }
      this.pdfUploading = false;
      this.cdr.detectChanges();
    };
    reader.readAsArrayBuffer(file);
  }

  removePdf(mode: 'new' | 'edit'): void {
    if (mode === 'new') {
      this.newCours.pdfUrl = '';
      this.newPdfNomFichier = '';
    } else {
      this.editCours.pdfUrl = '';
      this.editPdfNomFichier = '';
    }
    this.cdr.detectChanges();
  }

  getPdfDisplayName(mode: 'new' | 'edit'): string {
    return mode === 'new' ? this.newPdfNomFichier : this.editPdfNomFichier;
  }

  voirProfil(): void {
    this.router.navigate(['/teacher-profile']);
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  shouldShowError(control: NgModel | null, form: NgForm): boolean {
    return Boolean(control && control.invalid && (control.touched || form.submitted));
  }

  shouldShowSelectError(value: number | string | null | undefined, submitted: boolean): boolean {
    return submitted && !Number(value);
  }

  getRequiredError(label: string): string {
    return `${label} est obligatoire.`;
  }

  getUrlError(label: string): string {
    return `${label} doit commencer par http:// ou https://.`;
  }

  private chargerInscriptionsPourCours(coursIds: number[]): void {
    let loaded = 0;
    const allInscrits: InscriptionResponse[] = [];

    coursIds.forEach((coursId) => {
      this.inscriptionService.getByCours(coursId).subscribe({
        next: (data: InscriptionResponse[]) => {
          allInscrits.push(...data);
          loaded++;
          if (loaded === coursIds.length) {
            this.inscriptions = allInscrits;
            this.inscLoading = false;
            this.cdr.detectChanges();
          }
        },
        error: (err: unknown) => {
          console.error(err);
          loaded++;
          if (loaded === coursIds.length) {
            this.inscriptions = allInscrits;
            this.inscLoading = false;
            this.cdr.detectChanges();
          }
        }
      });
    });
  }

  private createEmptyCourseRequest(): CoursRequest {
    return {
      titre: '',
      description: '',
      formateurId: Number(localStorage.getItem('userId')),
      categorieId: 0,
      sousCategorieId: 0,
      duree: '',
      niveau: 'debutant',
      videoUrl: '',
      imageUrl: '',
      pdfUrl: '',
      prix: 0,
    };
  }

  private validateCourseForm(source: CoursRequest): string {
    if (!source.titre.trim()) return 'Le titre est obligatoire.';
    if (!source.categorieId) return 'Choisissez une categorie.';
    if (!source.sousCategorieId) return 'Choisissez une sous-categorie.';
    if (!source.description.trim()) return 'La description est obligatoire.';
    if (Number(source.prix ?? 0) < 0) return 'Le prix ne peut pas etre negatif.';
    if (source.videoUrl?.trim() && !this.isValidUrl(source.videoUrl)) return 'L URL video doit etre valide.';
    if (source.imageUrl?.trim() && !this.isValidUrl(source.imageUrl)) return 'L URL image doit etre valide.';
    if (source.pdfUrl?.trim() && source.pdfUrl.startsWith('http') && !this.isValidUrl(source.pdfUrl)) {
      return 'L URL du document doit etre valide.';
    }
    return '';
  }

  private buildCoursPayload(source: CoursRequest): CoursRequest {
    return {
      ...source,
      titre: source.titre.trim(),
      description: source.description.trim(),
      formateurId: Number(source.formateurId || localStorage.getItem('userId')),
      categorieId: Number(source.categorieId),
      sousCategorieId: Number(source.sousCategorieId),
      duree: source.duree?.trim() ?? '',
      niveau: source.niveau?.trim() || 'debutant',
      videoUrl: source.videoUrl?.trim() ?? '',
      imageUrl: source.imageUrl?.trim() ?? '',
      pdfUrl: source.pdfUrl?.trim() ?? '',
      prix: Number(source.prix ?? 0),
    };
  }

  private getPdfFileName(path: string | undefined): string {
    if (!path) return '';
    return path.split('/').pop() ?? 'Document existant';
  }

  private isValidUrl(value: string): boolean {
    try {
      const url = new URL(value);
      return ['http:', 'https:'].includes(url.protocol);
    } catch {
      return false;
    }
  }

  private showFormationFeedback(message: string, tone: 'success' | 'error' | 'info'): void {
    this.formationFeedbackMessage = message;
    this.formationFeedbackTone = tone;
    setTimeout(() => {
      if (this.formationFeedbackMessage === message) {
        this.formationFeedbackMessage = '';
        this.cdr.detectChanges();
      }
    }, 3200);
  }

  private showEtudiantFeedback(message: string, tone: 'success' | 'error' | 'info'): void {
    this.etudiantFeedbackMessage = message;
    this.etudiantFeedbackTone = tone;
    setTimeout(() => {
      if (this.etudiantFeedbackMessage === message) {
        this.etudiantFeedbackMessage = '';
        this.cdr.detectChanges();
      }
    }, 3200);
  }
}
