package com.jccv.tuprivadaapp.repository.survey;

import com.jccv.tuprivadaapp.model.survey.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {
    
    List<SurveyAnswer> findByResponseId(Long responseId);
    
    List<SurveyAnswer> findByQuestionId(Long questionId);
    
    @Query("SELECT sa.textAnswer FROM SurveyAnswer sa " +
           "WHERE sa.question.id = :questionId AND sa.textAnswer IS NOT NULL")
    List<String> findAllTextAnswersByQuestionId(@Param("questionId") Long questionId);
    
    @Query("SELECT AVG(sa.ratingValue) FROM SurveyAnswer sa " +
           "WHERE sa.question.id = :questionId AND sa.ratingValue IS NOT NULL")
    Double findAverageRatingByQuestionId(@Param("questionId") Long questionId);
}
