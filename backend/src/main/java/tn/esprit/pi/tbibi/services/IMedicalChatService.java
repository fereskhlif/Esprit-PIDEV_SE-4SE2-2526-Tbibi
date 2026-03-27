package tn.esprit.pi.tbibi.services;

import tn.esprit.pi.tbibi.entities.MedicalChat;

import java.util.List;

public interface IMedicalChatService {

    MedicalChat create(MedicalChat chat);

    MedicalChat updateMessage(Long messageId, Long currentUserId, String message);

    void deleteMessage(Long messageId, Long currentUserId);

    List<MedicalChat> getMessagesForUser(Long userId);

    List<MedicalChat> getConversation(Long currentUserId, Long otherUserId);
}
