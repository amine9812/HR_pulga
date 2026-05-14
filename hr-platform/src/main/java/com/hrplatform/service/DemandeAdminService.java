package com.hrplatform.service;

import com.hrplatform.model.DemandeAdministrative;
import com.hrplatform.model.Employe;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.model.enums.Role;
import com.hrplatform.model.enums.StatutDemande;
import com.hrplatform.repository.DemandeAdministrativeRepository;
import com.hrplatform.repository.EmployeRepository;
import com.hrplatform.repository.UtilisateurRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandeAdminService {

    private final DemandeAdministrativeRepository demandeAdministrativeRepository;
    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public DemandeAdminService(DemandeAdministrativeRepository demandeAdministrativeRepository,
                               EmployeRepository employeRepository,
                               UtilisateurRepository utilisateurRepository,
                               NotificationService notificationService,
                               AuditService auditService) {
        this.demandeAdministrativeRepository = demandeAdministrativeRepository;
        this.employeRepository = employeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public DemandeAdministrative soumettre(DemandeAdministrative demande, Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new IllegalArgumentException("Employe introuvable"));
        demande.setEmploye(employe);
        demande.setStatut(StatutDemande.EN_ATTENTE);
        demande.setDateCreation(LocalDateTime.now());
        DemandeAdministrative saved = demandeAdministrativeRepository.save(demande);
        utilisateurRepository.findByRole(Role.RESPONSABLE_RH).forEach(rh ->
                notificationService.creerNotification(rh.getId(),
                        "Nouvelle demande administrative de " + employe.getNomComplet(),
                        "/demandes"));
        utilisateurRepository.findByRole(Role.ADMIN).forEach(admin ->
                notificationService.creerNotification(admin.getId(),
                        "Nouvelle demande administrative de " + employe.getNomComplet(),
                        "/demandes"));
        auditService.enregistrerAction("SOUMISSION_DEMANDE_ADMIN", "Demande administrative soumise", "DemandeAdministrative", saved.getId());
        return saved;
    }

    @Transactional
    public DemandeAdministrative traiter(Long id, String reponse, StatutDemande statut, Long rh) {
        DemandeAdministrative demande = trouverParId(id);
        Employe traiteePar = employeRepository.findById(rh)
                .orElseThrow(() -> new IllegalArgumentException("Responsable RH introuvable"));
        demande.setReponse(reponse);
        demande.setStatut(statut);
        demande.setTraiteePar(traiteePar);
        demande.setDateTraitement(LocalDateTime.now());
        DemandeAdministrative saved = demandeAdministrativeRepository.save(demande);
        utilisateurParEmploye(demande.getEmploye()).forEach(utilisateur ->
                notificationService.creerNotification(utilisateur.getId(),
                        "Votre demande administrative a ete traitee",
                        "/demandes"));
        auditService.enregistrerAction("TRAITEMENT_DEMANDE_ADMIN", "Traitement d'une demande administrative", "DemandeAdministrative", id);
        return saved;
    }

    @Transactional(readOnly = true)
    public DemandeAdministrative trouverParId(Long id) {
        return demandeAdministrativeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande administrative introuvable"));
    }

    @Transactional(readOnly = true)
    public List<DemandeAdministrative> listerParEmploye(Long id) {
        return trier(demandeAdministrativeRepository.findByEmployeId(id));
    }

    @Transactional(readOnly = true)
    public List<DemandeAdministrative> listerParStatut(StatutDemande statut) {
        return trier(demandeAdministrativeRepository.findByStatut(statut));
    }

    @Transactional(readOnly = true)
    public List<DemandeAdministrative> listerToutes() {
        return trier(demandeAdministrativeRepository.findAll());
    }

    private List<DemandeAdministrative> trier(List<DemandeAdministrative> demandes) {
        return demandes.stream()
                .sorted(Comparator.comparing(DemandeAdministrative::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<Utilisateur> utilisateurParEmploye(Employe employe) {
        if (employe == null || employe.getId() == null) {
            return List.of();
        }
        return utilisateurRepository.findAll().stream()
                .filter(u -> u.getEmploye() != null && employe.getId().equals(u.getEmploye().getId()))
                .toList();
    }

}
