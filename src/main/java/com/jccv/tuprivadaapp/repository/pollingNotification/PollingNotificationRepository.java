package com.jccv.tuprivadaapp.repository.pollingNotification;

import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.pollingNotification.PollingNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollingNotificationRepository extends JpaRepository<PollingNotification, Long> {
    List<PollingNotification> findByUserAndReadFalse(User user);
}

