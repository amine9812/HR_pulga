package com.hrplatform.repository;

import com.hrplatform.model.DemandeCongé;
import com.hrplatform.model.enums.StatutDemande;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandeCongéRepository extends JpaRepository<DemandeCongé, Long> {

    List<DemandeCongé> findByEmployeId(Long id);

    List<DemandeCongé> findByStatut(StatutDemande statut);

    List<DemandeCongé> findByEmployeResponsableId(Long id);
}
