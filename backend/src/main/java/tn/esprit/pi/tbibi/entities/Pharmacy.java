package tn.esprit.pi.tbibi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pharmacy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pharmacyId;
    private String pharmacyName;
    private String pharmacyAddress;

    @OneToMany(mappedBy = "pharmacy")
    private List<Order> orders;
}