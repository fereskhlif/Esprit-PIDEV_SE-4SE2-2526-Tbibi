import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { HealthGoal } from '../models/health-goal';

@Injectable({
  providedIn: 'root'
})
export class HealthGoalService {
  private readonly apiUrl = 'http://localhost:8088/api/health-goals';

  constructor(private http: HttpClient) {}

  getGoals(userId: number): Observable<HealthGoal[]> {
    return this.http.get<HealthGoal[]>(`${this.apiUrl}/user/${userId}`);
  }

  addGoal(goal: HealthGoal): Observable<HealthGoal> {
    return this.http.post<HealthGoal>(this.apiUrl, goal);
  }

  updateGoal(id: number, goal: HealthGoal): Observable<HealthGoal> {
    return this.http.put<HealthGoal>(`${this.apiUrl}/${id}`, goal);
  }

  deleteGoal(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
