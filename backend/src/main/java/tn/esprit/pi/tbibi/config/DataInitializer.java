package tn.esprit.pi.tbibi.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.esprit.pi.tbibi.entities.HealthGoal;
import tn.esprit.pi.tbibi.entities.MedicalChat;
import tn.esprit.pi.tbibi.entities.User;
import tn.esprit.pi.tbibi.repositories.HealthGoalRepo;
import tn.esprit.pi.tbibi.repositories.MedicalChatRepo;
import tn.esprit.pi.tbibi.repositories.UserRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepo userRepo, HealthGoalRepo healthGoalRepo, MedicalChatRepo medicalChatRepo) {
        return args -> {
            seedUsers(userRepo);

            Map<String, User> usersByEmail = userRepo.findAll()
                    .stream()
                    .collect(Collectors.toMap(User::getEmail, Function.identity()));

            seedGoals(healthGoalRepo, usersByEmail);
            seedMessages(medicalChatRepo, usersByEmail);
        };
    }

    private void seedUsers(UserRepo userRepo) {
        if (userRepo.count() > 0) {
            return;
        }

        userRepo.saveAll(List.of(
                buildUser("Amal Ben Salah", "amal@tbibi.tn", "amal1234", "Tunis Centre"),
                buildUser("Youssef Trabelsi", "youssef@tbibi.tn", "youssef1234", "Sousse Medina"),
                buildUser("Mariem Gharbi", "mariem@tbibi.tn", "mariem1234", "Sfax El Jadida")
        ));
    }

    private void seedGoals(HealthGoalRepo healthGoalRepo, Map<String, User> usersByEmail) {
        if (healthGoalRepo.count() > 0) {
            return;
        }

        User amal = usersByEmail.get("amal@tbibi.tn");
        User youssef = usersByEmail.get("youssef@tbibi.tn");

        if (amal == null || youssef == null) {
            return;
        }

        healthGoalRepo.saveAll(List.of(
                buildGoal("Walk 8,000 steps", "Daily walking routine to improve stamina.", false, LocalDate.now().minusDays(3), amal),
                buildGoal("Drink 2 liters of water", "Maintain daily hydration level.", true, LocalDate.now().minusDays(5), amal),
                buildGoal("Sleep before 11 PM", "Reduce fatigue and improve recovery.", false, LocalDate.now().minusDays(1), youssef)
        ));
    }

    private void seedMessages(MedicalChatRepo medicalChatRepo, Map<String, User> usersByEmail) {
        if (medicalChatRepo.count() > 0) {
            return;
        }

        User amal = usersByEmail.get("amal@tbibi.tn");
        User youssef = usersByEmail.get("youssef@tbibi.tn");
        User mariem = usersByEmail.get("mariem@tbibi.tn");

        if (amal == null || youssef == null || mariem == null) {
            return;
        }

        medicalChatRepo.saveAll(List.of(
                buildMessage("Salem, have you checked your latest blood pressure values?", LocalDateTime.now().minusHours(6), amal, youssef),
                buildMessage("Yes, it was a bit high this morning. I will monitor it again tonight.", LocalDateTime.now().minusHours(5), youssef, amal),
                buildMessage("Perfect. Also remember your hydration goal today.", LocalDateTime.now().minusHours(4), amal, youssef),
                buildMessage("Can you send me your meal plan notes later?", LocalDateTime.now().minusHours(2), mariem, amal)
        ));
    }

    private User buildUser(String name, String email, String password, String adresse) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setAdresse(adresse);
        return user;
    }

    private HealthGoal buildGoal(String title, String description, boolean achieved, LocalDate createdDate, User user) {
        HealthGoal goal = new HealthGoal();
        goal.setGoalTitle(title);
        goal.setGoalDescription(description);
        goal.setAchieved(achieved);
        goal.setCreatedDate(createdDate);
        goal.setUser(user);
        return goal;
    }

    private MedicalChat buildMessage(String message, LocalDateTime createdAt, User sender, User receiver) {
        MedicalChat chat = new MedicalChat();
        chat.setMessage(message);
        chat.setCreatedAt(createdAt);
        chat.setSender(sender);
        chat.setReceiver(receiver);
        return chat;
    }
}
