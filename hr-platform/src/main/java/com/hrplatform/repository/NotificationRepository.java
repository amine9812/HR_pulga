package com.hrplatform.repository;

import com.hrplatform.model.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireIdAndLueFalse(Long id);

    List<Notification> findByDestinataireId(Long id);
}
