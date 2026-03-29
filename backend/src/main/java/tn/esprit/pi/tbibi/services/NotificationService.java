package tn.esprit.pi.tbibi.services;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.esprit.pi.tbibi.DTO.notification.NotificationResponse;
import tn.esprit.pi.tbibi.entities.*;
import tn.esprit.pi.tbibi.repositories.NotificationRepository;
import tn.esprit.pi.tbibi.repositories.UserRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class NotificationService {

    NotificationRepository notificationRepo;
    UserRepo userRepo;
    SimpMessagingTemplate messagingTemplate;

    public void createAndSend(User recipient, String message, NotificationType type, String redirectUrl) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRedirectUrl(redirectUrl);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepo.save(notification);
        NotificationResponse dto = toDto(saved);

        messagingTemplate.convertAndSend(
                "/topic/notifications/" + recipient.getUserId(), dto
        );
    }

    public void notifyAllByRole(String roleName, String message, NotificationType type, String redirectUrl) {
        List<User> users = userRepo.findByRole_RoleName(roleName);
        for (User user : users) {
            createAndSend(user, message, type, redirectUrl);
        }
    }

    public List<NotificationResponse> getForUser(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepo.findByRecipientOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepo.countByRecipientAndIsReadFalse(user);
    }

    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        return toDto(notificationRepo.save(notification));
    }

    public void markAllAsRead(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Notification> unread = notificationRepo.findByRecipientAndIsReadFalse(user);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepo.saveAll(unread);
    }

    private NotificationResponse toDto(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .type(n.getType() != null ? n.getType().name() : null)
                .redirectUrl(n.getRedirectUrl())
                .recipientId(n.getRecipient() != null ? n.getRecipient().getUserId() : null)
                .build();
    }
}