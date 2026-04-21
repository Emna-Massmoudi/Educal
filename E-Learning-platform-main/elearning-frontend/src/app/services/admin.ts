import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cours } from './cours';
import { FormateurResponse } from './formateur';

export interface EtudiantResponse {
  id: number;
  nom: string;
  email: string;
  status: string;
}

export interface AdminDashboardOverview {
  totalCours: number;
  coursPublies: number;
  coursEnAttenteValidation: number;
  coursBrouillons: number;
  coursSupprimes: number;
  totalFormateurs: number;
  formateursActifs: number;
  formateursEnAttente: number;
  totalEtudiants: number;
  etudiantsActifs: number;
  paiementsEnAttente: number;
  paiementsApprouves: number;
  certificatsGeneres: number;
  revenusPlateforme: number;
  revenusFormateurs: number;
}

export interface AdminDashboardPayment {
  id: number;
  codePaiement: string;
  montant: number;
  commissionPlateforme: number;
  montantFormateur: number;
  statut: 'EN_ATTENTE' | 'APPROUVE' | 'REFUSE';
  dateCreation?: string;
  dateDecision?: string;
  etudiantId: number;
  etudiantNom: string;
  coursId: number;
  coursTitre: string;
  formateurId?: number;
  formateurNom?: string;
  inscriptionId: number;
}

export interface AdminDashboardResponse {
  overview: AdminDashboardOverview;
  recentCourses: Cours[];
  topCourses: Cours[];
  pendingFormateurs: FormateurResponse[];
  pendingPayments: AdminDashboardPayment[];
}

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private apiUrl = 'http://localhost:8081/api/admin';

  constructor(private readonly http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return { Authorization: `Bearer ${token}` };
  }

  getDashboard(): Observable<AdminDashboardResponse> {
    return this.http.get<AdminDashboardResponse>(`${this.apiUrl}/dashboard`, {
      headers: this.getAuthHeaders(),
    });
  }

  getEtudiants(): Observable<EtudiantResponse[]> {
    return this.http.get<EtudiantResponse[]>(`${this.apiUrl}/etudiants`, {
      headers: this.getAuthHeaders(),
    });
  }

  getEtudiantsBloques(): Observable<EtudiantResponse[]> {
    return this.http.get<EtudiantResponse[]>(`${this.apiUrl}/etudiants/bloques`, {
      headers: this.getAuthHeaders(),
    });
  }

  bloquerEtudiant(id: number): Observable<EtudiantResponse> {
    return this.http.patch<EtudiantResponse>(`${this.apiUrl}/etudiants/${id}/bloquer`, {}, {
      headers: this.getAuthHeaders(),
    });
  }

  debloquerEtudiant(id: number): Observable<EtudiantResponse> {
    return this.http.patch<EtudiantResponse>(`${this.apiUrl}/etudiants/${id}/debloquer`, {}, {
      headers: this.getAuthHeaders(),
    });
  }

  getFormateursBloques(): Observable<FormateurResponse[]> {
    return this.http.get<FormateurResponse[]>(`${this.apiUrl}/formateurs/bloques`, {
      headers: this.getAuthHeaders(),
    });
  }

  bloquerFormateur(id: number): Observable<FormateurResponse> {
    return this.http.patch<FormateurResponse>(`${this.apiUrl}/formateurs/${id}/bloquer`, {}, {
      headers: this.getAuthHeaders(),
    });
  }

  debloquerFormateur(id: number): Observable<FormateurResponse> {
    return this.http.patch<FormateurResponse>(`${this.apiUrl}/formateurs/${id}/debloquer`, {}, {
      headers: this.getAuthHeaders(),
    });
  }
}
