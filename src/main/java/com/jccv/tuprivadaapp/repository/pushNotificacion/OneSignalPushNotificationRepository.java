package com.jccv.tuprivadaapp.repository.pushNotificacion;

import com.jccv.tuprivadaapp.model.pushNotification.OneSignalPushNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OneSignalPushNotificationRepository extends JpaRepository<OneSignalPushNotification, Long> {
    List<OneSignalPushNotification> findByUserId(Long userId);
    OneSignalPushNotification findByOneSignalIdAndSubscriptionId(String oneSignalId, String subscriptionId);
}