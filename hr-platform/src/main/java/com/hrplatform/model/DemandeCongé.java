package com.hrplatform.model;

import com.hrplatform.model.enums.StatutDemande;
import com.hrplatform.model.enums.TypeConge;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "demandes_conges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeCongé {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le type de conge est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeConge type;

    @NotNull(message = "La date de debut est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    @Column(length = 1000)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traitee_par_id")
    private Employe traiteePar;

    private LocalDateTime dateTraitement;

    @Column(length = 1000)
    private String commentaireReponse;

    private LocalDateTime dateCreation;
}
