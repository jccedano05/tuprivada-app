package com.jccv.tuprivadaapp.repository.survey;

import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.survey.Survey;
import com.jccv.tuprivadaapp.model.survey.SurveyVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyVoteRepository extends JpaRepository<SurveyVote, Long> {
    Optional<SurveyVote> findByResidentAndSurveyOption_Survey(Resident resident, Survey survey);
}
