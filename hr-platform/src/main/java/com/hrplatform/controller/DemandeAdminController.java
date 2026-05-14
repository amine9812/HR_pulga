package com.hrplatform.controller;

import com.hrplatform.model.DemandeAdministrative;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.model.enums.Role;
import com.hrplatform.model.enums.StatutDemande;
import com.hrplatform.service.DemandeAdminService;
import com.hrplatform.service.DocumentService;
import com.hrplatform.service.NotificationService;
import com.hrplatform.service.UtilisateurService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DemandeAdminController {

    private final DemandeAdminService demandeAdminService;
    private final DocumentService documentService;
    private final UtilisateurService utilisateurService;
    private final NotificationService notificationService;

    public DemandeAdminController(DemandeAdminService demandeAdminService,
                                  DocumentService documentService,
                                  UtilisateurService utilisateurService,
                                  NotificationService notificationService) {
        this.demandeAdminService = demandeAdminService;
        this.documentService = documentService;
        this.utilisateurService = utilisateurService;
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void ajouterDonneesCommunes(Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("notificationsNonLues", utilisateur == null ? 0L : notificationService.compterNonLues(utilisateur.getId()));
    }

    @GetMapping("/demandes")
    public String lister(@RequestParam(value = "statut", required = false) StatutDemande statut, Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        List<DemandeAdministrative> demandes = demandesSelonRole(utilisateur);
        if (statut != null) {
            demandes = demandes.stream().filter(d -> statut == d.getStatut()).toList();
        }
        model.addAttribute("demandes", demandes);
        model.addAttribute("statuts", StatutDemande.values());
        model.addAttribute("statutFiltre", statut);
        return "demandes/list";
    }

    @GetMapping("/demandes/nouvelle")
    public String nouvelle(Model model) {
        model.addAttribute("demandeAdministrative", new DemandeAdministrative());
        return "demandes/form";
    }

    @PostMapping("/demandes/soumettre")
    public String soumettre(@Valid @ModelAttribute("demandeAdministrative") DemandeAdministrative demande,
                            BindingResult bindingResult,
                            @RequestParam(value = "pieceJointe", required = false) MultipartFile pieceJointe,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        if (utilisateur == null || utilisateur.getEmploye() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aucun employe n'est lie a votre compte.");
            return "redirect:/demandes";
        }
        if (bindingResult.hasErrors()) {
            return "demandes/form";
        }
        try {
            DemandeAdministrative saved = demandeAdminService.soumettre(demande, utilisateur.getEmploye().getId());
            if (pieceJointe != null && !pieceJointe.isEmpty()) {
                documentService.televerser(pieceJointe, "Demande administrative", utilisateur.getEmploye().getId(), saved.getId(), utilisateur.getId());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Demande administrative soumise avec succes.");
            return "redirect:/demandes";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "demandes/form";
        }
    }

    @PostMapping("/demandes/{id}/traiter")
    @PreAuthorize("hasAnyRole('RESPONSABLE_RH','ADMIN')")
    public String traiter(@PathVariable Long id,
                          @RequestParam("reponse") String reponse,
                          @RequestParam("statut") StatutDemande statut,
                          RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
            if (utilisateur == null || utilisateur.getEmploye() == null) {
                throw new IllegalStateException("Aucun employe n'est lie a votre compte.");
            }
            demandeAdminService.traiter(id, reponse, statut, utilisateur.getEmploye().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Demande traitee avec succes.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/demandes";
    }

    private List<DemandeAdministrative> demandesSelonRole(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getEmploye() == null) {
            return List.of();
        }
        if (utilisateur.getRole() == Role.ADMIN || utilisateur.getRole() == Role.RESPONSABLE_RH) {
            return demandeAdminService.listerToutes();
        }
        return demandeAdminService.listerParEmploye(utilisateur.getEmploye().getId());
    }
}
