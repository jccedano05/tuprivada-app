package com.jccv.tuprivadaapp.repository.survey;

import com.jccv.tuprivadaapp.model.survey.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    
    boolean existsBySurveyIdAndUserId(Long surveyId, Long userId);
    
    Optional<SurveyResponse> findBySurveyIdAndUserId(Long surveyId, Long userId);
    
    List<SurveyResponse> findBySurveyId(Long surveyId);
    
    @Query("SELECT sr FROM SurveyResponse sr " +
           "LEFT JOIN FETCH sr.survey s " +
           "LEFT JOIN FETCH sr.answers " +
           "WHERE sr.user.id = :userId " +
           "ORDER BY sr.submittedAt DESC")
    List<SurveyResponse> findAllByUserIdWithDetails(@Param("userId") Long userId);
    
    Integer countBySurveyId(Long surveyId);
}
