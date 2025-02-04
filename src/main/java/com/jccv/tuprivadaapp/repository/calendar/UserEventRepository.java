package com.jccv.tuprivadaapp.repository.calendar;

import com.jccv.tuprivadaapp.model.calendar.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    Optional<UserEvent> findByEventIdAndDeviceId(Long eventId, String deviceId);
}
