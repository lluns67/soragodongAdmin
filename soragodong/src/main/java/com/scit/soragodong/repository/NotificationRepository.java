package com.scit.soragodong.repository;

import com.scit.soragodong.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
	List<Notification> findByNotiTypeAndRefId(String notiType, Long refId);
}
