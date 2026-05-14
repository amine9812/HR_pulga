package com.hrplatform.repository;

import com.hrplatform.model.Employe;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeRepository extends JpaRepository<Employe, Long> {

    List<Employe> findByDepartementId(Long id);

    List<Employe> findByNomContainingOrPrenomContaining(String nom, String prenom);

    List<Employe> findByActifTrue();

    List<Employe> findByResponsableId(Long id);
}
