import { FormsModule } from '@angular/forms';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of } from 'rxjs';

import { HealthGoalComponent } from './health-goal.component';
import { HealthGoal } from '../models/health-goal';
import { User } from '../models/user';
import { HealthGoalService } from '../services/health-goal.service';
import { UserService } from '../services/user.service';

const ACTIVE_USER: User = {
  userId: 1,
  name: 'Amal Ben Salah',
  email: 'amal@tbibi.tn',
  adresse: 'Tunis Centre'
};

const GOAL: HealthGoal = {
  id: 4,
  goalTitle: 'Daily walking',
  goalDescription: 'Walk for at least 30 minutes every day',
  achieved: false,
  createdDate: '2026-03-25',
  userId: 1
};

class UserServiceStub {
  private readonly currentUserSubject = new BehaviorSubject<User | null>(ACTIVE_USER);

  readonly currentUser$ = this.currentUserSubject.asObservable();
}

class HealthGoalServiceStub {
  readonly getGoals = jasmine.createSpy('getGoals').and.returnValue(of([GOAL]));
  readonly addGoal = jasmine.createSpy('addGoal').and.returnValue(of({ ...GOAL, id: 5 }));
  readonly updateGoal = jasmine.createSpy('updateGoal').and.returnValue(of({ ...GOAL, achieved: true }));
  readonly deleteGoal = jasmine.createSpy('deleteGoal').and.returnValue(of(void 0));
}

describe('HealthGoalComponent', () => {
  let component: HealthGoalComponent;
  let fixture: ComponentFixture<HealthGoalComponent>;
  let goalService: HealthGoalServiceStub;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [HealthGoalComponent],
      imports: [FormsModule],
      providers: [
        { provide: UserService, useClass: UserServiceStub },
        { provide: HealthGoalService, useClass: HealthGoalServiceStub }
      ]
    }).compileComponents();

    goalService = TestBed.inject(HealthGoalService) as unknown as HealthGoalServiceStub;
    fixture = TestBed.createComponent(HealthGoalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load goals for the active user on init', () => {
    expect(goalService.getGoals).toHaveBeenCalledWith(1);
    expect(component.goals).toEqual([GOAL]);
    expect(component.totalGoals).toBe(1);
    expect(component.pendingGoals).toBe(1);
  });

  it('should block saving when the form is invalid', () => {
    component.goalForm = {
      goalTitle: '',
      goalDescription: 'short',
      achieved: false,
      createdDate: '',
      userId: 1
    };

    component.saveGoal();

    expect(goalService.addGoal).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('Please correct the highlighted goal fields.');
    expect(component.getGoalFieldError('goalTitle')).toBe('Goal title is required.');
  });

  it('should trim and create a new goal for the active user', () => {
    goalService.addGoal.calls.reset();
    component.goalForm = {
      goalTitle: '  Daily walk  ',
      goalDescription: '  Walk for at least thirty minutes every day  ',
      achieved: false,
      createdDate: '2026-03-25',
      userId: 0
    };

    component.saveGoal();

    expect(goalService.addGoal).toHaveBeenCalledWith({
      goalTitle: 'Daily walk',
      goalDescription: 'Walk for at least thirty minutes every day',
      achieved: false,
      createdDate: '2026-03-25',
      userId: 1
    });
    expect(component.statusMessage).toBe('Goal created successfully.');
    expect(component.editingGoalId).toBeNull();
  });

  it('should populate the form when editing an existing goal', () => {
    component.editGoal(GOAL);

    expect(component.editingGoalId).toBe(4);
    expect(component.goalForm.goalTitle).toBe('Daily walking');
    expect(component.statusMessage).toContain('Daily walking');
  });

  it('should toggle the achieved state using the update service', () => {
    component.toggleAchieved(GOAL);

    expect(goalService.updateGoal).toHaveBeenCalledWith(4, {
      ...GOAL,
      achieved: true,
      userId: 1
    });
  });

  it('should delete a goal and leave edit mode when needed', () => {
    component.editingGoalId = 4;

    component.deleteGoal(GOAL);

    expect(goalService.deleteGoal).toHaveBeenCalledWith(4);
    expect(component.editingGoalId).toBeNull();
    expect(component.statusMessage).toBe('Goal deleted.');
  });
});
