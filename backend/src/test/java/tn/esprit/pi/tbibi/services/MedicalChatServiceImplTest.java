package tn.esprit.pi.tbibi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.tbibi.entities.MedicalChat;
import tn.esprit.pi.tbibi.entities.User;
import tn.esprit.pi.tbibi.exceptions.ForbiddenOperationException;
import tn.esprit.pi.tbibi.repositories.MedicalChatRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalChatServiceImplTest {

    @Mock
    private MedicalChatRepo medicalChatRepo;

    private MedicalChatServiceImpl medicalChatService;

    @BeforeEach
    void setUp() {
        medicalChatService = new MedicalChatServiceImpl(medicalChatRepo);
    }

    @Test
    void createShouldPersistChatMessage() {
        MedicalChat chat = new MedicalChat();
        chat.setMessage("Follow-up scheduled");

        when(medicalChatRepo.save(chat)).thenReturn(chat);

        MedicalChat saved = medicalChatService.create(chat);

        assertThat(saved).isSameAs(chat);
        verify(medicalChatRepo).save(chat);
    }

    @Test
    void updateMessageShouldPersistTrimmedMessageForSender() {
        MedicalChat chat = chat(7L, 1L, 2L, "Original note");
        when(medicalChatRepo.findById(7L)).thenReturn(Optional.of(chat));
        when(medicalChatRepo.save(chat)).thenReturn(chat);

        MedicalChat updated = medicalChatService.updateMessage(7L, 1L, "  Updated note  ");

        assertThat(updated.getMessage()).isEqualTo("Updated note");
        assertThat(updated.getUpdatedAt()).isNotNull();
        verify(medicalChatRepo).save(chat);
    }

    @Test
    void updateMessageShouldRejectNonOwner() {
        MedicalChat chat = chat(7L, 2L, 1L, "Original note");
        when(medicalChatRepo.findById(7L)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> medicalChatService.updateMessage(7L, 1L, "Updated note"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You can only update your own messages.");

        verify(medicalChatRepo, never()).save(chat);
    }

    @Test
    void deleteMessageShouldRemoveOwnedMessage() {
        MedicalChat chat = chat(9L, 1L, 2L, "To delete");
        when(medicalChatRepo.findById(9L)).thenReturn(Optional.of(chat));

        medicalChatService.deleteMessage(9L, 1L);

        verify(medicalChatRepo).delete(chat);
    }

    @Test
    void deleteMessageShouldRejectNonOwner() {
        MedicalChat chat = chat(9L, 2L, 1L, "To delete");
        when(medicalChatRepo.findById(9L)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> medicalChatService.deleteMessage(9L, 1L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You can only delete your own messages.");

        verify(medicalChatRepo, never()).delete(chat);
    }

    @Test
    void getMessagesForUserShouldReturnRepositoryResult() {
        MedicalChat first = new MedicalChat();
        MedicalChat second = new MedicalChat();
        when(medicalChatRepo.findMessagesForUser(1L)).thenReturn(List.of(first, second));

        List<MedicalChat> messages = medicalChatService.getMessagesForUser(1L);

        assertThat(messages).containsExactly(first, second);
        verify(medicalChatRepo).findMessagesForUser(1L);
    }

    @Test
    void getConversationShouldReturnRepositoryResult() {
        MedicalChat message = new MedicalChat();
        when(medicalChatRepo.findConversation(1L, 2L)).thenReturn(List.of(message));

        List<MedicalChat> conversation = medicalChatService.getConversation(1L, 2L);

        assertThat(conversation).containsExactly(message);
        verify(medicalChatRepo).findConversation(1L, 2L);
    }

    private MedicalChat chat(Long id, Long senderId, Long receiverId, String message) {
        User sender = new User();
        sender.setUserId(senderId);
        User receiver = new User();
        receiver.setUserId(receiverId);

        MedicalChat chat = new MedicalChat();
        chat.setId(id);
        chat.setMessage(message);
        chat.setSender(sender);
        chat.setReceiver(receiver);
        return chat;
    }
}
