package com.jccv.tuprivadaapp.controller.survey;

import com.jccv.tuprivadaapp.dto.survey.*;
import com.jccv.tuprivadaapp.dto.survey.mapper.SurveyMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.exception.SurveyAlreadyVotedException;
import com.jccv.tuprivadaapp.exception.SurveyValidationException;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.survey.Survey;
import com.jccv.tuprivadaapp.model.survey.SurveyResponse;
import com.jccv.tuprivadaapp.model.survey.SurveyStatus;
import com.jccv.tuprivadaapp.service.survey.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Slf4j
public class SurveyController {

    private final SurveyService surveyService;
    private final SurveyMapper surveyMapper;

    @GetMapping
    public ResponseEntity<List<SurveyDto>> getAllSurveys(
            @RequestParam Long condominiumId,
            @RequestParam(required = false) SurveyStatus status) {
        try {
            User currentUser = getCurrentUser();
            boolean isAdmin = isAdmin(currentUser);
            
            log.info("GET /api/surveys - condominiumId: {}, status: {}, isAdmin: {}", condominiumId, status, isAdmin);
            List<Survey> surveys = surveyService.getAllSurveysByCondominium(condominiumId, status, isAdmin);
            List<SurveyDto> surveyDtos = surveys.stream()
                    .map(surveyMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(surveyDtos);
        } catch (Exception e) {
            log.error("Error al obtener encuestas: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<SurveyDto>> getActiveSurveys(@RequestParam Long condominiumId) {
        try {
            log.info("GET /api/surveys/active - condominiumId: {}", condominiumId);
            List<Survey> surveys = surveyService.getActiveSurveys(condominiumId);
            List<SurveyDto> surveyDtos = surveys.stream()
                    .map(surveyMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(surveyDtos);
        } catch (Exception e) {
            log.error("Error al obtener encuestas activas: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyDto> getSurveyById(@PathVariable Long surveyId) {
        try {
            log.info("GET /api/surveys/{}", surveyId);
            Survey survey = surveyService.getSurveyById(surveyId);
            SurveyDto surveyDto = surveyMapper.toDto(survey);
            return ResponseEntity.ok(surveyDto);
        } catch (ResourceNotFoundException e) {
            log.error("Encuesta no encontrada: {}", surveyId);
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener encuesta {}: {}", surveyId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<SurveyDto> createSurvey(@Valid @RequestBody CreateSurveyRequest request) {
        try {
            User currentUser = getCurrentUser();
            
            if (!isAdmin(currentUser)) {
                log.warn("Usuario sin permisos intentó crear encuesta: {}", currentUser.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            log.info("POST /api/surveys - Creating survey: {} by user: {}", request.getTitle(), currentUser.getId());
            Survey survey = surveyService.createSurvey(request, currentUser.getId());
            SurveyDto surveyDto = surveyMapper.toDto(survey);
            return ResponseEntity.status(HttpStatus.CREATED).body(surveyDto);
        } catch (ResourceNotFoundException | SurveyValidationException e) {
            log.error("Error de validación al crear encuesta: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al crear encuesta: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear encuesta", e);
        }
    }

    @PutMapping("/{surveyId}")
    public ResponseEntity<SurveyDto> updateSurvey(
            @PathVariable Long surveyId,
            @Valid @RequestBody UpdateSurveyRequest request) {
        try {
            User currentUser = getCurrentUser();
            
            if (!isAdmin(currentUser)) {
                log.warn("Usuario sin permisos intentó actualizar encuesta: {}", currentUser.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            log.info("PUT /api/surveys/{} by user: {}", surveyId, currentUser.getId());
            Survey survey = surveyService.updateSurvey(surveyId, request, currentUser.getId());
            SurveyDto surveyDto = surveyMapper.toDto(survey);
            return ResponseEntity.ok(surveyDto);
        } catch (ResourceNotFoundException | BadRequestException | SurveyValidationException e) {
            log.error("Error al actualizar encuesta {}: {}", surveyId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar encuesta", e);
        }
    }

    @DeleteMapping("/{surveyId}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable Long surveyId) {
        try {
            User currentUser = getCurrentUser();
            
            if (!isAdmin(currentUser)) {
                log.warn("Usuario sin permisos intentó eliminar encuesta: {}", currentUser.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            log.info("DELETE /api/surveys/{} by user: {}", surveyId, currentUser.getId());
            surveyService.deleteSurvey(surveyId, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.error("Encuesta no encontrada para eliminar: {}", surveyId);
            throw e;
        } catch (Exception e) {
            log.error("Error al eliminar encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar encuesta", e);
        }
    }

    @PostMapping("/{surveyId}/responses")
    public ResponseEntity<SurveyResponseDto> submitResponse(
            @PathVariable Long surveyId,
            @Valid @RequestBody SubmitSurveyResponseRequest request) {
        try {
            log.info("POST /api/surveys/{}/responses by user: {}", surveyId, request.getUserId());
            SurveyResponse response = surveyService.submitResponse(surveyId, request);
            Survey survey = surveyService.getSurveyById(surveyId);
            SurveyResponseDto responseDto = surveyMapper.toResponseDto(response, !survey.getIsAnonymous());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (ResourceNotFoundException | SurveyValidationException | SurveyAlreadyVotedException e) {
            log.error("Error al enviar respuesta: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al enviar respuesta para encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al enviar respuesta", e);
        }
    }

    @PutMapping("/{surveyId}/responses/{responseId}")
    public ResponseEntity<SurveyResponseDto> updateResponse(
            @PathVariable Long surveyId,
            @PathVariable Long responseId,
            @Valid @RequestBody UpdateSurveyResponseRequest request) {
        try {
            log.info("PUT /api/surveys/{}/responses/{} by user: {}", surveyId, responseId, request.getUserId());
            SurveyResponse response = surveyService.updateResponse(surveyId, responseId, request);
            Survey survey = surveyService.getSurveyById(surveyId);
            SurveyResponseDto responseDto = surveyMapper.toResponseDto(response, !survey.getIsAnonymous());
            return ResponseEntity.ok(responseDto);
        } catch (ResourceNotFoundException | BadRequestException | SurveyValidationException e) {
            log.error("Error al actualizar respuesta: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar respuesta {} para encuesta {}: {}", responseId, surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar respuesta", e);
        }
    }

    @PutMapping("/{surveyId}/responses")
    public ResponseEntity<SurveyResponseDto> updateResponseByUser(
            @PathVariable Long surveyId,
            @Valid @RequestBody UpdateSurveyResponseRequest request) {
        try {
            log.info("PUT /api/surveys/{}/responses by user: {}", surveyId, request.getUserId());
            SurveyResponse response = surveyService.updateResponseByUser(surveyId, request);
            Survey survey = surveyService.getSurveyById(surveyId);
            SurveyResponseDto responseDto = surveyMapper.toResponseDto(response, !survey.getIsAnonymous());
            return ResponseEntity.ok(responseDto);
        } catch (ResourceNotFoundException | BadRequestException | SurveyValidationException e) {
            log.error("Error al actualizar respuesta: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar respuesta para encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar respuesta", e);
        }
    }

    @GetMapping("/{surveyId}/can-edit-response")
    public ResponseEntity<CanEditResponseDto> canEditResponse(
            @PathVariable Long surveyId,
            @RequestParam Long userId) {
        try {
            log.info("GET /api/surveys/{}/can-edit-response - userId: {}", surveyId, userId);
            CanEditResponseDto result = surveyService.canEditResponse(surveyId, userId);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            log.error("Encuesta no encontrada: {}", surveyId);
            throw e;
        } catch (Exception e) {
            log.error("Error al verificar permisos de edición: {}", e.getMessage(), e);
            throw new RuntimeException("Error al verificar permisos de edición", e);
        }
    }

    @GetMapping("/{surveyId}/results")
    public ResponseEntity<SurveyResultsDto> getSurveyResults(@PathVariable Long surveyId) {
        try {
            log.info("GET /api/surveys/{}/results", surveyId);
            SurveyResultsDto results = surveyService.getSurveyResults(surveyId);
            return ResponseEntity.ok(results);
        } catch (ResourceNotFoundException e) {
            log.error("Encuesta no encontrada para resultados: {}", surveyId);
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener resultados de encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener resultados", e);
        }
    }

    @GetMapping("/{surveyId}/check-voted")
    public ResponseEntity<CheckVotedResponse> checkIfUserVoted(
            @PathVariable Long surveyId,
            @RequestParam Long userId) {
        try {
            log.info("GET /api/surveys/{}/check-voted - userId: {}", surveyId, userId);
            boolean hasVoted = surveyService.hasUserVoted(surveyId, userId);
            CheckVotedResponse response = CheckVotedResponse.builder()
                    .hasVoted(hasVoted)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al verificar voto: {}", e.getMessage(), e);
            throw new RuntimeException("Error al verificar voto", e);
        }
    }

    @GetMapping("/users/{userId}/responses")
    public ResponseEntity<List<SurveyResponseDto>> getUserResponses(@PathVariable Long userId) {
        try {
            log.info("GET /api/surveys/users/{}/responses", userId);
            List<SurveyResponse> responses = surveyService.getUserResponses(userId);
            List<SurveyResponseDto> responseDtos = responses.stream()
                    .map(response -> surveyMapper.toResponseDto(response, true))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDtos);
        } catch (Exception e) {
            log.error("Error al obtener respuestas del usuario {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener respuestas", e);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new BadRequestException("Usuario no autenticado");
    }

    private boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.SUPERADMIN;
    }

    @PatchMapping("/{surveyId}/status")
    public ResponseEntity<SurveyDto> updateSurveyStatus(
            @PathVariable Long surveyId,
            @Valid @RequestBody UpdateStatusRequest request) {
        try {
            User currentUser = getCurrentUser();
            
            if (!isAdmin(currentUser)) {
                log.warn("Usuario sin permisos intentó cambiar estado de encuesta: {}", currentUser.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            log.info("PATCH /api/surveys/{}/status - Cambiando estado a {} por user: {}", 
                    surveyId, request.getStatus(), currentUser.getId());
            
            Survey survey = surveyService.updateSurveyStatus(surveyId, request.getStatus(), currentUser.getId());
            SurveyDto surveyDto = surveyMapper.toDto(survey);
            return ResponseEntity.ok(surveyDto);
        } catch (ResourceNotFoundException | BadRequestException | SurveyValidationException e) {
            log.error("Error al actualizar estado de encuesta {}: {}", surveyId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar estado de encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar estado de encuesta", e);
        }
    }

    @GetMapping("/{surveyId}/responses")
    public ResponseEntity<List<?>> getSurveyResponses(@PathVariable Long surveyId) {
        try {
            User currentUser = getCurrentUser();
            
            if (!isAdmin(currentUser)) {
                log.warn("Usuario sin permisos intentó ver respuestas de encuesta: {}", currentUser.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            log.info("GET /api/surveys/{}/responses by admin user: {}", surveyId, currentUser.getId());
            
            Survey survey = surveyService.getSurveyById(surveyId);
            List<SurveyResponse> responses = surveyService.getSurveyResponses(surveyId);
            
            if (survey.getIsAnonymous()) {
                List<SurveyResponseDto> anonymousResponses = responses.stream()
                        .map(response -> surveyMapper.toResponseDto(response, false))
                        .collect(Collectors.toList());
                return ResponseEntity.ok(anonymousResponses);
            } else {
                List<SurveyResponseWithUserDto> responsesWithUser = responses.stream()
                        .map(surveyMapper::toResponseWithUserDto)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(responsesWithUser);
            }
        } catch (ResourceNotFoundException e) {
            log.error("Encuesta no encontrada: {}", surveyId);
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener respuestas de encuesta {}: {}", surveyId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener respuestas", e);
        }
    }

    @GetMapping("/condominiums/{condominiumId}/stats")
    public ResponseEntity<SurveyStatsDto> getCondominiumStats(@PathVariable Long condominiumId) {
        try {
            User currentUser = getCurrentUser();
            
            if (!isAdmin(currentUser)) {
                log.warn("Usuario sin permisos intentó ver estadísticas: {}", currentUser.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            log.info("GET /api/surveys/condominiums/{}/stats by admin user: {}", 
                    condominiumId, currentUser.getId());
            
            SurveyStatsDto stats = surveyService.getCondominiumStats(condominiumId);
            return ResponseEntity.ok(stats);
        } catch (ResourceNotFoundException e) {
            log.error("Condominio no encontrado: {}", condominiumId);
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener estadísticas del condominio {}: {}", condominiumId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener estadísticas", e);
        }
    }
}
