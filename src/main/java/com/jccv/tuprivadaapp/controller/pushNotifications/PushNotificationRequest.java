package com.jccv.tuprivadaapp.controller.pushNotifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushNotificationRequest {
    private Long userId;
    private String title;
    private String message;
    private Map<String, String> data;
}
