package tn.esprit.pi.tbibi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.tbibi.DTO.ChatConversationDto;
import tn.esprit.pi.tbibi.DTO.MedicalChatDto;
import tn.esprit.pi.tbibi.DTO.MedicalChatUpdateDto;
import tn.esprit.pi.tbibi.entities.MedicalChat;
import tn.esprit.pi.tbibi.entities.User;
import tn.esprit.pi.tbibi.exceptions.ResourceNotFoundException;
import tn.esprit.pi.tbibi.repositories.UserRepo;
import tn.esprit.pi.tbibi.services.IMedicalChatService;
import tn.esprit.pi.tbibi.services.MedicalChatMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medical-chat")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class MedicalChatController {

    private final IMedicalChatService service;
    private final MedicalChatMapper mapper;
    private final UserRepo userRepo;

    @PostMapping("/send")
    public MedicalChatDto send(@Valid @RequestBody MedicalChatDto dto) {
        MedicalChat chat = mapper.toEntity(dto);

        User sender = userRepo.findById(dto.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with id " + dto.getSenderId()));

        User receiver = userRepo.findById(dto.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id " + dto.getReceiverId()));

        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setUpdatedAt(null);

        return mapper.toDto(service.create(chat));
    }

    @PutMapping("/{messageId}")
    public MedicalChatDto updateMessage(@PathVariable Long messageId, @Valid @RequestBody MedicalChatUpdateDto dto) {
        return mapper.toDto(service.updateMessage(messageId, dto.getCurrentUserId(), dto.getMessage()));
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable Long messageId, @RequestParam Long currentUserId) {
        service.deleteMessage(messageId, currentUserId);
    }

    @GetMapping("/user/{id}")
    public List<MedicalChatDto> getUserMessages(@PathVariable Long id) {
        return service.getMessagesForUser(id)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @GetMapping("/conversation")
    public List<MedicalChatDto> getConversation(
            @RequestParam Long currentUserId,
            @RequestParam Long otherUserId) {

        return service.getConversation(currentUserId, otherUserId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @GetMapping("/conversations/{userId}")
    public List<ChatConversationDto> getConversations(@PathVariable Long userId) {
        Map<Long, MedicalChat> latestConversationByUser = new LinkedHashMap<>();

        for (MedicalChat chat : service.getMessagesForUser(userId)) {
            User otherUser = mapper.resolveOtherUser(chat, userId);
            latestConversationByUser.putIfAbsent(otherUser.getUserId(), chat);
        }

        return latestConversationByUser.values()
                .stream()
                .map(chat -> mapper.toConversationDto(chat, userId))
                .sorted((first, second) -> second.getLastMessageAt().compareTo(first.getLastMessageAt()))
                .toList();
    }
}
