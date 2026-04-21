import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AvisCoursRequest {
  etudiantId: number;
  coursId: number;
  note: number;
  commentaire?: string;
}

export interface AvisCoursResponse {
  id: number;
  etudiantId: number;
  etudiantNom: string;
  coursId: number;
  coursTitre: string;
  note: number;
  commentaire?: string;
  dateCreation?: string;
  dateModification?: string;
}

@Injectable({ providedIn: 'root' })
export class AvisCoursService {
  private readonly baseUrl = 'http://localhost:8081/api/avis-cours';

  constructor(private readonly http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return { Authorization: `Bearer ${token}` };
  }

  save(request: AvisCoursRequest): Observable<AvisCoursResponse> {
    return this.http.post<AvisCoursResponse>(this.baseUrl, request, {
      headers: this.getAuthHeaders(),
    });
  }

  getByEtudiant(etudiantId: number): Observable<AvisCoursResponse[]> {
    return this.http.get<AvisCoursResponse[]>(`${this.baseUrl}/etudiant/${etudiantId}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getByCours(coursId: number): Observable<AvisCoursResponse[]> {
    return this.http.get<AvisCoursResponse[]>(`${this.baseUrl}/cours/${coursId}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getPublicByCours(coursId: number): Observable<AvisCoursResponse[]> {
    return this.http.get<AvisCoursResponse[]>(`${this.baseUrl}/public/cours/${coursId}`);
  }
}
