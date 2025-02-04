package com.jccv.tuprivadaapp.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDto {

    private Long userId;
    private Long eventId;
    private String deviceId;
}
