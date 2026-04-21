// cSpell:ignore formateur categorie BROUILLON ATTENTE VALIDATION SUPPRIME

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// Miroir exact de EtatCours.java
export type EtatCours = 'BROUILLON' | 'EN_ATTENTE_VALIDATION' | 'PUBLIE' | 'SUPPRIME';

export interface Cours {
  id:               number;
  titre:            string;
  description:      string;
  etatPublication:  EtatCours;
  statut:           EtatCours;
  categorieId:      number;
  categorieNom:     string;
  sousCategorieId:  number;
  sousCategorieNom: string;
  formateurId:      number;
  formateurNom:     string;
  formateurEmail?:  string;
  dateCreation?:    string;
  dateMiseAJour?:   string;
  nombreInscrits?:  number;
  duree?:           string;
  niveau?:          'debutant' | 'intermediaire' | 'avance';
  imageUrl?:        string;
  videoUrl?:        string;
  pdfUrl?:          string;
  prix?:            number;
  noteMoyenne?:     number;
  nombreAvis?:      number;
}

export interface CoursRequest {
  titre:           string;
  description:     string;
  formateurId:     number;
  categorieId:     number;
  sousCategorieId: number;
  duree?:          string;
  niveau?:         string;
  imageUrl?:       string;
  videoUrl?:       string;
  pdfUrl?:         string;
  prix?:           number;
}

@Injectable({ providedIn: 'root' })
export class CoursService {

  private readonly baseUrl = 'http://localhost:8081/api/cours';

  constructor(private http: HttpClient) {}

  getAllCours(): Observable<Cours[]> {
    return this.http.get<Cours[]>(this.baseUrl);
  }

  getPublicPublishedCours(): Observable<Cours[]> {
    return this.http.get<Cours[]>(`${this.baseUrl}/public/published`);
  }

  getPublicPublishedCoursById(id: number): Observable<Cours> {
    return this.http.get<Cours>(`${this.baseUrl}/public/published/${id}`);
  }

  getCoursById(id: number): Observable<Cours> {
    return this.http.get<Cours>(`${this.baseUrl}/${id}`);
  }

  getCoursByFormateur(formateurId: number): Observable<Cours[]> {
    return this.http.get<Cours[]>(`${this.baseUrl}/formateur/${formateurId}`);
  }

  getCoursByEtat(etat: EtatCours): Observable<Cours[]> {
    return this.http.get<Cours[]>(`${this.baseUrl}/etat/${etat}`);
  }

  getCoursEnAttente(): Observable<Cours[]> { return this.getCoursByEtat('EN_ATTENTE_VALIDATION'); }
  getCoursPublies():   Observable<Cours[]> { return this.getCoursByEtat('PUBLIE'); }

  createCours(request: CoursRequest): Observable<Cours> {
    return this.http.post<Cours>(this.baseUrl, request);
  }

  updateCours(id: number, request: CoursRequest): Observable<Cours> {
    return this.http.put<Cours>(`${this.baseUrl}/${id}`, request);
  }

  supprimerCours(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  validerCours(id: number):      Observable<Cours> { return this.updateEtat(id, 'EN_ATTENTE_VALIDATION'); }
  publierCours(id: number):      Observable<Cours> { return this.updateEtat(id, 'PUBLIE'); }
  retirerCours(id: number):      Observable<Cours> { return this.updateEtat(id, 'SUPPRIME'); }
  remettreEnAttente(id: number): Observable<Cours> { return this.updateEtat(id, 'EN_ATTENTE_VALIDATION'); }

  updateEtat(id: number, etat: EtatCours): Observable<Cours> {
    const params = new HttpParams().set('etat', etat);
    return this.http.patch<Cours>(`${this.baseUrl}/${id}/etat`, {}, { params });
  }
}
