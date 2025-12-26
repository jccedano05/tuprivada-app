package com.jccv.tuprivadaapp.repository.survey;

import com.jccv.tuprivadaapp.model.survey.Survey;
import com.jccv.tuprivadaapp.model.survey.SurveyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    
    List<Survey> findByCondominiumIdAndIsDeletedFalse(Long condominiumId);
    
    List<Survey> findByCondominiumIdAndStatusAndIsDeletedFalse(Long condominiumId, SurveyStatus status);
    
    @Query("SELECT s FROM Survey s WHERE s.condominium.id = :condominiumId " +
           "AND s.status = 'ACTIVE' " +
           "AND s.isDeleted = false " +
           "AND s.startDate <= :now " +
           "AND s.endDate >= :now")
    List<Survey> findActiveByCondominiumId(@Param("condominiumId") Long condominiumId, 
                                           @Param("now") LocalDateTime now);
    
    @Query("SELECT DISTINCT s FROM Survey s " +
           "LEFT JOIN FETCH s.questions q " +
           "WHERE s.id = :surveyId AND s.isDeleted = false")
    Optional<Survey> findByIdWithQuestionsAndOptions(@Param("surveyId") Long surveyId);
    
    @Query("SELECT s FROM Survey s WHERE s.condominium.id = :condominiumId " +
           "AND s.isDeleted = false " +
           "ORDER BY s.createdAt DESC")
    List<Survey> findAllByCondominiumIdOrderByCreatedAtDesc(@Param("condominiumId") Long condominiumId);
}
