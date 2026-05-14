package com.hrplatform.repository;

import com.hrplatform.model.HistoriqueAction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriqueActionRepository extends JpaRepository<HistoriqueAction, Long> {

    List<HistoriqueAction> findByUtilisateurId(Long id);

    List<HistoriqueAction> findTop50ByOrderByDateActionDesc();
}
