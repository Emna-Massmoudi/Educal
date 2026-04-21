import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';

import { CategoryService, Categorie, SousCategorie } from '../../services/category';
import { Cours, CoursService } from '../../services/cours';
import { InscriptionResponse, InscriptionService } from '../../services/inscription';
import { PageHeroComponent } from '../../shared/ui/page-hero/page-hero';

@Component({
  selector: 'app-category-courses',
  standalone: true,
  imports: [CommonModule, RouterModule, PageHeroComponent],
  templateUrl: './category-courses.html',
  styleUrl: './category-courses.scss',
})
export class CategoryCoursesComponent implements OnInit {
  categoryId = 0;
  category: Categorie | null = null;
  courses: Cours[] = [];
  enrolledCourseIds = new Set<number>();
  selectedSousCategorieId: number | null = null;
  loading = false;
  error = false;
  enrollingCourseId: number | null = null;
  enrollmentFeedbackMessage = '';
  enrollmentFeedbackTone: 'success' | 'error' | 'info' = 'info';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly categoryService: CategoryService,
    private readonly coursService: CoursService,
    private readonly inscriptionService: InscriptionService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const categoryId = Number(params.get('id'));
      this.categoryId = Number.isFinite(categoryId) ? categoryId : 0;
      this.loadCategoryCourses();
    });
    this.loadStudentEnrollments();
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

  get pageTitle(): string {
    return this.category?.nom || 'Formations par categorie';
  }

  get pageSubtitle(): string {
    if (this.category?.description?.trim()) {
      return this.category.description;
    }

    return 'Retrouvez toutes les formations disponibles dans cette categorie et connectez-vous pour acceder a votre espace d apprentissage.';
  }

  get eyebrow(): string {
    return this.category ? `Categorie ${this.category.nom}` : 'Catalogue EduNet';
  }

  get availableSousCategories(): SousCategorie[] {
    const byId = new Map<number, SousCategorie>();

    for (const sousCategorie of this.category?.sousCategories ?? []) {
      if (typeof sousCategorie.id === 'number') {
        byId.set(sousCategorie.id, sousCategorie);
      }
    }

    for (const course of this.courses) {
      if (typeof course.sousCategorieId === 'number' && course.sousCategorieNom?.trim()) {
        byId.set(course.sousCategorieId, {
          id: course.sousCategorieId,
          nom: course.sousCategorieNom,
          description: '',
          categorieId: this.categoryId,
        });
      }
    }

    return Array.from(byId.values()).sort((left, right) => left.nom.localeCompare(right.nom));
  }

  get filteredCourses(): Cours[] {
    if (this.selectedSousCategorieId === null) {
      return this.courses;
    }

    return this.courses.filter((course) => course.sousCategorieId === this.selectedSousCategorieId);
  }

  get hasSousCategorieFilter(): boolean {
    return this.availableSousCategories.length > 0;
  }

  get selectedSousCategorieName(): string {
    if (this.selectedSousCategorieId === null) {
      return 'Toutes les sous-categories';
    }

    return this.availableSousCategories.find((item) => item.id === this.selectedSousCategorieId)?.nom ?? 'Sous-categorie';
  }

  retry(): void {
    this.loadCategoryCourses();
  }

  selectSousCategorie(sousCategorieId: number | null): void {
    this.selectedSousCategorieId = sousCategorieId;
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
      },
      error: () => {
        this.enrolledCourseIds.clear();
      },
    });
  }

  loadCategoryCourses(): void {
    if (!this.categoryId) {
      this.category = null;
      this.courses = [];
      this.selectedSousCategorieId = null;
      this.loading = false;
      this.error = true;
      return;
    }

    this.loading = true;
    this.error = false;

    forkJoin({
      categories: this.categoryService.getPublicCategories(),
      courses: this.coursService.getPublicPublishedCours(),
    }).subscribe({
      next: ({ categories, courses }) => {
        this.category = categories.find((category) => category.id === this.categoryId) ?? null;
        this.courses = courses.filter((course) => course.categorieId === this.categoryId);
        this.selectedSousCategorieId = null;
        this.loading = false;
      },
      error: () => {
        this.category = null;
        this.courses = [];
        this.selectedSousCategorieId = null;
        this.loading = false;
        this.error = true;
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

  getCourseCtaRoute(): string {
    return this.isAuthenticated ? this.dashboardRoute : '/login';
  }

  getCourseCtaLabel(): string {
    return this.isAuthenticated ? 'Ouvrir mon espace' : 'Se connecter pour consulter';
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
      return 'S inscrire';
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
      return 'Disponible maintenant';
    }

    return this.isAuthenticated ? 'Acces actif' : 'Connexion requise';
  }

  getCourseMetaLocked(course: Cours): boolean {
    return !this.canEnroll(course) && !this.isAuthenticated;
  }

  handleCourseAction(course: Cours): void {
    if (this.canEnroll(course)) {
      this.enrollInCourse(course);
      return;
    }

    if (this.canEnrollAsStudent && this.isCourseEnrolled(course.id)) {
      this.router.navigate(['/student']);
      return;
    }

    this.router.navigate([this.getCourseCtaRoute()]);
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
        },
        error: () => {
          this.enrollingCourseId = null;
          this.showEnrollmentFeedback("Impossible d'envoyer l'inscription pour le moment.", 'error');
        },
      });
  }

  private showEnrollmentFeedback(message: string, tone: 'success' | 'error' | 'info'): void {
    this.enrollmentFeedbackMessage = message;
    this.enrollmentFeedbackTone = tone;
    setTimeout(() => {
      if (this.enrollmentFeedbackMessage === message) {
        this.enrollmentFeedbackMessage = '';
      }
    }, 3200);
  }
}
