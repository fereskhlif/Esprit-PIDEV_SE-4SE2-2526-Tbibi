import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { CreateUserRequest, User } from '../models/user';
import { UserService } from './user.service';

const USERS: User[] = [
  {
    userId: 1,
    name: 'Amal Ben Salah',
    email: 'amal@tbibi.tn',
    adresse: 'Tunis Centre'
  },
  {
    userId: 2,
    name: 'Youssef Trabelsi',
    email: 'youssef@tbibi.tn',
    adresse: 'Sousse Medina'
  }
];

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should fetch users from the backend', () => {
    service.getUsers().subscribe(users => {
      expect(users).toEqual(USERS);
    });

    const request = httpMock.expectOne('http://localhost:8088/users');
    expect(request.request.method).toBe('GET');
    request.flush(USERS);
  });

  it('should create a user through the backend API', () => {
    const payload: CreateUserRequest = {
      name: 'Mariem Gharbi',
      email: 'mariem@tbibi.tn',
      password: 'pass1234',
      adresse: 'Sfax'
    };

    service.createUser(payload).subscribe(user => {
      expect(user.name).toBe('Mariem Gharbi');
    });

    const request = httpMock.expectOne('http://localhost:8088/users');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ userId: 3, ...payload });
  });

  it('should encode emails when looking up a user by email', () => {
    const email = 'test+user@tbibi.tn';

    service.findUserByEmail(email).subscribe(user => {
      expect(user.email).toBe(email);
    });

    const request = httpMock.expectOne(`http://localhost:8088/users/email/${encodeURIComponent(email)}`);
    expect(request.request.method).toBe('GET');
    request.flush({ userId: 3, name: 'Test User', email, adresse: 'Ariana' });
  });

  it('should persist and clear the active user in local storage', () => {
    service.setCurrentUser(USERS[0]);

    expect(service.getCurrentUserSnapshot()).toEqual(USERS[0]);
    expect(localStorage.getItem('tbibi.active-user')).toContain('Amal Ben Salah');

    service.setCurrentUser(null);

    expect(service.getCurrentUserSnapshot()).toBeNull();
    expect(localStorage.getItem('tbibi.active-user')).toBeNull();
  });

  it('should keep the current user when present and fallback when it disappears', () => {
    service.setCurrentUser(USERS[1]);
    service.ensureCurrentUser(USERS);

    expect(service.getCurrentUserSnapshot()).toEqual(USERS[1]);

    service.ensureCurrentUser([USERS[0]]);
    expect(service.getCurrentUserSnapshot()).toEqual(USERS[0]);

    service.ensureCurrentUser([]);
    expect(service.getCurrentUserSnapshot()).toBeNull();
  });
});
