package tn.esprit.pi.tbibi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Appointement")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor //
@AllArgsConstructor
@Builder

public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long schedueId;
    private LocalDate startTime;
    private LocalDate endTime;
    private Boolean isAvailable;
    @OneToMany(mappedBy = "schedule")
    private List<Appointement> appointements;

}
