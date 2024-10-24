package com.jccv.tuprivadaapp.model.pushNotification;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
public class OneSignalPushNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // ID del usuario residente

    private String oneSignalId; // OneSignal Player ID
    private String subscriptionId; // OneSignal Player ID


}