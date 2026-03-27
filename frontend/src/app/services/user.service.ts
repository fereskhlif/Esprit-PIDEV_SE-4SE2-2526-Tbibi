import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

import { CreateUserRequest, User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = 'http://localhost:8088/users';
  private readonly storageKey = 'tbibi.active-user';
  private readonly currentUserSubject = new BehaviorSubject<User | null>(this.readStoredUser());

  readonly currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  createUser(payload: CreateUserRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, payload);
  }

  findUserByEmail(email: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/email/${encodeURIComponent(email)}`);
  }

  setCurrentUser(user: User | null): void {
    this.currentUserSubject.next(user);

    if (typeof localStorage === 'undefined') {
      return;
    }

    if (user) {
      localStorage.setItem(this.storageKey, JSON.stringify(user));
      return;
    }

    localStorage.removeItem(this.storageKey);
  }

  getCurrentUserSnapshot(): User | null {
    return this.currentUserSubject.value;
  }

  ensureCurrentUser(users: User[]): void {
    const currentUser = this.currentUserSubject.value;

    if (users.length === 0) {
      this.setCurrentUser(null);
      return;
    }

    if (!currentUser) {
      this.setCurrentUser(users[0]);
      return;
    }

    const refreshedUser = users.find(user => user.userId === currentUser.userId) ?? null;
    this.setCurrentUser(refreshedUser ?? users[0]);
  }

  private readStoredUser(): User | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }

    const rawUser = localStorage.getItem(this.storageKey);
    if (!rawUser) {
      return null;
    }

    try {
      return JSON.parse(rawUser) as User;
    } catch {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }
}
