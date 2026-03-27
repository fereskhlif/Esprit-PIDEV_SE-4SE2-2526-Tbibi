package tn.esprit.pi.tbibi.entities;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "Appointement")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor //
@AllArgsConstructor
@Builder
public class Appointement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long appointementId;
    private String doctor;
    private String availableDoctor;
    private LocalDate dateAppointement;
    @Enumerated(EnumType.STRING)
    private StatusAppointement statusAppointement;
    private String service;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Schedule schedule;
    @ManyToOne
    private User user;

}
