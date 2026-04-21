import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CategoryService, Categorie } from '../../services/category';
import { Cours, CoursService } from '../../services/cours';
import { InscriptionResponse, InscriptionService } from '../../services/inscription';
import { PaiementCoursResponse, PaiementService } from '../../services/paiement';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class HomeComponent implements OnInit {
  categories: Categorie[] = [];
  publishedCourses: Cours[] = [];
  enrolledCourseIds = new Set<number>();
  categoriesLoading = false;
  coursesLoading = false;
  categoriesError = false;
  coursesError = false;
  enrollingCourseId: number | null = null;
  enrollmentFeedbackMessage = '';
  enrollmentFeedbackTone: 'success' | 'error' | 'info' = 'info';
  showPaymentCourse: Cours | null = null;
  paymentCode = '';
  paymentError = '';
  paymentLoading = false;

  readonly highlights = [
    {
      title: 'Catalogue accessible',
      description: 'Parcourez librement les catégories et les cours disponibles avant de vous inscrire.',
    },
    {
      title: 'Parcours personnalisé',
      description: 'Accédez à votre espace dédié, suivez vos cours et mesurez votre progression.',
    },
    {
      title: 'Apprentissage flexible',
      description: 'Apprenez à votre rythme grâce à des cours structurés, des quiz interactifs et un suivi complet.',
    },
  ];

  constructor(
    private readonly categoryService: CategoryService,
    private readonly coursService: CoursService,
    private readonly inscriptionService: InscriptionService,
    private readonly paiementService: PaiementService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadPublishedCourses();
    this.loadStudentEnrollments();
  }

  retryAllHomeData(): void {
    this.loadCategories();
    this.loadPublishedCourses();
  }

  retryCategories(): void {
    this.loadCategories();
  }

  retryPublishedCourses(): void {
    this.loadPublishedCourses();
  }

  get isAuthenticated(): boolean {
    return Boolean(localStorage.getItem('token'));
  }

  get currentRole(): string {
    return localStorage.getItem('role') ?? '';
  }

  get currentUserId(): number {
    return Number(localStorage.getItem('userId'));
  }

  get canEnrollAsStudent(): boolean {
    return this.currentRole === 'ETUDIANT' && Number.isFinite(this.currentUserId) && this.currentUserId > 0;
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

  loadCategories(): void {
    this.categoriesLoading = true;
    this.categoriesError = false;

    this.categoryService.getPublicCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.categoriesLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.categoriesLoading = false;
        this.categoriesError = true;
        this.cdr.detectChanges();
      },
    });
  }

  loadPublishedCourses(): void {
    this.coursesLoading = true;
    this.coursesError = false;

    this.coursService.getPublicPublishedCours().subscribe({
      next: (courses) => {
        this.publishedCourses = [...courses]
          .sort((left, right) => {
            const ratingGap = this.getCourseRatingValue(right) - this.getCourseRatingValue(left);
            if (ratingGap !== 0) {
              return ratingGap;
            }

            const reviewsGap = this.getCourseReviewCount(right) - this.getCourseReviewCount(left);
            if (reviewsGap !== 0) {
              return reviewsGap;
            }

            return this.getCourseTimestamp(right) - this.getCourseTimestamp(left);
          })
          .slice(0, 6);
        this.coursesLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.coursesLoading = false;
        this.coursesError = true;
        this.cdr.detectChanges();
      },
    });
  }

  getCourseImage(course: Cours, index: number): string {
    if (course.imageUrl?.trim()) {
      return course.imageUrl;
    }

    const imageIndex = (index % 9) + 1;
    return `assets/img/course/course-${imageIndex}.jpg`;
  }

  getCategoryIcon(index: number): string {
    const icons = [
      'assets/img/icon/category/tablet.svg',
      'assets/img/icon/category/graph.svg',
      'assets/img/icon/category/home.svg',
      'assets/img/icon/category/paint.svg',
      'assets/img/icon/category/pie-chart.svg',
      'assets/img/icon/category/compass.svg',
    ];

    return icons[index % icons.length];
  }

  getBadgeClass(index: number): string {
    const classes = ['blue', 'green', 'orange', 'pink', 'blue-2', 'yellow'];
    return classes[index % classes.length];
  }

  getCourseExcerpt(course: Cours): string {
    const description = course.description?.trim();

    if (!description) {
      return 'Un parcours concret construit pour aider les apprenants a progresser avec une structure claire et accessible.';
    }

    return description.length > 130 ? `${description.slice(0, 127)}...` : description;
  }

  getCourseLevelLabel(course: Cours): string {
    switch (course.niveau) {
      case 'avance':
        return 'Avance';
      case 'intermediaire':
        return 'Intermediaire';
      case 'debutant':
        return 'Debutant';
      default:
        return 'Tous niveaux';
    }
  }

  getCourseRatingValue(course: Cours | null | undefined): number {
    const rating = Number(course?.noteMoyenne ?? 0);
    return Number.isFinite(rating) ? rating : 0;
  }

  getCourseReviewCount(course: Cours | null | undefined): number {
    const reviews = Number(course?.nombreAvis ?? 0);
    return Number.isFinite(reviews) ? reviews : 0;
  }

  getCourseRatingLabel(course: Cours | null | undefined): string {
    const rating = this.getCourseRatingValue(course);
    const reviews = this.getCourseReviewCount(course);

    if (!reviews) {
      return 'Nouveau cours';
    }

    return `${rating.toFixed(1)} / 5`;
  }

  getCourseRatingHint(course: Cours | null | undefined): string {
    const reviews = this.getCourseReviewCount(course);
    return reviews ? `${reviews} avis` : 'Aucun avis pour le moment';
  }

  getCourseCtaRoute(): string {
    return this.isAuthenticated ? this.dashboardRoute : '/login';
  }

  getCourseCtaLabel(): string {
    return this.isAuthenticated ? 'Ouvrir mon espace' : 'Se connecter pour consulter';
  }

  loadStudentEnrollments(): void {
    if (!this.canEnrollAsStudent) {
      this.enrolledCourseIds.clear();
      return;
    }

    this.inscriptionService.getByEtudiant(this.currentUserId).subscribe({
      next: (inscriptions: InscriptionResponse[]) => {
        this.enrolledCourseIds = new Set(
          inscriptions
            .filter((item) => item.statut !== 'ANNULE')
            .map((item) => item.coursId),
        );
        this.cdr.detectChanges();
      },
      error: () => {
        this.enrolledCourseIds.clear();
        this.cdr.detectChanges();
      },
    });
  }

  isCourseEnrolled(courseId: number): boolean {
    return this.enrolledCourseIds.has(courseId);
  }

  canEnroll(course: Cours): boolean {
    return this.canEnrollAsStudent && !this.isCourseEnrolled(course.id);
  }

  isEnrolling(courseId: number): boolean {
    return this.enrollingCourseId === courseId;
  }

  getCourseActionLabel(course: Cours): string {
    if (this.canEnroll(course)) {
      return this.isPaidCourse(course) ? 'Payer et s inscrire' : 'S inscrire';
    }

    if (this.canEnrollAsStudent && this.isCourseEnrolled(course.id)) {
      return 'Voir mes cours';
    }

    return this.getCourseCtaLabel();
  }

  getCourseMetaLabel(course: Cours): string {
    if (this.canEnrollAsStudent && this.isCourseEnrolled(course.id)) {
      return 'Inscription envoyee';
    }

    if (this.canEnroll(course)) {
      return this.getCoursePriceLabel(course);
    }

    return this.isAuthenticated ? 'Acces actif' : 'Connexion requise';
  }

  getCourseMetaLocked(course: Cours): boolean {
    return !this.canEnroll(course) && !this.isAuthenticated;
  }

  handleCourseAction(course: Cours): void {
    if (this.canEnroll(course)) {
      if (this.isPaidCourse(course)) {
        this.openPaymentModal(course);
        return;
      }
      this.enrollInCourse(course);
      return;
    }

    if (this.canEnrollAsStudent && this.isCourseEnrolled(course.id)) {
      this.router.navigate(['/student']);
      return;
    }

    this.router.navigate([this.getCourseCtaRoute()]);
  }

  isPaidCourse(course: Cours | null | undefined): boolean {
    return Number(course?.prix ?? 0) > 0;
  }

  getCoursePriceLabel(course: Cours | null | undefined): string {
    const amount = Number(course?.prix ?? 0);
    return amount > 0 ? `${amount.toFixed(2)} TND` : 'Gratuit';
  }

  openPaymentModal(course: Cours): void {
    this.showPaymentCourse = course;
    this.paymentCode = '';
    this.paymentError = '';
  }

  closePaymentModal(): void {
    this.showPaymentCourse = null;
    this.paymentCode = '';
    this.paymentError = '';
  }

  submitPaymentCode(): void {
    const course = this.showPaymentCourse;
    if (!course || this.paymentLoading) {
      return;
    }

    if (!this.paymentCode.trim()) {
      this.paymentError = 'Saisis le code recu apres ton transfert bancaire.';
      return;
    }

    this.paymentLoading = true;
    this.paymentError = '';
    this.paiementService.submitCode({
      etudiantId: this.currentUserId,
      coursId: course.id,
      codePaiement: this.paymentCode.trim(),
    }).subscribe({
      next: (payment: PaiementCoursResponse) => {
        this.enrolledCourseIds = new Set([...this.enrolledCourseIds, payment.coursId]);
        this.paymentLoading = false;
        this.closePaymentModal();
        this.showEnrollmentFeedback(`Code envoye pour ${payment.coursTitre}. Validation admin en attente.`, 'success');
        this.cdr.detectChanges();
      },
      error: (err: { error?: { message?: string } }) => {
        this.paymentLoading = false;
        this.paymentError = err?.error?.message ?? "Impossible d envoyer le code de paiement.";
        this.cdr.detectChanges();
      },
    });
  }

  private enrollInCourse(course: Cours): void {
    if (!this.canEnroll(course) || this.isEnrolling(course.id)) {
      return;
    }

    this.enrollingCourseId = course.id;
    this.enrollmentFeedbackMessage = '';

    this.inscriptionService
      .inscrire({
        etudiantId: this.currentUserId,
        coursId: course.id,
      })
      .subscribe({
        next: () => {
          this.enrolledCourseIds = new Set([...this.enrolledCourseIds, course.id]);
          this.enrollingCourseId = null;
          this.showEnrollmentFeedback(`Ton inscription a ${course.titre} a bien ete envoyee.`, 'success');
          this.cdr.detectChanges();
        },
        error: () => {
          this.enrollingCourseId = null;
          this.showEnrollmentFeedback("Impossible d'envoyer l'inscription pour le moment.", 'error');
          this.cdr.detectChanges();
        },
      });
  }

  private showEnrollmentFeedback(message: string, tone: 'success' | 'error' | 'info'): void {
    this.enrollmentFeedbackMessage = message;
    this.enrollmentFeedbackTone = tone;
    setTimeout(() => {
      if (this.enrollmentFeedbackMessage === message) {
        this.enrollmentFeedbackMessage = '';
        this.cdr.detectChanges();
      }
    }, 3200);
  }

  private getCourseTimestamp(course: Cours): number {
    const rawDate = course.dateMiseAJour ?? course.dateCreation ?? '';
    const timestamp = rawDate ? new Date(rawDate).getTime() : 0;
    return Number.isFinite(timestamp) ? timestamp : 0;
  }
}
