package com.jccv.tuprivadaapp.controller.survey;

import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.survey.Survey;
import com.jccv.tuprivadaapp.model.survey.SurveyOption;
import com.jccv.tuprivadaapp.model.survey.SurveyVote;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import com.jccv.tuprivadaapp.repository.survey.SurveyOptionRepository;
import com.jccv.tuprivadaapp.repository.survey.SurveyRepository;
import com.jccv.tuprivadaapp.repository.survey.SurveyVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private SurveyOptionRepository optionRepository;

    @Autowired
    private SurveyVoteRepository surveyVoteRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @PostMapping("/{surveyId}/vote")
    public ResponseEntity<String> vote(
            @PathVariable Long surveyId,
            @RequestParam Long residentId,
            @RequestBody List<Long> optionIds) {

        Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
        if (surveyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Survey survey = surveyOpt.get();
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Residente no encontrado"));

        // Comprobar si el residente ya votó en esta encuesta
        Optional<SurveyVote> existingVote = surveyVoteRepository.findByResidentAndSurveyOption_Survey(resident, survey);
        if (existingVote.isPresent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("El residente ya ha votado en esta encuesta.");
        }

        // Registrar los votos
        for (Long optionId : optionIds) {
            SurveyOption option = optionRepository.findById(optionId)
                    .orElseThrow(() -> new RuntimeException("Opción no encontrada"));

            SurveyVote vote = new SurveyVote();
            vote.setResident(resident);
            vote.setSurveyOption(option);
            surveyVoteRepository.save(vote);
        }

        return ResponseEntity.ok("Voto registrado");
    }
}
