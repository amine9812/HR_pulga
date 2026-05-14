package com.hrplatform.service;

import com.hrplatform.model.Departement;
import com.hrplatform.model.Poste;
import com.hrplatform.repository.DepartementRepository;
import com.hrplatform.repository.PosteRepository;
import com.hrplatform.repository.ServiceRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class DepartementService {

    private final DepartementRepository departementRepository;
    private final ServiceRepository serviceRepository;
    private final PosteRepository posteRepository;
    private final AuditService auditService;

    public DepartementService(DepartementRepository departementRepository,
                              ServiceRepository serviceRepository,
                              PosteRepository posteRepository,
                              AuditService auditService) {
        this.departementRepository = departementRepository;
        this.serviceRepository = serviceRepository;
        this.posteRepository = posteRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Departement creerDepartement(Departement departement) {
        Departement saved = departementRepository.save(departement);
        auditService.enregistrerAction("CREATION_DEPARTEMENT", "Creation du departement " + saved.getLibelle(), "Departement", saved.getId());
        return saved;
    }

    @Transactional
    public Departement modifierDepartement(Departement departement) {
        Departement existing = trouverDepartementParId(departement.getId());
        existing.setLibelle(departement.getLibelle());
        existing.setDescription(departement.getDescription());
        Departement saved = departementRepository.save(existing);
        auditService.enregistrerAction("MODIFICATION_DEPARTEMENT", "Modification du departement " + saved.getLibelle(), "Departement", saved.getId());
        return saved;
    }

    @Transactional
    public void supprimerDepartement(Long id) {
        Departement departement = trouverDepartementParId(id);
        departementRepository.delete(departement);
        auditService.enregistrerAction("SUPPRESSION_DEPARTEMENT", "Suppression du departement " + departement.getLibelle(), "Departement", id);
    }

    @Transactional(readOnly = true)
    public Departement trouverDepartementParId(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Departement introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Departement> listerDepartements() {
        return departementRepository.findAll().stream()
                .sorted(Comparator.comparing(Departement::getLibelle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public com.hrplatform.model.Service creerService(com.hrplatform.model.Service service) {
        service.setDepartement(resolveDepartement(service.getDepartement()));
        com.hrplatform.model.Service saved = serviceRepository.save(service);
        auditService.enregistrerAction("CREATION_SERVICE", "Creation du service " + saved.getLibelle(), "Service", saved.getId());
        return saved;
    }

    @Transactional
    public com.hrplatform.model.Service modifierService(com.hrplatform.model.Service service) {
        com.hrplatform.model.Service existing = trouverServiceParId(service.getId());
        existing.setLibelle(service.getLibelle());
        existing.setDescription(service.getDescription());
        existing.setDepartement(resolveDepartement(service.getDepartement()));
        com.hrplatform.model.Service saved = serviceRepository.save(existing);
        auditService.enregistrerAction("MODIFICATION_SERVICE", "Modification du service " + saved.getLibelle(), "Service", saved.getId());
        return saved;
    }

    @Transactional
    public void supprimerService(Long id) {
        com.hrplatform.model.Service service = trouverServiceParId(id);
        serviceRepository.delete(service);
        auditService.enregistrerAction("SUPPRESSION_SERVICE", "Suppression du service " + service.getLibelle(), "Service", id);
    }

    @Transactional(readOnly = true)
    public com.hrplatform.model.Service trouverServiceParId(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service introuvable"));
    }

    @Transactional(readOnly = true)
    public List<com.hrplatform.model.Service> listerServices() {
        return serviceRepository.findAll().stream()
                .sorted(Comparator.comparing(com.hrplatform.model.Service::getLibelle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public Poste creerPoste(Poste poste) {
        Poste saved = posteRepository.save(poste);
        auditService.enregistrerAction("CREATION_POSTE", "Creation du poste " + saved.getLibelle(), "Poste", saved.getId());
        return saved;
    }

    @Transactional
    public Poste modifierPoste(Poste poste) {
        Poste existing = trouverPosteParId(poste.getId());
        existing.setLibelle(poste.getLibelle());
        existing.setDescription(poste.getDescription());
        existing.setNiveau(poste.getNiveau());
        Poste saved = posteRepository.save(existing);
        auditService.enregistrerAction("MODIFICATION_POSTE", "Modification du poste " + saved.getLibelle(), "Poste", saved.getId());
        return saved;
    }

    @Transactional
    public void supprimerPoste(Long id) {
        Poste poste = trouverPosteParId(id);
        posteRepository.delete(poste);
        auditService.enregistrerAction("SUPPRESSION_POSTE", "Suppression du poste " + poste.getLibelle(), "Poste", id);
    }

    @Transactional(readOnly = true)
    public Poste trouverPosteParId(Long id) {
        return posteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Poste introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Poste> listerPostes() {
        return posteRepository.findAll().stream()
                .sorted(Comparator.comparing(Poste::getLibelle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private Departement resolveDepartement(Departement departement) {
        if (departement == null || departement.getId() == null) {
            return null;
        }
        return departementRepository.findById(departement.getId()).orElse(null);
    }

}
