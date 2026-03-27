import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ChatConversation, MedicalChatMessage } from '../models/medical-chat';
import { User } from '../models/user';
import { MedicalChatService } from '../services/medical-chat.service';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-medical-chat',
  templateUrl: './medical-chat.component.html',
  styleUrls: ['./medical-chat.component.css']
})
export class MedicalChatComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/i;

  readonly maxMessageLength = 1000;
  readonly maxEmailLength = 120;

  allUsers: User[] = [];
  conversations: ChatConversation[] = [];
  messages: MedicalChatMessage[] = [];
  currentUser: User | null = null;
  selectedUser: User | null = null;
  searchEmail = '';
  newMessage = '';
  loadingMessages = false;
  isSending = false;
  statusMessage = '';
  errorMessage = '';
  searchTouched = false;
  messageTouched = false;
  searchError = '';
  messageError = '';
  editingMessageId: number | null = null;
  editingMessageValue = '';
  editingMessageTouched = false;
  editingMessageError = '';
  savingMessageId: number | null = null;
  deletingMessageId: number | null = null;

  constructor(
    private chatService: MedicalChatService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userService.currentUser$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => {
        this.currentUser = user;
        this.statusMessage = '';
        this.errorMessage = '';
        this.searchError = '';
        this.messageError = '';
        this.clearEditingState();

        if (!user) {
          this.resetState();
          return;
        }

        if (this.selectedUser?.userId === user.userId) {
          this.selectedUser = null;
        }

        this.loadUsers();
        this.loadConversations();
      });
  }

  get otherUsers(): User[] {
    return this.allUsers.filter(user => user.userId !== this.currentUser?.userId);
  }

  get messageLength(): number {
    return this.newMessage.length;
  }

  get editMessageLength(): number {
    return this.editingMessageValue.length;
  }

  get canSendMessage(): boolean {
    return !!this.currentUser
      && !!this.selectedUser
      && this.isSending === false
      && this.buildMessageError(this.newMessage).length === 0;
  }

  openConversation(user: User): void {
    if (this.currentUser && user.userId === this.currentUser.userId) {
      return;
    }

    this.selectedUser = user;
    this.messageTouched = false;
    this.messageError = '';
    this.clearEditingState();
    this.loadMessages();
    this.statusMessage = 'Conversation ready with ' + user.name + '.';
    this.errorMessage = '';
  }

  markSearchTouched(): void {
    this.searchTouched = true;
    this.searchError = this.buildSearchError(this.searchEmail);
  }

  onSearchEmailChange(): void {
    if (this.searchTouched || this.searchEmail.trim().length > 0) {
      this.searchError = this.buildSearchError(this.searchEmail);
    }
  }

  searchUser(): void {
    this.searchTouched = true;
    this.searchError = this.buildSearchError(this.searchEmail);
    this.statusMessage = '';

    if (this.searchError.length > 0) {
      this.errorMessage = 'Please correct the email field before searching.';
      return;
    }

    this.errorMessage = '';

    this.userService.findUserByEmail(this.searchEmail.trim().toLowerCase()).subscribe({
      next: user => {
        if (this.currentUser && user.userId === this.currentUser.userId) {
          this.errorMessage = 'Choose a different user than the active one.';
          return;
        }

        if (!this.allUsers.some(item => item.userId === user.userId)) {
          this.allUsers = [...this.allUsers, user].sort((first, second) => first.name.localeCompare(second.name));
        }

        this.searchEmail = '';
        this.searchTouched = false;
        this.searchError = '';
        this.openConversation(user);
      },
      error: error => {
        this.errorMessage = this.extractError(error, 'No user was found with that email.');
      }
    });
  }

  markMessageTouched(): void {
    this.messageTouched = true;
    this.messageError = this.buildMessageError(this.newMessage);
  }

  onMessageChange(): void {
    if (this.messageTouched || this.newMessage.trim().length > 0) {
      this.messageError = this.buildMessageError(this.newMessage);
    }
  }

  markEditMessageTouched(): void {
    this.editingMessageTouched = true;
    this.editingMessageError = this.buildMessageError(this.editingMessageValue);
  }

  onEditMessageChange(): void {
    if (this.editingMessageTouched || this.editingMessageValue.trim().length > 0) {
      this.editingMessageError = this.buildMessageError(this.editingMessageValue);
    }
  }

  hasSearchError(): boolean {
    return this.searchTouched && this.searchError.length > 0;
  }

  hasMessageError(): boolean {
    return this.messageTouched && this.messageError.length > 0;
  }

  hasEditMessageError(): boolean {
    return this.editingMessageTouched && this.editingMessageError.length > 0;
  }

  sendMessage(): void {
    if (!this.currentUser || !this.selectedUser) {
      this.errorMessage = 'Choose a conversation before sending a message.';
      return;
    }

    this.messageTouched = true;
    this.messageError = this.buildMessageError(this.newMessage);

    if (this.messageError.length > 0) {
      this.errorMessage = 'Please correct the message field before sending.';
      return;
    }

    this.isSending = true;
    this.errorMessage = '';

    const payload: MedicalChatMessage = {
      senderId: this.currentUser.userId,
      receiverId: this.selectedUser.userId,
      message: this.newMessage.trim()
    };

    this.chatService.sendMessage(payload).subscribe({
      next: () => {
        this.newMessage = '';
        this.messageTouched = false;
        this.messageError = '';
        this.isSending = false;
        this.statusMessage = 'Message sent successfully.';
        this.loadMessages();
        this.loadConversations();
      },
      error: error => {
        this.isSending = false;
        this.errorMessage = this.extractError(error, 'Unable to send the message right now.');
      }
    });
  }

  startEditing(message: MedicalChatMessage): void {
    if (!this.isMine(message) || !message.id) {
      this.errorMessage = 'You can only edit your own messages.';
      return;
    }

    this.editingMessageId = message.id;
    this.editingMessageValue = message.message;
    this.editingMessageTouched = false;
    this.editingMessageError = '';
    this.errorMessage = '';
    this.statusMessage = 'Editing your message.';
  }

  cancelEditing(): void {
    this.clearEditingState();
    this.statusMessage = 'Edit cancelled.';
    this.errorMessage = '';
  }

  saveEditedMessage(message: MedicalChatMessage): void {
    if (!this.currentUser || !message.id || !this.isMine(message)) {
      this.errorMessage = 'You can only edit your own messages.';
      return;
    }

    this.editingMessageTouched = true;
    this.editingMessageError = this.buildMessageError(this.editingMessageValue);

    if (this.editingMessageError.length > 0) {
      this.errorMessage = 'Please correct the message field before saving.';
      return;
    }

    this.savingMessageId = message.id;
    this.errorMessage = '';

    this.chatService.updateMessage(message.id, this.currentUser.userId, this.editingMessageValue.trim()).subscribe({
      next: () => {
        this.savingMessageId = null;
        this.clearEditingState();
        this.statusMessage = 'Message updated successfully.';
        this.loadMessages();
        this.loadConversations();
      },
      error: error => {
        this.savingMessageId = null;
        this.errorMessage = this.extractError(error, 'Unable to update this message right now.');
      }
    });
  }

  deleteMessage(message: MedicalChatMessage): void {
    if (!this.currentUser || !message.id || !this.isMine(message)) {
      this.errorMessage = 'You can only delete your own messages.';
      return;
    }

    if (window.confirm('Delete this message?') === false) {
      return;
    }

    this.deletingMessageId = message.id;
    this.errorMessage = '';

    this.chatService.deleteMessage(message.id, this.currentUser.userId).subscribe({
      next: () => {
        if (this.editingMessageId === message.id) {
          this.clearEditingState();
        }

        this.deletingMessageId = null;
        this.statusMessage = 'Message deleted successfully.';
        this.loadMessages();
        this.loadConversations();
      },
      error: error => {
        this.deletingMessageId = null;
        this.errorMessage = this.extractError(error, 'Unable to delete this message right now.');
      }
    });
  }

  isMine(message: MedicalChatMessage): boolean {
    return this.currentUser?.userId === message.senderId;
  }

  isEditing(message: MedicalChatMessage): boolean {
    return !!message.id && this.editingMessageId === message.id;
  }

  isMessageBusy(message: MedicalChatMessage): boolean {
    return !!message.id && (this.savingMessageId === message.id || this.deletingMessageId === message.id);
  }

  canSaveEditedMessage(): boolean {
    return !!this.currentUser
      && this.editingMessageId !== null
      && this.savingMessageId === null
      && this.buildMessageError(this.editingMessageValue).length === 0;
  }

  private loadUsers(): void {
    this.userService.getUsers().subscribe({
      next: users => {
        this.allUsers = users.sort((first, second) => first.name.localeCompare(second.name));

        if (this.selectedUser) {
          const refreshedUser = users.find(user => user.userId === this.selectedUser?.userId);
          if (refreshedUser) {
            this.selectedUser = refreshedUser;
          }
        }
      },
      error: error => {
        this.errorMessage = this.extractError(error, 'Unable to load users.');
      }
    });
  }

  private loadConversations(): void {
    if (!this.currentUser) {
      return;
    }

    this.chatService.getConversations(this.currentUser.userId).subscribe({
      next: conversations => {
        this.conversations = conversations;

        if (!this.selectedUser && conversations.length > 0) {
          this.selectedUser = this.toUser(conversations[0]);
          this.loadMessages();
        }
      },
      error: error => {
        this.errorMessage = this.extractError(error, 'Unable to load conversation summaries.');
      }
    });
  }

  private loadMessages(): void {
    if (!this.currentUser || !this.selectedUser) {
      this.messages = [];
      return;
    }

    this.loadingMessages = true;

    this.chatService.getConversation(this.currentUser.userId, this.selectedUser.userId).subscribe({
      next: messages => {
        this.messages = messages;
        this.loadingMessages = false;

        if (this.editingMessageId && !messages.some(item => item.id === this.editingMessageId)) {
          this.clearEditingState();
        }
      },
      error: error => {
        this.loadingMessages = false;
        this.errorMessage = this.extractError(error, 'Unable to load the selected conversation.');
      }
    });
  }

  toUser(conversation: ChatConversation): User {
    return this.allUsers.find(user => user.userId === conversation.userId) ?? {
      userId: conversation.userId,
      name: conversation.name,
      email: conversation.email,
      adresse: 'Address not provided'
    };
  }

  private buildSearchError(value: string): string {
    const email = value.trim().toLowerCase();

    if (email.length === 0) {
      return 'Email is required.';
    }

    if (email.length > this.maxEmailLength) {
      return 'Email cannot exceed 120 characters.';
    }

    if (this.emailPattern.test(email) === false) {
      return 'Enter a valid email address.';
    }

    return '';
  }

  private buildMessageError(value: string): string {
    const message = value.trim();

    if (message.length === 0) {
      return 'Message is required.';
    }

    if (message.length < 2) {
      return 'Message must contain at least 2 characters.';
    }

    if (message.length > this.maxMessageLength) {
      return 'Message cannot exceed 1000 characters.';
    }

    return '';
  }

  private clearEditingState(): void {
    this.editingMessageId = null;
    this.editingMessageValue = '';
    this.editingMessageTouched = false;
    this.editingMessageError = '';
    this.savingMessageId = null;
  }

  private resetState(): void {
    this.conversations = [];
    this.messages = [];
    this.selectedUser = null;
    this.searchEmail = '';
    this.newMessage = '';
    this.searchTouched = false;
    this.messageTouched = false;
    this.searchError = '';
    this.messageError = '';
    this.deletingMessageId = null;
    this.clearEditingState();
  }

  private extractError(error: HttpErrorResponse, fallback: string): string {
    return error.error?.message ?? fallback;
  }
}
