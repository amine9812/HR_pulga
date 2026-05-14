package com.hrplatform.controller;

import com.hrplatform.model.Notification;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.service.NotificationService;
import com.hrplatform.service.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurService utilisateurService;

    public NotificationController(NotificationService notificationService, UtilisateurService utilisateurService) {
        this.notificationService = notificationService;
        this.utilisateurService = utilisateurService;
    }

    @ModelAttribute
    public void ajouterDonneesCommunes(Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("notificationsNonLues", utilisateur == null ? 0L : notificationService.compterNonLues(utilisateur.getId()));
    }

    @GetMapping("/notifications")
    public String lister(Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        model.addAttribute("notifications", utilisateur == null ? java.util.List.<Notification>of() : notificationService.listerParUtilisateur(utilisateur.getId()));
        return "notifications/list";
    }

    @PostMapping("/notifications/{id}/lire")
    public String lire(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
            boolean appartient = utilisateur != null && notificationService.listerParUtilisateur(utilisateur.getId()).stream()
                    .anyMatch(notification -> notification.getId().equals(id));
            if (!appartient) {
                throw new IllegalStateException("Notification inaccessible.");
            }
            notificationService.marquerCommeLue(id);
            redirectAttributes.addFlashAttribute("successMessage", "Notification marquee comme lue.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/lire-toutes")
    public String lireToutes(RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        if (utilisateur != null) {
            notificationService.marquerToutesCommeLues(utilisateur.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Toutes les notifications sont marquees comme lues.");
        }
        return "redirect:/notifications";
    }
}
