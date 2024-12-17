package com.jccv.tuprivadaapp.service.pollingNotification;

import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;

import java.util.List;

public interface PollingNotificationService {
    public void createNotification(PollingNotificationDto pollingNotificationDto);

    public void createNotificationForCondominium(Long condominiumId, PollingNotificationDto pollingNotificationDto);

    public void markNotificationsAsRead(List<Long> notificationIds);

    public List<PollingNotificationDto> getUnreadNotificationsForUser(Long userId);

}
