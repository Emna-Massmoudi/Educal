import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';

import { AvisCoursResponse, AvisCoursService } from '../../services/avis-cours';
import { Cours, CoursService } from '../../services/cours';
import { InscriptionResponse, InscriptionService } from '../../services/inscription';
import { PaiementCoursResponse, PaiementService } from '../../services/paiement';
import { LeconPreviewResponse, QuizService } from '../../services/quiz';

@Component({
  selector: 'app-course-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './course-detail.html',
  styleUrl: './course-detail.scss',
})
export class CourseDetailComponent implements OnInit {
  courseId = 0;
  course: Cours | null = null;
  lessonPreviews: LeconPreviewResponse[] = [];
  reviews: AvisCoursResponse[] = [];
  relatedCourses: Cours[] = [];
  enrolledCourseIds = new Set<number>();

  loading = false;
  error = false;
  pageFeedbackMessage = '';
  pageFeedbackTone: 'success' | 'error' | 'info' = 'info';
  actionLoading = false;

  showPaymentModal = false;
  paymentCode = '';
  paymentError = '';
  paymentLoading = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly coursService: CoursService,
    private readonly quizService: QuizService,
    private readonly avisCoursService: AvisCoursService,
    private readonly inscriptionService: InscriptionService,
    private readonly paiementService: PaiementService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const courseId = Number(params.get('id'));
      this.courseId = Number.isFinite(courseId) ? courseId : 0;
      this.showPaymentModal = false;
      this.paymentCode = '';
      this.paymentError = '';
      this.loadCourseDetail();
      this.loadStudentEnrollments();
    });
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

  get hasCourseLoaded(): boolean {
    return Boolean(this.course);
  }

  get displayedRating(): number {
    const rating = Number(this.course?.noteMoyenne ?? 0);
    return Number.isFinite(rating) ? rating : 0;
  }

  get reviewCount(): number {
    const count = Number(this.course?.nombreAvis ?? 0);
    return Number.isFinite(count) ? count : 0;
  }

  get ratingLabel(): string {
    if (!this.reviewCount) {
      return 'Nouveau cours';
    }
    return `${this.displayedRating.toFixed(1)} / 5`;
  }

  get reviewSummaryLabel(): string {
    if (!this.reviewCount) {
      return 'Aucun avis pour le moment';
    }
    return `${this.reviewCount} avis verifies`;
  }

  get primaryActionLabel(): string {
    if (!this.course) {
      return 'Retour au catalogue';
    }

    if (this.canEnroll(this.course)) {
      return this.isPaidCourse(this.course) ? 'Payer et s inscrire' : 'S inscrire maintenant';
    }

    if (this.canEnrollAsStudent && this.isCourseEnrolled(this.course.id)) {
      return 'Voir mes cours';
    }

    if (!this.isAuthenticated) {
      return 'Se connecter pour continuer';
    }

    return 'Ouvrir mon espace';
  }

  get secondaryActionLabel(): string {
    return this.isAuthenticated ? 'Retour a mon espace' : 'Explorer les categories';
  }

  retry(): void {
    this.loadCourseDetail();
  }

  getCourseImage(course: Cours | null | undefined, fallbackSeed = 0): string {
    if (course?.imageUrl?.trim()) {
      return course.imageUrl;
    }

    const imageIndex = (fallbackSeed % 9) + 1;
    return `assets/img/course/course-${imageIndex}.jpg`;
  }

  getRelatedCourseImage(course: Cours, index: number): string {
    return this.getCourseImage(course, index + 2);
  }

  getCourseLevelLabel(course: Cours | null | undefined): string {
    switch (course?.niveau) {
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

  getCoursePriceLabel(course: Cours | null | undefined): string {
    const amount = Number(course?.prix ?? 0);
    return amount > 0 ? `${amount.toFixed(2)} TND` : 'Gratuit';
  }

  getReviewDateLabel(review: AvisCoursResponse): string {
    if (!review.dateCreation) {
      return 'Recemment';
    }

    return new Intl.DateTimeFormat('fr-TN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    }).format(new Date(review.dateCreation));
  }

  isPaidCourse(course: Cours | null | undefined): boolean {
    return Number(course?.prix ?? 0) > 0;
  }

  isCourseEnrolled(courseId: number): boolean {
    return this.enrolledCourseIds.has(courseId);
  }

  canEnroll(course: Cours | null | undefined): boolean {
    return Boolean(course && this.canEnrollAsStudent && !this.isCourseEnrolled(course.id));
  }

  goToSecondaryAction(): void {
    if (this.isAuthenticated) {
      this.router.navigate([this.dashboardRoute]);
      return;
    }

    this.router.navigate(['/home'], { fragment: 'categories' });
  }

  handlePrimaryAction(): void {
    const course = this.course;

    if (!course) {
      this.router.navigate(['/home'], { fragment: 'courses' });
      return;
    }

    if (this.canEnroll(course)) {
      if (this.isPaidCourse(course)) {
        this.openPaymentModal();
        return;
      }
      this.enrollInCourse(course);
      return;
    }

    if (this.canEnrollAsStudent && this.isCourseEnrolled(course.id)) {
      this.router.navigate(['/student']);
      return;
    }

    this.router.navigate([this.isAuthenticated ? this.dashboardRoute : '/login']);
  }

  openPaymentModal(): void {
    if (!this.course || !this.canEnroll(this.course)) {
      return;
    }

    this.showPaymentModal = true;
    this.paymentCode = '';
    this.paymentError = '';
  }

  closePaymentModal(): void {
    this.showPaymentModal = false;
    this.paymentCode = '';
    this.paymentError = '';
  }

  submitPaymentCode(): void {
    const course = this.course;
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
        this.showPageFeedback(`Code de paiement envoye pour ${payment.coursTitre}.`, 'success');
        this.cdr.detectChanges();
      },
      error: (err: { error?: { message?: string } }) => {
        this.paymentLoading = false;
        this.paymentError = err?.error?.message ?? "Impossible d envoyer le code de paiement.";
        this.cdr.detectChanges();
      },
    });
  }

  navigateToCourse(courseId: number): void {
    this.router.navigate(['/formations', courseId]);
  }

  private loadCourseDetail(): void {
    if (!this.courseId) {
      this.course = null;
      this.lessonPreviews = [];
      this.reviews = [];
      this.relatedCourses = [];
      this.loading = false;
      this.error = true;
      return;
    }

    this.loading = true;
    this.error = false;

    forkJoin({
      course: this.coursService.getPublicPublishedCoursById(this.courseId),
      lessons: this.quizService.getPublicLeconsByCours(this.courseId).pipe(
        catchError(() => of([] as LeconPreviewResponse[])),
      ),
      reviews: this.avisCoursService.getPublicByCours(this.courseId).pipe(
        catchError(() => of([] as AvisCoursResponse[])),
      ),
      courses: this.coursService.getPublicPublishedCours().pipe(
        catchError(() => of([] as Cours[])),
      ),
    }).subscribe({
      next: ({ course, lessons, reviews, courses }) => {
        this.course = course;
        this.lessonPreviews = [...lessons].sort((left, right) => left.ordre - right.ordre);
        this.reviews = reviews.slice(0, 6);
        this.relatedCourses = courses
          .filter((item) => item.id !== course.id && item.categorieId === course.categorieId)
          .sort((left, right) => {
            const ratingGap = Number(right.noteMoyenne ?? 0) - Number(left.noteMoyenne ?? 0);
            if (ratingGap !== 0) {
              return ratingGap;
            }

            return Number(right.nombreAvis ?? 0) - Number(left.nombreAvis ?? 0);
          })
          .slice(0, 3);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.course = null;
        this.lessonPreviews = [];
        this.reviews = [];
        this.relatedCourses = [];
        this.loading = false;
        this.error = true;
        this.cdr.detectChanges();
      },
    });
  }

  private loadStudentEnrollments(): void {
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

  private enrollInCourse(course: Cours): void {
    if (!this.canEnroll(course) || this.actionLoading) {
      return;
    }

    this.actionLoading = true;
    this.inscriptionService.inscrire({
      etudiantId: this.currentUserId,
      coursId: course.id,
    }).subscribe({
      next: () => {
        this.enrolledCourseIds = new Set([...this.enrolledCourseIds, course.id]);
        this.actionLoading = false;
        this.showPageFeedback(`Ton inscription a ${course.titre} a bien ete envoyee.`, 'success');
        this.cdr.detectChanges();
      },
      error: () => {
        this.actionLoading = false;
        this.showPageFeedback("Impossible d envoyer l inscription pour le moment.", 'error');
        this.cdr.detectChanges();
      },
    });
  }

  private showPageFeedback(message: string, tone: 'success' | 'error' | 'info'): void {
    this.pageFeedbackMessage = message;
    this.pageFeedbackTone = tone;
    setTimeout(() => {
      if (this.pageFeedbackMessage === message) {
        this.pageFeedbackMessage = '';
        this.cdr.detectChanges();
      }
    }, 3200);
  }
}
