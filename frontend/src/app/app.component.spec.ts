import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of } from 'rxjs';

import { AppComponent } from './app.component';
import { User } from './models/user';
import { UserService } from './services/user.service';

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

class UserServiceStub {
  private readonly currentUserSubject = new BehaviorSubject<User | null>(null);

  readonly currentUser$ = this.currentUserSubject.asObservable();
  readonly getUsers = jasmine.createSpy('getUsers').and.returnValue(of(USERS));
  readonly ensureCurrentUser = jasmine.createSpy('ensureCurrentUser');
  readonly setCurrentUser = jasmine.createSpy('setCurrentUser').and.callFake((user: User | null) => {
    this.currentUserSubject.next(user);
  });

  emitCurrentUser(user: User | null): void {
    this.currentUserSubject.next(user);
  }
}

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let userService: UserServiceStub;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AppComponent],
      providers: [{ provide: UserService, useClass: UserServiceStub }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    userService = TestBed.inject(UserService) as unknown as UserServiceStub;
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load users on init', () => {
    expect(component).toBeTruthy();
    expect(component.title).toBe('Tbibi Care Portal');
    expect(component.users).toEqual(USERS);
    expect(userService.getUsers).toHaveBeenCalled();
    expect(userService.ensureCurrentUser).toHaveBeenCalledWith(USERS);
  });

  it('should keep the current user in sync with the shared identity stream', () => {
    userService.emitCurrentUser(USERS[0]);

    expect(component.currentUser).toEqual(USERS[0]);
  });

  it('should activate a selected user by id', () => {
    component.users = USERS;

    component.onUserChangeById('2');

    expect(userService.setCurrentUser).toHaveBeenCalledWith(USERS[1]);
  });
});
