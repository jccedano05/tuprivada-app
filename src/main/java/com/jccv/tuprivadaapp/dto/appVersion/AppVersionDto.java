package com.jccv.tuprivadaapp.dto.appVersion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionDto {
    private String minRequiredVersion;
    private String latestVersion;
}
