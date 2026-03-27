package tn.esprit.pi.tbibi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi.tbibi.entities.MedicalChat;
import tn.esprit.pi.tbibi.exceptions.ForbiddenOperationException;
import tn.esprit.pi.tbibi.exceptions.ResourceNotFoundException;
import tn.esprit.pi.tbibi.repositories.MedicalChatRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalChatServiceImpl implements IMedicalChatService {

    private final MedicalChatRepo repo;

    @Override
    public MedicalChat create(MedicalChat chat) {
        return repo.save(chat);
    }

    @Override
    @Transactional
    public MedicalChat updateMessage(Long messageId, Long currentUserId, String message) {
        MedicalChat chat = getOwnedMessage(messageId, currentUserId, "update");
        chat.setMessage(message.trim());
        chat.setUpdatedAt(LocalDateTime.now());
        return repo.save(chat);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long currentUserId) {
        MedicalChat chat = getOwnedMessage(messageId, currentUserId, "delete");
        repo.delete(chat);
    }

    @Override
    public List<MedicalChat> getMessagesForUser(Long userId) {
        return repo.findMessagesForUser(userId);
    }

    @Override
    public List<MedicalChat> getConversation(Long currentUserId, Long otherUserId) {
        return repo.findConversation(currentUserId, otherUserId);
    }

    private MedicalChat getOwnedMessage(Long messageId, Long currentUserId, String action) {
        MedicalChat chat = repo.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id " + messageId));

        Long senderId = chat.getSender() != null ? chat.getSender().getUserId() : null;
        if (senderId == null || senderId.equals(currentUserId) == false) {
            throw new ForbiddenOperationException("You can only " + action + " your own messages.");
        }

        return chat;
    }
}
