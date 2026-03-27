import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { HealthGoal } from '../models/health-goal';
import { HealthGoalService } from './health-goal.service';

const GOAL: HealthGoal = {
  id: 4,
  goalTitle: 'Daily walking',
  goalDescription: 'Walk for at least 30 minutes every day',
  achieved: false,
  createdDate: '2026-03-25',
  userId: 1
};

describe('HealthGoalService', () => {
  let service: HealthGoalService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(HealthGoalService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch goals for a user', () => {
    service.getGoals(1).subscribe(goals => {
      expect(goals).toEqual([GOAL]);
    });

    const request = httpMock.expectOne('http://localhost:8088/api/health-goals/user/1');
    expect(request.request.method).toBe('GET');
    request.flush([GOAL]);
  });

  it('should add a goal', () => {
    service.addGoal(GOAL).subscribe(goal => {
      expect(goal.id).toBe(4);
    });

    const request = httpMock.expectOne('http://localhost:8088/api/health-goals');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(GOAL);
    request.flush(GOAL);
  });

  it('should update a goal', () => {
    service.updateGoal(4, GOAL).subscribe(goal => {
      expect(goal.goalTitle).toBe('Daily walking');
    });

    const request = httpMock.expectOne('http://localhost:8088/api/health-goals/4');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(GOAL);
    request.flush(GOAL);
  });

  it('should delete a goal', () => {
    service.deleteGoal(4).subscribe(response => {
      expect(response).toBeNull();
    });

    const request = httpMock.expectOne('http://localhost:8088/api/health-goals/4');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
