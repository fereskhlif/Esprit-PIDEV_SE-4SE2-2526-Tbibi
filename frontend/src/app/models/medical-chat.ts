export interface MedicalChatMessage {
  id?: number;
  message: string;
  createdAt?: string;
  updatedAt?: string;
  senderId: number;
  senderName?: string;
  senderEmail?: string;
  receiverId: number;
  receiverName?: string;
  receiverEmail?: string;
}

export interface ChatConversation {
  userId: number;
  name: string;
  email: string;
  lastMessage: string;
  lastMessageAt: string;
}
