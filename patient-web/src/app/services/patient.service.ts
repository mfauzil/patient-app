import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PageResponse, Patient } from '../models/patient.model';

@Injectable({ providedIn: 'root' })
export class PatientService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/patients`;

  list(keyword: string, page: number, size: number): Observable<PageResponse<Patient>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    const cleanKeyword = keyword.trim();
    if (cleanKeyword) {
      params = params.set('keyword', cleanKeyword);
    }
    return this.http.get<PageResponse<Patient>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.baseUrl}/${id}`);
  }

  create(patient: Patient): Observable<Patient> {
    return this.http.post<Patient>(this.baseUrl, patient);
  }

  update(id: number, patient: Patient): Observable<Patient> {
    return this.http.put<Patient>(`${this.baseUrl}/${id}`, patient);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}