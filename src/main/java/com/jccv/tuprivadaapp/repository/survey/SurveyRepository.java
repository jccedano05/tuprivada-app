package com.jccv.tuprivadaapp.repository.survey;

import com.jccv.tuprivadaapp.model.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {}
