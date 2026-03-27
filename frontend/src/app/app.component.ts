import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';

import { User } from './models/user';
import { UserService } from './services/user.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'Tbibi Care Portal';
  private readonly destroyRef = inject(DestroyRef);

  readonly navItems = [
    {
      label: 'Patient Directory',
      route: '/users',
      badge: 'Registration',
      caption: 'Create and activate patient profiles',
      icon: 'users'
    },
    {
      label: 'Clinical Messages',
      route: '/chat',
      badge: 'Communication',
      caption: 'Coordinate patient follow-up in real time',
      icon: 'chat'
    },
    {
      label: 'Care Goals',
      route: '/goals',
      badge: 'Follow-up',
      caption: 'Track goals, completion, and progress',
      icon: 'goals'
    }
  ];

  users: User[] = [];
  currentUser: User | null = null;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    const currentUserStream = (this.userService as unknown as Record<string, Observable<User | null>>)['currentUser' + String.fromCharCode(36)];

    currentUserStream
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => {
        this.currentUser = user;
      });

    this.refreshUsers();
  }

  refreshUsers(): void {
    this.userService.getUsers().subscribe({
      next: users => {
        this.users = users;
        this.userService.ensureCurrentUser(users);
      }
    });
  }

  onUserChangeById(userIdValue: string): void {
    const userId = Number(userIdValue);
    const selectedUser = this.users.find(user => user.userId === userId) ?? null;
    this.userService.setCurrentUser(selectedUser);
  }
}
