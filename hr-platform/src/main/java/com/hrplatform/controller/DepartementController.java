package com.hrplatform.controller;

import com.hrplatform.model.Departement;
import com.hrplatform.model.Poste;
import com.hrplatform.model.Service;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.service.DepartementService;
import com.hrplatform.service.NotificationService;
import com.hrplatform.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE_RH')")
public class DepartementController {

    private final DepartementService departementService;
    private final UtilisateurService utilisateurService;
    private final NotificationService notificationService;

    public DepartementController(DepartementService departementService,
                                 UtilisateurService utilisateurService,
                                 NotificationService notificationService) {
        this.departementService = departementService;
        this.utilisateurService = utilisateurService;
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void ajouterDonneesCommunes(Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("notificationsNonLues", utilisateur == null ? 0L : notificationService.compterNonLues(utilisateur.getId()));
    }

    @GetMapping("/departements")
    public String lister(Model model) {
        chargerListes(model);
        model.addAttribute("departementForm", new Departement());
        model.addAttribute("serviceForm", new Service());
        model.addAttribute("posteForm", new Poste());
        return "departements/list";
    }

    @GetMapping("/departements/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("departement", new Departement());
        return "departements/form";
    }

    @GetMapping("/departements/{id}/modifier")
    public String modifier(@PathVariable Long id, Model model) {
        model.addAttribute("departement", departementService.trouverDepartementParId(id));
        return "departements/form";
    }

    @PostMapping("/departements/sauvegarder")
    public String sauvegarderDepartement(@Valid @ModelAttribute("departement") Departement departement,
                                         BindingResult bindingResult,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        if (bindingResult.hasErrors()) {
            if (departement.getId() == null) {
                chargerListes(model);
                model.addAttribute("departementForm", departement);
                model.addAttribute("serviceForm", new Service());
                model.addAttribute("posteForm", new Poste());
                return "departements/list";
            }
            return "departements/form";
        }
        try {
            if (departement.getId() == null) {
                departementService.creerDepartement(departement);
            } else {
                departementService.modifierDepartement(departement);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Departement enregistre avec succes.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/departements";
    }

    @PostMapping("/services/sauvegarder")
    public String sauvegarderService(@Valid @ModelAttribute("serviceForm") Service service,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le libelle du service est obligatoire.");
            return "redirect:/departements";
        }
        try {
            if (service.getId() == null) {
                departementService.creerService(service);
            } else {
                departementService.modifierService(service);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Service enregistre avec succes.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/departements";
    }

    @PostMapping("/postes/sauvegarder")
    public String sauvegarderPoste(@Valid @ModelAttribute("posteForm") Poste poste,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le libelle du poste est obligatoire.");
            return "redirect:/departements";
        }
        try {
            if (poste.getId() == null) {
                departementService.creerPoste(poste);
            } else {
                departementService.modifierPoste(poste);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Poste enregistre avec succes.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/departements";
    }

    @PostMapping("/departements/{id}/supprimer")
    public String supprimerDepartement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departementService.supprimerDepartement(id);
            redirectAttributes.addFlashAttribute("successMessage", "Departement supprime.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Suppression impossible: " + ex.getMessage());
        }
        return "redirect:/departements";
    }

    @PostMapping("/services/{id}/supprimer")
    public String supprimerService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departementService.supprimerService(id);
            redirectAttributes.addFlashAttribute("successMessage", "Service supprime.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Suppression impossible: " + ex.getMessage());
        }
        return "redirect:/departements";
    }

    @PostMapping("/postes/{id}/supprimer")
    public String supprimerPoste(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departementService.supprimerPoste(id);
            redirectAttributes.addFlashAttribute("successMessage", "Poste supprime.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Suppression impossible: " + ex.getMessage());
        }
        return "redirect:/departements";
    }

    private void chargerListes(Model model) {
        model.addAttribute("departements", departementService.listerDepartements());
        model.addAttribute("services", departementService.listerServices());
        model.addAttribute("postes", departementService.listerPostes());
    }
}
