package com.jccv.tuprivadaapp.service.survey;

import com.jccv.tuprivadaapp.dto.survey.*;
import com.jccv.tuprivadaapp.exception.BadRequestException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyOptionRepository optionRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyAnswerRepository answerRepository;
    private final CondominiumRepository condominiumRepository;
    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;

    @Transactional(readOnly = true)
    public List<Survey> getAllSurveysByCondominium(Long condominiumId, SurveyStatus status, boolean isAdmin) {
        try {
            log.info("Obteniendo encuestas para condominio ID: {}, status: {}, isAdmin: {}", condominiumId, status, isAdmin);
            
            List<Survey> surveys;
            if (status != null) {
                surveys = surveyRepository.findByCondominiumIdAndStatusAndIsDeletedFalse(condominiumId, status);
            } else {
                surveys = surveyRepository.findByCondominiumIdAndIsDeletedFalse(condominiumId);
            }
            
            // Filtrar drafts para usuarios no-admin
            if (!isAdmin) {
                surveys = surveys.stream()
                        .filter(survey -> survey.getStatus() != SurveyStatus.DRAFT)
                        .collect(Collectors.toList());
                log.info("Filtradas {} encuestas (excluyendo drafts para usuario no-admin)", surveys.size());
            }
            
            // Actualizar estados automáticamente basado en fechas
            surveys.forEach(this::updateSurveyStatusBasedOnDates);
            
            return surveys;
        } catch (Exception e) {
            log.error("Error al obtener encuestas para condominio {}: {}", condominiumId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener encuestas", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Survey> getActiveSurveys(Long condominiumId) {
        try {
            log.info("Obteniendo encuestas activas para condominio ID: {}", condominiumId);
            LocalDateTime now = LocalDateTime.now();
            return surveyRepository.findActiveByCondominiumId(condominiumId, now);
        } catch (Exception e) {
            log.error("Error al obtener encuestas activas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener encuestas activas", e);
        }
    }

    @Transactional(readOnly = true)
    public Survey getSurveyById(Long surveyId) {
        try {
            log.info("Obteniendo encuesta por ID: {}", surveyId);
            Survey survey = surveyRepository.findByIdWithQuestionsAndOptions(surveyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada con ID: " + surveyId));

            // Actualizar estado automáticamente basado en fechas
            updateSurveyStatusBasedOnDates(survey);

            // Inicializar colecciones relacionadas dentro de la transacción para evitar problemas de lazy loading
            if (survey.getQuestions() != null) {
                survey.getQuestions().forEach(question -> {
                    if (question.getOptions() != null) {
                        question.getOptions().size();
                    }
                });
            }

            return survey;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener encuesta", e);
        }
    }

    @Transactional
    public Survey createSurvey(CreateSurveyRequest request, Long userId) {
        try {
            log.info("Creando nueva encuesta: {} por usuario: {}", request.getTitle(), userId);
            
            // Establecer fechas por defecto si no se especifican
            LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now();
            LocalDateTime endDate = request.getEndDate() != null ? request.getEndDate() : startDate.plusDays(7);
            
            validateSurveyDates(startDate, endDate);
            
            Condominium condominium = condominiumRepository.findById(request.getCondominiumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Condominio no encontrado"));
            
            User creator = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            Survey survey = Survey.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .type(request.getType())
                    .status(request.getStatus() != null ? request.getStatus() : SurveyStatus.DRAFT)
                    .startDate(startDate)
                    .endDate(endDate)
                    .isAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false)
                    .allowMultipleVotes(request.getAllowMultipleVotes() != null ? request.getAllowMultipleVotes() : false)
                    .isDeleted(false)
                    .condominium(condominium)
                    .createdBy(creator)
                    .questions(new ArrayList<>())
                    .build();
            
            for (CreateSurveyQuestionRequest questionReq : request.getQuestions()) {
                SurveyQuestion question = createQuestionFromRequest(questionReq, survey);
                survey.getQuestions().add(question);
            }
            
            Survey savedSurvey = surveyRepository.save(survey);
            log.info("Encuesta creada exitosamente con ID: {}", savedSurvey.getId());
            
            return savedSurvey;
        } catch (ResourceNotFoundException | SurveyValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al crear encuesta: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear encuesta", e);
        }
    }

    @Transactional
    public Survey updateSurvey(Long surveyId, UpdateSurveyRequest request, Long userId) {
        try {
            log.info("Actualizando encuesta ID: {} por usuario: {}", surveyId, userId);
            
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada"));
            
            if (survey.getIsDeleted()) {
                throw new BadRequestException("No se puede actualizar una encuesta eliminada");
            }
            
            if (!survey.getCreatedBy().getId().equals(userId)) {
                log.warn("Usuario {} intentó actualizar encuesta {} creada por {}", 
                        userId, surveyId, survey.getCreatedBy().getId());
            }
            
            boolean hasResponses = !survey.getResponses().isEmpty();
            
            if (hasResponses && request.getQuestions() != null) {
                log.warn("Actualizando encuesta con respuestas existentes. ID: {}", surveyId);
            }
            
            if (request.getTitle() != null) {
                survey.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                survey.setDescription(request.getDescription());
            }
            if (request.getType() != null) {
                survey.setType(request.getType());
            }
            if (request.getStatus() != null) {
                survey.setStatus(request.getStatus());
            }
            if (request.getStartDate() != null && request.getEndDate() != null) {
                validateSurveyDates(request.getStartDate(), request.getEndDate());
                survey.setStartDate(request.getStartDate());
                survey.setEndDate(request.getEndDate());
            }
            if (request.getIsAnonymous() != null) {
                survey.setIsAnonymous(request.getIsAnonymous());
            }
            if (request.getAllowMultipleVotes() != null) {
                survey.setAllowMultipleVotes(request.getAllowMultipleVotes());
            }
            
            if (request.getQuestions() != null) {
                survey.getQuestions().clear();
                for (CreateSurveyQuestionRequest questionReq : request.getQuestions()) {
                    SurveyQuestion question = createQuestionFromRequest(questionReq, survey);
                    survey.getQuestions().add(question);
                }
            }
            
            Survey updatedSurvey = surveyRepository.save(survey);
            log.info("Encuesta actualizada exitosamente: {}", surveyId);
            
            return updatedSurvey;
        } catch (ResourceNotFoundException | BadRequestException | SurveyValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar encuesta", e);
        }
    }

    @Transactional
    public void deleteSurvey(Long surveyId, Long userId) {
        try {
            log.info("Eliminando encuesta ID: {} por usuario: {}", surveyId, userId);
            
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada"));
            
            if (!survey.getCreatedBy().getId().equals(userId)) {
                log.warn("Usuario {} intentó eliminar encuesta {} creada por {}", 
                        userId, surveyId, survey.getCreatedBy().getId());
            }
            
            survey.setIsDeleted(true);
            surveyRepository.save(survey);
            
            log.info("Encuesta eliminada (soft delete) exitosamente: {}", surveyId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al eliminar encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar encuesta", e);
        }
    }

    @Transactional
    public SurveyResponse submitResponse(Long surveyId, SubmitSurveyResponseRequest request) {
        try {
            log.info("Enviando respuesta para encuesta ID: {} por usuario: {}", surveyId, request.getUserId());
            
            Survey survey = surveyRepository.findByIdWithQuestionsAndOptions(surveyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada"));
            
            validateSurveyIsActive(survey);
            
            boolean hasVoted = responseRepository.existsBySurveyIdAndUserId(surveyId, request.getUserId());
            if (hasVoted && !survey.getAllowMultipleVotes()) {
                throw new SurveyAlreadyVotedException("El usuario ya votó en esta encuesta");
            }
            
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            Resident resident = null;
            if (request.getResidentId() != null) {
                resident = residentRepository.findById(request.getResidentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Residente no encontrado"));
            }
            
            validateAllRequiredQuestionsAnswered(survey, request.getAnswers());
            
            SurveyResponse response = SurveyResponse.builder()
                    .survey(survey)
                    .user(user)
                    .resident(resident)
                    .answers(new ArrayList<>())
                    .build();
            
            for (SurveyAnswerRequest answerReq : request.getAnswers()) {
                SurveyQuestion question = survey.getQuestions().stream()
                        .filter(q -> q.getId().equals(answerReq.getQuestionId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada"));
                
                SurveyAnswer answer = createAnswerFromRequest(answerReq, question, response);
                response.getAnswers().add(answer);
                
                if (answerReq.getSelectedOptionIds() != null) {
                    for (Long optionId : answerReq.getSelectedOptionIds()) {
                        SurveyOption option = optionRepository.findById(optionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Opción no encontrada"));
                        option.setVotes(option.getVotes() + 1);
                        optionRepository.save(option);
                    }
                }
            }
            
            SurveyResponse savedResponse = responseRepository.save(response);
            
            // Inicializar todas las propiedades lazy necesarias para el mapper dentro de la transacción
            // Esto evita LazyInitializationException y recursión infinita al serializar
            if (savedResponse.getSurvey() != null) {
                savedResponse.getSurvey().getId();
                savedResponse.getSurvey().getTitle();
            }
            if (savedResponse.getUser() != null) {
                savedResponse.getUser().getId();
            }
            if (savedResponse.getResident() != null) {
                savedResponse.getResident().getId();
            }
            if (savedResponse.getAnswers() != null) {
                savedResponse.getAnswers().forEach(answer -> {
                    if (answer.getQuestion() != null) {
                        answer.getQuestion().getId();
                        answer.getQuestion().getQuestion();
                    }
                    if (answer.getSelectedOptionIds() != null) {
                        answer.getSelectedOptionIds().size();
                    }
                });
            }
            
            log.info("Respuesta registrada exitosamente para encuesta: {}", surveyId);
            
            return savedResponse;
        } catch (ResourceNotFoundException | SurveyValidationException | SurveyAlreadyVotedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al enviar respuesta para encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al enviar respuesta", e);
        }
    }

    @Transactional(readOnly = true)
    public SurveyResultsDto getSurveyResults(Long surveyId) {
        try {
            log.info("Obteniendo resultados para encuesta ID: {}", surveyId);
            
            Survey survey = surveyRepository.findByIdWithQuestionsAndOptions(surveyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada"));
            
            Integer totalResponses = responseRepository.countBySurveyId(surveyId);
            
            List<QuestionResultDto> questionResults = new ArrayList<>();
            for (SurveyQuestion question : survey.getQuestions()) {
                QuestionResultDto questionResult = buildQuestionResult(question, totalResponses);
                questionResults.add(questionResult);
            }
            
            return SurveyResultsDto.builder()
                    .surveyId(surveyId)
                    .totalResponses(totalResponses)
                    .responseRate(calculateResponseRate(totalResponses, survey.getCondominium().getId()))
                    .questions(questionResults)
                    .build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener resultados de encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener resultados", e);
        }
    }

    @Transactional(readOnly = true)
    public boolean hasUserVoted(Long surveyId, Long userId) {
        try {
            log.info("Verificando si usuario {} votó en encuesta {}", userId, surveyId);
            return responseRepository.existsBySurveyIdAndUserId(surveyId, userId);
        } catch (Exception e) {
            log.error("Error al verificar voto de usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error al verificar voto", e);
        }
    }

    @Transactional(readOnly = true)
    public List<SurveyResponse> getUserResponses(Long userId) {
        try {
            log.info("Obteniendo respuestas del usuario ID: {}", userId);
            return responseRepository.findAllByUserIdWithDetails(userId);
        } catch (Exception e) {
            log.error("Error al obtener respuestas del usuario {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener respuestas", e);
        }
    }

    private void validateSurveyDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new SurveyValidationException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
    }

    private void validateSurveyIsActive(Survey survey) {
        if (!SurveyStatus.ACTIVE.equals(survey.getStatus())) {
            throw new SurveyValidationException("La encuesta no está activa");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(survey.getStartDate()) || now.isAfter(survey.getEndDate())) {
            throw new SurveyValidationException("La encuesta no está dentro del rango de fechas válido");
        }
    }

    private void validateAllRequiredQuestionsAnswered(Survey survey, List<SurveyAnswerRequest> answers) {
        List<Long> answeredQuestionIds = answers.stream()
                .map(SurveyAnswerRequest::getQuestionId)
                .collect(Collectors.toList());
        
        for (SurveyQuestion question : survey.getQuestions()) {
            if (question.getIsRequired() && !answeredQuestionIds.contains(question.getId())) {
                throw new SurveyValidationException(
                        "La pregunta requerida '" + question.getQuestion() + "' no fue respondida");
            }
        }
    }

    private SurveyQuestion createQuestionFromRequest(CreateSurveyQuestionRequest request, Survey survey) {
        SurveyQuestion question = SurveyQuestion.builder()
                .survey(survey)
                .question(request.getQuestion())
                .type(request.getType())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .order(request.getOrder())
                .options(new ArrayList<>())
                .build();
        
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (CreateSurveyOptionRequest optionReq : request.getOptions()) {
                SurveyOption option = SurveyOption.builder()
                        .question(question)
                        .text(optionReq.getText())
                        .order(optionReq.getOrder())
                        .votes(0)
                        .build();
                question.getOptions().add(option);
            }
        }
        
        return question;
    }

    private SurveyAnswer createAnswerFromRequest(SurveyAnswerRequest request, 
                                                  SurveyQuestion question, 
                                                  SurveyResponse response) {
        return SurveyAnswer.builder()
                .response(response)
                .question(question)
                .selectedOptionIds(request.getSelectedOptionIds())
                .textAnswer(request.getTextAnswer())
                .ratingValue(request.getRatingValue())
                .build();
    }

    private QuestionResultDto buildQuestionResult(SurveyQuestion question, Integer totalResponses) {
        List<SurveyOptionDto> optionDtos = question.getOptions().stream()
                .map(option -> SurveyOptionDto.builder()
                        .id(option.getId())
                        .text(option.getText())
                        .order(option.getOrder())
                        .votes(option.getVotes())
                        .percentage(option.getPercentage(totalResponses))
                        .build())
                .collect(Collectors.toList());
        
        List<String> textAnswers = null;
        if (QuestionType.TEXT.equals(question.getType())) {
            textAnswers = answerRepository.findAllTextAnswersByQuestionId(question.getId());
        }
        
        Double averageRating = null;
        if (QuestionType.RATING.equals(question.getType())) {
            averageRating = answerRepository.findAverageRatingByQuestionId(question.getId());
        }
        
        return QuestionResultDto.builder()
                .questionId(question.getId())
                .question(question.getQuestion())
                .type(question.getType())
                .totalAnswers(answerRepository.findByQuestionId(question.getId()).size())
                .options(optionDtos)
                .textAnswers(textAnswers)
                .averageRating(averageRating)
                .build();
    }

    private Double calculateResponseRate(Integer totalResponses, Long condominiumId) {
        try {
            Long totalResidents = residentRepository.countByCondominiumId(condominiumId);
            if (totalResidents == 0) {
                return 0.0;
            }
            return (totalResponses * 100.0) / totalResidents;
        } catch (Exception e) {
            log.warn("No se pudo calcular el response rate: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    public Survey updateSurveyStatus(Long surveyId, SurveyStatus newStatus, Long userId) {
        try {
            log.info("Actualizando estado de encuesta ID: {} a {} por usuario: {}", surveyId, newStatus, userId);
            
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada"));
            
            if (survey.getIsDeleted()) {
                throw new BadRequestException("No se puede actualizar el estado de una encuesta eliminada");
            }
            
            validateStatusTransition(survey.getStatus(), newStatus, survey.getResponses().size());
            
            survey.setStatus(newStatus);
            Survey updatedSurvey = surveyRepository.save(survey);
            
            log.info("Estado de encuesta {} actualizado exitosamente a {}", surveyId, newStatus);
            return updatedSurvey;
        } catch (ResourceNotFoundException | BadRequestException | SurveyValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar estado de encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar estado de encuesta", e);
        }
    }

    @Transactional(readOnly = true)
    public List<SurveyResponse> getSurveyResponses(Long surveyId) {
        try {
            log.info("Obteniendo respuestas de encuesta ID: {}", surveyId);
            
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada"));
            
            List<SurveyResponse> responses = responseRepository.findBySurveyId(surveyId);
            log.info("Encontradas {} respuestas para encuesta {}", responses.size(), surveyId);
            
            return responses;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener respuestas de encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener respuestas", e);
        }
    }

    @Transactional(readOnly = true)
    public SurveyStatsDto getCondominiumStats(Long condominiumId) {
        try {
            log.info("Obteniendo estadísticas de encuestas para condominio ID: {}", condominiumId);
            
            if (!condominiumRepository.existsById(condominiumId)) {
                throw new ResourceNotFoundException("Condominio no encontrado");
            }
            
            List<Survey> allSurveys = surveyRepository.findByCondominiumIdAndIsDeletedFalse(condominiumId);
            LocalDateTime now = LocalDateTime.now();
            
            long totalSurveys = allSurveys.size();
            
            long activeSurveys = allSurveys.stream()
                    .filter(s -> s.getStatus() == SurveyStatus.ACTIVE 
                              && !s.getStartDate().isAfter(now) 
                              && !s.getEndDate().isBefore(now))
                    .count();
            
            long totalResponses = allSurveys.stream()
                    .mapToLong(s -> responseRepository.countBySurveyId(s.getId()))
                    .sum();
            
            double averageResponseRate = calculateAverageResponseRate(allSurveys, condominiumId);
            
            Map<String, Long> surveysByType = allSurveys.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getType().name().toLowerCase(),
                            Collectors.counting()
                    ));
            
            surveysByType.putIfAbsent("poll", 0L);
            surveysByType.putIfAbsent("survey", 0L);
            surveysByType.putIfAbsent("voting", 0L);
            
            SurveyStatsDto stats = SurveyStatsDto.builder()
                    .totalSurveys(totalSurveys)
                    .activeSurveys(activeSurveys)
                    .totalResponses(totalResponses)
                    .averageResponseRate(averageResponseRate)
                    .surveysByType(surveysByType)
                    .build();
            
            log.info("Estadísticas calculadas para condominio {}: {} encuestas, {} activas", 
                    condominiumId, totalSurveys, activeSurveys);
            
            return stats;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener estadísticas del condominio {}: {}", condominiumId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener estadísticas", e);
        }
    }

    private void validateStatusTransition(SurveyStatus currentStatus, SurveyStatus newStatus, int responseCount) {
        if (currentStatus == newStatus) {
            throw new SurveyValidationException("La encuesta ya tiene el estado: " + currentStatus);
        }
        
        if (currentStatus == SurveyStatus.CLOSED && newStatus == SurveyStatus.ACTIVE && responseCount > 0) {
            throw new SurveyValidationException(
                    "No se puede reactivar una encuesta cerrada que ya tiene respuestas");
        }
        
        if (newStatus == SurveyStatus.ACTIVE) {
            log.info("Transición a ACTIVE: verificar que se cumplan condiciones de fechas");
        }
        
        log.info("Transición de estado validada: {} -> {}", currentStatus, newStatus);
    }

    private double calculateAverageResponseRate(List<Survey> surveys, Long condominiumId) {
        try {
            Long totalResidents = residentRepository.countByCondominiumId(condominiumId);
            if (totalResidents == 0 || surveys.isEmpty()) {
                return 0.0;
            }
            
            double totalRate = surveys.stream()
                    .mapToDouble(survey -> {
                        int responses = responseRepository.countBySurveyId(survey.getId());
                        return (responses * 100.0) / totalResidents;
                    })
                    .sum();
            
            return totalRate / surveys.size();
        } catch (Exception e) {
            log.warn("Error calculando tasa promedio de respuesta: {}", e.getMessage());
            return 0.0;
        }
    }

    @Transactional
    public SurveyResponse updateResponseByUser(Long surveyId, UpdateSurveyResponseRequest request) {
        try {
            log.info("Actualizando respuesta para encuesta ID: {} por usuario: {}", surveyId, request.getUserId());
            
            // Buscar la respuesta existente del usuario
            SurveyResponse existingResponse = responseRepository.findBySurveyIdAndUserId(surveyId, request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró una respuesta del usuario para esta encuesta"));
            
            // Delegar al método principal con el responseId encontrado
            return updateResponse(surveyId, existingResponse.getId(), request);
        } catch (ResourceNotFoundException | BadRequestException | SurveyValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar respuesta por usuario {}: {}", request.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Error al actualizar respuesta", e);
        }
    }

    @Transactional
    public SurveyResponse updateResponse(Long surveyId, Long responseId, UpdateSurveyResponseRequest request) {
        try {
            log.info("Actualizando respuesta ID: {} para encuesta ID: {} por usuario: {}", 
                    responseId, surveyId, request.getUserId());
            
            Survey survey = surveyRepository.findByIdWithQuestionsAndOptions(surveyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada"));
            
            SurveyResponse existingResponse = responseRepository.findById(responseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Respuesta no encontrada"));
            
            // Validar que la respuesta pertenece a la encuesta
            if (!existingResponse.getSurvey().getId().equals(surveyId)) {
                throw new BadRequestException("La respuesta no pertenece a esta encuesta");
            }
            
            // Validar que el usuario puede editar (es el mismo usuario)
            if (!existingResponse.getUser().getId().equals(request.getUserId())) {
                throw new BadRequestException("No tienes permiso para editar esta respuesta");
            }
            
            // Validar que la encuesta esté activa y dentro del rango de fechas
            validateSurveyIsActive(survey);
            validateAllRequiredQuestionsAnswered(survey, request.getAnswers());
            
            // Decrementar votos de las opciones anteriores
            existingResponse.getAnswers().forEach(oldAnswer -> {
                if (oldAnswer.getSelectedOptionIds() != null) {
                    oldAnswer.getSelectedOptionIds().forEach(optionId -> {
                        SurveyOption option = optionRepository.findById(optionId).orElse(null);
                        if (option != null && option.getVotes() > 0) {
                            option.setVotes(option.getVotes() - 1);
                            optionRepository.save(option);
                        }
                    });
                }
            });
            
            // Eliminar respuestas anteriores
            existingResponse.getAnswers().clear();
            
            // Agregar nuevas respuestas
            for (SurveyAnswerRequest answerReq : request.getAnswers()) {
                SurveyQuestion question = survey.getQuestions().stream()
                        .filter(q -> q.getId().equals(answerReq.getQuestionId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada"));
                
                SurveyAnswer answer = createAnswerFromRequest(answerReq, question, existingResponse);
                existingResponse.getAnswers().add(answer);
                
                // Incrementar votos de las nuevas opciones
                if (answerReq.getSelectedOptionIds() != null) {
                    for (Long optionId : answerReq.getSelectedOptionIds()) {
                        SurveyOption option = optionRepository.findById(optionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Opción no encontrada"));
                        option.setVotes(option.getVotes() + 1);
                        optionRepository.save(option);
                    }
                }
            }
            
            SurveyResponse updatedResponse = responseRepository.save(existingResponse);
            
            // Inicializar propiedades lazy para el mapper
            if (updatedResponse.getSurvey() != null) {
                updatedResponse.getSurvey().getId();
                updatedResponse.getSurvey().getTitle();
            }
            if (updatedResponse.getUser() != null) {
                updatedResponse.getUser().getId();
            }
            if (updatedResponse.getResident() != null) {
                updatedResponse.getResident().getId();
            }
            if (updatedResponse.getAnswers() != null) {
                updatedResponse.getAnswers().forEach(answer -> {
                    if (answer.getQuestion() != null) {
                        answer.getQuestion().getId();
                        answer.getQuestion().getQuestion();
                    }
                    if (answer.getSelectedOptionIds() != null) {
                        answer.getSelectedOptionIds().size();
                    }
                });
            }
            
            log.info("Respuesta actualizada exitosamente: {}", responseId);
            return updatedResponse;
        } catch (ResourceNotFoundException | BadRequestException | SurveyValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar respuesta {}: {}", responseId, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar respuesta", e);
        }
    }

    @Transactional(readOnly = true)
    public CanEditResponseDto canEditResponse(Long surveyId, Long userId) {
        try {
            log.info("Verificando si usuario {} puede editar respuesta en encuesta {}", userId, surveyId);
            
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada"));
            
            // Validar status
            if (!SurveyStatus.ACTIVE.equals(survey.getStatus())) {
                return CanEditResponseDto.builder()
                        .canEdit(false)
                        .reason("La encuesta no está activa")
                        .build();
            }
            
            // Validar fechas
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(survey.getStartDate())) {
                return CanEditResponseDto.builder()
                        .canEdit(false)
                        .reason("La encuesta aún no ha comenzado")
                        .build();
            }
            
            if (now.isAfter(survey.getEndDate())) {
                return CanEditResponseDto.builder()
                        .canEdit(false)
                        .reason("La encuesta ha finalizado")
                        .build();
            }
            
            // Verificar que el usuario tenga una respuesta existente
            boolean hasResponse = responseRepository.existsBySurveyIdAndUserId(surveyId, userId);
            if (!hasResponse) {
                return CanEditResponseDto.builder()
                        .canEdit(false)
                        .reason("No tienes una respuesta registrada para editar")
                        .build();
            }
            
            return CanEditResponseDto.builder()
                    .canEdit(true)
                    .reason(null)
                    .build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al verificar permisos de edición: {}", e.getMessage(), e);
            throw new RuntimeException("Error al verificar permisos de edición", e);
        }
    }

    private void updateSurveyStatusBasedOnDates(Survey survey) {
        try {
            LocalDateTime now = LocalDateTime.now();
            SurveyStatus currentStatus = survey.getStatus();
            
            // Solo actualizar automáticamente si está en ACTIVE o DRAFT
            if (currentStatus == SurveyStatus.ACTIVE) {
                // Si la fecha de fin ha pasado, cambiar a CLOSED
                if (now.isAfter(survey.getEndDate())) {
                    log.info("Encuesta {} automáticamente cerrada (endDate pasado)", survey.getId());
                    survey.setStatus(SurveyStatus.CLOSED);
                    surveyRepository.save(survey);
                }
            } else if (currentStatus == SurveyStatus.DRAFT) {
                // Si está en draft y la fecha de inicio ha pasado, podría cambiar a SCHEDULED
                // pero solo si no se ha activado manualmente
                if (now.isBefore(survey.getStartDate())) {
                    // La encuesta está programada para el futuro
                    log.debug("Encuesta {} programada para: {}", survey.getId(), survey.getStartDate());
                }
            }
        } catch (Exception e) {
            log.warn("Error al actualizar estado automático de encuesta {}: {}", survey.getId(), e.getMessage());
        }
    }
}
