package com.hrplatform.config;

import com.hrplatform.model.Departement;
import com.hrplatform.model.Employe;
import com.hrplatform.model.Poste;
import com.hrplatform.model.Service;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.model.enums.Role;
import com.hrplatform.repository.DepartementRepository;
import com.hrplatform.repository.EmployeRepository;
import com.hrplatform.repository.PosteRepository;
import com.hrplatform.repository.ServiceRepository;
import com.hrplatform.repository.UtilisateurRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final ServiceRepository serviceRepository;
    private final PosteRepository posteRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UtilisateurRepository utilisateurRepository,
                           EmployeRepository employeRepository,
                           DepartementRepository departementRepository,
                           ServiceRepository serviceRepository,
                           PosteRepository posteRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
        this.departementRepository = departementRepository;
        this.serviceRepository = serviceRepository;
        this.posteRepository = posteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (utilisateurRepository.count() > 0) {
            return;
        }

        Departement informatique = departementRepository.save(Departement.builder()
                .libelle("Informatique")
                .description("Departement charge des systemes d'information et du developpement")
                .build());
        Departement ressourcesHumaines = departementRepository.save(Departement.builder()
                .libelle("Ressources Humaines")
                .description("Departement charge de la gestion administrative du personnel")
                .build());

        Service developpement = serviceRepository.save(Service.builder()
                .libelle("Developpement")
                .description("Applications internes et plateformes web")
                .departement(informatique)
                .build());
        Service infrastructure = serviceRepository.save(Service.builder()
                .libelle("Infrastructure")
                .description("Reseaux, serveurs et support technique")
                .departement(informatique)
                .build());
        Service administrationRh = serviceRepository.save(Service.builder()
                .libelle("Administration RH")
                .description("Contrats, dossiers et documents administratifs")
                .departement(ressourcesHumaines)
                .build());
        Service recrutement = serviceRepository.save(Service.builder()
                .libelle("Recrutement")
                .description("Sourcing, entretiens et integration")
                .departement(ressourcesHumaines)
                .build());

        Poste developpeur = posteRepository.save(Poste.builder()
                .libelle("Developpeur")
                .description("Conception et maintenance des applications")
                .niveau("Cadre")
                .build());
        Poste chefProjet = posteRepository.save(Poste.builder()
                .libelle("Chef de projet")
                .description("Pilotage des projets et coordination des equipes")
                .niveau("Manager")
                .build());
        Poste chargeRh = posteRepository.save(Poste.builder()
                .libelle("Charge RH")
                .description("Suivi administratif et accompagnement RH")
                .niveau("Cadre")
                .build());

        Employe adminEmploye = employeRepository.save(Employe.builder()
                .matricule("EMP-0001")
                .nom("Martin")
                .prenom("Jeanne")
                .email("jeanne.martin@hrplatform.local")
                .telephone("0600000001")
                .dateNaissance(LocalDate.of(1984, 3, 12))
                .dateEmbauche(LocalDate.of(2018, 1, 15))
                .adresse("12 rue de la Paix, Paris")
                .departement(ressourcesHumaines)
                .service(administrationRh)
                .poste(chargeRh)
                .actif(true)
                .build());
        Employe rhEmploye = employeRepository.save(Employe.builder()
                .matricule("EMP-0002")
                .nom("Bernard")
                .prenom("Sophie")
                .email("sophie.bernard@hrplatform.local")
                .telephone("0600000002")
                .dateNaissance(LocalDate.of(1990, 7, 4))
                .dateEmbauche(LocalDate.of(2020, 2, 10))
                .adresse("8 avenue Hassan II, Casablanca")
                .departement(ressourcesHumaines)
                .service(recrutement)
                .poste(chargeRh)
                .responsable(adminEmploye)
                .actif(true)
                .build());
        Employe managerEmploye = employeRepository.save(Employe.builder()
                .matricule("EMP-0003")
                .nom("Dubois")
                .prenom("Marc")
                .email("marc.dubois@hrplatform.local")
                .telephone("0600000003")
                .dateNaissance(LocalDate.of(1987, 11, 22))
                .dateEmbauche(LocalDate.of(2019, 5, 6))
                .adresse("25 boulevard Zerktouni, Casablanca")
                .departement(informatique)
                .service(developpement)
                .poste(chefProjet)
                .responsable(adminEmploye)
                .actif(true)
                .build());
        Employe employe = employeRepository.save(Employe.builder()
                .matricule("EMP-0004")
                .nom("Petit")
                .prenom("Claire")
                .email("claire.petit@hrplatform.local")
                .telephone("0600000004")
                .dateNaissance(LocalDate.of(1996, 9, 18))
                .dateEmbauche(LocalDate.of(2022, 9, 1))
                .adresse("17 rue Ibn Sina, Rabat")
                .departement(informatique)
                .service(infrastructure)
                .poste(developpeur)
                .responsable(managerEmploye)
                .actif(true)
                .build());

        utilisateurRepository.save(Utilisateur.builder()
                .login("admin")
                .motDePasse(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .actif(true)
                .employe(adminEmploye)
                .build());
        utilisateurRepository.save(Utilisateur.builder()
                .login("rh")
                .motDePasse(passwordEncoder.encode("rh123"))
                .role(Role.RESPONSABLE_RH)
                .actif(true)
                .employe(rhEmploye)
                .build());
        utilisateurRepository.save(Utilisateur.builder()
                .login("manager")
                .motDePasse(passwordEncoder.encode("manager123"))
                .role(Role.RESPONSABLE_HIERARCHIQUE)
                .actif(true)
                .employe(managerEmploye)
                .build());
        utilisateurRepository.save(Utilisateur.builder()
                .login("employe")
                .motDePasse(passwordEncoder.encode("employe123"))
                .role(Role.EMPLOYE)
                .actif(true)
                .employe(employe)
                .build());
    }
}
