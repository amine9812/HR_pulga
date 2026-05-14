package com.hrplatform.service;

import com.hrplatform.model.Utilisateur;
import com.hrplatform.repository.UtilisateurRepository;
import java.util.List;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtilisateurService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              PasswordEncoder passwordEncoder,
                              AuditService auditService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable: " + login));

        return User.withUsername(utilisateur.getLogin())
                .password(utilisateur.getMotDePasse())
                .roles(utilisateur.getRole().name())
                .disabled(!utilisateur.isActif())
                .build();
    }

    @Transactional
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        auditService.enregistrerAction("CREATION_UTILISATEUR", "Creation de l'utilisateur " + saved.getLogin(), "Utilisateur", saved.getId());
        return saved;
    }

    @Transactional
    public void modifierMotDePasse(Long id, String newPassword) {
        Utilisateur utilisateur = trouverParId(id);
        utilisateur.setMotDePasse(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(utilisateur);
        auditService.enregistrerAction("MODIFICATION_MOT_DE_PASSE", "Modification du mot de passe de " + utilisateur.getLogin(), "Utilisateur", id);
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> listerTous() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Utilisateur trouverParId(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    @Transactional
    public void desactiver(Long id) {
        Utilisateur utilisateur = trouverParId(id);
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
        auditService.enregistrerAction("DESACTIVATION_UTILISATEUR", "Desactivation de " + utilisateur.getLogin(), "Utilisateur", id);
    }

    @Transactional(readOnly = true)
    public Utilisateur getUtilisateurConnecte() {
        return auditService.utilisateurConnecte();
    }
}
