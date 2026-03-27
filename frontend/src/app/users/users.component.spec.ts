import { FormsModule } from '@angular/forms';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of } from 'rxjs';

import { UsersComponent } from './users.component';
import { CreateUserRequest, User } from '../models/user';
import { UserService } from '../services/user.service';

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
  readonly createUser = jasmine.createSpy('createUser');
  readonly findUserByEmail = jasmine.createSpy('findUserByEmail');
  readonly ensureCurrentUser = jasmine.createSpy('ensureCurrentUser');
  readonly setCurrentUser = jasmine.createSpy('setCurrentUser').and.callFake((user: User | null) => {
    this.currentUserSubject.next(user);
  });
}

describe('UsersComponent', () => {
  let component: UsersComponent;
  let fixture: ComponentFixture<UsersComponent>;
  let userService: UserServiceStub;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UsersComponent],
      imports: [FormsModule],
      providers: [{ provide: UserService, useClass: UserServiceStub }]
    }).compileComponents();

    userService = TestBed.inject(UserService) as unknown as UserServiceStub;
    fixture = TestBed.createComponent(UsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load users on init and sync the active profile', () => {
    expect(component.users).toEqual(USERS);
    expect(userService.getUsers).toHaveBeenCalled();
    expect(userService.ensureCurrentUser).toHaveBeenCalledWith(USERS);
  });

  it('should block submission when the form is invalid', () => {
    component.userForm = {
      name: 'A',
      email: 'bad-email',
      password: '123',
      adresse: 'abc'
    };

    component.createUser();

    expect(userService.createUser).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('Please correct the highlighted fields.');
    expect(component.getUserFieldError('email')).toBe('Enter a valid email address.');
  });

  it('should trim the payload, create the user, and activate it', () => {
    const createdUser: User = {
      userId: 3,
      name: 'Mariem Gharbi',
      email: 'mariem@tbibi.tn',
      adresse: 'Sfax'
    };

    userService.createUser.and.returnValue(of(createdUser));

    component.userForm = {
      name: '  Mariem Gharbi  ',
      email: '  MARIEM@TBIBI.TN  ',
      password: '  pass1234  ',
      adresse: '  Sfax  '
    };

    component.createUser();

    expect(userService.createUser).toHaveBeenCalledWith({
      name: 'Mariem Gharbi',
      email: 'mariem@tbibi.tn',
      password: 'pass1234',
      adresse: 'Sfax'
    } as CreateUserRequest);
    expect(userService.setCurrentUser).toHaveBeenCalledWith(createdUser);
    expect(component.statusMessage).toContain('Mariem Gharbi');
    expect(component.userForm).toEqual({ name: '', email: '', password: '', adresse: '' });
  });

  it('should activate a selected user from the list', () => {
    component.activateUser(USERS[0]);

    expect(userService.setCurrentUser).toHaveBeenCalledWith(USERS[0]);
    expect(component.statusMessage).toContain('Amal Ben Salah');
  });
});
