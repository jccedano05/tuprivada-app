package com.jccv.tuprivadaapp.controller.featureFlag;

import com.jccv.tuprivadaapp.model.featureFlag.FeatureFlag;
import com.jccv.tuprivadaapp.service.featureFlag.FeatureFlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flags")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    @Autowired
    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @GetMapping
    public Map<String, Boolean> list(
            @RequestParam(required = false) Long condominiumId,
            @RequestParam String platform
    ) {
        Map<String,Boolean> features=  featureFlagService.list(condominiumId, platform)
                .stream()
                .collect(Collectors.toMap(
                        FeatureFlag::getFeatureKey,
                        FeatureFlag::getEnabled
                ));

        System.out.println(features.toString());

        return features;
    }
    // Insertar flags pero poner restriccion solo para superadmin
//    @PostMapping
//    public FeatureFlag upsert(@RequestBody FeatureFlag flag) {
//        return featureFlagService.upsert(flag);
//    }

    @GetMapping("/check")
    public boolean check(
            @RequestParam String key,
            @RequestParam(required = false) Long condominiumId,
            @RequestParam String platform
    ) {
        return featureFlagService.isEnabled(key, condominiumId, platform);
    }
}
