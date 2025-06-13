package com.jccv.tuprivadaapp.service.featureFlag;

import com.jccv.tuprivadaapp.model.featureFlag.FeatureFlag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FeatureFlagService {
    boolean isEnabled(String key, Long condoId, String platform);
    FeatureFlag upsert(FeatureFlag flag);
    List<FeatureFlag> list(Long condoId, String platform);
}