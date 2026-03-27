package tn.esprit.pi.tbibi.services;

import org.springframework.stereotype.Component;
import tn.esprit.pi.tbibi.DTO.ChatConversationDto;
import tn.esprit.pi.tbibi.DTO.MedicalChatDto;
import tn.esprit.pi.tbibi.entities.MedicalChat;
import tn.esprit.pi.tbibi.entities.User;

@Component
public class MedicalChatMapper {

    public MedicalChatDto toDto(MedicalChat entity) {
        MedicalChatDto dto = new MedicalChatDto();

        dto.setId(entity.getId());
        dto.setMessage(entity.getMessage());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getSender() != null) {
            dto.setSenderId(entity.getSender().getUserId());
            dto.setSenderName(entity.getSender().getName());
            dto.setSenderEmail(entity.getSender().getEmail());
        }

        if (entity.getReceiver() != null) {
            dto.setReceiverId(entity.getReceiver().getUserId());
            dto.setReceiverName(entity.getReceiver().getName());
            dto.setReceiverEmail(entity.getReceiver().getEmail());
        }

        return dto;
    }

    public MedicalChat toEntity(MedicalChatDto dto) {
        MedicalChat entity = new MedicalChat();
        entity.setId(dto.getId());
        entity.setMessage(dto.getMessage());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }

    public ChatConversationDto toConversationDto(MedicalChat entity, Long currentUserId) {
        User otherUser = resolveOtherUser(entity, currentUserId);

        return new ChatConversationDto(
                otherUser.getUserId(),
                otherUser.getName(),
                otherUser.getEmail(),
                entity.getMessage(),
                entity.getUpdatedAt() != null ? entity.getUpdatedAt() : entity.getCreatedAt()
        );
    }

    public User resolveOtherUser(MedicalChat entity, Long currentUserId) {
        if (entity.getSender() != null && entity.getSender().getUserId().equals(currentUserId)) {
            return entity.getReceiver();
        }
        return entity.getSender();
    }
}
