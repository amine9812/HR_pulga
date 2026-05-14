package com.hrplatform.repository;

import com.hrplatform.model.Utilisateur;
import com.hrplatform.model.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByLogin(String login);

    List<Utilisateur> findByRole(Role role);
}
