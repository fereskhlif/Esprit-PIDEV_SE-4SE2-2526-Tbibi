import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { MedicalChatMessage } from '../models/medical-chat';
import { MedicalChatService } from './medical-chat.service';

describe('MedicalChatService', () => {
  let service: MedicalChatService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(MedicalChatService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should post a chat message', () => {
    const payload: MedicalChatMessage = {
      senderId: 1,
      receiverId: 2,
      message: 'Please confirm the appointment'
    };

    service.sendMessage(payload).subscribe(message => {
      expect(message.message).toBe(payload.message);
    });

    const request = httpMock.expectOne('http://localhost:8088/medical-chat/send');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ id: 10, ...payload, createdAt: '2026-03-26T10:00:00' });
  });

  it('should put an updated chat message', () => {
    service.updateMessage(10, 1, 'Updated appointment note').subscribe(message => {
      expect(message.updatedAt).toBe('2026-03-26T11:15:00');
    });

    const request = httpMock.expectOne('http://localhost:8088/medical-chat/10');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      currentUserId: 1,
      message: 'Updated appointment note'
    });
    request.flush({
      id: 10,
      senderId: 1,
      receiverId: 2,
      message: 'Updated appointment note',
      createdAt: '2026-03-26T10:00:00',
      updatedAt: '2026-03-26T11:15:00'
    });
  });

  it('should delete a chat message for the current user', () => {
    service.deleteMessage(10, 1).subscribe(response => {
      expect(response).toBeNull();
    });

    const request = httpMock.expectOne('http://localhost:8088/medical-chat/10?currentUserId=1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('should request a full conversation between two users', () => {
    service.getConversation(1, 2).subscribe(messages => {
      expect(messages.length).toBe(1);
    });

    const request = httpMock.expectOne(
      'http://localhost:8088/medical-chat/conversation?currentUserId=1&otherUserId=2'
    );
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 1, senderId: 1, receiverId: 2, message: 'Hello' }]);
  });

  it('should request conversation summaries for a user', () => {
    service.getConversations(3).subscribe(conversations => {
      expect(conversations[0].userId).toBe(2);
    });

    const request = httpMock.expectOne('http://localhost:8088/medical-chat/conversations/3');
    expect(request.request.method).toBe('GET');
    request.flush([
      {
        userId: 2,
        name: 'Youssef Trabelsi',
        email: 'youssef@tbibi.tn',
        lastMessage: 'Confirmed',
        lastMessageAt: '2026-03-26T09:30:00'
      }
    ]);
  });
});
