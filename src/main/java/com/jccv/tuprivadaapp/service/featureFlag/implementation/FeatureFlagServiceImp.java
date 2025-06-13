package com.jccv.tuprivadaapp.service.featureFlag.implementation;

import com.jccv.tuprivadaapp.model.featureFlag.FeatureFlag;
import com.jccv.tuprivadaapp.repository.featureFlag.FeatureFlagRepository;
import com.jccv.tuprivadaapp.service.featureFlag.FeatureFlagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class FeatureFlagServiceImp implements FeatureFlagService {
    private final FeatureFlagRepository featureFlagRepository;

    public FeatureFlagServiceImp(FeatureFlagRepository featureFlagRepository) {
        this.featureFlagRepository = featureFlagRepository;
    }

    @Override
    public boolean isEnabled(String key, Long condoId, String platform) {
        // Primero condominio específico, luego global, ya sea para plataforma o BOTH
        List<Long> condoIds = Arrays.asList(condoId, null);
        List<FeatureFlag> flags = featureFlagRepository.findAvailableFlags(condoIds, platform);
        return flags.stream()
                .filter(f -> f.getFeatureKey().equals(key))
                .map(FeatureFlag::getEnabled)
                .findFirst()
                .orElse(false);
    }

    @Override
    @Transactional
    public FeatureFlag upsert(FeatureFlag flag) {
        flag.setUpdatedAt(LocalDateTime.now());
        return featureFlagRepository.findByFeatureKeyAndCondominiumIdAndPlatform(
                flag.getFeatureKey(), flag.getCondominiumId(), flag.getPlatform()
        ).map(existing -> {
            existing.setEnabled(flag.getEnabled());
            existing.setUpdatedAt(flag.getUpdatedAt());
            return featureFlagRepository.save(existing);
        }).orElseGet(() -> featureFlagRepository.save(flag));
    }

    @Override
    public List<FeatureFlag> list(Long condoId, String platform) {
        List<Long> condoIds = Arrays.asList(condoId, null);
        return featureFlagRepository.findAvailableFlags(condoIds, platform);
    }
}