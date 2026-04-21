import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type TypeEvaluation = 'QUIZ' | 'EXAMEN';
export type TypeRessourceLecon = 'VIDEO' | 'IMAGE' | 'DOCUMENT';

export interface LeconRessource {
  id?: number;
  nom: string;
  type: TypeRessourceLecon;
  url: string;
}

export interface LeconResponse {
  id: number;
  titre: string;
  description: string;
  contenuHtml?: string;
  ordre: number;
  coursId: number;
  coursTitre?: string;
  pdfUrl?: string;
  ressources?: LeconRessource[];
}

export interface LeconPreviewResponse {
  id: number;
  titre: string;
  description: string;
  ordre: number;
  coursId: number;
}

export interface LeconRequest {
  titre: string;
  description: string;
  contenuHtml?: string;
  ordre: number;
  coursId: number;
  pdfUrl?: string;
  ressources?: LeconRessource[];
}

export interface ChoixResponse {
  id: number;
  texte: string;
  estCorrect?: boolean | null;
  questionId?: number;
}

export interface ChoixRequest {
  texte: string;
  estCorrect: boolean;
  questionId: number;
}

export interface QuestionChoiceDraft {
  texte: string;
  estCorrect: boolean;
}

export interface QuestionResponse {
  id: number;
  enonce: string;
  point: number;
  evaluationId?: number;
  evaluationTitre?: string;
  choix: ChoixResponse[];
}

export interface QuestionRequest {
  enonce: string;
  point: number;
  evaluationId: number;
  choix: QuestionChoiceDraft[];
}

export interface EvaluationResponse {
  id: number;
  titre: string;
  type: TypeEvaluation;
  noteMax: number;
  noteMin: number;
  publie: boolean;
  questionCount: number;
  leconId: number;
  leconTitre: string;
  questions?: QuestionResponse[];
  dateCreation?: string;
  derniereNote?: number | null;
  dernierScore?: number | null;
  dernierTotalPoints?: number | null;
  dernierPourcentage?: number | null;
  derniereReussite?: boolean | null;
  nombreTentatives?: number | null;
  derniereSoumission?: string | null;
}

export interface EvaluationRequest {
  titre: string;
  type: TypeEvaluation;
  noteMax: number;
  noteMin: number;
  leconId: number;
}

export interface EvaluationAnswerRequest {
  questionId: number;
  choixId: number;
}

export interface EvaluationSubmissionRequest {
  reponses: EvaluationAnswerRequest[];
}

export interface EvaluationSubmissionResponse {
  evaluationId: number;
  evaluationTitre: string;
  type: TypeEvaluation;
  score: number;
  totalPoints: number;
  pourcentage: number;
  noteObtenue: number;
  noteMax: number;
  noteMin: number;
  reussi: boolean;
  tentativeNumero: number;
  dateSoumission?: string;
}

export interface ResultatQuiz {
  score: number;
  total: number;
  pourcentage: number;
  reussi: boolean;
  certificat?: string;
}

@Injectable({ providedIn: 'root' })
export class QuizService {
  private readonly baseLecon = 'http://localhost:8081/api/lecons';
  private readonly baseEval = 'http://localhost:8081/api/evaluations';
  private readonly baseQues = 'http://localhost:8081/api/questions';
  private readonly baseChoix = 'http://localhost:8081/api/choix';

  constructor(private readonly http: HttpClient) {}

  private headers() {
    return { Authorization: `Bearer ${localStorage.getItem('token')}` };
  }

  getLeconsByCours(coursId: number): Observable<LeconResponse[]> {
    return this.http.get<LeconResponse[]>(`${this.baseLecon}/cours/${coursId}`, { headers: this.headers() });
  }

  getPublicLeconsByCours(coursId: number): Observable<LeconPreviewResponse[]> {
    return this.http.get<LeconPreviewResponse[]>(`${this.baseLecon}/public/cours/${coursId}`);
  }

  createLecon(req: LeconRequest): Observable<LeconResponse> {
    return this.http.post<LeconResponse>(this.baseLecon, req, { headers: this.headers() });
  }

  updateLecon(id: number, req: LeconRequest): Observable<LeconResponse> {
    return this.http.put<LeconResponse>(`${this.baseLecon}/${id}`, req, { headers: this.headers() });
  }

  deleteLecon(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseLecon}/${id}`, { headers: this.headers() });
  }

  getEvaluationsByLecon(leconId: number): Observable<EvaluationResponse[]> {
    return this.http.get<EvaluationResponse[]>(`${this.baseEval}/lecon/${leconId}`, { headers: this.headers() });
  }

  getEvaluationById(id: number): Observable<EvaluationResponse> {
    return this.http.get<EvaluationResponse>(`${this.baseEval}/${id}`, { headers: this.headers() });
  }

  createEvaluation(req: EvaluationRequest): Observable<EvaluationResponse> {
    return this.http.post<EvaluationResponse>(this.baseEval, req, { headers: this.headers() });
  }

  updateEvaluation(id: number, req: EvaluationRequest): Observable<EvaluationResponse> {
    return this.http.put<EvaluationResponse>(`${this.baseEval}/${id}`, req, { headers: this.headers() });
  }

  setEvaluationPublication(id: number, publie: boolean): Observable<EvaluationResponse> {
    return this.http.patch<EvaluationResponse>(`${this.baseEval}/${id}/publication?publie=${publie}`, {}, { headers: this.headers() });
  }

  submitEvaluation(id: number, req: EvaluationSubmissionRequest): Observable<EvaluationSubmissionResponse> {
    return this.http.post<EvaluationSubmissionResponse>(`${this.baseEval}/${id}/soumissions`, req, { headers: this.headers() });
  }

  deleteEvaluation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseEval}/${id}`, { headers: this.headers() });
  }

  getQuestionsByEvaluation(evaluationId: number): Observable<QuestionResponse[]> {
    return this.http.get<QuestionResponse[]>(`${this.baseQues}/evaluation/${evaluationId}`, { headers: this.headers() });
  }

  createQuestion(req: QuestionRequest): Observable<QuestionResponse> {
    return this.http.post<QuestionResponse>(this.baseQues, req, { headers: this.headers() });
  }

  updateQuestion(id: number, req: QuestionRequest): Observable<QuestionResponse> {
    return this.http.put<QuestionResponse>(`${this.baseQues}/${id}`, req, { headers: this.headers() });
  }

  deleteQuestion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseQues}/${id}`, { headers: this.headers() });
  }

  uploadPdf(file: File): Observable<{ url: string; fileName: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ url: string; fileName: string }>('http://localhost:8081/api/upload/pdf', formData, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
    });
  }

  uploadImage(file: File): Observable<{ url: string; fileName: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ url: string; fileName: string }>('http://localhost:8081/api/upload/image', formData, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
    });
  }

  uploadAttachment(file: File): Observable<{ url: string; fileName: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ url: string; fileName: string }>('http://localhost:8081/api/upload/attachment', formData, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
    });
  }

  createChoix(req: ChoixRequest): Observable<ChoixResponse> {
    return this.http.post<ChoixResponse>(this.baseChoix, req, { headers: this.headers() });
  }

  updateChoix(id: number, req: ChoixRequest): Observable<ChoixResponse> {
    return this.http.put<ChoixResponse>(`${this.baseChoix}/${id}`, req, { headers: this.headers() });
  }

  deleteChoix(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseChoix}/${id}`, { headers: this.headers() });
  }

  calculerScore(questions: QuestionResponse[], reponses: Map<number, number>): ResultatQuiz {
    let score = 0;
    let total = 0;

    questions.forEach((question) => {
      total += question.point ?? 1;
      const choixId = reponses.get(question.id);
      if (!choixId) {
        return;
      }

      const bonChoix = question.choix.find((choix) => choix.id === choixId && choix.estCorrect);
      if (bonChoix) {
        score += question.point ?? 1;
      }
    });

    const pourcentage = total > 0 ? Math.round((score / total) * 100) : 0;
    return {
      score,
      total,
      pourcentage,
      reussi: pourcentage >= 70,
    };
  }
}
