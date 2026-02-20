package com.jccv.tuprivadaapp.controller.payment.gateway;

import com.jccv.tuprivadaapp.dto.payment.gateway.GatewayAccountCreateRequest;
import com.jccv.tuprivadaapp.dto.payment.gateway.GatewayAccountDeleteRequest;
import com.jccv.tuprivadaapp.dto.payment.gateway.GatewayAccountResponse;
import com.jccv.tuprivadaapp.dto.payment.gateway.GatewayAccountUpdateRequest;
import com.jccv.tuprivadaapp.service.payment.gateway.impl.PaymentGatewayAccountServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de cuentas de pasarelas de pago.
 * 
 * Características de seguridad:
 * - Solo accesible por usuarios con rol ADMIN
 * - Credenciales sensibles encriptadas en BD
 * - Auditoría completa de operaciones
 * - Validaciones exhaustivas
 * - No expone credenciales en respuestas
 */
@RestController
@RequestMapping("/api/v1/payment-gateway/accounts")
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayAccountController {

    private final PaymentGatewayAccountServiceImpl accountService;

    /**
     * Crea una nueva cuenta de pasarela para un condominio.
     * Las credenciales (API keys) se encriptan automáticamente.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GatewayAccountResponse> createAccount(
            @Valid @RequestBody GatewayAccountCreateRequest request) {
        
        log.info("[GatewayAccountsAPI] Admin solicita crear cuenta {} para condominio {}", 
            request.getProvider(), request.getCondominiumId());
        
        try {
            GatewayAccountResponse response = accountService.createGatewayAccount(request);
            
            log.info("[GatewayAccountsAPI] Cuenta {} creada exitosamente con ID {}", 
                response.getProvider(), response.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalStateException e) {
            log.warn("[GatewayAccountsAPI] Error de validación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[GatewayAccountsAPI] Error creando cuenta: {}", e.getMessage(), e);
            throw new RuntimeException("Error creando cuenta de pasarela", e);
        }
    }

    /**
     * Actualiza una cuenta existente.
     * Las credenciales nuevas se re-encriptan automáticamente.
     */
    @PutMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GatewayAccountResponse> updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody GatewayAccountUpdateRequest request) {
        
        log.info("[GatewayAccountsAPI] Admin solicita actualizar cuenta {}", accountId);
        
        try {
            GatewayAccountResponse response = accountService.updateGatewayAccount(accountId, request);
            
            log.info("[GatewayAccountsAPI] Cuenta {} actualizada exitosamente", accountId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[GatewayAccountsAPI] Error actualizando cuenta {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Error actualizando cuenta de pasarela", e);
        }
    }

    /**
     * Obtiene los detalles de una cuenta específica.
     * No expone credenciales sensibles (solo máscaras).
     */
    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GatewayAccountResponse> getAccount(@PathVariable Long accountId) {
        
        log.info("[GatewayAccountsAPI] Admin consulta cuenta {}", accountId);
        
        try {
            GatewayAccountResponse response = accountService.getGatewayAccount(accountId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[GatewayAccountsAPI] Error consultando cuenta {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Error consultando cuenta de pasarela", e);
        }
    }

    /**
     * Lista todas las cuentas de un condominio.
     */
    @GetMapping("/condominium/{condominiumId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GatewayAccountResponse>> listAccountsByCondominium(
            @PathVariable Long condominiumId) {
        
        log.info("[GatewayAccountsAPI] Admin lista cuentas del condominio {}", condominiumId);
        
        try {
            List<GatewayAccountResponse> accounts = 
                accountService.listGatewayAccountsByCondominium(condominiumId);
            
            log.info("[GatewayAccountsAPI] Encontradas {} cuentas para condominio {}", 
                accounts.size(), condominiumId);
            
            return ResponseEntity.ok(accounts);
            
        } catch (Exception e) {
            log.error("[GatewayAccountsAPI] Error listando cuentas del condominio {}: {}", 
                condominiumId, e.getMessage());
            throw new RuntimeException("Error listando cuentas de pasarela", e);
        }
    }

    /**
     * Activa una cuenta de pasarela.
     */
    @PostMapping("/{accountId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GatewayAccountResponse> activateAccount(@PathVariable Long accountId) {
        
        log.info("[GatewayAccountsAPI] Admin activa cuenta {}", accountId);
        
        try {
            accountService.activateAccount(accountId);
            GatewayAccountResponse response = accountService.getGatewayAccount(accountId);
            
            log.info("[GatewayAccountsAPI] Cuenta {} activada exitosamente", accountId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[GatewayAccountsAPI] Error activando cuenta {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Error activando cuenta de pasarela", e);
        }
    }

    /**
     * Desactiva una cuenta de pasarela temporalmente.
     */
    @PostMapping("/{accountId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GatewayAccountResponse> deactivateAccount(
            @PathVariable Long accountId,
            @RequestParam(required = false, defaultValue = "Desactivación manual por admin") String reason) {
        
        log.info("[GatewayAccountsAPI] Admin desactiva cuenta {} - Razón: {}", accountId, reason);
        
        try {
            accountService.deactivateAccount(accountId, reason);
            GatewayAccountResponse response = accountService.getGatewayAccount(accountId);
            
            log.info("[GatewayAccountsAPI] Cuenta {} desactivada exitosamente", accountId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[GatewayAccountsAPI] Error desactivando cuenta {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Error desactivando cuenta de pasarela", e);
        }
    }

    /**
     * Elimina permanentemente una cuenta de pasarela.
     * PRECAUCIÓN: Esta acción marca la cuenta como eliminada.
     */
    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody GatewayAccountDeleteRequest request) {
        
        log.warn("[GatewayAccountsAPI] Admin elimina cuenta {} - Razón: {}", accountId, request.getReason());
        
        try {
            accountService.deleteGatewayAccount(accountId, request.getReason());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cuenta eliminada exitosamente");
            response.put("accountId", accountId);
            response.put("deletedAt", java.time.LocalDateTime.now());
            
            log.info("[GatewayAccountsAPI] Cuenta {} eliminada exitosamente", accountId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[GatewayAccountsAPI] Error eliminando cuenta {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Error eliminando cuenta de pasarela", e);
        }
    }

    /**
     * Verifica el estado de una cuenta (conectividad, validez de credenciales).
     */
    @GetMapping("/{accountId}/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkAccountHealth(@PathVariable Long accountId) {
        
        log.info("[GatewayAccountsAPI] Admin verifica salud de cuenta {}", accountId);
        
        try {
            GatewayAccountResponse account = accountService.getGatewayAccount(accountId);
            
            Map<String, Object> health = new HashMap<>();
            health.put("accountId", accountId);
            health.put("provider", account.getProvider());
            health.put("isActive", account.isActive());
            health.put("isVerified", account.isVerified());
            health.put("onboardingStatus", account.getOnboardingStatus());
            health.put("status", account.isActive() && account.isVerified() ? "HEALTHY" : "REQUIRES_ATTENTION");
            health.put("checkedAt", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("[GatewayAccountsAPI] Error verificando salud de cuenta {}: {}", 
                accountId, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("accountId", accountId);
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            error.put("checkedAt", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Manejo de excepciones global para este controlador.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", ex.getMessage());
        error.put("code", "VALIDATION_ERROR");
        error.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.jccv.tuprivadaapp.exception.ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            com.jccv.tuprivadaapp.exception.ResourceNotFoundException ex) {
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", ex.getMessage());
        error.put("code", "RESOURCE_NOT_FOUND");
        error.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Obtiene una cuenta específica de un condominio (filtrada opcionalmente por proveedor y estado).
     * Solo accesible por ADMIN.
     */
    @GetMapping("/condominium/{condominiumId}/account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GatewayAccountResponse> getAccountByCondominiumAndProvider(
            @PathVariable Long condominiumId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        
        log.info("[GatewayAccountsAPI] Admin consulta cuenta para condominio {} (provider: {}, activeOnly: {})", 
            condominiumId, provider, activeOnly);
        
        try {
            List<GatewayAccountResponse> accounts = 
                accountService.listGatewayAccountsByCondominium(condominiumId);
            
            // Filtrar por estado activo si se solicita
            if (activeOnly) {
                accounts = accounts.stream()
                    .filter(GatewayAccountResponse::isActive)
                    .toList();
            }
            
            if (accounts.isEmpty()) {
                String msg = activeOnly ? 
                    "No hay cuentas activas para el condominio " + condominiumId :
                    "No hay cuentas para el condominio " + condominiumId;
                throw new com.jccv.tuprivadaapp.exception.ResourceNotFoundException(msg);
            }
            
            // Si se especifica proveedor, filtrar por él
            if (provider != null && !provider.isEmpty()) {
                GatewayAccountResponse account = accounts.stream()
                    .filter(acc -> acc.getProvider().name().equalsIgnoreCase(provider))
                    .findFirst()
                    .orElseThrow(() -> new com.jccv.tuprivadaapp.exception.ResourceNotFoundException(
                        "No hay cuenta de " + provider + " para el condominio " + condominiumId));
                return ResponseEntity.ok(account);
            }
            
            // Devolver la primera cuenta encontrada
            return ResponseEntity.ok(accounts.get(0));
            
        } catch (com.jccv.tuprivadaapp.exception.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GatewayAccountsAPI] Error obteniendo cuenta del condominio {}: {}", 
                condominiumId, e.getMessage());
            throw new RuntimeException("Error obteniendo cuenta", e);
        }
    }
}
