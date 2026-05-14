package com.hrplatform.controller;

import com.hrplatform.model.Document;
import com.hrplatform.model.Employe;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.model.enums.Role;
import com.hrplatform.service.DocumentService;
import com.hrplatform.service.EmployeService;
import com.hrplatform.service.NotificationService;
import com.hrplatform.service.UtilisateurService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DocumentController {

    private final DocumentService documentService;
    private final EmployeService employeService;
    private final UtilisateurService utilisateurService;
    private final NotificationService notificationService;

    public DocumentController(DocumentService documentService,
                              EmployeService employeService,
                              UtilisateurService utilisateurService,
                              NotificationService notificationService) {
        this.documentService = documentService;
        this.employeService = employeService;
        this.utilisateurService = utilisateurService;
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void ajouterDonneesCommunes(Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("notificationsNonLues", utilisateur == null ? 0L : notificationService.compterNonLues(utilisateur.getId()));
    }

    @GetMapping("/documents")
    public String lister(@RequestParam(value = "categorie", required = false) String categorie,
                         @RequestParam(value = "employeId", required = false) Long employeId,
                         Model model) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        List<Document> documents = documentsAccessibles(utilisateur);
        if (StringUtils.hasText(categorie)) {
            documents = documents.stream()
                    .filter(d -> d.getCategorie() != null && d.getCategorie().equalsIgnoreCase(categorie.trim()))
                    .toList();
        }
        if (employeId != null) {
            documents = documents.stream()
                    .filter(d -> d.getEmploye() != null && employeId.equals(d.getEmploye().getId()))
                    .toList();
        }
        model.addAttribute("documents", documents);
        model.addAttribute("employes", employesAccessibles(utilisateur));
        model.addAttribute("categorieFiltre", categorie);
        model.addAttribute("employeFiltre", employeId);
        return "documents/list";
    }

    @PostMapping("/documents/televerser")
    public String televerser(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "categorie", required = false) String categorie,
                             @RequestParam(value = "employeId", required = false) Long employeId,
                             RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
            if (utilisateur == null) {
                throw new IllegalStateException("Utilisateur non connecte.");
            }
            if (!peutGererTousLesDocuments(utilisateur) && utilisateur.getEmploye() == null) {
                throw new IllegalStateException("Aucun employe n'est lie a votre compte.");
            }
            Long cibleEmployeId = peutGererTousLesDocuments(utilisateur) ? employeId : utilisateur.getEmploye().getId();
            documentService.televerser(file, categorie, cibleEmployeId, null, utilisateur.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Document televerse avec succes.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/documents";
    }

    @GetMapping("/documents/{id}/telecharger")
    public ResponseEntity<Resource> telecharger(@PathVariable Long id) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        Document document = documentService.trouverParId(id);
        if (!peutAcceder(document, utilisateur)) {
            return ResponseEntity.status(403).build();
        }
        Resource resource = documentService.telecharger(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getNomOriginal() + "\"")
                .body(resource);
    }

    @PostMapping("/documents/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
            Document document = documentService.trouverParId(id);
            if (!peutSupprimer(document, utilisateur)) {
                throw new IllegalStateException("Vous n'etes pas autorise a supprimer ce document.");
            }
            documentService.supprimer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Document supprime.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/documents";
    }

    private List<Document> documentsAccessibles(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return List.of();
        }
        if (peutGererTousLesDocuments(utilisateur)) {
            return documentService.lister();
        }
        if (utilisateur.getEmploye() == null) {
            return List.of();
        }
        Long employeId = utilisateur.getEmploye().getId();
        if (utilisateur.getRole() == Role.RESPONSABLE_HIERARCHIQUE) {
            return documentService.lister().stream()
                    .filter(d -> d.getEmploye() != null
                            && (employeId.equals(d.getEmploye().getId())
                            || (d.getEmploye().getResponsable() != null && employeId.equals(d.getEmploye().getResponsable().getId()))))
                    .toList();
        }
        return documentService.listerParEmploye(employeId);
    }

    private List<Employe> employesAccessibles(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return List.of();
        }
        if (peutGererTousLesDocuments(utilisateur)) {
            return employeService.listerActifs();
        }
        if (utilisateur.getEmploye() == null) {
            return List.of();
        }
        List<Employe> employes = new ArrayList<>();
        employes.add(utilisateur.getEmploye());
        if (utilisateur.getRole() == Role.RESPONSABLE_HIERARCHIQUE) {
            employes.addAll(employeService.listerParResponsable(utilisateur.getEmploye().getId()));
        }
        return employes;
    }

    private boolean peutAcceder(Document document, Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getEmploye() == null) {
            return false;
        }
        if (peutGererTousLesDocuments(utilisateur)) {
            return true;
        }
        Employe employe = document.getEmploye();
        Long currentEmployeId = utilisateur.getEmploye().getId();
        return employe != null && (currentEmployeId.equals(employe.getId())
                || (utilisateur.getRole() == Role.RESPONSABLE_HIERARCHIQUE
                && employe.getResponsable() != null
                && currentEmployeId.equals(employe.getResponsable().getId())));
    }

    private boolean peutSupprimer(Document document, Utilisateur utilisateur) {
        if (utilisateur == null) {
            return false;
        }
        return peutGererTousLesDocuments(utilisateur)
                || (document.getUploadePar() != null && utilisateur.getId().equals(document.getUploadePar().getId()));
    }

    private boolean peutGererTousLesDocuments(Utilisateur utilisateur) {
        return utilisateur != null && (utilisateur.getRole() == Role.ADMIN || utilisateur.getRole() == Role.RESPONSABLE_RH);
    }
}
