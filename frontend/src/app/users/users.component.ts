import { HttpErrorResponse } from "@angular/common/http";
import { Component, DestroyRef, OnInit, inject } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Observable } from "rxjs";

import { CreateUserRequest, User } from "../models/user";
import { UserService } from "../services/user.service";

type UserField = "name" | "email" | "password" | "adresse";
type UserFieldState = Record<UserField, string>;
type UserTouchedState = Record<UserField, boolean>;

@Component({
  selector: "app-users",
  templateUrl: "./users.component.html",
  styleUrls: ["./users.component.css"]
})
export class UsersComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/i;

  readonly maxNameLength = 80;
  readonly maxEmailLength = 120;
  readonly maxPasswordLength = 100;
  readonly maxAddressLength = 120;

  users: User[] = [];
  currentUser: User | null = null;
  isSubmitting = false;
  statusMessage = "";
  errorMessage = "";

  userForm: CreateUserRequest = this.createEmptyForm();
  userErrors: UserFieldState = this.createEmptyErrors();
  userTouched: UserTouchedState = this.createEmptyTouched();

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    const currentUserStream = (this.userService as unknown as Record<string, Observable<User | null>>)["currentUser" + String.fromCharCode(36)];

    currentUserStream
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => {
        this.currentUser = user;
      });

    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getUsers().subscribe({
      next: users => {
        this.users = users;
        this.userService.ensureCurrentUser(users);
      },
      error: error => {
        this.errorMessage = this.extractError(error, "Unable to load users from the backend.");
      }
    });
  }

  createUser(): void {
    if (this.validateUserForm() === false) {
      this.errorMessage = "Please correct the highlighted fields.";
      this.statusMessage = "";
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = "";
    this.statusMessage = "";

    const payload: CreateUserRequest = {
      name: this.userForm.name.trim(),
      email: this.userForm.email.trim().toLowerCase(),
      password: this.userForm.password.trim(),
      adresse: this.userForm.adresse.trim()
    };

    this.userService.createUser(payload).subscribe({
      next: user => {
        this.statusMessage = user.name + " was created and selected as the active user.";
        this.resetUserForm();
        this.userService.setCurrentUser(user);
        this.isSubmitting = false;
        this.loadUsers();
      },
      error: error => {
        this.isSubmitting = false;
        this.errorMessage = this.extractError(error, "Unable to create this user.");
      }
    });
  }

  activateUser(user: User): void {
    this.userService.setCurrentUser(user);
    this.statusMessage = user.name + " is now the active user for chat and goals.";
    this.errorMessage = "";
  }

  markUserFieldTouched(field: UserField): void {
    this.userTouched[field] = true;
    this.validateUserField(field);
  }

  onUserFieldChange(field: UserField): void {
    if (this.userTouched[field] === false && this.userForm[field].trim().length === 0) {
      this.userErrors[field] = "";
      return;
    }

    this.validateUserField(field);
  }

  hasUserFieldError(field: UserField): boolean {
    return this.userTouched[field] && this.userErrors[field].length > 0;
  }

  getUserFieldError(field: UserField): string {
    return this.hasUserFieldError(field) ? this.userErrors[field] : "";
  }

  private validateUserForm(): boolean {
    const fields: UserField[] = ["name", "email", "password", "adresse"];

    for (const field of fields) {
      this.userTouched[field] = true;
      this.validateUserField(field);
    }

    return fields.every(field => this.userErrors[field].length === 0);
  }

  private validateUserField(field: UserField): void {
    const value = this.userForm[field].trim();

    switch (field) {
      case "name":
        if (value.length === 0) {
          this.userErrors.name = "Full name is required.";
        } else if (value.length < 2) {
          this.userErrors.name = "Full name must contain at least 2 characters.";
        } else if (value.length > this.maxNameLength) {
          this.userErrors.name = "Full name cannot exceed 80 characters.";
        } else {
          this.userErrors.name = "";
        }
        break;
      case "email":
        if (value.length === 0) {
          this.userErrors.email = "Email is required.";
        } else if (value.length > this.maxEmailLength) {
          this.userErrors.email = "Email cannot exceed 120 characters.";
        } else if (this.emailPattern.test(value) === false) {
          this.userErrors.email = "Enter a valid email address.";
        } else {
          this.userErrors.email = "";
        }
        break;
      case "password":
        if (value.length === 0) {
          this.userErrors.password = "Password is required.";
        } else if (value.length < 4) {
          this.userErrors.password = "Password must contain at least 4 characters.";
        } else if (value.length > this.maxPasswordLength) {
          this.userErrors.password = "Password cannot exceed 100 characters.";
        } else {
          this.userErrors.password = "";
        }
        break;
      case "adresse":
        if (value.length === 0) {
          this.userErrors.adresse = "Address is required.";
        } else if (value.length < 4) {
          this.userErrors.adresse = "Address must contain at least 4 characters.";
        } else if (value.length > this.maxAddressLength) {
          this.userErrors.adresse = "Address cannot exceed 120 characters.";
        } else {
          this.userErrors.adresse = "";
        }
        break;
    }
  }

  private resetUserForm(): void {
    this.userForm = this.createEmptyForm();
    this.userErrors = this.createEmptyErrors();
    this.userTouched = this.createEmptyTouched();
  }

  private createEmptyForm(): CreateUserRequest {
    return {
      name: "",
      email: "",
      password: "",
      adresse: ""
    };
  }

  private createEmptyErrors(): UserFieldState {
    return {
      name: "",
      email: "",
      password: "",
      adresse: ""
    };
  }

  private createEmptyTouched(): UserTouchedState {
    return {
      name: false,
      email: false,
      password: false,
      adresse: false
    };
  }

  private extractError(error: HttpErrorResponse, fallback: string): string {
    return error.error?.message ?? fallback;
  }
}
