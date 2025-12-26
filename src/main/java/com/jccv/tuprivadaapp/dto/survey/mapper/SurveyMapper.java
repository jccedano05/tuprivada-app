package com.jccv.tuprivadaapp.dto.survey.mapper;

import com.jccv.tuprivadaapp.dto.survey.*;
import com.jccv.tuprivadaapp.model.survey.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SurveyMapper {

    public SurveyDto toDto(Survey survey) {
        if (survey == null) {
            return null;
        }

        return SurveyDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .type(survey.getType())
                .status(survey.getStatus())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .isAnonymous(survey.getIsAnonymous())
                .allowMultipleVotes(survey.getAllowMultipleVotes())
                .totalVotes(survey.getTotalVotes())
                .condominiumId(survey.getCondominium() != null ? survey.getCondominium().getId() : null)
                .createdBy(survey.getCreatedBy() != null ? survey.getCreatedBy().getId() : null)
                .questions(toQuestionDtoList(survey.getQuestions()))
                .createdAt(survey.getCreatedAt())
                .updatedAt(survey.getUpdatedAt())
                .build();
    }

    public SurveyQuestionDto toQuestionDto(SurveyQuestion question) {
        if (question == null) {
            return null;
        }

        return SurveyQuestionDto.builder()
                .id(question.getId())
                .question(question.getQuestion())
                .type(question.getType())
                .isRequired(question.getIsRequired())
                .order(question.getOrder())
                .options(toOptionDtoList(question.getOptions()))
                .build();
    }

    public SurveyOptionDto toOptionDto(SurveyOption option, Integer totalVotes) {
        if (option == null) {
            return null;
        }

        return SurveyOptionDto.builder()
                .id(option.getId())
                .text(option.getText())
                .order(option.getOrder())
                .votes(option.getVotes())
                .percentage(option.getPercentage(totalVotes))
                .build();
    }

    public SurveyResponseDto toResponseDto(SurveyResponse response, boolean includeUserId) {
        if (response == null) {
            return null;
        }

        return SurveyResponseDto.builder()
                .id(response.getId())
                .surveyId(response.getSurvey() != null ? response.getSurvey().getId() : null)
                .surveyTitle(response.getSurvey() != null ? response.getSurvey().getTitle() : null)
                .userId(includeUserId && response.getUser() != null ? response.getUser().getId() : null)
                .residentId(response.getResident() != null ? response.getResident().getId() : null)
                .answers(toAnswerDtoList(response.getAnswers()))
                .submittedAt(response.getSubmittedAt())
                .build();
    }

    public SurveyAnswerDto toAnswerDto(SurveyAnswer answer) {
        if (answer == null) {
            return null;
        }

        return SurveyAnswerDto.builder()
                .id(answer.getId())
                .questionId(answer.getQuestion() != null ? answer.getQuestion().getId() : null)
                .questionText(answer.getQuestion() != null ? answer.getQuestion().getQuestion() : null)
                .selectedOptionIds(answer.getSelectedOptionIds())
                .textAnswer(answer.getTextAnswer())
                .ratingValue(answer.getRatingValue())
                .build();
    }

    private List<SurveyQuestionDto> toQuestionDtoList(List<SurveyQuestion> questions) {
        if (questions == null) {
            return new ArrayList<>();
        }
        return questions.stream()
                .map(this::toQuestionDto)
                .collect(Collectors.toList());
    }

    private List<SurveyOptionDto> toOptionDtoList(List<SurveyOption> options) {
        if (options == null) {
            return new ArrayList<>();
        }
        return options.stream()
                .map(option -> toOptionDto(option, null))
                .collect(Collectors.toList());
    }

    private List<SurveyAnswerDto> toAnswerDtoList(List<SurveyAnswer> answers) {
        if (answers == null) {
            return new ArrayList<>();
        }
        return answers.stream()
                .map(this::toAnswerDto)
                .collect(Collectors.toList());
    }

    public SurveyResponseWithUserDto toResponseWithUserDto(SurveyResponse response) {
        if (response == null) {
            return null;
        }

        return SurveyResponseWithUserDto.builder()
                .id(response.getId())
                .surveyId(response.getSurvey() != null ? response.getSurvey().getId() : null)
                .surveyTitle(response.getSurvey() != null ? response.getSurvey().getTitle() : null)
                .userId(response.getUser() != null ? response.getUser().getId() : null)
                .userFirstName(response.getUser() != null ? response.getUser().getFirstName() : null)
                .userLastName(response.getUser() != null ? response.getUser().getLastName() : null)
                .userEmail(response.getUser() != null ? response.getUser().getEmail() : null)
                .residentId(response.getResident() != null ? response.getResident().getId() : null)
                .answers(toAnswerDtoList(response.getAnswers()))
                .submittedAt(response.getSubmittedAt())
                .build();
    }
}
