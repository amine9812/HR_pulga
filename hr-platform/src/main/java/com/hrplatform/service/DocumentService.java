package com.hrplatform.service;

import com.hrplatform.model.DemandeAdministrative;
import com.hrplatform.model.Document;
import com.hrplatform.model.Employe;
import com.hrplatform.model.Utilisateur;
import com.hrplatform.repository.DemandeAdministrativeRepository;
import com.hrplatform.repository.DocumentRepository;
import com.hrplatform.repository.EmployeRepository;
import com.hrplatform.repository.UtilisateurRepository;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final EmployeRepository employeRepository;
    private final DemandeAdministrativeRepository demandeAdministrativeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditService auditService;
    private final Path uploadRoot;

    public DocumentService(DocumentRepository documentRepository,
                           EmployeRepository employeRepository,
                           DemandeAdministrativeRepository demandeAdministrativeRepository,
                           UtilisateurRepository utilisateurRepository,
                           AuditService auditService,
                           @Value("${file.upload-dir}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.employeRepository = employeRepository;
        this.demandeAdministrativeRepository = demandeAdministrativeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.auditService = auditService;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public Document televerser(MultipartFile file, String categorie, Long employeId, Long demandeId, Long uploadeurId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est obligatoire");
        }
        try {
            Path documentsDir = uploadRoot.resolve("documents");
            Files.createDirectories(documentsDir);
            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
            String extension = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                extension = original.substring(dot);
            }
            String storedName = UUID.randomUUID() + extension;
            Path target = documentsDir.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Employe employe = employeId == null ? null : employeRepository.findById(employeId).orElse(null);
            DemandeAdministrative demande = demandeId == null ? null : demandeAdministrativeRepository.findById(demandeId).orElse(null);
            Utilisateur uploadeur = utilisateurRepository.findById(uploadeurId)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

            Document document = Document.builder()
                    .nomFichier(storedName)
                    .nomOriginal(original)
                    .categorie(StringUtils.hasText(categorie) ? categorie : "General")
                    .cheminFichier(target.toString())
                    .dateAjout(LocalDateTime.now())
                    .taille(file.getSize())
                    .employe(employe)
                    .demandeAdmin(demande)
                    .uploadePar(uploadeur)
                    .build();
            Document saved = documentRepository.save(document);
            auditService.enregistrerAction("UPLOAD_DOCUMENT", "Televersement du document " + original, "Document", saved.getId());
            return saved;
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'enregistrer le document", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Document> lister() {
        return documentRepository.findAll().stream()
                .sorted(Comparator.comparing(Document::getDateAjout, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Document> listerParEmploye(Long id) {
        return documentRepository.findByEmployeId(id).stream()
                .sorted(Comparator.comparing(Document::getDateAjout, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Document trouverParId(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document introuvable"));
    }

    @Transactional(readOnly = true)
    public Resource telecharger(Long id) {
        Document document = trouverParId(id);
        try {
            Path path = Paths.get(document.getCheminFichier()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("Fichier introuvable");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Chemin du fichier invalide", ex);
        }
    }

    @Transactional
    public void supprimer(Long id) {
        Document document = trouverParId(id);
        try {
            Files.deleteIfExists(Paths.get(document.getCheminFichier()));
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de supprimer le fichier", ex);
        }
        documentRepository.delete(document);
        auditService.enregistrerAction("SUPPRESSION_DOCUMENT", "Suppression du document " + document.getNomOriginal(), "Document", id);
    }
}
