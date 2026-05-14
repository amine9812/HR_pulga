package com.hrplatform.service;

import com.hrplatform.model.HistoriqueAction;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.repository.HistoriqueActionRepository;
import com.hrplatform.repository.UtilisateurRepository;
import java.time.LocalDateTime;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final HistoriqueActionRepository historiqueActionRepository;
    private final UtilisateurRepository utilisateurRepository;

    public AuditService(HistoriqueActionRepository historiqueActionRepository,
                        UtilisateurRepository utilisateurRepository) {
        this.historiqueActionRepository = historiqueActionRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public void enregistrerAction(String action, String details, String entite, Long entiteId) {
        historiqueActionRepository.save(HistoriqueAction.builder()
                .action(action)
                .details(details)
                .dateAction(LocalDateTime.now())
                .utilisateur(utilisateurConnecte())
                .entiteConcernee(entite)
                .entiteId(entiteId)
                .build());
    }

    public Utilisateur utilisateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return utilisateurRepository.findByLogin(authentication.getName()).orElse(null);
    }
}
