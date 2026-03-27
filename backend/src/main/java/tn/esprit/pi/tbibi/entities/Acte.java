package tn.esprit.pi.tbibi.entities;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Acte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int acte_id;
    private Date date;
    private String description;
    private String typeOfActe;



}