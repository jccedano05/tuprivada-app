package com.jccv.tuprivadaapp.repository.survey;

import com.jccv.tuprivadaapp.model.survey.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
    
    List<SurveyQuestion> findBySurveyIdOrderByOrderAsc(Long surveyId);
}
