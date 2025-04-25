package com.jccv.tuprivadaapp.controller.appVersion;


import com.jccv.tuprivadaapp.dto.appVersion.AppVersionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app-version")
public class AppVersionController {

    @GetMapping
    public ResponseEntity<AppVersionDto> getAppVersion() {
        AppVersionDto versionInfo = new AppVersionDto();
        versionInfo.setMinRequiredVersion("1.1.0");
        versionInfo.setLatestVersion("1.1.0");
        return ResponseEntity.ok(versionInfo);
    }
}
