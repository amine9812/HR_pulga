package com.hrplatform.service;

import com.hrplatform.model.Departement;
import com.hrplatform.model.Employe;
import com.hrplatform.model.Poste;
import com.hrplatform.repository.DepartementRepository;
import com.hrplatform.repository.EmployeRepository;
import com.hrplatform.repository.PosteRepository;
import com.hrplatform.repository.ServiceRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final ServiceRepository serviceRepository;
    private final PosteRepository posteRepository;
    private final AuditService auditService;
    private final Path uploadRoot;

    public EmployeService(EmployeRepository employeRepository,
                          DepartementRepository departementRepository,
                          ServiceRepository serviceRepository,
                          PosteRepository posteRepository,
                          AuditService auditService,
                          @Value("${file.upload-dir}") String uploadDir) {
        this.employeRepository = employeRepository;
        this.departementRepository = departementRepository;
        this.serviceRepository = serviceRepository;
        this.posteRepository = posteRepository;
        this.auditService = auditService;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public Employe creer(Employe employe) {
        appliquerRelations(employe);
        employe.setActif(true);
        Employe saved = employeRepository.save(employe);
        auditService.enregistrerAction("CREATION_EMPLOYE", "Creation de " + saved.getNomComplet(), "Employe", saved.getId());
        return saved;
    }

    @Transactional
    public Employe modifier(Employe employe) {
        Employe existing = trouverParId(employe.getId());
        existing.setMatricule(employe.getMatricule());
        existing.setNom(employe.getNom());
        existing.setPrenom(employe.getPrenom());
        existing.setEmail(employe.getEmail());
        existing.setTelephone(employe.getTelephone());
        existing.setDateNaissance(employe.getDateNaissance());
        existing.setDateEmbauche(employe.getDateEmbauche());
        existing.setAdresse(employe.getAdresse());
        existing.setActif(employe.isActif());
        existing.setDepartement(resolveDepartement(employe.getDepartement()));
        existing.setService(resolveService(employe.getService()));
        existing.setPoste(resolvePoste(employe.getPoste()));
        existing.setResponsable(resolveResponsable(employe.getResponsable(), existing.getId()));
        Employe saved = employeRepository.save(existing);
        auditService.enregistrerAction("MODIFICATION_EMPLOYE", "Modification de " + saved.getNomComplet(), "Employe", saved.getId());
        return saved;
    }

    @Transactional
    public void archiver(Long id) {
        Employe employe = trouverParId(id);
        employe.setActif(false);
        employeRepository.save(employe);
        auditService.enregistrerAction("ARCHIVAGE_EMPLOYE", "Archivage de " + employe.getNomComplet(), "Employe", id);
    }

    @Transactional(readOnly = true)
    public Employe trouverParId(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employe introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Employe> listerTous() {
        return employeRepository.findAll().stream()
                .sorted(Comparator.comparing(Employe::getNom, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Employe> listerActifs() {
        return employeRepository.findByActifTrue().stream()
                .sorted(Comparator.comparing(Employe::getNom, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Employe> rechercher(String terme) {
        if (!StringUtils.hasText(terme)) {
            return listerTous();
        }
        String normalized = terme.toLowerCase(Locale.ROOT).trim();
        return employeRepository.findAll().stream()
                .filter(e -> contient(e.getNom(), normalized)
                        || contient(e.getPrenom(), normalized)
                        || contient(e.getEmail(), normalized)
                        || contient(e.getMatricule(), normalized)
                        || (e.getDepartement() != null && contient(e.getDepartement().getLibelle(), normalized))
                        || (e.getPoste() != null && contient(e.getPoste().getLibelle(), normalized)))
                .sorted(Comparator.comparing(Employe::getNom, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Employe> listerParDepartement(Long deptId) {
        return employeRepository.findByDepartementId(deptId);
    }

    @Transactional(readOnly = true)
    public List<Employe> listerParResponsable(Long responsableId) {
        return employeRepository.findByResponsableId(responsableId);
    }

    @Transactional
    public String sauvegarderPhoto(MultipartFile file, Long employeId) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Employe employe = trouverParId(employeId);
        try {
            Path photosDir = uploadRoot.resolve("photos");
            Files.createDirectories(photosDir);
            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "photo" : file.getOriginalFilename());
            String extension = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                extension = original.substring(dot);
            }
            String fileName = "employe-" + employeId + "-" + UUID.randomUUID() + extension;
            Path target = photosDir.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String relativePath = "uploads/photos/" + fileName;
            employe.setPhoto(relativePath);
            employeRepository.save(employe);
            auditService.enregistrerAction("UPLOAD_PHOTO", "Photo ajoutee pour " + employe.getNomComplet(), "Employe", employeId);
            return relativePath;
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'enregistrer la photo", ex);
        }
    }

    private void appliquerRelations(Employe employe) {
        employe.setDepartement(resolveDepartement(employe.getDepartement()));
        employe.setService(resolveService(employe.getService()));
        employe.setPoste(resolvePoste(employe.getPoste()));
        employe.setResponsable(resolveResponsable(employe.getResponsable(), employe.getId()));
    }

    private Departement resolveDepartement(Departement departement) {
        if (departement == null || departement.getId() == null) {
            return null;
        }
        return departementRepository.findById(departement.getId()).orElse(null);
    }

    private com.hrplatform.model.Service resolveService(com.hrplatform.model.Service service) {
        if (service == null || service.getId() == null) {
            return null;
        }
        return serviceRepository.findById(service.getId()).orElse(null);
    }

    private Poste resolvePoste(Poste poste) {
        if (poste == null || poste.getId() == null) {
            return null;
        }
        return posteRepository.findById(poste.getId()).orElse(null);
    }

    private Employe resolveResponsable(Employe responsable, Long employeId) {
        if (responsable == null || responsable.getId() == null || responsable.getId().equals(employeId)) {
            return null;
        }
        return employeRepository.findById(responsable.getId()).orElse(null);
    }

    private boolean contient(String valeur, String terme) {
        return valeur != null && valeur.toLowerCase(Locale.ROOT).contains(terme);
    }

}
