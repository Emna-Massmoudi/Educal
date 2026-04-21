// cSpell:ignore etudiant inscription cours quiz lecon

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CoursService, Cours } from '../../services/cours';
import { InscriptionService, InscriptionResponse } from '../../services/inscription';
import { QuizService, LeconResponse, LeconRessource, EvaluationResponse, EvaluationSubmissionResponse } from '../../services/quiz';
import { AvisCoursService, AvisCoursResponse } from '../../services/avis-cours';
import { PaiementService, PaiementCoursResponse } from '../../services/paiement';
import { CourseProgressResponse, ProgressionService } from '../../services/progression';
import { CertificatService } from '../../services/certificat';
import { ToastService } from '../../services/toast.service';

type Section = 'dashboard' | 'catalogue' | 'mesCours' | 'profil' | 'cours';

@Component({
  selector: 'app-student',
  standalone: true,
  templateUrl: './student.html',
  styleUrl: './student.scss',
  imports: [CommonModule, FormsModule, RouterModule],
})
export class Student implements OnInit {
  nomEtudiant = localStorage.getItem('nom') ?? 'Etudiant';
  emailEtudiant = localStorage.getItem('email') ?? '';
  etudiantId = Number(localStorage.getItem('userId'));
  initiales = this.getInitiales(localStorage.getItem('nom') ?? '');

  section: Section = 'dashboard';
  mobileMenuOpen = false;

  catalogue: Cours[] = [];
  mesCours: InscriptionResponse[] = [];
  catalogueLoading = false;
  mesCoursLoading = false;
  errorMessage = '';
  catalogueError = '';
  mesCoursError = '';
  searchQuery = '';
  filterCat = '';
  interests: string[] = [];
  progressions = new Map<number, CourseProgressResponse>();
  progressLoading = false;
  downloadingCertificateCourseId: number | null = null;
  private readonly notifiedCertificateCodes = new Set<string>();
  private progressionsReady = false;

  inscriptionLoading = false;
  inscriptionSuccess = '';
  inscriptionError = '';
  showConfirmInscrit: Cours | null = null;
  paymentMethod: 'code' | 'card' = 'code';
  paymentCode = '';
  showConfirmAnnuler: InscriptionResponse | null = null;
  showAvisModal: InscriptionResponse | null = null;
  avisParCours = new Map<number, AvisCoursResponse>();
  avisNote = 5;
  avisCommentaire = '';
  avisLoading = false;
  avisSubmitting = false;
  avisError = '';

  coursActif: Cours | null = null;
  lecons: LeconResponse[] = [];
  leconActive: LeconResponse | null = null;
  leconLoading = false;
  pendingLessonIdToOpen: number | null = null;

  evaluations: EvaluationResponse[] = [];
  evalActive: EvaluationResponse | null = null;
  quizVue: 'liste' | 'passage' | 'resultat' = 'liste';

  reponsesEtudiant = new Map<number, number>();
  quizTimer = 0;
  quizTimerMax = 0;
  timerInterval: ReturnType<typeof setInterval> | null = null;
  quizLoading = false;

  scoreObtenu = 0;
  scoreTotal = 0;
  scorePct = 0;
  quizReussi = false;
  noteObtenue = 0;
  noteMax = 0;
  tentativeNumero = 0;
  derniereSoumission = '';
  quizError = '';
  pageFeedbackMessage = '';
  pageFeedbackTone: 'success' | 'error' | 'info' = 'info';

  constructor(
    private readonly coursService: CoursService,
    private readonly inscriptionService: InscriptionService,
    private readonly quizService: QuizService,
    private readonly avisCoursService: AvisCoursService,
    private readonly paiementService: PaiementService,
    private readonly progressionService: ProgressionService,
    private readonly certificatService: CertificatService,
    private readonly toastService: ToastService,
    private readonly sanitizer: DomSanitizer,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.restoreNotifiedCertificates();
    this.loadInterests();
    this.chargerCatalogue();
    this.chargerMesCours();
    this.chargerAvisEtudiant();
    this.chargerProgressions();
  }

  retryDashboardData(): void {
    this.chargerCatalogue();
    this.chargerMesCours();
    this.chargerProgressions();
  }

  retryCatalogue(): void {
    this.chargerCatalogue();
  }

  retryMesCours(): void {
    this.chargerMesCours();
    this.chargerProgressions();
  }

  loadInterests(): void {
    const key = `student_interests_${this.etudiantId || 'anonymous'}`;
    try {
      this.interests = JSON.parse(localStorage.getItem(key) ?? '[]');
    } catch {
      this.interests = [];
    }
  }

  chargerCatalogue(): void {
    this.catalogueLoading = true;
    this.catalogueError = '';
    this.coursService.getAllCours().subscribe({
      next: (data: Cours[]) => {
        this.catalogue = data.filter((course) => course.etatPublication === 'PUBLIE');
        this.catalogueLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
        this.catalogueLoading = false;
        this.catalogueError = 'Le catalogue est indisponible pour le moment.';
        this.cdr.detectChanges();
      },
    });
  }

  chargerMesCours(): void {
    this.mesCoursLoading = true;
    this.mesCoursError = '';
    this.inscriptionService.getByEtudiant(this.etudiantId).subscribe({
      next: (data: InscriptionResponse[]) => {
        this.mesCours = data;
        this.mesCoursLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
        this.mesCoursLoading = false;
        this.mesCoursError = 'Impossible de recuperer tes inscriptions pour le moment.';
        this.cdr.detectChanges();
      },
    });
  }

  chargerAvisEtudiant(): void {
    if (!this.etudiantId) {
      return;
    }

    this.avisLoading = true;
    this.avisCoursService.getByEtudiant(this.etudiantId).subscribe({
      next: (data: AvisCoursResponse[]) => {
        this.avisParCours = new Map(data.map((avis) => [avis.coursId, avis]));
        this.avisLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
        this.avisLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  chargerProgressions(): void {
    if (!this.etudiantId) {
      this.progressions.clear();
      return;
    }

    this.progressLoading = true;
    this.progressionService.getMyCourseProgress().subscribe({
      next: (data: CourseProgressResponse[]) => {
        this.progressions = new Map(data.map((progress) => [progress.coursId, progress]));
        this.handleNewCertificateNotifications(data);
        this.progressLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
        this.progressions.clear();
        this.progressLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  get availableCourses(): Cours[] {
    return this.catalogue.filter((course) => !this.isInscrit(course.id));
  }

  get recommendedCourses(): Cours[] {
    const courses = [...this.availableCourses];

    if (!this.interests.length) {
      return courses;
    }

    const interests = this.interests.map((item) => item.toLowerCase());

    return courses.sort((left, right) => this.getInterestScore(right, interests) - this.getInterestScore(left, interests));
  }

  get latestEnrollments(): InscriptionResponse[] {
    return [...this.mesCours]
      .sort((left, right) => {
        const leftDate = new Date(left.dateInscription ?? '').getTime();
        const rightDate = new Date(right.dateInscription ?? '').getTime();
        return rightDate - leftDate;
      })
      .slice(0, 4);
  }

  get dashboardCourses(): Cours[] {
    const prioritized = this.recommendedCourses.filter((course) => this.getInterestScore(course, this.interests.map((item) => item.toLowerCase())) > 0);
    return (prioritized.length ? prioritized : this.recommendedCourses).slice(0, 4);
  }

  filteredCatalogue(): Cours[] {
    return this.catalogue.filter((course) => {
      const q = this.searchQuery.toLowerCase().trim();
      const matchSearch =
        !q ||
        course.titre.toLowerCase().includes(q) ||
        (course.formateurNom ?? '').toLowerCase().includes(q) ||
        (course.categorieNom ?? '').toLowerCase().includes(q) ||
        (course.sousCategorieNom ?? '').toLowerCase().includes(q);
      const matchCat = !this.filterCat || (course.categorieNom ?? '') === this.filterCat;
      return matchSearch && matchCat;
    });
  }

  getCategories(): string[] {
    const categories = this.catalogue.map((course) => course.categorieNom ?? '').filter(Boolean);
    return [...new Set(categories)];
  }

  get totalInscrits(): number {
    return this.mesCours.length;
  }

  get coursActifs(): number {
    return this.mesCours.filter((item) => item.statut === 'VALIDE').length;
  }

  get coursEnAttente(): number {
    return this.mesCours.filter((item) => item.statut === 'EN_ATTENTE').length;
  }

  get recommendedCount(): number {
    return this.availableCourses.length;
  }

  get hasInterests(): boolean {
    return this.interests.length > 0;
  }

  get dashboardLoading(): boolean {
    return this.catalogueLoading || this.mesCoursLoading || this.progressLoading;
  }

  get earnedCertificates(): CourseProgressResponse[] {
    return Array.from(this.progressions.values())
      .filter((progress) => Boolean(progress.certificatDisponible))
      .sort((left, right) => {
        const leftDate = new Date(left.certificatDateObtention ?? '').getTime();
        const rightDate = new Date(right.certificatDateObtention ?? '').getTime();
        return rightDate - leftDate;
      });
  }

  openLastEnrollment(item: InscriptionResponse): void {
    if (item.statut === 'VALIDE') {
      this.reprendreCours(item);
      return;
    }

    this.section = 'mesCours';
  }

  goToMyCourses(): void {
    this.section = 'mesCours';
  }

  goToInterests(): void {
    this.router.navigate(['/student-onboarding']);
  }

  ouvrirCours(insc: InscriptionResponse, preferredLessonId: number | null = null): void {
    this.leconActive = null;
    this.evalActive = null;
    this.lecons = [];
    this.evaluations = [];
    this.quizVue = 'liste';
    this.pendingLessonIdToOpen = preferredLessonId ?? this.getResumeLessonId(insc.coursId);

    const cours = this.catalogue.find((item) => item.id === insc.coursId);
    if (cours) {
      this.coursActif = cours;
      this.section = 'cours';
      this.chargerLecons(cours.id);
      this.cdr.detectChanges();
      return;
    }

    this.coursService.getCoursById(insc.coursId).subscribe({
      next: (course: Cours) => {
        this.coursActif = course;
        this.section = 'cours';
        this.chargerLecons(course.id);
        this.cdr.detectChanges();
      },
      error: (err: unknown) => console.error(err),
    });
  }

  chargerLecons(coursId: number): void {
    this.leconLoading = true;
    this.quizService.getLeconsByCours(coursId).subscribe({
      next: (data: LeconResponse[]) => {
        this.lecons = data.sort((left, right) => left.ordre - right.ordre);
        this.leconLoading = false;
        const lessonIdToOpen = this.pendingLessonIdToOpen;
        this.pendingLessonIdToOpen = null;
        if (lessonIdToOpen) {
          const lessonToOpen = this.lecons.find((lesson) => lesson.id === lessonIdToOpen);
          if (lessonToOpen) {
            this.selectionnerLecon(lessonToOpen);
            return;
          }
        }
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
        this.leconLoading = false;
        this.pendingLessonIdToOpen = null;
        this.pageFeedbackMessage = 'Impossible de charger les lecons de ce cours pour le moment.';
        this.pageFeedbackTone = 'error';
        this.cdr.detectChanges();
      },
    });
  }

  selectionnerLecon(lecon: LeconResponse): void {
    this.leconActive = lecon;
    this.evalActive = null;
    this.quizVue = 'liste';
    this.quizError = '';
    this.progressionService.markLessonViewed(lecon.id).subscribe({
      next: (progress: CourseProgressResponse) => {
        this.progressions.set(progress.coursId, progress);
        this.cdr.detectChanges();
      },
      error: (err: unknown) => console.error(err),
    });
    this.quizService.getEvaluationsByLecon(lecon.id).subscribe({
      next: (data: EvaluationResponse[]) => {
        this.evaluations = data
          .filter((evaluation) => evaluation.publie)
          .sort((left, right) => (right.questionCount ?? 0) - (left.questionCount ?? 0));
        this.cdr.detectChanges();
      },
      error: (err: unknown) => console.error(err),
    });
    this.cdr.detectChanges();
  }

  retourLecons(): void {
    this.leconActive = null;
    this.evalActive = null;
    this.quizVue = 'liste';
  }

  getPdfUrl(url: string): SafeResourceUrl {
    const fullUrl = url.startsWith('http') ? url : `http://localhost:8081${url}`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(fullUrl);
  }

  getAssetUrl(url: string): string {
    return url.startsWith('http') ? url : `http://localhost:8081${url}`;
  }

  isYoutube(url: string): boolean {
    return url?.includes('youtube.com') || url?.includes('youtu.be');
  }

  getYoutubeEmbed(url: string): SafeResourceUrl {
    let videoId = '';
    if (url.includes('youtu.be/')) {
      videoId = url.split('youtu.be/')[1].split('?')[0];
    } else if (url.includes('v=')) {
      videoId = url.split('v=')[1].split('&')[0];
    }
    return this.sanitizer.bypassSecurityTrustResourceUrl(`https://www.youtube.com/embed/${videoId}`);
  }

  getLessonResources(type: 'VIDEO' | 'IMAGE' | 'DOCUMENT'): LeconRessource[] {
    return (this.leconActive?.ressources ?? []).filter((resource) => resource.type === type);
  }

  isPdfDocument(resource: LeconRessource): boolean {
    return resource.url.toLowerCase().endsWith('.pdf');
  }

  commencerQuiz(eval_: EvaluationResponse): void {
    this.reponsesEtudiant = new Map();
    this.scoreObtenu = 0;
    this.scorePct = 0;
    this.quizReussi = false;
    this.noteObtenue = 0;
    this.noteMax = eval_.noteMax;
    this.tentativeNumero = 0;
    this.derniereSoumission = '';
    this.quizError = '';
    this.quizLoading = true;
    this.quizVue = 'passage';

    this.quizService.getQuestionsByEvaluation(eval_.id).subscribe({
      next: (questions) => {
        this.evalActive = { ...eval_, questions };
        this.quizLoading = false;

        const nbQ = questions.length;
        this.quizTimerMax = nbQ > 0 ? nbQ * 120 : 300;
        this.quizTimer = this.quizTimerMax;
        this.demarrerTimer();
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
        this.quizLoading = false;
        this.quizVue = 'liste';
        this.quizError = 'Impossible de charger cette evaluation pour le moment.';
        this.cdr.detectChanges();
      },
    });
  }

  demarrerTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    this.timerInterval = setInterval(() => {
      this.quizTimer--;
      if (this.quizTimer <= 0) {
        this.soumettreQuiz();
      }
      this.cdr.detectChanges();
    }, 1000);
  }

  arreterTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  choisirReponse(questionId: number, choixId: number): void {
    this.reponsesEtudiant.set(questionId, choixId);
  }

  getReponseChoisie(questionId: number): number {
    return this.reponsesEtudiant.get(questionId) ?? -1;
  }

  soumettreQuiz(): void {
    this.arreterTimer();
    if (!this.evalActive) {
      return;
    }
    this.quizLoading = true;
    this.quizError = '';

    const payload = {
      reponses: Array.from(this.reponsesEtudiant.entries()).map(([questionId, choixId]) => ({
        questionId,
        choixId,
      })),
    };

    this.quizService.submitEvaluation(this.evalActive.id, payload).subscribe({
      next: (resultat: EvaluationSubmissionResponse) => {
        this.scoreObtenu = resultat.score;
        this.scoreTotal = resultat.totalPoints;
        this.scorePct = resultat.pourcentage;
        this.quizReussi = resultat.reussi;
        this.noteObtenue = resultat.noteObtenue;
        this.noteMax = resultat.noteMax;
        this.tentativeNumero = resultat.tentativeNumero;
        this.derniereSoumission = resultat.dateSoumission ?? '';
        this.quizLoading = false;
        this.quizVue = 'resultat';
        this.appliquerResultatEvaluation(resultat);
        this.chargerProgressions();
        this.cdr.detectChanges();
      },
      error: (err: { error?: { message?: string } }) => {
        console.error(err);
        this.quizLoading = false;
        this.quizVue = 'liste';
        this.quizError = err?.error?.message ?? "Impossible de soumettre cette evaluation pour le moment.";
        this.cdr.detectChanges();
      },
    });
  }

  recommencerQuiz(): void {
    if (this.evalActive) {
      this.commencerQuiz(this.evalActive);
    }
  }

  retourQuizListe(): void {
    this.arreterTimer();
    this.evalActive = null;
    this.quizVue = 'liste';
    this.quizError = '';
  }

  get timerMin(): string {
    return String(Math.floor(this.quizTimer / 60)).padStart(2, '0');
  }

  get timerSec(): string {
    return String(this.quizTimer % 60).padStart(2, '0');
  }

  get timerPct(): number {
    return this.quizTimerMax > 0 ? (this.quizTimer / this.quizTimerMax) * 100 : 100;
  }

  get timerDanger(): boolean {
    return this.quizTimer < 30;
  }

  get evalQuestions(): NonNullable<EvaluationResponse['questions']> {
    return this.evalActive?.questions ?? [];
  }

  get evalQuestionCount(): number {
    return this.evalQuestions.length;
  }

  isInscrit(coursId: number): boolean {
    return this.mesCours.some((item) => item.coursId === coursId && item.statut !== 'ANNULE');
  }

  getInscriptionByCourse(coursId: number): InscriptionResponse | undefined {
    return this.mesCours.find((item) => item.coursId === coursId && item.statut !== 'ANNULE');
  }

  openCourseDetail(coursId: number): void {
    this.router.navigate(['/formations', coursId]);
  }

  getCourseProgress(coursId: number): CourseProgressResponse | undefined {
    return this.progressions.get(coursId);
  }

  getCourseProgressPercent(coursId: number): number {
    return this.getCourseProgress(coursId)?.pourcentage ?? 0;
  }

  getCourseProgressLabel(coursId: number): string {
    const progress = this.getCourseProgress(coursId);

    if (!progress || !progress.totalLecons) {
      return 'Pret a commencer';
    }

    if (progress.certificatDisponible) {
      return 'Certificat disponible';
    }

    if (progress.termine) {
      return 'Termine';
    }

    if ((progress.evaluationsPubliees ?? 0) > 0 && progress.pourcentage >= 100) {
      return 'Evaluations a valider';
    }

    if (progress.leconsConsultees > 0) {
      return `${progress.pourcentage}% termine`;
    }

    return 'Pret a commencer';
  }

  getCourseProgressMeta(coursId: number): string {
    const progress = this.getCourseProgress(coursId);

    if (!progress || !progress.totalLecons) {
      return 'Aucune lecon ouverte pour le moment.';
    }

    if (progress.certificatDisponible) {
      return `Certificat pret avec le code ${progress.certificatCode ?? 'EduNet'}.`;
    }

    if (progress.termine) {
      return `Parcours complete avec ${progress.totalLecons} lecons consultees.`;
    }

    if ((progress.evaluationsPubliees ?? 0) > 0 && progress.pourcentage >= 100) {
      return `${progress.evaluationsValidees ?? 0}/${progress.evaluationsPubliees ?? 0} evaluations validees pour debloquer le certificat.`;
    }

    if (progress.derniereLeconTitre) {
      return `Derniere lecon: ${progress.derniereLeconTitre}`;
    }

    if (progress.prochaineLeconTitre) {
      return `Prochaine etape: ${progress.prochaineLeconTitre}`;
    }

    return `${progress.leconsConsultees}/${progress.totalLecons} lecons consultees.`;
  }

  getResumeLessonId(coursId: number): number | null {
    const progress = this.getCourseProgress(coursId);
    const lessonId = Number(progress?.prochaineLeconId ?? progress?.derniereLeconId ?? 0);
    return lessonId > 0 ? lessonId : null;
  }

  canContinueCourse(coursId: number): boolean {
    return this.getInscriptionByCourse(coursId)?.statut === 'VALIDE';
  }

  isCourseCompleted(coursId: number): boolean {
    return Boolean(this.getCourseProgress(coursId)?.termine);
  }

  hasCertificate(coursId: number): boolean {
    return Boolean(this.getCourseProgress(coursId)?.certificatDisponible);
  }

  isCertificateDownloading(coursId: number): boolean {
    return this.downloadingCertificateCourseId === coursId;
  }

  getCertificateDateLabel(coursId: number): string {
    const rawDate = this.getCourseProgress(coursId)?.certificatDateObtention;
    if (!rawDate) {
      return 'Certificat disponible';
    }

    return new Intl.DateTimeFormat('fr-TN', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    }).format(new Date(rawDate));
  }

  getCourseTitle(coursId: number): string {
    return this.catalogue.find((course) => course.id === coursId)?.titre
      ?? this.mesCours.find((item) => item.coursId === coursId)?.coursTitre
      ?? 'Formation EduNet';
  }

  openCertificate(coursId: number): void {
    const progress = this.getCourseProgress(coursId);
    const certificatId = Number(progress?.certificatId ?? 0);

    if (!progress?.certificatDisponible || certificatId <= 0) {
      return;
    }

    this.downloadingCertificateCourseId = coursId;
    const downloadRequest = this.certificatService.downloadById(certificatId).subscribe({
      next: (response) => {
        if (!response.body) {
          this.showPageFeedback('Le certificat est vide. Reessaie dans un instant.', 'error');
          return;
        }

        const downloadUrl = window.URL.createObjectURL(response.body);
        const anchor = document.createElement('a');
        anchor.href = downloadUrl;
        anchor.download = this.getCertificateFileName(coursId, response.headers.get('content-disposition'));
        anchor.click();
        window.setTimeout(() => window.URL.revokeObjectURL(downloadUrl), 1000);
        this.showPageFeedback('Le certificat est pret et le telechargement a commence.', 'success');
      },
      error: (err: { error?: { message?: string } }) => {
        console.error(err);
        this.showPageFeedback(
          err?.error?.message ?? 'Impossible de telecharger le certificat pour le moment.',
          'error',
        );
      },
    });
    downloadRequest.add(() => {
      this.downloadingCertificateCourseId = null;
      this.cdr.detectChanges();
    });
  }

  reprendreCours(inscription: InscriptionResponse): void {
    this.ouvrirCours(inscription, this.getResumeLessonId(inscription.coursId));
  }

  getCataloguePrimaryLabel(course: Cours): string {
    if (!this.isInscrit(course.id)) {
      return this.getInscriptionButtonLabel(course);
    }

    return this.canContinueCourse(course.id) ? 'Continuer' : 'Voir mes inscriptions';
  }

  handleCataloguePrimaryAction(course: Cours): void {
    if (!this.isInscrit(course.id)) {
      this.ouvrirConfirmInscrit(course);
      return;
    }

    if (this.canContinueCourse(course.id)) {
      const inscription = this.getInscriptionByCourse(course.id);
      if (inscription) {
        this.reprendreCours(inscription);
      }
      return;
    }

    this.section = 'mesCours';
  }

  ouvrirConfirmInscrit(cours: Cours): void {
    this.showConfirmInscrit = cours;
    this.inscriptionError = '';
    this.paymentMethod = 'code';
    this.paymentCode = '';
  }

  annulerConfirmInscrit(): void {
    this.showConfirmInscrit = null;
    this.paymentCode = '';
    this.inscriptionError = '';
  }

  confirmerInscription(): void {
    const cours = this.showConfirmInscrit;
    if (!cours) {
      return;
    }

    if (this.isPaidCourse(cours) && !this.paymentCode.trim()) {
      this.inscriptionError = 'Saisis le code recu apres ton transfert bancaire.';
      return;
    }

    this.inscriptionLoading = true;
    this.inscriptionError = '';

    if (this.isPaidCourse(cours)) {
      this.paiementService
        .submitCode({
          etudiantId: this.etudiantId,
          coursId: cours.id,
          codePaiement: this.paymentCode.trim(),
        })
        .subscribe({
          next: (payment: PaiementCoursResponse) => {
            const pending: InscriptionResponse = {
              id: payment.inscriptionId,
              etudiantId: this.etudiantId,
              etudiantNom: this.nomEtudiant,
              coursId: payment.coursId,
              coursTitre: payment.coursTitre,
              statut: 'EN_ATTENTE',
              dateInscription: new Date().toISOString(),
            };
            this.mesCours = this.upsertInscription(pending);
            this.inscriptionLoading = false;
            this.showConfirmInscrit = null;
            this.paymentCode = '';
            this.inscriptionSuccess = `Paiement de "${payment.coursTitre}" envoye pour validation.`;
            this.showPageFeedback('Ton code de paiement a ete envoye. L admin doit maintenant le valider.', 'success');
            this.scheduleClearInscriptionSuccess();
            this.cdr.detectChanges();
          },
          error: (err: { error?: { message?: string } }) => {
            console.error(err);
            this.inscriptionError = err?.error?.message ?? "Impossible d envoyer le paiement pour le moment.";
            this.inscriptionLoading = false;
            this.cdr.detectChanges();
          },
        });
      return;
    }

    this.inscriptionService
      .inscrire({
        etudiantId: this.etudiantId,
        coursId: cours.id,
      })
      .subscribe({
        next: (res: InscriptionResponse) => {
          this.mesCours = this.upsertInscription(res);
          this.inscriptionLoading = false;
          this.showConfirmInscrit = null;
          this.inscriptionSuccess = `Inscription a "${res.coursTitre}" envoyee.`;
          this.showPageFeedback('Ta demande d inscription a bien ete envoyee.', 'success');
          this.scheduleClearInscriptionSuccess();
          this.cdr.detectChanges();
        },
        error: (err: unknown) => {
          console.error(err);
          this.inscriptionError = "Impossible d envoyer l inscription pour le moment.";
          this.inscriptionLoading = false;
          this.cdr.detectChanges();
        },
      });
  }

  isPaidCourse(course: Cours | null | undefined): boolean {
    return Number(course?.prix ?? 0) > 0;
  }

  getPriceLabel(course: Cours | null | undefined): string {
    const amount = Number(course?.prix ?? 0);
    return amount > 0 ? `${amount.toFixed(2)} TND` : 'Gratuit';
  }

  getInscriptionButtonLabel(course: Cours): string {
    return this.isPaidCourse(course) ? 'Payer et s inscrire' : "S'inscrire";
  }

  ouvrirConfirmAnnuler(insc: InscriptionResponse): void {
    this.showConfirmAnnuler = insc;
  }

  annulerConfirmAnnuler(): void {
    this.showConfirmAnnuler = null;
  }

  confirmerAnnulation(): void {
    if (!this.showConfirmAnnuler) {
      return;
    }

    const id = this.showConfirmAnnuler.id;
    this.inscriptionLoading = true;
    this.inscriptionService.annuler(id).subscribe({
      next: () => {
        this.mesCours = this.mesCours.filter((item) => item.id !== id);
        this.inscriptionLoading = false;
        this.showConfirmAnnuler = null;
        this.showPageFeedback("L'inscription a bien ete annulee.", 'info');
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
        this.inscriptionLoading = false;
        this.showConfirmAnnuler = null;
        this.showPageFeedback("Impossible d annuler cette inscription pour le moment.", 'error');
        this.cdr.detectChanges();
      },
    });
  }

  getAvisForCourse(coursId: number): AvisCoursResponse | undefined {
    return this.avisParCours.get(coursId);
  }

  getCoursActifInscription(): InscriptionResponse | undefined {
    if (!this.coursActif) {
      return undefined;
    }
    return this.mesCours.find((item) => item.coursId === this.coursActif?.id && item.statut === 'VALIDE');
  }

  canReview(item: InscriptionResponse): boolean {
    return item.statut === 'VALIDE';
  }

  ouvrirAvis(item: InscriptionResponse): void {
    if (!this.canReview(item)) {
      this.showPageFeedback('Tu peux donner un avis uniquement apres validation de ton inscription.', 'info');
      return;
    }

    const avis = this.getAvisForCourse(item.coursId);
    this.showAvisModal = item;
    this.avisNote = avis?.note ?? 5;
    this.avisCommentaire = avis?.commentaire ?? '';
    this.avisError = '';
  }

  fermerAvis(): void {
    this.showAvisModal = null;
    this.avisError = '';
  }

  setAvisNote(note: number): void {
    this.avisNote = note;
  }

  submitAvis(): void {
    if (!this.showAvisModal) {
      return;
    }

    if (this.avisNote < 1 || this.avisNote > 5) {
      this.avisError = 'Choisis une note entre 1 et 5.';
      return;
    }

    this.avisSubmitting = true;
    this.avisError = '';
    this.avisCoursService.save({
      etudiantId: this.etudiantId,
      coursId: this.showAvisModal.coursId,
      note: this.avisNote,
      commentaire: this.avisCommentaire.trim(),
    }).subscribe({
      next: (avis: AvisCoursResponse) => {
        this.avisParCours.set(avis.coursId, avis);
        this.avisSubmitting = false;
        this.showAvisModal = null;
        this.showPageFeedback('Merci, ton avis a bien ete enregistre.', 'success');
        this.cdr.detectChanges();
      },
      error: (err: { error?: { message?: string } }) => {
        console.error(err);
        this.avisSubmitting = false;
        this.avisError = err?.error?.message ?? "Impossible d enregistrer ton avis pour le moment.";
        this.cdr.detectChanges();
      },
    });
  }

  getInterestLabel(): string {
    if (!this.interests.length) {
      return 'Aucun centre d interet defini';
    }
    return this.interests.join(' • ');
  }

  getInitiales(nom: string): string {
    if (!nom?.trim()) {
      return '?';
    }
    return nom
      .trim()
      .split(' ')
      .map((part: string) => part[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getIcon(categorieNom: string): string {
    const nom = (categorieNom ?? '').toLowerCase();
    if (nom.includes('informatique') || nom.includes('programmation') || nom.includes('developpement')) return '💻';
    if (nom.includes('langue') || nom.includes('anglais')) return '🌐';
    if (nom.includes('science') || nom.includes('math') || nom.includes('data')) return '🔬';
    if (nom.includes('design') || nom.includes('ux')) return '🎨';
    if (nom.includes('ia') || nom.includes('intelligence')) return '🤖';
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

  getStatutClass(statut: string): string {
    const map: Record<string, string> = {
      EN_ATTENTE: 'attente',
      VALIDE: 'valide',
      REFUSE: 'refuse',
      ANNULE: 'annule',
    };
    return map[statut] ?? '';
  }

  getStatutLabel(statut: string): string {
    const map: Record<string, string> = {
      EN_ATTENTE: 'En attente',
      VALIDE: 'Acceptee',
      REFUSE: 'Refusee',
      ANNULE: 'Annulee',
    };
    return map[statut] ?? statut;
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  private getInterestScore(course: Cours, interests: string[]): number {
    const haystack = [
      course.categorieNom,
      course.sousCategorieNom,
      course.titre,
      course.description,
      course.niveau,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();

    return interests.reduce((score, interest) => {
      return haystack.includes(interest) ? score + 1 : score;
    }, 0);
  }

  private appliquerResultatEvaluation(resultat: EvaluationSubmissionResponse): void {
    this.evaluations = this.evaluations.map((evaluation) =>
      evaluation.id === resultat.evaluationId
        ? {
            ...evaluation,
            derniereNote: resultat.noteObtenue,
            dernierScore: resultat.score,
            dernierTotalPoints: resultat.totalPoints,
            dernierPourcentage: resultat.pourcentage,
            derniereReussite: resultat.reussi,
            nombreTentatives: resultat.tentativeNumero,
            derniereSoumission: resultat.dateSoumission ?? evaluation.derniereSoumission ?? null,
          }
        : evaluation,
    );
  }

  private upsertInscription(inscription: InscriptionResponse): InscriptionResponse[] {
    const exists = this.mesCours.some((item) => item.id === inscription.id);
    if (exists) {
      return this.mesCours.map((item) => item.id === inscription.id ? inscription : item);
    }
    return [...this.mesCours, inscription];
  }

  private scheduleClearInscriptionSuccess(): void {
    setTimeout(() => {
      this.inscriptionSuccess = '';
      this.cdr.detectChanges();
    }, 3000);
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

  private getCertificateFileName(coursId: number, disposition: string | null): string {
    const headerMatch = disposition?.match(/filename="?([^"]+)"?/i);
    if (headerMatch?.[1]) {
      return headerMatch[1];
    }

    const titleSlug = this.getCourseTitle(coursId)
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)/g, '');

    return `certificat-edunet-${titleSlug || 'formation'}.pdf`;
  }

  private handleNewCertificateNotifications(progressions: CourseProgressResponse[]): void {
    const currentCertificateCodes = progressions
      .map((progress) => progress.certificatCode ?? '')
      .filter((code) => code.trim().length > 0);

    if (!this.progressionsReady) {
      if (this.notifiedCertificateCodes.size === 0) {
        currentCertificateCodes.forEach((code) => this.notifiedCertificateCodes.add(code));
        this.persistNotifiedCertificates();
      }
      this.progressionsReady = true;
      return;
    }

    const newlyAvailable = progressions.filter((progress) => {
      const code = progress.certificatCode?.trim();
      return Boolean(progress.certificatDisponible && code && !this.notifiedCertificateCodes.has(code));
    });

    if (!newlyAvailable.length) {
      return;
    }

    newlyAvailable.forEach((progress) => {
      const code = progress.certificatCode?.trim();
      if (!code) {
        return;
      }

      this.notifiedCertificateCodes.add(code);
      this.toastService.success(
        'Certificat disponible',
        `Ton certificat pour ${this.resolveCertificateCourseTitle(progress)} est pret au telechargement.`,
        5000,
      );
    });

    this.persistNotifiedCertificates();
  }

  private resolveCertificateCourseTitle(progress: CourseProgressResponse): string {
    return this.catalogue.find((course) => course.id === progress.coursId)?.titre
      ?? this.mesCours.find((item) => item.coursId === progress.coursId)?.coursTitre
      ?? (this.coursActif?.id === progress.coursId ? this.coursActif.titre : undefined)
      ?? 'ta formation';
  }

  private restoreNotifiedCertificates(): void {
    try {
      const storedCodes = JSON.parse(localStorage.getItem(this.getCertificateStorageKey()) ?? '[]') as string[];
      storedCodes
        .filter((code) => typeof code === 'string' && code.trim().length > 0)
        .forEach((code) => this.notifiedCertificateCodes.add(code));
    } catch {
      this.notifiedCertificateCodes.clear();
    }
  }

  private persistNotifiedCertificates(): void {
    localStorage.setItem(
      this.getCertificateStorageKey(),
      JSON.stringify(Array.from(this.notifiedCertificateCodes.values())),
    );
  }

  private getCertificateStorageKey(): string {
    return `student_certificate_notifications_${this.etudiantId || 'anonymous'}`;
  }
 getCoursFromCatalogue(coursId: number) {
  return this.catalogue.find(c => c.id === coursId);
}

getSafeImageUrl(coursId: number): string {
  const cours = this.getCoursFromCatalogue(coursId);

  if (!cours || !cours.imageUrl) {
    return '';
  }

  return cours.imageUrl.startsWith('http')
    ? cours.imageUrl
    : 'http://localhost:8081' + cours.imageUrl;
}
}
