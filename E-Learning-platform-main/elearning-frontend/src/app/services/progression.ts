import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CourseProgressResponse {
  coursId: number;
  totalLecons: number;
  leconsConsultees: number;
  pourcentage: number;
  termine: boolean;
  derniereLeconId?: number | null;
  derniereLeconTitre?: string | null;
  dateDerniereConsultation?: string | null;
  prochaineLeconId?: number | null;
  prochaineLeconTitre?: string | null;
  evaluationsPubliees?: number | null;
  evaluationsValidees?: number | null;
  certificatDisponible?: boolean | null;
  certificatId?: number | null;
  certificatCode?: string | null;
  certificatDateObtention?: string | null;
}

@Injectable({ providedIn: 'root' })
export class ProgressionService {
  private readonly baseUrl = 'http://localhost:8081/api/progressions';

  constructor(private readonly http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return { Authorization: `Bearer ${token}` };
  }

  getMyCourseProgress(): Observable<CourseProgressResponse[]> {
    return this.http.get<CourseProgressResponse[]>(`${this.baseUrl}/mes-cours`, {
      headers: this.getAuthHeaders(),
    });
  }

  markLessonViewed(leconId: number): Observable<CourseProgressResponse> {
    return this.http.post<CourseProgressResponse>(`${this.baseUrl}/lecons/${leconId}/consulter`, {}, {
      headers: this.getAuthHeaders(),
    });
  }
}
