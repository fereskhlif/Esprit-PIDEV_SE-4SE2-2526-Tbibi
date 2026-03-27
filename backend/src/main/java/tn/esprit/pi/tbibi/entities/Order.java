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
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    private Date deliveryDate;
    private float totalAmount;

    @Enumerated(EnumType.STRING)
    private Status orderStatus;
    private Date orderDate;

    @ManyToMany
    @JoinTable(
            name = "order_orderline",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "orderline_id")
    )
    private List<OrderLine> orderLines;

    @ManyToOne
    private Pharmacy pharmacy;

    @ManyToOne
    private User user;
}