package com.jccv.tuprivadaapp.dto.pushNotifications;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OneSignalPushNotificationDto {

    private Long userId;
    private String oneSignalId;
    private String subscriptionId;

}
