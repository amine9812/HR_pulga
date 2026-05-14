package com.hrplatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le matricule est obligatoire")
    @Column(nullable = false, unique = true)
    private String matricule;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    @Column(nullable = false)
    private String prenom;

    @Email(message = "L'adresse email est invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(nullable = false)
    private String email;

    private String telephone;

    private LocalDate dateNaissance;

    @NotNull(message = "La date d'embauche est obligatoire")
    private LocalDate dateEmbauche;

    @Column(length = 1000)
    private String adresse;

    private String photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id")
    private Poste poste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Employe responsable;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    public String getNomComplet() {
        return (prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom);
    }
}
