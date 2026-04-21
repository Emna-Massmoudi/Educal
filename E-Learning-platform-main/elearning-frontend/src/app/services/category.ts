import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Categorie {
  id?: number;
  nom: string;
  description: string;
  sousCategorieCount?: number;
  sousCategories?: SousCategorie[];
}

export interface SousCategorie {
  id?: number;
  nom: string;
  description: string;
  categorieId: number;  // Correspond au champ dans le backend
  createdAt?: Date;
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private apiUrl = 'http://localhost:8081/api/categories';
  private sousCategoriesUrl = 'http://localhost:8081/api/sous-categories';

  constructor(private http: HttpClient) {}

  // === CATEGORIES ===
  getAll(): Observable<Categorie[]> {
    return this.http.get<Categorie[]>(this.apiUrl);
  }

  getPublicCategories(): Observable<Categorie[]> {
    return this.http.get<Categorie[]>(`${this.apiUrl}/public`);
  }

  getById(id: number): Observable<Categorie> {
    return this.http.get<Categorie>(`${this.apiUrl}/${id}`);
  }

  create(cat: Categorie): Observable<Categorie> {
    return this.http.post<Categorie>(this.apiUrl, cat);
  }

  update(id: number, cat: Categorie): Observable<Categorie> {
    return this.http.put<Categorie>(`${this.apiUrl}/${id}`, cat);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  // === SOUS-CATEGORIES ===
  // ✅ Utilise le bon endpoint qui correspond au repository
  getSousCategoriesByCategorieId(categorieId: number): Observable<SousCategorie[]> {
    return this.http.get<SousCategorie[]>(`${this.sousCategoriesUrl}/categorie/${categorieId}`);
  }

  createSousCategorie(sousCat: SousCategorie): Observable<SousCategorie> {
    return this.http.post<SousCategorie>(this.sousCategoriesUrl, sousCat);
  }

  updateSousCategorie(id: number, sousCat: SousCategorie): Observable<SousCategorie> {
    return this.http.put<SousCategorie>(`${this.sousCategoriesUrl}/${id}`, sousCat);
  }

  deleteSousCategorie(id: number): Observable<any> {
    return this.http.delete(`${this.sousCategoriesUrl}/${id}`);
  }
}
