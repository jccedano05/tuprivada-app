package com.jccv.tuprivadaapp.repository.pushNotificacion;

import com.jccv.tuprivadaapp.model.pushNotification.OneSignalPushNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OneSignalPushNotificationRepository extends JpaRepository<OneSignalPushNotification, Long> {

    @Query("SELECT p FROM OneSignalPushNotification p WHERE p.user.id IN :userIds")
    List<OneSignalPushNotification> findAllByUserIds(List<Long> userIds);

    List<OneSignalPushNotification> findAllByUserId(Long userId);

    @Query("SELECT p FROM OneSignalPushNotification p WHERE p.user.condominium.id = :condominiumId AND p.user.role = com.jccv.tuprivadaapp.model.Role.RESIDENT")
    List<OneSignalPushNotification> findByCondominiumIdAndResidentRole(Long condominiumId);


    OneSignalPushNotification findByOneSignalIdAndSubscriptionId(String oneSignalId, String subscriptionId);
}