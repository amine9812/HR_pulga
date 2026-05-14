package com.hrplatform.repository;

import com.hrplatform.model.Poste;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosteRepository extends JpaRepository<Poste, Long> {
}
