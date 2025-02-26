package com.jccv.tuprivadaapp.service.pollingNotification;

import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.model.User;

import java.util.List;

public interface PollingNotificationService {
    public void createNotification(PollingNotificationDto pollingNotificationDto);

    public void createNotificationForCondominium(Long condominiumId, PollingNotificationDto pollingNotificationDto);

    public void createNotificationForByUserIds(List<Long> userIds, PollingNotificationDto pollingNotificationDto);

    public void markNotificationsAsRead(List<Long> notificationIds);

    void createNotificationForUserList(List<User> users, PollingNotificationDto pollingNotificationDto);

    public List<PollingNotificationDto> getUnreadNotificationsForUser(Long userId);

}
