package com.hrplatform.controller;

import com.hrplatform.model.Departement;
import com.hrplatform.model.Employe;
import com.hrplatform.model.Poste;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.service.CongeService;
import com.hrplatform.service.DepartementService;
import com.hrplatform.service.DocumentService;
import com.hrplatform.service.EmployeService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmployeController {

    private final EmployeService employeService;
    private final DepartementService departementService;
    private final CongeService congeService;
    private final DocumentService documentService;
    private final UtilisateurService utilisateurService;
    private final NotificationService notificationService;

    public EmployeController(EmployeService employeService,
                             DepartementService departementService,
                             CongeService congeService,
                             DocumentService documentService,
                             UtilisateurService utilisateurService,
                             NotificationService notificationService) {
        this.employeService = employeService;
        this.departementService = departementService;
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

    @GetMapping("/employes")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE_RH','RESPONSABLE_HIERARCHIQUE')")
    public String lister(@RequestParam(value = "search", required = false) String search, Model model) {
        model.addAttribute("employes", employeService.rechercher(search));
        model.addAttribute("search", search);
        return "employes/list";
    }

    @GetMapping("/employes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE_RH','RESPONSABLE_HIERARCHIQUE')")
    public String detail(@PathVariable Long id, Model model) {
        Employe employe = employeService.trouverParId(id);
        model.addAttribute("employe", employe);
        model.addAttribute("conges", congeService.listerParEmploye(id));
        model.addAttribute("documents", documentService.listerParEmploye(id));
        return "employes/detail";
    }

    @GetMapping("/employes/nouveau")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE_RH')")
    public String nouveau(Model model) {
        preparerFormulaire(model, Employe.builder().actif(true).build());
        return "employes/form";
    }

    @GetMapping("/employes/{id}/modifier")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE_RH')")
    public String modifier(@PathVariable Long id, Model model) {
        preparerFormulaire(model, employeService.trouverParId(id));
        return "employes/form";
    }

    @PostMapping("/employes/sauvegarder")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE_RH')")
    public String sauvegarder(@Valid @ModelAttribute("employe") Employe employe,
                              BindingResult bindingResult,
                              @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preparerFormulaire(model, employe);
            return "employes/form";
        }
        try {
            Employe saved = employe.getId() == null ? employeService.creer(employe) : employeService.modifier(employe);
            if (photoFile != null && !photoFile.isEmpty()) {
                employeService.sauvegarderPhoto(photoFile, saved.getId());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Employe enregistre avec succes.");
            return "redirect:/employes/" + saved.getId();
        } catch (RuntimeException ex) {
            preparerFormulaire(model, employe);
            model.addAttribute("errorMessage", ex.getMessage());
            return "employes/form";
        }
    }

    @PostMapping("/employes/{id}/archiver")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE_RH')")
    public String archiver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            employeService.archiver(id);
            redirectAttributes.addFlashAttribute("successMessage", "Employe archive avec succes.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/employes";
    }

    @PostMapping("/employes/{id}/photo")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE_RH')")
    public String uploadPhoto(@PathVariable Long id,
                              @RequestParam("photoFile") MultipartFile photoFile,
                              RedirectAttributes redirectAttributes) {
        try {
            employeService.sauvegarderPhoto(photoFile, id);
            redirectAttributes.addFlashAttribute("successMessage", "Photo mise a jour.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/employes/" + id;
    }

    private void preparerFormulaire(Model model, Employe employe) {
        if (employe.getDepartement() == null) {
            employe.setDepartement(new Departement());
        }
        if (employe.getService() == null) {
            employe.setService(new com.hrplatform.model.Service());
        }
        if (employe.getPoste() == null) {
            employe.setPoste(new Poste());
        }
        if (employe.getResponsable() == null) {
            employe.setResponsable(new Employe());
        }
        model.addAttribute("employe", employe);
        model.addAttribute("departements", departementService.listerDepartements());
        model.addAttribute("services", departementService.listerServices());
        model.addAttribute("postes", departementService.listerPostes());
        model.addAttribute("responsables", employeService.listerActifs());
    }
}
