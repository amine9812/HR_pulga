package com.hrplatform.controller;

import com.hrplatform.model.Utilisateur;
import com.hrplatform.service.NotificationService;
import com.hrplatform.service.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final NotificationService notificationService;

    public AuthController(UtilisateurService utilisateurService, NotificationService notificationService) {
        this.utilisateurService = utilisateurService;
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void ajouterDonneesCommunes(Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("notificationsNonLues", utilisateur == null ? 0L : notificationService.compterNonLues(utilisateur.getId()));
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}
