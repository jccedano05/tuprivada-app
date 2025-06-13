package com.jccv.tuprivadaapp.repository.featureFlag;

import com.jccv.tuprivadaapp.model.featureFlag.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    List<FeatureFlag> findByCondominiumIdInAndPlatform(List<Long> condoIds, String platform);
    Optional<FeatureFlag> findByFeatureKeyAndCondominiumIdAndPlatform(String featureKey, Long condoId, String platform);
    @Query("SELECT f FROM FeatureFlag f " +
            "WHERE (f.condominiumId IN :condoIds OR f.condominiumId IS NULL) " +
            "AND (f.platform = :platform OR f.platform = 'BOTH')")
    List<FeatureFlag> findAvailableFlags(List<Long> condoIds, String platform);

}
