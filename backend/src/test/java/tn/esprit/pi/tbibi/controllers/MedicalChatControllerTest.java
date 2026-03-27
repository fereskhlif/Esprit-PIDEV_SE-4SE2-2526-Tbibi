package tn.esprit.pi.tbibi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.pi.tbibi.DTO.MedicalChatDto;
import tn.esprit.pi.tbibi.DTO.MedicalChatUpdateDto;
import tn.esprit.pi.tbibi.entities.MedicalChat;
import tn.esprit.pi.tbibi.entities.User;
import tn.esprit.pi.tbibi.exceptions.ForbiddenOperationException;
import tn.esprit.pi.tbibi.exceptions.GlobalExceptionHandler;
import tn.esprit.pi.tbibi.repositories.UserRepo;
import tn.esprit.pi.tbibi.services.IMedicalChatService;
import tn.esprit.pi.tbibi.services.MedicalChatMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicalChatController.class)
@Import({GlobalExceptionHandler.class, MedicalChatMapper.class})
class MedicalChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IMedicalChatService medicalChatService;

    @MockBean
    private UserRepo userRepo;

    @Test
    void sendShouldAttachUsersAndReturnMappedDto() throws Exception {
        User sender = user(1L, "Amal Ben Salah");
        User receiver = user(2L, "Youssef Trabelsi");

        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(medicalChatService.create(any(MedicalChat.class))).thenAnswer(invocation -> {
            MedicalChat savedMessage = invocation.getArgument(0);
            savedMessage.setId(88L);
            return savedMessage;
        });

        MedicalChatDto payload = new MedicalChatDto();
        payload.setMessage("Please confirm tomorrow's appointment");
        payload.setSenderId(1L);
        payload.setReceiverId(2L);

        mockMvc.perform(post("/medical-chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(88))
                .andExpect(jsonPath("$.senderId").value(1))
                .andExpect(jsonPath("$.senderName").value("Amal Ben Salah"))
                .andExpect(jsonPath("$.receiverName").value("Youssef Trabelsi"))
                .andExpect(jsonPath("$.message").value("Please confirm tomorrow's appointment"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        ArgumentCaptor<MedicalChat> chatCaptor = ArgumentCaptor.forClass(MedicalChat.class);
        verify(medicalChatService).create(chatCaptor.capture());

        MedicalChat savedChat = chatCaptor.getValue();
        assertThat(savedChat.getSender().getUserId()).isEqualTo(1L);
        assertThat(savedChat.getReceiver().getUserId()).isEqualTo(2L);
        assertThat(savedChat.getCreatedAt()).isNotNull();
    }

    @Test
    void sendShouldRejectInvalidPayload() throws Exception {
        MedicalChatDto payload = new MedicalChatDto();
        payload.setMessage("");
        payload.setSenderId(1L);
        payload.setReceiverId(2L);

        mockMvc.perform(post("/medical-chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("message:")));

        verifyNoInteractions(medicalChatService, userRepo);
    }

    @Test
    void updateShouldReturnUpdatedMessage() throws Exception {
        User sender = user(1L, "Amal Ben Salah");
        User receiver = user(2L, "Youssef Trabelsi");
        MedicalChat updated = chat(88L, "Updated appointment note", LocalDateTime.of(2026, 3, 26, 10, 30), sender, receiver);
        updated.setUpdatedAt(LocalDateTime.of(2026, 3, 26, 11, 15));

        when(medicalChatService.updateMessage(88L, 1L, "Updated appointment note")).thenReturn(updated);

        MedicalChatUpdateDto payload = new MedicalChatUpdateDto("Updated appointment note", 1L);

        mockMvc.perform(put("/medical-chat/88")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(88))
                .andExpect(jsonPath("$.message").value("Updated appointment note"))
                .andExpect(jsonPath("$.updatedAt").value("2026-03-26T11:15:00"));

        verify(medicalChatService).updateMessage(88L, 1L, "Updated appointment note");
    }

    @Test
    void updateShouldTranslateForbiddenEditToForbiddenStatus() throws Exception {
        when(medicalChatService.updateMessage(88L, 2L, "Updated appointment note"))
                .thenThrow(new ForbiddenOperationException("You can only update your own messages."));

        MedicalChatUpdateDto payload = new MedicalChatUpdateDto("Updated appointment note", 2L);

        mockMvc.perform(put("/medical-chat/88")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only update your own messages."));
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/medical-chat/88")
                        .param("currentUserId", "1"))
                .andExpect(status().isNoContent());

        verify(medicalChatService).deleteMessage(88L, 1L);
    }

    @Test
    void getConversationShouldReturnOrderedMessages() throws Exception {
        User currentUser = user(1L, "Amal Ben Salah");
        User otherUser = user(2L, "Youssef Trabelsi");
        MedicalChat firstMessage = chat(10L, "First message", LocalDateTime.of(2026, 3, 20, 8, 30), currentUser, otherUser);
        MedicalChat secondMessage = chat(11L, "Reply message", LocalDateTime.of(2026, 3, 20, 8, 45), otherUser, currentUser);

        when(medicalChatService.getConversation(1L, 2L)).thenReturn(List.of(firstMessage, secondMessage));

        mockMvc.perform(get("/medical-chat/conversation")
                        .param("currentUserId", "1")
                        .param("otherUserId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("First message"))
                .andExpect(jsonPath("$[1].receiverId").value(1));
    }

    @Test
    void getConversationsShouldReturnLatestSummaryPerOtherUserSortedByRecency() throws Exception {
        User currentUser = user(1L, "Amal Ben Salah");
        User youssef = user(2L, "Youssef Trabelsi");
        User mariem = user(3L, "Mariem Gharbi");

        MedicalChat latestMariem = chat(1L, "Latest update", LocalDateTime.of(2026, 3, 26, 12, 0), currentUser, mariem);
        latestMariem.setUpdatedAt(LocalDateTime.of(2026, 3, 26, 12, 30));
        MedicalChat latestYoussef = chat(2L, "Need confirmation", LocalDateTime.of(2026, 3, 26, 10, 0), youssef, currentUser);
        MedicalChat olderMariem = chat(3L, "Older note", LocalDateTime.of(2026, 3, 25, 9, 0), mariem, currentUser);

        when(medicalChatService.getMessagesForUser(1L)).thenReturn(List.of(latestMariem, latestYoussef, olderMariem));

        mockMvc.perform(get("/medical-chat/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(3))
                .andExpect(jsonPath("$[0].lastMessage").value("Latest update"))
                .andExpect(jsonPath("$[1].userId").value(2));
    }

    private MedicalChat chat(Long id, String message, LocalDateTime createdAt, User sender, User receiver) {
        MedicalChat chat = new MedicalChat();
        chat.setId(id);
        chat.setMessage(message);
        chat.setCreatedAt(createdAt);
        chat.setSender(sender);
        chat.setReceiver(receiver);
        return chat;
    }

    private User user(Long id, String name) {
        User user = new User();
        user.setUserId(id);
        user.setName(name);
        user.setEmail(name.toLowerCase().replace(" ", ".") + "@tbibi.tn");
        return user;
    }
}
