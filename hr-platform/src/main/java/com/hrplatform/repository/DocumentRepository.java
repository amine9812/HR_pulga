package com.hrplatform.repository;

import com.hrplatform.model.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByEmployeId(Long id);
}
