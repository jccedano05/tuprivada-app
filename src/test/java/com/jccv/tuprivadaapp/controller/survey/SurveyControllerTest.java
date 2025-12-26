package com.jccv.tuprivadaapp.controller.survey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jccv.tuprivadaapp.dto.survey.*;
import com.jccv.tuprivadaapp.dto.survey.mapper.SurveyMapper;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.exception.SurveyAlreadyVotedException;
import com.jccv.tuprivadaapp.exception.SurveyValidationException;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.survey.*;
import com.jccv.tuprivadaapp.service.survey.SurveyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SurveyController.class)
class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SurveyService surveyService;

    @MockBean
    private SurveyMapper surveyMapper;

    private Survey testSurvey;
    private SurveyDto testSurveyDto;
    private User adminUser;
    private CreateSurveyRequest createRequest;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@test.com")
                .role(Role.ADMIN)
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
                .questions(new ArrayList<>())
                .build();

        testSurveyDto = SurveyDto.builder()
                .id(1L)
                .title("Test Survey")
                .description("Test Description")
                .type(SurveyType.POLL)
                .status(SurveyStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .isAnonymous(false)
                .allowMultipleVotes(false)
                .questions(new ArrayList<>())
                .build();

        CreateSurveyQuestionRequest questionRequest = CreateSurveyQuestionRequest.builder()
                .question("¿Estás satisfecho?")
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
    @WithMockUser
    void getAllSurveys_ShouldReturnSurveyList() throws Exception {
        List<Survey> surveys = List.of(testSurvey);
        List<SurveyDto> surveyDtos = List.of(testSurveyDto);

        when(surveyService.getAllSurveysByCondominium(1L, null, false)).thenReturn(surveys);
        when(surveyMapper.toDto(any(Survey.class))).thenReturn(testSurveyDto);

        mockMvc.perform(get("/api/surveys")
                        .param("condominiumId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Survey"));

        verify(surveyService).getAllSurveysByCondominium(1L, null, false);
    }

    @Test
    @WithMockUser
    void getAllSurveys_WithStatus_ShouldFilterByStatus() throws Exception {
        List<Survey> surveys = List.of(testSurvey);

        when(surveyService.getAllSurveysByCondominium(1L, SurveyStatus.ACTIVE, false)).thenReturn(surveys);
        when(surveyMapper.toDto(any(Survey.class))).thenReturn(testSurveyDto);

        mockMvc.perform(get("/api/surveys")
                        .param("condominiumId", "1")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        verify(surveyService).getAllSurveysByCondominium(1L, SurveyStatus.ACTIVE, false);
    }

    @Test
    @WithMockUser
    void getActiveSurveys_ShouldReturnActiveSurveys() throws Exception {
        List<Survey> surveys = List.of(testSurvey);

        when(surveyService.getActiveSurveys(1L)).thenReturn(surveys);
        when(surveyMapper.toDto(any(Survey.class))).thenReturn(testSurveyDto);

        mockMvc.perform(get("/api/surveys/active")
                        .param("condominiumId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(surveyService).getActiveSurveys(1L);
    }

    @Test
    @WithMockUser
    void getSurveyById_WhenExists_ShouldReturnSurvey() throws Exception {
        when(surveyService.getSurveyById(1L)).thenReturn(testSurvey);
        when(surveyMapper.toDto(testSurvey)).thenReturn(testSurveyDto);

        mockMvc.perform(get("/api/surveys/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Survey"));

        verify(surveyService).getSurveyById(1L);
    }

    @Test
    @WithMockUser
    void getSurveyById_WhenNotExists_ShouldReturn404() throws Exception {
        when(surveyService.getSurveyById(1L)).thenThrow(new ResourceNotFoundException("Survey not found"));

        mockMvc.perform(get("/api/surveys/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSurvey_AsAdmin_ShouldCreateSuccessfully() throws Exception {
        when(surveyService.createSurvey(any(CreateSurveyRequest.class), eq(1L)))
                .thenReturn(testSurvey);
        when(surveyMapper.toDto(testSurvey)).thenReturn(testSurveyDto);

        mockMvc.perform(post("/api/surveys")
                        .with(user(adminUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Survey"));

        verify(surveyService).createSurvey(any(CreateSurveyRequest.class), eq(1L));
    }

    @Test
    void createSurvey_WithInvalidData_ShouldReturn400() throws Exception {
        createRequest.setTitle(null);

        mockMvc.perform(post("/api/surveys")
                        .with(user(adminUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSurvey_AsAdmin_ShouldUpdateSuccessfully() throws Exception {
        UpdateSurveyRequest updateRequest = UpdateSurveyRequest.builder()
                .title("Updated Title")
                .status(SurveyStatus.ACTIVE)
                .build();

        when(surveyService.updateSurvey(eq(1L), any(UpdateSurveyRequest.class), eq(1L)))
                .thenReturn(testSurvey);
        when(surveyMapper.toDto(testSurvey)).thenReturn(testSurveyDto);

        mockMvc.perform(put("/api/surveys/1")
                        .with(user(adminUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(surveyService).updateSurvey(eq(1L), any(UpdateSurveyRequest.class), eq(1L));
    }

    @Test
    void deleteSurvey_AsAdmin_ShouldDeleteSuccessfully() throws Exception {
        doNothing().when(surveyService).deleteSurvey(1L, 1L);

        mockMvc.perform(delete("/api/surveys/1")
                        .with(user(adminUser)))
                .andExpect(status().isNoContent());

        verify(surveyService).deleteSurvey(1L, 1L);
    }

    @Test
    @WithMockUser
    void submitResponse_WithValidData_ShouldCreateResponse() throws Exception {
        SurveyAnswerRequest answerRequest = SurveyAnswerRequest.builder()
                .questionId(1L)
                .selectedOptionIds(List.of(1L))
                .build();

        SubmitSurveyResponseRequest submitRequest = SubmitSurveyResponseRequest.builder()
                .userId(1L)
                .answers(List.of(answerRequest))
                .build();

        SurveyResponse response = new SurveyResponse();
        SurveyResponseDto responseDto = new SurveyResponseDto();

        when(surveyService.submitResponse(eq(1L), any(SubmitSurveyResponseRequest.class)))
                .thenReturn(response);
        when(surveyService.getSurveyById(1L)).thenReturn(testSurvey);
        when(surveyMapper.toResponseDto(response, true)).thenReturn(responseDto);

        mockMvc.perform(post("/api/surveys/1/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isCreated());

        verify(surveyService).submitResponse(eq(1L), any(SubmitSurveyResponseRequest.class));
    }

    @Test
    @WithMockUser
    void submitResponse_WhenAlreadyVoted_ShouldReturn409() throws Exception {
        SurveyAnswerRequest answerRequest = SurveyAnswerRequest.builder()
                .questionId(1L)
                .selectedOptionIds(List.of(1L))
                .build();

        SubmitSurveyResponseRequest submitRequest = SubmitSurveyResponseRequest.builder()
                .userId(1L)
                .answers(List.of(answerRequest))
                .build();

        when(surveyService.submitResponse(eq(1L), any(SubmitSurveyResponseRequest.class)))
                .thenThrow(new SurveyAlreadyVotedException("Already voted"));

        mockMvc.perform(post("/api/surveys/1/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void getSurveyResults_ShouldReturnResults() throws Exception {
        SurveyResultsDto resultsDto = SurveyResultsDto.builder()
                .surveyId(1L)
                .totalResponses(10)
                .responseRate(50.0)
                .questions(new ArrayList<>())
                .build();

        when(surveyService.getSurveyResults(1L)).thenReturn(resultsDto);

        mockMvc.perform(get("/api/surveys/1/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surveyId").value(1))
                .andExpect(jsonPath("$.totalResponses").value(10));

        verify(surveyService).getSurveyResults(1L);
    }

    @Test
    @WithMockUser
    void checkIfUserVoted_WhenVoted_ShouldReturnTrue() throws Exception {
        when(surveyService.hasUserVoted(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/surveys/1/check-voted")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasVoted").value(true));

        verify(surveyService).hasUserVoted(1L, 1L);
    }

    @Test
    @WithMockUser
    void getUserResponses_ShouldReturnUserResponses() throws Exception {
        List<SurveyResponse> responses = new ArrayList<>();
        List<SurveyResponseDto> responseDtos = new ArrayList<>();

        when(surveyService.getUserResponses(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/surveys/users/1/responses"))
                .andExpect(status().isOk());

        verify(surveyService).getUserResponses(1L);
    }
}
