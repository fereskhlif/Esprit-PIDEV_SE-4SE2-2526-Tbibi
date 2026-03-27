import { FormsModule } from '@angular/forms';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of } from 'rxjs';

import { MedicalChatComponent } from './medical-chat.component';
import { ChatConversation, MedicalChatMessage } from '../models/medical-chat';
import { User } from '../models/user';
import { MedicalChatService } from '../services/medical-chat.service';
import { UserService } from '../services/user.service';

const ACTIVE_USER: User = {
  userId: 1,
  name: 'Amal Ben Salah',
  email: 'amal@tbibi.tn',
  adresse: 'Tunis Centre'
};

const SECONDARY_USER: User = {
  userId: 2,
  name: 'Youssef Trabelsi',
  email: 'youssef@tbibi.tn',
  adresse: 'Sousse Medina'
};

const THIRD_USER: User = {
  userId: 3,
  name: 'Mariem Gharbi',
  email: 'mariem@tbibi.tn',
  adresse: 'Sfax El Jadida'
};

const SUMMARY: ChatConversation = {
  userId: 2,
  name: 'Youssef Trabelsi',
  email: 'youssef@tbibi.tn',
  lastMessage: 'Please confirm tomorrow',
  lastMessageAt: '2026-03-26T09:30:00'
};

const MESSAGE: MedicalChatMessage = {
  id: 8,
  senderId: 1,
  receiverId: 2,
  message: 'Please confirm tomorrow',
  createdAt: '2026-03-26T09:30:00'
};

class UserServiceStub {
  private readonly currentUserSubject = new BehaviorSubject<User | null>(ACTIVE_USER);

  readonly currentUser$ = this.currentUserSubject.asObservable();
  readonly getUsers = jasmine.createSpy('getUsers').and.returnValue(of([ACTIVE_USER, SECONDARY_USER]));
  readonly findUserByEmail = jasmine.createSpy('findUserByEmail');
  readonly setCurrentUser = jasmine.createSpy('setCurrentUser').and.callFake((user: User | null) => {
    this.currentUserSubject.next(user);
  });
}

class MedicalChatServiceStub {
  readonly getConversations = jasmine.createSpy('getConversations').and.returnValue(of([SUMMARY]));
  readonly getConversation = jasmine.createSpy('getConversation').and.returnValue(of([MESSAGE]));
  readonly sendMessage = jasmine.createSpy('sendMessage').and.returnValue(of({ ...MESSAGE }));
  readonly updateMessage = jasmine.createSpy('updateMessage').and.returnValue(of({
    ...MESSAGE,
    message: 'Updated note',
    updatedAt: '2026-03-26T10:15:00'
  }));
  readonly deleteMessage = jasmine.createSpy('deleteMessage').and.returnValue(of(void 0));
}

describe('MedicalChatComponent', () => {
  let component: MedicalChatComponent;
  let fixture: ComponentFixture<MedicalChatComponent>;
  let userService: UserServiceStub;
  let chatService: MedicalChatServiceStub;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MedicalChatComponent],
      imports: [FormsModule],
      providers: [
        { provide: UserService, useClass: UserServiceStub },
        { provide: MedicalChatService, useClass: MedicalChatServiceStub }
      ]
    }).compileComponents();

    userService = TestBed.inject(UserService) as unknown as UserServiceStub;
    chatService = TestBed.inject(MedicalChatService) as unknown as MedicalChatServiceStub;
    fixture = TestBed.createComponent(MedicalChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load users, summaries, and the first conversation on init', () => {
    expect(component.currentUser).toEqual(ACTIVE_USER);
    expect(component.selectedUser?.userId).toBe(SECONDARY_USER.userId);
    expect(component.messages).toEqual([MESSAGE]);
    expect(chatService.getConversations).toHaveBeenCalledWith(1);
    expect(chatService.getConversation).toHaveBeenCalledWith(1, 2);
  });

  it('should reject an invalid email before searching', () => {
    component.searchEmail = 'bad-email';

    component.searchUser();

    expect(userService.findUserByEmail).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('Please correct the email field before searching.');
    expect(component.hasSearchError()).toBeTrue();
  });

  it('should search a user by email and open the returned conversation', () => {
    userService.getUsers.and.returnValue(of([ACTIVE_USER]));
    userService.findUserByEmail.and.returnValue(of(THIRD_USER));
    chatService.getConversations.and.returnValue(of([]));
    chatService.getConversation.and.returnValue(of([{ ...MESSAGE, receiverId: 3, message: 'Welcome Mariem' }]));

    component.searchEmail = '  MARIEM@TBIBI.TN  ';
    component.searchUser();

    expect(userService.findUserByEmail).toHaveBeenCalledWith('mariem@tbibi.tn');
    expect(component.selectedUser?.userId).toBe(3);
    expect(component.searchEmail).toBe('');
    expect(component.allUsers.some(user => user.userId === 3)).toBeTrue();
  });

  it('should block sending if no conversation is selected', () => {
    component.selectedUser = null;
    component.newMessage = 'Need help';

    component.sendMessage();

    expect(chatService.sendMessage).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('Choose a conversation before sending a message.');
  });

  it('should trim and send a valid message', () => {
    chatService.sendMessage.calls.reset();
    component.selectedUser = SECONDARY_USER;
    component.newMessage = '  Need a quick follow-up  ';

    component.sendMessage();

    expect(chatService.sendMessage).toHaveBeenCalledWith({
      senderId: 1,
      receiverId: 2,
      message: 'Need a quick follow-up'
    });
    expect(component.newMessage).toBe('');
    expect(component.statusMessage).toBe('Message sent successfully.');
  });

  it('should edit one of my messages', () => {
    component.startEditing(MESSAGE);
    component.editingMessageValue = '  Updated note  ';

    component.saveEditedMessage(MESSAGE);

    expect(chatService.updateMessage).toHaveBeenCalledWith(8, 1, 'Updated note');
    expect(component.editingMessageId).toBeNull();
    expect(component.statusMessage).toBe('Message updated successfully.');
  });

  it('should delete one of my messages after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);

    component.deleteMessage(MESSAGE);

    expect(chatService.deleteMessage).toHaveBeenCalledWith(8, 1);
    expect(component.statusMessage).toBe('Message deleted successfully.');
  });
});
