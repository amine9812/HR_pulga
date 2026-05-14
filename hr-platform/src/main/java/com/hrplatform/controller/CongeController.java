package com.hrplatform.controller;

import com.hrplatform.model.DemandeCongé;
import com.hrplatform.model.Employe;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.model.enums.Role;
import com.hrplatform.model.enums.StatutDemande;
import com.hrplatform.model.enums.TypeConge;
import com.hrplatform.service.CongeService;
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
public class CongeController {

    private final CongeService congeService;
    private final DocumentService documentService;
    private final UtilisateurService utilisateurService;
    private final NotificationService notificationService;

    public CongeController(CongeService congeService,
                           DocumentService documentService,
                           UtilisateurService utilisateurService,
                           NotificationService notificationService) {
        this.congeService = congeService;
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

    @GetMapping("/conges")
    public String lister(@RequestParam(value = "statut", required = false) StatutDemande statut,
                         @RequestParam(value = "type", required = false) TypeConge type,
                         Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        List<DemandeCongé> demandes = demandesSelonRole(utilisateur);
        if (statut != null) {
            demandes = demandes.stream().filter(d -> statut == d.getStatut()).toList();
        }
        if (type != null) {
            demandes = demandes.stream().filter(d -> type == d.getType()).toList();
        }
        model.addAttribute("demandes", demandes);
        model.addAttribute("statuts", StatutDemande.values());
        model.addAttribute("types", TypeConge.values());
        model.addAttribute("statutFiltre", statut);
        model.addAttribute("typeFiltre", type);
        return "conges/list";
    }

    @GetMapping("/conges/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("demandeConge", new DemandeCongé());
        model.addAttribute("types", TypeConge.values());
        return "conges/form";
    }

    @PostMapping("/conges/soumettre")
    public String soumettre(@Valid @ModelAttribute("demandeConge") DemandeCongé demande,
                            BindingResult bindingResult,
                            @RequestParam(value = "justificatif", required = false) MultipartFile justificatif,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        if (utilisateur == null || utilisateur.getEmploye() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aucun employe n'est lie a votre compte.");
            return "redirect:/conges";
        }
        if (demande.getDateDebut() != null && demande.getDateFin() != null && demande.getDateFin().isBefore(demande.getDateDebut())) {
            bindingResult.rejectValue("dateFin", "dateFin.invalid", "La date de fin doit etre apres la date de debut.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", TypeConge.values());
            return "conges/form";
        }
        try {
            DemandeCongé saved = congeService.soumettreDemandeConge(demande, utilisateur.getEmploye().getId());
            if (justificatif != null && !justificatif.isEmpty()) {
                documentService.televerser(justificatif, "Justificatif conge", utilisateur.getEmploye().getId(), null, utilisateur.getId());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Demande de conge soumise avec succes.");
            return "redirect:/conges?demande=" + saved.getId();
        } catch (RuntimeException ex) {
            model.addAttribute("types", TypeConge.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "conges/form";
        }
    }

    @PostMapping("/conges/{id}/valider")
    @PreAuthorize("hasAnyRole('RESPONSABLE_HIERARCHIQUE','RESPONSABLE_RH','ADMIN')")
    public String valider(@PathVariable Long id,
                          @RequestParam(value = "commentaire", required = false) String commentaire,
                          RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
            if (!peutTraiter(id, utilisateur)) {
                throw new IllegalStateException("Vous n'etes pas autorise a traiter cette demande.");
            }
            congeService.valider(id, utilisateur.getEmploye().getId(), commentaire);
            redirectAttributes.addFlashAttribute("successMessage", "Demande validee.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/conges";
    }

    @PostMapping("/conges/{id}/refuser")
    @PreAuthorize("hasAnyRole('RESPONSABLE_HIERARCHIQUE','RESPONSABLE_RH','ADMIN')")
    public String refuser(@PathVariable Long id,
                          @RequestParam(value = "commentaire", required = false) String commentaire,
                          RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
            if (!peutTraiter(id, utilisateur)) {
                throw new IllegalStateException("Vous n'etes pas autorise a traiter cette demande.");
            }
            congeService.refuser(id, utilisateur.getEmploye().getId(), commentaire);
            redirectAttributes.addFlashAttribute("successMessage", "Demande refusee.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/conges";
    }

    @PostMapping("/conges/{id}/annuler")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
            if (utilisateur == null || utilisateur.getEmploye() == null) {
                throw new IllegalStateException("Aucun employe n'est lie a votre compte.");
            }
            congeService.annuler(id, utilisateur.getEmploye().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Demande annulee.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/conges";
    }

    private List<DemandeCongé> demandesSelonRole(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getEmploye() == null) {
            return List.of();
        }
        if (utilisateur.getRole() == Role.ADMIN || utilisateur.getRole() == Role.RESPONSABLE_RH) {
            return congeService.listerTous();
        }
        if (utilisateur.getRole() == Role.RESPONSABLE_HIERARCHIQUE) {
            return congeService.listerPourResponsable(utilisateur.getEmploye().getId());
        }
        return congeService.listerParEmploye(utilisateur.getEmploye().getId());
    }

    private boolean peutTraiter(Long demandeId, Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getEmploye() == null) {
            return false;
        }
        if (utilisateur.getRole() == Role.ADMIN || utilisateur.getRole() == Role.RESPONSABLE_RH) {
            return true;
        }
        DemandeCongé demande = congeService.trouverParId(demandeId);
        Employe responsable = demande.getEmploye().getResponsable();
        return responsable != null && responsable.getId().equals(utilisateur.getEmploye().getId());
    }
}
