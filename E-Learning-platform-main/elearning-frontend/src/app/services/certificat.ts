import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CertificatService {
  private readonly baseUrl = 'http://localhost:8081/api/certificats';

  constructor(private readonly http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return { Authorization: `Bearer ${token}` };
  }

  downloadById(certificatId: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}/${certificatId}/download`, {
      headers: this.getAuthHeaders(),
      observe: 'response',
      responseType: 'blob',
    });
  }
}
