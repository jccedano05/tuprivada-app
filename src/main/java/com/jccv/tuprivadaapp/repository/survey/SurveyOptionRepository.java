package com.jccv.tuprivadaapp.repository.survey;

import com.jccv.tuprivadaapp.model.survey.SurveyOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyOptionRepository extends JpaRepository<SurveyOption, Long> {
    
    List<SurveyOption> findByQuestionIdOrderByOrderAsc(Long questionId);
}
