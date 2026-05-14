package com.hrplatform.controller;

import com.hrplatform.model.Utilisateur;
import com.hrplatform.model.enums.StatutDemande;
import java.util.List;
import com.hrplatform.service.CongeService;
import com.hrplatform.service.DemandeAdminService;
import com.hrplatform.service.EmployeService;
import com.hrplatform.service.NotificationService;
import com.hrplatform.service.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class DashboardController {

    private final UtilisateurService utilisateurService;
    private final NotificationService notificationService;
    private final EmployeService employeService;
    private final CongeService congeService;
    private final DemandeAdminService demandeAdminService;

    public DashboardController(UtilisateurService utilisateurService,
                               NotificationService notificationService,
                               EmployeService employeService,
                               CongeService congeService,
                               DemandeAdminService demandeAdminService) {
        this.utilisateurService = utilisateurService;
        this.notificationService = notificationService;
        this.employeService = employeService;
        this.congeService = congeService;
        this.demandeAdminService = demandeAdminService;
    }

    @ModelAttribute
    public void ajouterDonneesCommunes(Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("notificationsNonLues", utilisateur == null ? 0L : notificationService.compterNonLues(utilisateur.getId()));
    }

    @GetMapping("/")
    public String accueil() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        long notifications = utilisateur == null ? 0L : notificationService.compterNonLues(utilisateur.getId());
        List<com.hrplatform.model.Employe> employesActifs = employeService.listerActifs();
        List<com.hrplatform.model.DemandeCongé> conges = congeService.listerTous();
        List<com.hrplatform.model.DemandeAdministrative> demandesAdmin = demandeAdminService.listerToutes();
        long congesEnAttente = conges.stream().filter(demande -> demande.getStatut() == StatutDemande.EN_ATTENTE).count();
        long demandesAdminEnAttente = demandesAdmin.stream().filter(demande -> demande.getStatut() == StatutDemande.EN_ATTENTE).count();
        long congesValides = conges.stream().filter(demande -> demande.getStatut() == StatutDemande.VALIDEE).count();
        long demandesTraitees = demandesAdmin.stream().filter(demande -> demande.getStatut() != StatutDemande.EN_ATTENTE).count();
        long totalDemandes = conges.size() + demandesAdmin.size();
        long demandesResolues = conges.stream().filter(demande -> demande.getStatut() != StatutDemande.EN_ATTENTE).count() + demandesTraitees;
        long tauxTraitement = totalDemandes == 0 ? 100 : Math.round((demandesResolues * 100.0) / totalDemandes);
        long scoreRh = Math.max(42, Math.min(99, 82 + employesActifs.size() - (congesEnAttente * 3) - (demandesAdminEnAttente * 2) - notifications));

        model.addAttribute("roleActuel", utilisateur == null ? "" : utilisateur.getRole().name());
        model.addAttribute("roleLabel", utilisateur == null ? "Session" : utilisateur.getRole().name().replace("_", " "));
        model.addAttribute("totalEmployesActifs", employesActifs.size());
        model.addAttribute("congesEnAttente", congesEnAttente);
        model.addAttribute("demandesAdminEnAttente", demandesAdminEnAttente);
        model.addAttribute("congesValides", congesValides);
        model.addAttribute("demandesTraitees", demandesTraitees);
        model.addAttribute("tauxTraitement", tauxTraitement);
        model.addAttribute("scoreRh", scoreRh);
        model.addAttribute("notificationsDashboard", notifications);
        model.addAttribute("recentConges", conges.stream().limit(5).toList());
        model.addAttribute("recentDemandesAdmin", demandesAdmin.stream().limit(5).toList());
        model.addAttribute("employesPreview", employesActifs.stream().limit(8).toList());
        return "dashboard/index";
    }

    @GetMapping("/admin")
    public String administration(Model model) {
        return dashboard(model);
    }
}
