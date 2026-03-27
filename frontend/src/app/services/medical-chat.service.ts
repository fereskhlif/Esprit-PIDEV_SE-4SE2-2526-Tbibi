import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ChatConversation, MedicalChatMessage } from '../models/medical-chat';

@Injectable({
  providedIn: 'root'
})
export class MedicalChatService {
  private readonly apiUrl = 'http://localhost:8088/medical-chat';

  constructor(private http: HttpClient) {}

  sendMessage(message: MedicalChatMessage): Observable<MedicalChatMessage> {
    return this.http.post<MedicalChatMessage>(`${this.apiUrl}/send`, message);
  }

  updateMessage(messageId: number, currentUserId: number, message: string): Observable<MedicalChatMessage> {
    return this.http.put<MedicalChatMessage>(`${this.apiUrl}/${messageId}`, {
      currentUserId,
      message
    });
  }

  deleteMessage(messageId: number, currentUserId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${messageId}?currentUserId=${currentUserId}`);
  }

  getConversation(currentUserId: number, otherUserId: number): Observable<MedicalChatMessage[]> {
    return this.http.get<MedicalChatMessage[]>(
      `${this.apiUrl}/conversation?currentUserId=${currentUserId}&otherUserId=${otherUserId}`
    );
  }

  getConversations(userId: number): Observable<ChatConversation[]> {
    return this.http.get<ChatConversation[]>(`${this.apiUrl}/conversations/${userId}`);
  }
}
