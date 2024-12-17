package com.jccv.tuprivadaapp.dto.pollingNotification.mapper;

import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.model.pollingNotification.PollingNotification;
import org.springframework.stereotype.Component;

@Component
public class PollingNotificationMapper {

    public PollingNotificationDto convertToDto(PollingNotification entity){

        return PollingNotificationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .read(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .userId(entity.getUser().getId())
                .build();
    }
}
