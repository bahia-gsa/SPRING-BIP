package afpa.mra.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
@Data
@Entity
@Table(name = "embauches")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Embauche {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date dateDebut;

    @Column
    private Date dateFin;

    @ManyToOne()
    @JsonBackReference
    @JoinColumn(nullable=false, name = "utilisateur_id")
    private Utilisateur utilisateur;

    @JoinColumn(nullable = false)
    @ManyToOne
    private Entreprise entreprise;
}