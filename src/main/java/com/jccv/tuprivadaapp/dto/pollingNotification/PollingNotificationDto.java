package com.jccv.tuprivadaapp.dto.pollingNotification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollingNotificationDto {

    private Long id;
    private String title;
    private String message;
    private boolean read = false;
    private LocalDateTime createdAt;
//    private Long condominiumId;
    private Long userId;

}
