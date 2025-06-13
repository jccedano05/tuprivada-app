package com.jccv.tuprivadaapp.model.featureFlag;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feature_flags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long condominiumId; // null = global
    private String featureKey;
    private Boolean enabled;
    private String platform; // "ANDROID", "IOS", "BOTH"
    private LocalDateTime updatedAt;
}
