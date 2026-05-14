package com.hrplatform.service;

import com.hrplatform.model.DemandeCongé;
import com.hrplatform.model.Employe;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.model.enums.StatutDemande;
import com.hrplatform.repository.DemandeCongéRepository;
import com.hrplatform.repository.EmployeRepository;
import com.hrplatform.repository.UtilisateurRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongeService {

    private final DemandeCongéRepository demandeCongéRepository;
    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public CongeService(DemandeCongéRepository demandeCongéRepository,
                        EmployeRepository employeRepository,
                        UtilisateurRepository utilisateurRepository,
                        NotificationService notificationService,
                        AuditService auditService) {
        this.demandeCongéRepository = demandeCongéRepository;
        this.employeRepository = employeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public DemandeCongé soumettreDemandeConge(DemandeCongé demande, Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new IllegalArgumentException("Employe introuvable"));
        demande.setEmploye(employe);
        demande.setStatut(StatutDemande.EN_ATTENTE);
        demande.setDateCreation(LocalDateTime.now());
        DemandeCongé saved = demandeCongéRepository.save(demande);
        utilisateurParEmploye(employe.getResponsable()).forEach(manager ->
                notificationService.creerNotification(manager.getId(),
                        "Nouvelle demande de conge de " + employe.getNomComplet(),
                        "/conges"));
        auditService.enregistrerAction("SOUMISSION_CONGE", "Demande de conge soumise par " + employe.getNomComplet(), "DemandeCongé", saved.getId());
        return saved;
    }

    @Transactional
    public DemandeCongé valider(Long id, Long responsableId, String commentaire) {
        DemandeCongé demande = trouverParId(id);
        Employe responsable = employeRepository.findById(responsableId)
                .orElseThrow(() -> new IllegalArgumentException("Responsable introuvable"));
        demande.setStatut(StatutDemande.VALIDEE);
        demande.setTraiteePar(responsable);
        demande.setDateTraitement(LocalDateTime.now());
        demande.setCommentaireReponse(commentaire);
        DemandeCongé saved = demandeCongéRepository.save(demande);
        notifierEmploye(demande, "Votre demande de conge a ete validee");
        auditService.enregistrerAction("VALIDATION_CONGE", "Validation de la demande de conge", "DemandeCongé", id);
        return saved;
    }

    @Transactional
    public DemandeCongé refuser(Long id, Long responsableId, String commentaire) {
        DemandeCongé demande = trouverParId(id);
        Employe responsable = employeRepository.findById(responsableId)
                .orElseThrow(() -> new IllegalArgumentException("Responsable introuvable"));
        demande.setStatut(StatutDemande.REFUSEE);
        demande.setTraiteePar(responsable);
        demande.setDateTraitement(LocalDateTime.now());
        demande.setCommentaireReponse(commentaire);
        DemandeCongé saved = demandeCongéRepository.save(demande);
        notifierEmploye(demande, "Votre demande de conge a ete refusee");
        auditService.enregistrerAction("REFUS_CONGE", "Refus de la demande de conge", "DemandeCongé", id);
        return saved;
    }

    @Transactional
    public void annuler(Long id, Long employeId) {
        DemandeCongé demande = trouverParId(id);
        if (!demande.getEmploye().getId().equals(employeId) || demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new IllegalStateException("Cette demande ne peut pas etre annulee");
        }
        demande.setStatut(StatutDemande.CLOTUREE);
        demandeCongéRepository.save(demande);
        auditService.enregistrerAction("ANNULATION_CONGE", "Annulation de la demande de conge", "DemandeCongé", id);
    }

    @Transactional(readOnly = true)
    public DemandeCongé trouverParId(Long id) {
        return demandeCongéRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande de conge introuvable"));
    }

    @Transactional(readOnly = true)
    public List<DemandeCongé> listerParEmploye(Long id) {
        return trier(demandeCongéRepository.findByEmployeId(id));
    }

    @Transactional(readOnly = true)
    public List<DemandeCongé> listerEnAttente() {
        return trier(demandeCongéRepository.findByStatut(StatutDemande.EN_ATTENTE));
    }

    @Transactional(readOnly = true)
    public List<DemandeCongé> listerPourResponsable(Long responsableId) {
        return trier(demandeCongéRepository.findByEmployeResponsableId(responsableId));
    }

    @Transactional(readOnly = true)
    public List<DemandeCongé> listerTous() {
        return trier(demandeCongéRepository.findAll());
    }

    private List<DemandeCongé> trier(List<DemandeCongé> demandes) {
        return demandes.stream()
                .sorted(Comparator.comparing(DemandeCongé::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private void notifierEmploye(DemandeCongé demande, String message) {
        utilisateurParEmploye(demande.getEmploye()).forEach(utilisateur ->
                notificationService.creerNotification(utilisateur.getId(), message, "/conges"));
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
