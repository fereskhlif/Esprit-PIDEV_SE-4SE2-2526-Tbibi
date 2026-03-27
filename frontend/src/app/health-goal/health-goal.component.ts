import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { HealthGoal } from '../models/health-goal';
import { User } from '../models/user';
import { HealthGoalService } from '../services/health-goal.service';
import { UserService } from '../services/user.service';

type GoalField = 'goalTitle' | 'goalDescription' | 'createdDate';
type GoalFieldState = Record<GoalField, string>;
type GoalTouchedState = Record<GoalField, boolean>;

@Component({
  selector: 'app-health-goal',
  templateUrl: './health-goal.component.html',
  styleUrls: ['./health-goal.component.css']
})
export class HealthGoalComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  readonly maxTitleLength = 120;
  readonly maxDescriptionLength = 500;

  currentUser: User | null = null;
  goals: HealthGoal[] = [];
  editingGoalId: number | null = null;
  isSaving = false;
  statusMessage = '';
  errorMessage = '';

  goalForm: HealthGoal = this.createEmptyGoal();
  goalErrors: GoalFieldState = this.createEmptyErrors();
  goalTouched: GoalTouchedState = this.createEmptyTouched();

  constructor(
    private goalService: HealthGoalService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userService.currentUser$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => {
        this.currentUser = user;
        this.statusMessage = '';
        this.errorMessage = '';
        this.resetValidationState();

        if (!user) {
          this.goals = [];
          this.editingGoalId = null;
          this.goalForm = this.createEmptyGoal();
          return;
        }

        this.goalForm = this.createEmptyGoal(user.userId);
        this.editingGoalId = null;
        this.loadGoals();
      });
  }

  get totalGoals(): number {
    return this.goals.length;
  }

  get completedGoals(): number {
    return this.goals.filter(goal => goal.achieved).length;
  }

  get pendingGoals(): number {
    return this.goals.filter(goal => !goal.achieved).length;
  }

  get maxGoalDate(): string {
    return this.today();
  }

  get descriptionLength(): number {
    return this.goalForm.goalDescription.length;
  }

  saveGoal(): void {
    if (!this.currentUser) {
      this.errorMessage = 'Select an active user first.';
      return;
    }

    if (this.validateGoalForm() === false) {
      this.errorMessage = 'Please correct the highlighted goal fields.';
      this.statusMessage = '';
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    const payload: HealthGoal = {
      ...this.goalForm,
      goalTitle: this.goalForm.goalTitle.trim(),
      goalDescription: this.goalForm.goalDescription.trim(),
      userId: this.currentUser.userId
    };

    const request = this.editingGoalId
      ? this.goalService.updateGoal(this.editingGoalId, payload)
      : this.goalService.addGoal(payload);

    request.subscribe({
      next: () => {
        this.isSaving = false;
        this.statusMessage = this.editingGoalId ? 'Goal updated successfully.' : 'Goal created successfully.';
        this.cancelEdit(false);
        this.loadGoals();
      },
      error: error => {
        this.isSaving = false;
        this.errorMessage = this.extractError(error, 'Unable to save the goal.');
      }
    });
  }

  editGoal(goal: HealthGoal): void {
    this.editingGoalId = goal.id ?? null;
    this.goalForm = {
      ...goal,
      createdDate: goal.createdDate || this.today(),
      userId: this.currentUser?.userId ?? goal.userId
    };
    this.resetValidationState();
    this.statusMessage = 'Editing ' + goal.goalTitle + '.';
    this.errorMessage = '';
  }

  toggleAchieved(goal: HealthGoal): void {
    if (!goal.id || !this.currentUser) {
      return;
    }

    this.goalService.updateGoal(goal.id, {
      ...goal,
      achieved: !goal.achieved,
      userId: this.currentUser.userId
    }).subscribe({
      next: () => {
        this.statusMessage = 'Goal status updated.';
        this.loadGoals();
      },
      error: error => {
        this.errorMessage = this.extractError(error, 'Unable to update the goal status.');
      }
    });
  }

  deleteGoal(goal: HealthGoal): void {
    if (!goal.id) {
      return;
    }

    this.goalService.deleteGoal(goal.id).subscribe({
      next: () => {
        if (this.editingGoalId === goal.id) {
          this.cancelEdit(false);
        }
        this.statusMessage = 'Goal deleted.';
        this.loadGoals();
      },
      error: error => {
        this.errorMessage = this.extractError(error, 'Unable to delete the goal.');
      }
    });
  }

  cancelEdit(clearStatus = true): void {
    this.editingGoalId = null;
    this.goalForm = this.createEmptyGoal(this.currentUser?.userId ?? 0);
    this.resetValidationState();
    if (clearStatus) {
      this.statusMessage = '';
      this.errorMessage = '';
    }
  }

  markGoalFieldTouched(field: GoalField): void {
    this.goalTouched[field] = true;
    this.validateGoalField(field);
  }

  onGoalFieldChange(field: GoalField): void {
    if (this.goalTouched[field] === false && this.goalForm[field].trim().length === 0) {
      this.goalErrors[field] = '';
      return;
    }

    this.validateGoalField(field);
  }

  hasGoalFieldError(field: GoalField): boolean {
    return this.goalTouched[field] && this.goalErrors[field].length > 0;
  }

  getGoalFieldError(field: GoalField): string {
    return this.hasGoalFieldError(field) ? this.goalErrors[field] : '';
  }

  private loadGoals(): void {
    if (!this.currentUser) {
      return;
    }

    this.goalService.getGoals(this.currentUser.userId).subscribe({
      next: goals => {
        this.goals = goals;
      },
      error: error => {
        this.errorMessage = this.extractError(error, 'Unable to load goals for the selected user.');
      }
    });
  }

  private validateGoalForm(): boolean {
    const fields: GoalField[] = ['goalTitle', 'goalDescription', 'createdDate'];

    for (const field of fields) {
      this.goalTouched[field] = true;
      this.validateGoalField(field);
    }

    return fields.every(field => this.goalErrors[field].length === 0);
  }

  private validateGoalField(field: GoalField): void {
    const value = this.goalForm[field].trim();

    switch (field) {
      case 'goalTitle':
        if (value.length === 0) {
          this.goalErrors.goalTitle = 'Goal title is required.';
        } else if (value.length < 3) {
          this.goalErrors.goalTitle = 'Goal title must contain at least 3 characters.';
        } else if (value.length > this.maxTitleLength) {
          this.goalErrors.goalTitle = 'Goal title cannot exceed 120 characters.';
        } else {
          this.goalErrors.goalTitle = '';
        }
        break;
      case 'goalDescription':
        if (value.length === 0) {
          this.goalErrors.goalDescription = 'Description is required.';
        } else if (value.length < 10) {
          this.goalErrors.goalDescription = 'Description must contain at least 10 characters.';
        } else if (value.length > this.maxDescriptionLength) {
          this.goalErrors.goalDescription = 'Description cannot exceed 500 characters.';
        } else {
          this.goalErrors.goalDescription = '';
        }
        break;
      case 'createdDate':
        if (value.length === 0) {
          this.goalErrors.createdDate = 'Created date is required.';
        } else if (value > this.today()) {
          this.goalErrors.createdDate = 'Created date cannot be in the future.';
        } else {
          this.goalErrors.createdDate = '';
        }
        break;
    }
  }

  private createEmptyGoal(userId = 0): HealthGoal {
    return {
      goalTitle: '',
      goalDescription: '',
      achieved: false,
      createdDate: this.today(),
      userId
    };
  }

  private createEmptyErrors(): GoalFieldState {
    return {
      goalTitle: '',
      goalDescription: '',
      createdDate: ''
    };
  }

  private createEmptyTouched(): GoalTouchedState {
    return {
      goalTitle: false,
      goalDescription: false,
      createdDate: false
    };
  }

  private resetValidationState(): void {
    this.goalErrors = this.createEmptyErrors();
    this.goalTouched = this.createEmptyTouched();
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private extractError(error: HttpErrorResponse, fallback: string): string {
    return error.error?.message ?? fallback;
  }
}
