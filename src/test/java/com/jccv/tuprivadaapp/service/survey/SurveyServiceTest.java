package com.jccv.tuprivadaapp.service.survey;

import com.jccv.tuprivadaapp.dto.survey.*;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.exception.SurveyAlreadyVotedException;
import com.jccv.tuprivadaapp.exception.SurveyValidationException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.survey.*;
import com.jccv.tuprivadaapp.repository.auth.UserRepository;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import com.jccv.tuprivadaapp.repository.survey.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private SurveyQuestionRepository questionRepository;

    @Mock
    private SurveyOptionRepository optionRepository;

    @Mock
    private SurveyResponseRepository responseRepository;

    @Mock
    private SurveyAnswerRepository answerRepository;

    @Mock
    private CondominiumRepository condominiumRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResidentRepository residentRepository;

    @InjectMocks
    private SurveyService surveyService;

    private Condominium testCondominium;
    private User testUser;
    private Survey testSurvey;
    private CreateSurveyRequest createRequest;

    @BeforeEach
    void setUp() {
        testCondominium = Condominium.builder()
                .id(1L)
                .name("Test Condominium")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testSurvey = Survey.builder()
                .id(1L)
                .title("Test Survey")
                .description("Test Description")
                .type(SurveyType.POLL)
                .status(SurveyStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .isAnonymous(false)
                .allowMultipleVotes(false)
                .isDeleted(false)
                .condominium(testCondominium)
                .createdBy(testUser)
                .questions(new ArrayList<>())
                .responses(new ArrayList<>())
                .build();

        CreateSurveyQuestionRequest questionRequest = CreateSurveyQuestionRequest.builder()
                .question("¿Estás satisfecho con el servicio?")
                .type(QuestionType.SINGLE_CHOICE)
                .isRequired(true)
                .order(1)
                .options(List.of(
                        CreateSurveyOptionRequest.builder().text("Sí").order(1).build(),
                        CreateSurveyOptionRequest.builder().text("No").order(2).build()
                ))
                .build();

        createRequest = CreateSurveyRequest.builder()
                .title("Test Survey")
                .description("Test Description")
                .type(SurveyType.POLL)
                .status(SurveyStatus.DRAFT)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .isAnonymous(false)
                .allowMultipleVotes(false)
                .condominiumId(1L)
                .questions(List.of(questionRequest))
                .build();
    }

    @Test
    void getAllSurveysByCondominium_ShouldReturnAllSurveys() {
        List<Survey> expectedSurveys = List.of(testSurvey);
        when(surveyRepository.findByCondominiumIdAndIsDeletedFalse(1L))
                .thenReturn(expectedSurveys);

        List<Survey> result = surveyService.getAllSurveysByCondominium(1L, null, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSurvey.getId(), result.get(0).getId());
        verify(surveyRepository).findByCondominiumIdAndIsDeletedFalse(1L);
    }

    @Test
    void getAllSurveysByCondominium_WithStatus_ShouldFilterByStatus() {
        List<Survey> expectedSurveys = List.of(testSurvey);
        when(surveyRepository.findByCondominiumIdAndStatusAndIsDeletedFalse(1L, SurveyStatus.ACTIVE))
                .thenReturn(expectedSurveys);

        List<Survey> result = surveyService.getAllSurveysByCondominium(1L, SurveyStatus.ACTIVE, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(surveyRepository).findByCondominiumIdAndStatusAndIsDeletedFalse(1L, SurveyStatus.ACTIVE);
    }

    @Test
    void getActiveSurveys_ShouldReturnOnlyActiveSurveys() {
        List<Survey> expectedSurveys = List.of(testSurvey);
        when(surveyRepository.findActiveByCondominiumId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(expectedSurveys);

        List<Survey> result = surveyService.getActiveSurveys(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(surveyRepository).findActiveByCondominiumId(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void getSurveyById_WhenExists_ShouldReturnSurvey() {
        when(surveyRepository.findByIdWithQuestionsAndOptions(1L))
                .thenReturn(Optional.of(testSurvey));

        Survey result = surveyService.getSurveyById(1L);

        assertNotNull(result);
        assertEquals(testSurvey.getId(), result.getId());
        verify(surveyRepository).findByIdWithQuestionsAndOptions(1L);
    }

    @Test
    void getSurveyById_WhenNotExists_ShouldThrowException() {
        when(surveyRepository.findByIdWithQuestionsAndOptions(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            surveyService.getSurveyById(1L);
        });
    }

    @Test
    void createSurvey_WithValidData_ShouldCreateSuccessfully() {
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(testCondominium));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(surveyRepository.save(any(Survey.class))).thenReturn(testSurvey);

        Survey result = surveyService.createSurvey(createRequest, 1L);

        assertNotNull(result);
        verify(condominiumRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(surveyRepository).save(any(Survey.class));
    }

    @Test
    void createSurvey_WithInvalidDates_ShouldThrowException() {
        createRequest.setStartDate(LocalDateTime.now().plusDays(7));
        createRequest.setEndDate(LocalDateTime.now());

        assertThrows(SurveyValidationException.class, () -> {
            surveyService.createSurvey(createRequest, 1L);
        });
    }

    @Test
    void createSurvey_WhenCondominiumNotExists_ShouldThrowException() {
        when(condominiumRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            surveyService.createSurvey(createRequest, 1L);
        });
    }

    @Test
    void updateSurvey_WithValidData_ShouldUpdateSuccessfully() {
        UpdateSurveyRequest updateRequest = UpdateSurveyRequest.builder()
                .title("Updated Title")
                .status(SurveyStatus.ACTIVE)
                .build();

        when(surveyRepository.findById(1L)).thenReturn(Optional.of(testSurvey));
        when(surveyRepository.save(any(Survey.class))).thenReturn(testSurvey);

        Survey result = surveyService.updateSurvey(1L, updateRequest, 1L);

        assertNotNull(result);
        verify(surveyRepository).findById(1L);
        verify(surveyRepository).save(any(Survey.class));
    }

    @Test
    void deleteSurvey_ShouldSoftDelete() {
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(testSurvey));
        when(surveyRepository.save(any(Survey.class))).thenReturn(testSurvey);

        surveyService.deleteSurvey(1L, 1L);

        verify(surveyRepository).findById(1L);
        verify(surveyRepository).save(argThat(survey -> survey.getIsDeleted()));
    }

    @Test
    void submitResponse_WithValidData_ShouldCreateResponse() {
        SurveyQuestion question = SurveyQuestion.builder()
                .id(1L)
                .question("Test Question")
                .type(QuestionType.SINGLE_CHOICE)
                .isRequired(true)
                .order(1)
                .options(new ArrayList<>())
                .build();

        SurveyOption option = SurveyOption.builder()
                .id(1L)
                .text("Yes")
                .order(1)
                .votes(0)
                .question(question)
                .build();

        question.getOptions().add(option);
        testSurvey.getQuestions().add(question);

        SurveyAnswerRequest answerRequest = SurveyAnswerRequest.builder()
                .questionId(1L)
                .selectedOptionIds(List.of(1L))
                .build();

        SubmitSurveyResponseRequest submitRequest = SubmitSurveyResponseRequest.builder()
                .userId(1L)
                .answers(List.of(answerRequest))
                .build();

        when(surveyRepository.findByIdWithQuestionsAndOptions(1L))
                .thenReturn(Optional.of(testSurvey));
        when(responseRepository.existsBySurveyIdAndUserId(1L, 1L))
                .thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(optionRepository.findById(1L)).thenReturn(Optional.of(option));
        when(responseRepository.save(any(SurveyResponse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SurveyResponse result = surveyService.submitResponse(1L, submitRequest);

        assertNotNull(result);
        verify(responseRepository).save(any(SurveyResponse.class));
        verify(optionRepository).save(argThat(opt -> opt.getVotes() == 1));
    }

    @Test
    void submitResponse_WhenAlreadyVoted_ShouldThrowException() {
        SurveyAnswerRequest answerRequest = SurveyAnswerRequest.builder()
                .questionId(1L)
                .selectedOptionIds(List.of(1L))
                .build();

        SubmitSurveyResponseRequest submitRequest = SubmitSurveyResponseRequest.builder()
                .userId(1L)
                .answers(List.of(answerRequest))
                .build();

        when(surveyRepository.findByIdWithQuestionsAndOptions(1L))
                .thenReturn(Optional.of(testSurvey));
        when(responseRepository.existsBySurveyIdAndUserId(1L, 1L))
                .thenReturn(true);

        assertThrows(SurveyAlreadyVotedException.class, () -> {
            surveyService.submitResponse(1L, submitRequest);
        });
    }

    @Test
    void submitResponse_WhenSurveyNotActive_ShouldThrowException() {
        testSurvey.setStatus(SurveyStatus.CLOSED);

        SurveyAnswerRequest answerRequest = SurveyAnswerRequest.builder()
                .questionId(1L)
                .selectedOptionIds(List.of(1L))
                .build();

        SubmitSurveyResponseRequest submitRequest = SubmitSurveyResponseRequest.builder()
                .userId(1L)
                .answers(List.of(answerRequest))
                .build();

        when(surveyRepository.findByIdWithQuestionsAndOptions(1L))
                .thenReturn(Optional.of(testSurvey));

        assertThrows(SurveyValidationException.class, () -> {
            surveyService.submitResponse(1L, submitRequest);
        });
    }

    @Test
    void getSurveyResults_ShouldReturnResults() {
        when(surveyRepository.findByIdWithQuestionsAndOptions(1L))
                .thenReturn(Optional.of(testSurvey));
        when(responseRepository.countBySurveyId(1L))
                .thenReturn(10);
        when(residentRepository.countByCondominiumId(1L))
                .thenReturn(20L);

        SurveyResultsDto result = surveyService.getSurveyResults(1L);

        assertNotNull(result);
        assertEquals(10, result.getTotalResponses());
        assertEquals(50.0, result.getResponseRate());
        verify(surveyRepository).findByIdWithQuestionsAndOptions(1L);
    }

    @Test
    void hasUserVoted_WhenVoted_ShouldReturnTrue() {
        when(responseRepository.existsBySurveyIdAndUserId(1L, 1L))
                .thenReturn(true);

        boolean result = surveyService.hasUserVoted(1L, 1L);

        assertTrue(result);
        verify(responseRepository).existsBySurveyIdAndUserId(1L, 1L);
    }

    @Test
    void hasUserVoted_WhenNotVoted_ShouldReturnFalse() {
        when(responseRepository.existsBySurveyIdAndUserId(1L, 1L))
                .thenReturn(false);

        boolean result = surveyService.hasUserVoted(1L, 1L);

        assertFalse(result);
        verify(responseRepository).existsBySurveyIdAndUserId(1L, 1L);
    }

    @Test
    void getUserResponses_ShouldReturnAllUserResponses() {
        List<SurveyResponse> expectedResponses = new ArrayList<>();
        when(responseRepository.findAllByUserIdWithDetails(1L))
                .thenReturn(expectedResponses);

        List<SurveyResponse> result = surveyService.getUserResponses(1L);

        assertNotNull(result);
        verify(responseRepository).findAllByUserIdWithDetails(1L);
    }

    @Test
    void updateSurveyStatus_WithValidTransition_ShouldUpdateStatus() {
        testSurvey.setStatus(SurveyStatus.DRAFT);
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(testSurvey));
        when(surveyRepository.save(any(Survey.class))).thenReturn(testSurvey);

        Survey result = surveyService.updateSurveyStatus(1L, SurveyStatus.ACTIVE, 1L);

        assertNotNull(result);
        verify(surveyRepository).findById(1L);
        verify(surveyRepository).save(argThat(survey -> survey.getStatus() == SurveyStatus.ACTIVE));
    }

    @Test
    void updateSurveyStatus_WithSameStatus_ShouldThrowException() {
        testSurvey.setStatus(SurveyStatus.ACTIVE);
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(testSurvey));

        assertThrows(SurveyValidationException.class, () -> {
            surveyService.updateSurveyStatus(1L, SurveyStatus.ACTIVE, 1L);
        });
    }

    @Test
    void updateSurveyStatus_ReactivateClosedSurveyWithResponses_ShouldThrowException() {
        testSurvey.setStatus(SurveyStatus.CLOSED);
        SurveyResponse response = new SurveyResponse();
        testSurvey.getResponses().add(response);
        
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(testSurvey));

        assertThrows(SurveyValidationException.class, () -> {
            surveyService.updateSurveyStatus(1L, SurveyStatus.ACTIVE, 1L);
        });
    }

    @Test
    void getSurveyResponses_ShouldReturnAllResponses() {
        List<SurveyResponse> expectedResponses = new ArrayList<>();
        SurveyResponse response1 = new SurveyResponse();
        expectedResponses.add(response1);

        when(surveyRepository.findById(1L)).thenReturn(Optional.of(testSurvey));
        when(responseRepository.findBySurveyId(1L)).thenReturn(expectedResponses);

        List<SurveyResponse> result = surveyService.getSurveyResponses(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(responseRepository).findBySurveyId(1L);
    }

    @Test
    void getCondominiumStats_ShouldReturnStatistics() {
        List<Survey> surveys = new ArrayList<>();
        surveys.add(testSurvey);

        when(condominiumRepository.existsById(1L)).thenReturn(true);
        when(surveyRepository.findByCondominiumIdAndIsDeletedFalse(1L)).thenReturn(surveys);
        when(responseRepository.countBySurveyId(1L)).thenReturn(5);
        when(residentRepository.countByCondominiumId(1L)).thenReturn(10L);

        SurveyStatsDto result = surveyService.getCondominiumStats(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTotalSurveys());
        assertEquals(5L, result.getTotalResponses());
        assertNotNull(result.getSurveysByType());
        verify(surveyRepository).findByCondominiumIdAndIsDeletedFalse(1L);
    }

    @Test
    void getCondominiumStats_WhenCondominiumNotExists_ShouldThrowException() {
        when(condominiumRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            surveyService.getCondominiumStats(1L);
        });
    }
}
