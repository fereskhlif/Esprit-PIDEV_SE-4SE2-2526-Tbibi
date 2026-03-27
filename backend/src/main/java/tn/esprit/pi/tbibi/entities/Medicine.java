package tn.esprit.pi.tbibi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicineId;
    private String medicineName;
    private int quantity;
    private Date dateOfExpiration;
    private float price;
    private int stock;

    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL)
    private List<OrderLine> orderLines;

    @ManyToMany(mappedBy = "medicines")
    private List<Prescription> prescriptions;  // Added back-reference
}