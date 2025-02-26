package com.jccv.tuprivadaapp.model.pushNotification;


import com.jccv.tuprivadaapp.model.User;
import jakarta.persistence.*;
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

    // Relación ManyToOne con la entidad User, ya que un usuario puede tener varias notificaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Cambiado de Long userId a User user

    private String oneSignalId; // OneSignal Player ID
    private String subscriptionId; // OneSignal Player ID


}