package com.hrplatform.repository;

import com.hrplatform.model.DemandeAdministrative;
import com.hrplatform.model.enums.StatutDemande;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandeAdministrativeRepository extends JpaRepository<DemandeAdministrative, Long> {

    List<DemandeAdministrative> findByEmployeId(Long id);

    List<DemandeAdministrative> findByStatut(StatutDemande statut);
}
