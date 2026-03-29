package tn.esprit.pi.tbibi.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.tbibi.DTO.notification.NotificationResponse;
import tn.esprit.pi.tbibi.services.NotificationService;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class NotificationController {

    NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public List<NotificationResponse> getForUser(@PathVariable("userId") Integer userId) {
        return notificationService.getForUser(userId);
    }

    @GetMapping("/unread-count/{userId}")
    public long getUnreadCount(@PathVariable("userId") Integer userId) {
        return notificationService.getUnreadCount(userId);
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable("id") Long id) {
        return notificationService.markAsRead(id);
    }

    @PutMapping("/read-all/{userId}")
    public void markAllAsRead(@PathVariable("userId") Integer userId) {
        notificationService.markAllAsRead(userId);
    }
}