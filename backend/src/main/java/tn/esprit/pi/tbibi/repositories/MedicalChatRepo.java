package tn.esprit.pi.tbibi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi.tbibi.entities.MedicalChat;

import java.util.List;

public interface MedicalChatRepo extends JpaRepository<MedicalChat, Long> {

    @Query("""
            select chat
            from MedicalChat chat
            where chat.sender.userId = :userId or chat.receiver.userId = :userId
            order by chat.createdAt desc
            """)
    List<MedicalChat> findMessagesForUser(@Param("userId") Long userId);

    @Query("""
            select chat
            from MedicalChat chat
            where (chat.sender.userId = :currentUserId and chat.receiver.userId = :otherUserId)
               or (chat.sender.userId = :otherUserId and chat.receiver.userId = :currentUserId)
            order by chat.createdAt asc
            """)
    List<MedicalChat> findConversation(
            @Param("currentUserId") Long currentUserId,
            @Param("otherUserId") Long otherUserId
    );
}
