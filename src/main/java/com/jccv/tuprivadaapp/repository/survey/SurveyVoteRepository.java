package com.jccv.tuprivadaapp.repository.survey;

import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.survey.Survey;
import com.jccv.tuprivadaapp.model.survey.SurveyOption;
import com.jccv.tuprivadaapp.model.survey.SurveyVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SurveyVoteRepository extends JpaRepository<SurveyVote, Long> {
    
    @Query("SELECT sv FROM SurveyVote sv " +
           "WHERE sv.resident = :resident " +
           "AND sv.surveyOption.question.survey = :survey")
    Optional<SurveyVote> findByResidentAndSurvey(@Param("resident") Resident resident, 
                                                   @Param("survey") Survey survey);
    
    Optional<SurveyVote> findByResidentAndSurveyOption(Resident resident, SurveyOption surveyOption);
}
