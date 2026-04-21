import { ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Cours, CoursService } from '../../services/cours';
import {
  EvaluationRequest,
  EvaluationResponse,
  LeconRequest,
  LeconRessource,
  LeconResponse,
  QuestionRequest,
  QuestionResponse,
  QuizService,
  TypeEvaluation,
  TypeRessourceLecon,
} from '../../services/quiz';

type LeconResourceDraft = LeconRessource & { uploading?: boolean };

@Component({
  selector: 'app-teacher-quiz',
  standalone: true,
  templateUrl: './teacher-quiz.html',
  styleUrls: ['./teacher-quiz.scss'],
  imports: [CommonModule, FormsModule],
})
export class TeacherQuiz implements OnInit {
  @ViewChild('lessonEditor') lessonEditor?: ElementRef<HTMLDivElement>;

  vue: 'cours' | 'lecons' | 'quiz' | 'questions' = 'cours';

  mesCours: Cours[] = [];
  lecons: LeconResponse[] = [];
  evaluations: EvaluationResponse[] = [];
  questions: QuestionResponse[] = [];

  coursSelectionne: Cours | null = null;
  leconSelectionnee: LeconResponse | null = null;
  evalSelectionnee: EvaluationResponse | null = null;

  loading = false;
  formateurId = Number(localStorage.getItem('userId'));

  showLeconForm = false;
  leconForm: LeconRequest = { titre: '', description: '', contenuHtml: '', ordre: 1, coursId: 0, pdfUrl: '', ressources: [] };
  leconToEdit: LeconResponse | null = null;
  leconLoading = false;
  leconError = '';
  leconResources: LeconResourceDraft[] = [];
  leconImageUploading = false;
  leconAttachmentUploading = false;

  showEvalForm = false;
  evalForm: EvaluationRequest = { titre: '', type: 'QUIZ', noteMax: 20, noteMin: 14, leconId: 0 };
  evalToEdit: EvaluationResponse | null = null;
  evalLoading = false;
  evalError = '';

  showQuestionForm = false;
  questionForm: QuestionRequest = { enonce: '', point: 1, evaluationId: 0, choix: [] };
  questionToEdit: QuestionResponse | null = null;
  questionLoading = false;
  questionError = '';
  choixTemp: { texte: string; estCorrect: boolean }[] = [
    { texte: '', estCorrect: false },
    { texte: '', estCorrect: false },
  ];

  showDeleteConfirm = false;
  deleteTarget: { type: 'lecon' | 'eval' | 'question'; id: number; label: string } | null = null;
  deleteLoading = false;

  noticeMessage = '';
  noticeTone: 'success' | 'error' = 'success';

  constructor(
    private readonly coursService: CoursService,
    private readonly quizService: QuizService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.chargerMesCours();
  }

  chargerMesCours(): void {
    this.loading = true;
    this.coursService.getCoursByFormateur(this.formateurId).subscribe({
      next: (data) => {
        this.mesCours = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error(error);
        this.loading = false;
        this.setNotice('Impossible de charger vos cours.', 'error');
      },
    });
  }

  selectionnerCours(cours: Cours): void {
    this.coursSelectionne = cours;
    this.loading = true;
    this.noticeMessage = '';

    this.quizService.getLeconsByCours(cours.id).subscribe({
      next: (data) => {
        this.lecons = [...data].sort((left, right) => left.ordre - right.ordre);
        this.loading = false;
        this.vue = 'lecons';
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error(error);
        this.loading = false;
        this.setNotice('Impossible de charger les lecons de ce cours.', 'error');
      },
    });
  }

  selectionnerLecon(lecon: LeconResponse): void {
    this.leconSelectionnee = lecon;
    this.evalSelectionnee = null;
    this.questions = [];
    this.loading = true;

    this.quizService.getEvaluationsByLecon(lecon.id).subscribe({
      next: (data) => {
        this.evaluations = [...data].sort((left, right) => Number(right.publie) - Number(left.publie));
        this.loading = false;
        this.vue = 'quiz';
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error(error);
        this.loading = false;
        this.setNotice("Impossible de charger les evaluations de cette lecon.", 'error');
      },
    });
  }

  selectionnerEval(evaluation: EvaluationResponse): void {
    this.evalSelectionnee = evaluation;
    this.loading = true;

    this.quizService.getQuestionsByEvaluation(evaluation.id).subscribe({
      next: (questions) => {
        this.questions = questions;
        this.evalSelectionnee = { ...evaluation, questions };
        this.loading = false;
        this.vue = 'questions';
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error(error);
        this.loading = false;
        this.setNotice("Impossible de charger les questions de cette evaluation.", 'error');
      },
    });
  }

  retourCours(): void {
    this.vue = 'cours';
    this.coursSelectionne = null;
    this.leconSelectionnee = null;
    this.evalSelectionnee = null;
    this.questions = [];
  }

  retourLecons(): void {
    this.vue = 'lecons';
    this.leconSelectionnee = null;
    this.evalSelectionnee = null;
    this.questions = [];
  }

  retourQuiz(): void {
    this.vue = 'quiz';
    this.evalSelectionnee = null;
    this.questions = [];
  }

  onLeconPdfChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }

    const file = input.files[0];
    if (file.size > 10 * 1024 * 1024) {
      this.leconError = 'PDF trop volumineux (max 10 MB).';
      return;
    }

    this.leconAttachmentUploading = true;
    this.leconError = '';

    this.quizService.uploadPdf(file).subscribe({
      next: (res) => {
        this.upsertLeconResource({
          nom: this.getResourceNameFromFile(file.name, 'Support PDF'),
          type: 'DOCUMENT',
          url: res.url,
        });
        this.syncLegacyPdfUrl();
        this.leconAttachmentUploading = false;
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error(error);
        this.leconError = "Erreur lors de l'upload du PDF.";
        this.leconAttachmentUploading = false;
        this.cdr.detectChanges();
      },
    });
  }

  removeLeconPdf(): void {
    this.leconResources = this.leconResources.filter((resource) => resource.type !== 'DOCUMENT');
    this.syncLegacyPdfUrl();
    this.cdr.detectChanges();
  }

  onLeconImageChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }

    const file = input.files[0];
    if (!file.type.startsWith('image/')) {
      this.leconError = 'Selectionne une image valide.';
      return;
    }
    if (file.size > 8 * 1024 * 1024) {
      this.leconError = 'Image trop volumineuse (max 8 MB).';
      return;
    }

    this.leconImageUploading = true;
    this.leconError = '';

    this.quizService.uploadImage(file).subscribe({
      next: (res) => {
        this.upsertLeconResource({
          nom: this.getResourceNameFromFile(file.name, 'Illustration'),
          type: 'IMAGE',
          url: res.url,
        });
        this.leconImageUploading = false;
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error(error);
        this.leconError = "Erreur lors de l'upload de l'image.";
        this.leconImageUploading = false;
        this.cdr.detectChanges();
      },
    });
  }

  onLeconAttachmentChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }

    const file = input.files[0];
    if (file.size > 15 * 1024 * 1024) {
      this.leconError = 'Piece jointe trop volumineuse (max 15 MB).';
      return;
    }

    this.leconAttachmentUploading = true;
    this.leconError = '';

    this.quizService.uploadAttachment(file).subscribe({
      next: (res) => {
        this.upsertLeconResource({
          nom: this.getResourceNameFromFile(file.name, 'Piece jointe'),
          type: 'DOCUMENT',
          url: res.url,
        });
        this.syncLegacyPdfUrl();
        this.leconAttachmentUploading = false;
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error(error);
        this.leconError = "Erreur lors de l'upload de la piece jointe.";
        this.leconAttachmentUploading = false;
        this.cdr.detectChanges();
      },
    });
  }

  addLeconVideo(): void {
    this.leconResources = [
      ...this.leconResources,
      { nom: 'Video de la lecon', type: 'VIDEO', url: '' },
    ];
  }

  addLeconImageUrl(): void {
    this.leconResources = [
      ...this.leconResources,
      { nom: 'Illustration', type: 'IMAGE', url: '' },
    ];
  }

  addLeconAttachmentLink(): void {
    this.leconResources = [
      ...this.leconResources,
      { nom: 'Piece jointe', type: 'DOCUMENT', url: '' },
    ];
    this.syncLegacyPdfUrl();
  }

  removeLeconResource(index: number): void {
    this.leconResources.splice(index, 1);
    this.leconResources = [...this.leconResources];
    this.syncLegacyPdfUrl();
  }

  applyEditorCommand(command: 'bold' | 'italic' | 'underline' | 'insertUnorderedList' | 'insertOrderedList'): void {
    document.execCommand(command);
    this.captureEditorContent();
  }

  setEditorBlock(tag: 'p' | 'h2' | 'h3' | 'blockquote'): void {
    document.execCommand('formatBlock', false, tag);
    this.captureEditorContent();
  }

  captureEditorContent(): void {
    this.leconForm.contenuHtml = this.lessonEditor?.nativeElement.innerHTML ?? '';
  }

  openLeconForm(lecon?: LeconResponse): void {
    if (!this.coursSelectionne) {
      return;
    }

    this.leconToEdit = lecon ?? null;
    this.leconForm = lecon
      ? {
          titre: lecon.titre,
          description: lecon.description,
          contenuHtml: lecon.contenuHtml ?? '',
          ordre: lecon.ordre,
          coursId: lecon.coursId || this.coursSelectionne.id,
          pdfUrl: lecon.pdfUrl ?? '',
          ressources: lecon.ressources ?? [],
        }
      : {
          titre: '',
          description: '',
          contenuHtml: '',
          ordre: this.lecons.length + 1,
          coursId: this.coursSelectionne.id,
          pdfUrl: '',
          ressources: [],
        };
    this.leconResources = this.normalizeLeconResources(lecon);
    this.leconError = '';
    this.showLeconForm = true;
    setTimeout(() => this.setEditorContent(this.leconForm.contenuHtml ?? ''), 0);
  }

  closeLeconForm(): void {
    this.showLeconForm = false;
    this.leconError = '';
    this.leconLoading = false;
    this.leconImageUploading = false;
    this.leconAttachmentUploading = false;
  }

  sauvegarderLecon(): void {
    if (!this.leconForm.titre.trim()) {
      this.leconError = 'Le titre est obligatoire.';
      return;
    }
    this.captureEditorContent();

    const invalidVideo = this.leconResources.some((resource) => resource.type === 'VIDEO' && !this.isValidUrl(resource.url));
    if (invalidVideo) {
      this.leconError = 'Chaque video doit avoir une URL valide.';
      return;
    }

    const invalidImage = this.leconResources.some((resource) => resource.type === 'IMAGE' && !this.isValidUrl(resource.url));
    if (invalidImage) {
      this.leconError = 'Chaque image doit avoir une URL valide.';
      return;
    }

    const invalidDocument = this.leconResources.some((resource) => resource.type === 'DOCUMENT' && !this.isValidUrl(resource.url) && !resource.url.startsWith('/uploads/'));
    if (invalidDocument) {
      this.leconError = 'Chaque piece jointe doit avoir une URL valide ou provenir d un upload.';
      return;
    }

    this.leconLoading = true;
    const payload: LeconRequest = {
      ...this.leconForm,
      titre: this.leconForm.titre.trim(),
      description: this.leconForm.description?.trim() ?? '',
      contenuHtml: this.cleanEditorHtml(this.leconForm.contenuHtml ?? ''),
      ressources: this.leconResources
        .map((resource) => ({
          nom: resource.nom.trim() || this.getDefaultResourceName(resource.type),
          type: resource.type,
          url: resource.url.trim(),
        }))
        .filter((resource) => resource.url),
      pdfUrl: this.leconResources.find((resource) => resource.type === 'DOCUMENT')?.url ?? '',
    };
    const request$ = this.leconToEdit
      ? this.quizService.updateLecon(this.leconToEdit.id, payload)
      : this.quizService.createLecon(payload);

    request$.subscribe({
      next: (data) => {
        if (this.leconToEdit) {
          this.lecons = this.lecons.map((item) => (item.id === data.id ? data : item));
          this.setNotice('Lecon mise a jour avec succes.');
        } else {
          this.lecons = [...this.lecons, data].sort((left, right) => left.ordre - right.ordre);
          this.setNotice('Lecon ajoutee avec succes.');
        }
        this.leconLoading = false;
        this.closeLeconForm();
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error(error);
        this.leconError = 'Erreur lors de la sauvegarde de la lecon.';
        this.leconLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  openEvalForm(evaluation?: EvaluationResponse): void {
    this.evalToEdit = evaluation ?? null;
    this.evalForm = evaluation
      ? {
          titre: evaluation.titre,
          type: evaluation.type,
          noteMax: evaluation.noteMax,
          noteMin: evaluation.noteMin,
          leconId: evaluation.leconId,
        }
      : {
          titre: '',
          type: 'QUIZ',
          noteMax: 20,
          noteMin: 14,
          leconId: this.leconSelectionnee?.id ?? 0,
        };
    this.evalError = '';
    this.showEvalForm = true;
  }

  sauvegarderEval(): void {
    if (!this.evalForm.titre.trim()) {
      this.evalError = 'Le titre est obligatoire.';
      return;
    }
    if (this.evalForm.noteMax <= 0) {
      this.evalError = 'La note maximale doit etre superieure a zero.';
      return;
    }
    if (this.evalForm.noteMin < 0 || this.evalForm.noteMin > this.evalForm.noteMax) {
      this.evalError = 'La note minimale doit etre comprise entre 0 et la note maximale.';
      return;
    }

    this.evalLoading = true;
    const request$ = this.evalToEdit
      ? this.quizService.updateEvaluation(this.evalToEdit.id, this.evalForm)
      : this.quizService.createEvaluation(this.evalForm);

    request$.subscribe({
      next: (data) => {
        const normalized: EvaluationResponse = {
          ...data,
          publie: data.publie ?? false,
          questionCount: data.questionCount ?? this.evalToEdit?.questionCount ?? 0,
        };

        if (this.evalToEdit) {
          this.evaluations = this.evaluations.map((item) => (item.id === normalized.id ? { ...item, ...normalized } : item));
          this.setNotice('Evaluation mise a jour avec succes.');
        } else {
          this.evaluations = [...this.evaluations, normalized];
          this.setNotice('Evaluation creee en brouillon. Ajoutez les questions puis publiez-la.');
        }

        this.evalLoading = false;
        this.showEvalForm = false;
        this.evalToEdit = null;
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error(error);
        this.evalError = "Erreur lors de la sauvegarde de l'evaluation.";
        this.evalLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  changerPublication(evaluation: EvaluationResponse, publie: boolean): void {
    this.quizService.setEvaluationPublication(evaluation.id, publie).subscribe({
      next: (updated) => {
        this.evaluations = this.evaluations.map((item) =>
          item.id === updated.id ? { ...item, ...updated, questionCount: updated.questionCount ?? item.questionCount } : item,
        );
        if (this.evalSelectionnee?.id === updated.id) {
          this.evalSelectionnee = { ...this.evalSelectionnee, ...updated };
        }
        this.setNotice(publie ? 'Evaluation publiee avec succes.' : 'Evaluation retiree de la publication.');
        this.cdr.detectChanges();
      },
      error: (error: { error?: { message?: string } }) => {
        console.error(error);
        this.setNotice(error?.error?.message ?? 'Impossible de changer la publication de cette evaluation.', 'error');
      },
    });
  }

  openQuestionForm(question?: QuestionResponse): void {
    if (!this.evalSelectionnee) {
      return;
    }

    this.questionToEdit = question ?? null;
    this.questionForm = question
      ? {
          enonce: question.enonce,
          point: question.point,
          evaluationId: this.evalSelectionnee.id,
          choix: question.choix.map((choice) => ({
            texte: choice.texte,
            estCorrect: !!choice.estCorrect,
          })),
        }
      : {
          enonce: '',
          point: 1,
          evaluationId: this.evalSelectionnee.id,
          choix: [],
        };
    this.choixTemp =
      question?.choix?.length && question.choix.length >= 2
        ? question.choix.map((choice) => ({ texte: choice.texte, estCorrect: !!choice.estCorrect }))
        : [
            { texte: '', estCorrect: false },
            { texte: '', estCorrect: false },
          ];
    this.questionError = '';
    this.showQuestionForm = true;
  }

  ajouterChoix(): void {
    this.choixTemp.push({ texte: '', estCorrect: false });
  }

  supprimerChoix(index: number): void {
    if (this.choixTemp.length > 2) {
      this.choixTemp.splice(index, 1);
    }
  }

  definirCorrect(index: number): void {
    this.choixTemp = this.choixTemp.map((choice, choiceIndex) => ({
      ...choice,
      estCorrect: choiceIndex === index,
    }));
  }

  sauvegarderQuestion(): void {
    const choix = this.choixTemp
      .map((choice) => ({ texte: choice.texte.trim(), estCorrect: choice.estCorrect }))
      .filter((choice) => choice.texte);

    if (!this.questionForm.enonce.trim()) {
      this.questionError = "L'enonce est obligatoire.";
      return;
    }
    if (choix.length < 2) {
      this.questionError = 'Chaque question doit avoir au moins deux choix.';
      return;
    }
    if (choix.filter((choice) => choice.estCorrect).length !== 1) {
      this.questionError = 'Selectionnez exactement une bonne reponse.';
      return;
    }

    this.questionLoading = true;
    const payload: QuestionRequest = {
      enonce: this.questionForm.enonce.trim(),
      point: this.questionForm.point,
      evaluationId: this.questionForm.evaluationId,
      choix,
    };

    const request$ = this.questionToEdit
      ? this.quizService.updateQuestion(this.questionToEdit.id, payload)
      : this.quizService.createQuestion(payload);

    request$.subscribe({
      next: (question) => {
        if (this.questionToEdit) {
          this.questions = this.questions.map((item) => (item.id === question.id ? question : item));
          this.setNotice('Question mise a jour avec succes.');
        } else {
          this.questions = [...this.questions, question];
          this.syncEvaluationQuestionCount(this.questionForm.evaluationId, 1);
          this.setNotice('Question ajoutee avec succes.');
        }
        this.questionLoading = false;
        this.showQuestionForm = false;
        this.questionToEdit = null;
        this.cdr.detectChanges();
      },
      error: (error: { error?: { message?: string } }) => {
        console.error(error);
        this.questionError = error?.error?.message ?? 'Erreur lors de la sauvegarde de la question.';
        this.questionLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  confirmerSupprimer(type: 'lecon' | 'eval' | 'question', id: number, label: string): void {
    this.deleteTarget = { type, id, label };
    this.showDeleteConfirm = true;
  }

  annulerSupprimer(): void {
    this.showDeleteConfirm = false;
    this.deleteTarget = null;
  }

  execSupprimer(): void {
    if (!this.deleteTarget) {
      return;
    }

    this.deleteLoading = true;
    const { id, type } = this.deleteTarget;
    const request$ =
      type === 'lecon'
        ? this.quizService.deleteLecon(id)
        : type === 'eval'
          ? this.quizService.deleteEvaluation(id)
          : this.quizService.deleteQuestion(id);

    request$.subscribe({
      next: () => {
        if (type === 'lecon') {
          this.lecons = this.lecons.filter((item) => item.id !== id);
          this.setNotice('Lecon supprimee.');
        }
        if (type === 'eval') {
          this.evaluations = this.evaluations.filter((item) => item.id !== id);
          if (this.evalSelectionnee?.id === id) {
            this.retourQuiz();
          }
          this.setNotice('Evaluation supprimee.');
        }
        if (type === 'question') {
          this.questions = this.questions.filter((item) => item.id !== id);
          if (this.evalSelectionnee) {
            this.syncEvaluationQuestionCount(this.evalSelectionnee.id, -1);
            if (this.questions.length === 0 && this.evalSelectionnee.publie) {
              this.evalSelectionnee = { ...this.evalSelectionnee, publie: false };
              this.evaluations = this.evaluations.map((item) =>
                item.id === this.evalSelectionnee?.id ? { ...item, publie: false, questionCount: 0 } : item,
              );
            }
          }
          this.setNotice('Question supprimee.');
        }

        this.deleteLoading = false;
        this.showDeleteConfirm = false;
        this.deleteTarget = null;
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error(error);
        this.deleteLoading = false;
        this.setNotice('Impossible de supprimer cet element.', 'error');
      },
    });
  }

  getIcon(categorieNom: string): string {
    const nom = (categorieNom ?? '').toLowerCase();
    if (nom.includes('informatique') || nom.includes('programmation')) return '💻';
    if (nom.includes('langue')) return '🌐';
    if (nom.includes('science') || nom.includes('math')) return '🔬';
    if (nom.includes('design')) return '🎨';
    return '📚';
  }

  getBannerGradient(categorieId: number): string {
    const gradients = [
      'linear-gradient(135deg,#6366f1,#8b5cf6)',
      'linear-gradient(135deg,#f59e0b,#f97316)',
      'linear-gradient(135deg,#06b6d4,#0ea5e9)',
      'linear-gradient(135deg,#10b981,#06b6d4)',
    ];
    return gradients[(categorieId ?? 0) % gradients.length];
  }

  getTypeLabel(type: TypeEvaluation): string {
    return type === 'QUIZ' ? 'Quiz' : 'Examen';
  }

  getTypeClass(type: TypeEvaluation): string {
    return type === 'QUIZ' ? 'quiz' : 'examen';
  }

  totalPoints(): number {
    return this.questions.reduce((total, question) => total + (question.point ?? 1), 0);
  }

  private syncEvaluationQuestionCount(evaluationId: number, delta: number): void {
    this.evaluations = this.evaluations.map((item) =>
      item.id === evaluationId ? { ...item, questionCount: Math.max(0, (item.questionCount ?? 0) + delta) } : item,
    );

    if (this.evalSelectionnee?.id === evaluationId) {
      this.evalSelectionnee = {
        ...this.evalSelectionnee,
        questionCount: Math.max(0, (this.evalSelectionnee.questionCount ?? 0) + delta),
      };
    }
  }

  private setNotice(message: string, tone: 'success' | 'error' = 'success'): void {
    this.noticeMessage = message;
    this.noticeTone = tone;
    this.cdr.detectChanges();
  }

  private normalizeLeconResources(lecon?: LeconResponse | null): LeconResourceDraft[] {
    const resources = lecon?.ressources?.length
      ? lecon.ressources
      : lecon?.pdfUrl
        ? [{ nom: 'Support de lecon', type: 'DOCUMENT' as TypeRessourceLecon, url: lecon.pdfUrl }]
        : [];

    return resources.map((resource) => ({
      id: resource.id,
      nom: resource.nom,
      type: resource.type,
      url: resource.url,
    }));
  }

  private setEditorContent(content: string): void {
    if (this.lessonEditor?.nativeElement) {
      this.lessonEditor.nativeElement.innerHTML = content || '';
    }
  }

  private cleanEditorHtml(content: string): string {
    return content
      .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, '')
      .replace(/on\w+="[^"]*"/gi, '')
      .trim();
  }

  private upsertLeconResource(resource: LeconResourceDraft): void {
    if (resource.type === 'IMAGE') {
      const existingIndex = this.leconResources.findIndex((item) => item.type === 'IMAGE');
      if (existingIndex >= 0) {
        this.leconResources[existingIndex] = resource;
        this.leconResources = [...this.leconResources];
        return;
      }
    }

    this.leconResources = [...this.leconResources, resource];
  }

  syncLegacyPdfUrl(): void {
    this.leconForm.pdfUrl = this.leconResources.find((resource) => resource.type === 'DOCUMENT')?.url ?? '';
  }

  private getResourceNameFromFile(fileName: string, fallback: string): string {
    const cleanName = fileName?.trim();
    return cleanName ? cleanName.replace(/\.[^.]+$/, '') : fallback;
  }

  private getDefaultResourceName(type: TypeRessourceLecon): string {
    if (type === 'VIDEO') return 'Video de la lecon';
    if (type === 'IMAGE') return 'Illustration';
    return 'Piece jointe';
  }

  private isValidUrl(url: string): boolean {
    if (!url?.trim()) {
      return false;
    }

    if (url.startsWith('/uploads/')) {
      return true;
    }

    try {
      const parsed = new URL(url);
      return ['http:', 'https:'].includes(parsed.protocol);
    } catch {
      return false;
    }
  }
}
