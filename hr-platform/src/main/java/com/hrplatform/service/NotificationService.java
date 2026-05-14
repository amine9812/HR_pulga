package com.hrplatform.service;

import com.hrplatform.model.Notification;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.repository.NotificationRepository;
import com.hrplatform.repository.UtilisateurRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditService auditService;

    public NotificationService(NotificationRepository notificationRepository,
                               UtilisateurRepository utilisateurRepository,
                               AuditService auditService) {
        this.notificationRepository = notificationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Notification creerNotification(Long destinataireId, String message, String lien) {
        Utilisateur destinataire = utilisateurRepository.findById(destinataireId)
                .orElseThrow(() -> new IllegalArgumentException("Destinataire introuvable"));
        Notification notification = Notification.builder()
                .destinataire(destinataire)
                .message(message)
                .lien(lien)
                .dateEnvoi(LocalDateTime.now())
                .lue(false)
                .build();
        Notification saved = notificationRepository.save(notification);
        auditService.enregistrerAction("CREATION_NOTIFICATION", "Notification envoyee a " + destinataire.getLogin(), "Notification", saved.getId());
        return saved;
    }

    @Transactional
    public void marquerCommeLue(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification introuvable"));
        notification.setLue(true);
        notificationRepository.save(notification);
        auditService.enregistrerAction("LECTURE_NOTIFICATION", "Notification marquee comme lue", "Notification", id);
    }

    @Transactional
    public void marquerToutesCommeLues(Long userId) {
        List<Notification> notifications = notificationRepository.findByDestinataireIdAndLueFalse(userId);
        notifications.forEach(notification -> notification.setLue(true));
        notificationRepository.saveAll(notifications);
        auditService.enregistrerAction("LECTURE_NOTIFICATIONS", "Toutes les notifications ont ete marquees comme lues", "Utilisateur", userId);
    }

    @Transactional(readOnly = true)
    public Long compterNonLues(Long userId) {
        return (long) notificationRepository.findByDestinataireIdAndLueFalse(userId).size();
    }

    @Transactional(readOnly = true)
    public List<Notification> listerParUtilisateur(Long userId) {
        return notificationRepository.findByDestinataireId(userId).stream()
                .sorted(Comparator.comparing(Notification::getDateEnvoi, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

}
