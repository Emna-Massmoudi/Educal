// cSpell:ignore paiement paiements formateur etudiant approuve

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type StatutPaiement = 'EN_ATTENTE' | 'APPROUVE' | 'REFUSE';

export interface PaiementCodeRequest {
  etudiantId: number;
  coursId: number;
  codePaiement: string;
}

export interface PaiementCoursResponse {
  id: number;
  codePaiement: string;
  montant: number;
  commissionPlateforme: number;
  montantFormateur: number;
  statut: StatutPaiement;
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

export interface WalletFormateurResponse {
  formateurId: number;
  totalGagne: number;
  totalCommissionPlateforme: number;
  paiementsApprouves: number;
}

@Injectable({ providedIn: 'root' })
export class PaiementService {

  private readonly baseUrl = 'http://localhost:8081/api/paiements';

  constructor(private readonly http: HttpClient) {}

  submitCode(request: PaiementCodeRequest): Observable<PaiementCoursResponse> {
    return this.http.post<PaiementCoursResponse>(`${this.baseUrl}/code`, request);
  }

  getAllAdmin(): Observable<PaiementCoursResponse[]> {
    return this.http.get<PaiementCoursResponse[]>(`${this.baseUrl}/admin`);
  }

  getPendingAdmin(): Observable<PaiementCoursResponse[]> {
    return this.http.get<PaiementCoursResponse[]>(`${this.baseUrl}/admin/en-attente`);
  }

  approve(id: number): Observable<PaiementCoursResponse> {
    return this.http.patch<PaiementCoursResponse>(`${this.baseUrl}/${id}/approuver`, {});
  }

  reject(id: number): Observable<PaiementCoursResponse> {
    return this.http.patch<PaiementCoursResponse>(`${this.baseUrl}/${id}/refuser`, {});
  }

  getWalletFormateur(formateurId: number): Observable<WalletFormateurResponse> {
    return this.http.get<WalletFormateurResponse>(`${this.baseUrl}/formateur/${formateurId}/wallet`);
  }
}
