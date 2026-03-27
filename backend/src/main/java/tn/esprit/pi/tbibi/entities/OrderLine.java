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
public class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lineId;
    private int quantity;
    private float unitPrice;

    @ManyToMany(mappedBy = "orderLines")
    private List<Order> orders;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;
}