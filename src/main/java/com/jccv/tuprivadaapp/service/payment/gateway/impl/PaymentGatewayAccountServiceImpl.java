package com.jccv.tuprivadaapp.service.payment.gateway.impl;

import com.jccv.tuprivadaapp.dto.payment.gateway.*;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentGatewayAccountRepository;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayAccountService;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayRegistry;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayStrategy;
import com.jccv.tuprivadaapp.service.security.SensitiveDataEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayAccountServiceImpl implements PaymentGatewayAccountService {

    private final PaymentGatewayAccountRepository gatewayAccountRepository;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final SensitiveDataEncryptionService encryptionService;
    private final CondominiumRepository condominiumRepository;
    
    private static final String METADATA_API_KEY = "encrypted_api_key";
    private static final String METADATA_SECRET_KEY = "encrypted_secret_key";

    @Override
    @Transactional
    public PaymentGatewayAccount createOrUpdateAccount(Long condominiumId, PaymentGatewayAccount account) {
        log.info("[Payments] Guardando cuenta de pasarela {} para condominio {}", account.getProvider(), condominiumId);
        account.setCondominium(account.getCondominium());
        account.setUpdatedAt(LocalDateTime.now());
        if (account.getId() == null) {
            account.setCreatedAt(LocalDateTime.now());
        }
        return gatewayAccountRepository.save(account);
    }

    @Override
    @Transactional
    public PaymentGatewayAccount activateAccount(Long accountId) {
        PaymentGatewayAccount account = findAccountById(accountId);
        account.setActive(true);
        account.setActivatedAt(LocalDateTime.now());
        account.setDeactivatedAt(null);
        log.info("[Payments] Cuenta {} activada para proveedor {}", accountId, account.getProvider());
        return gatewayAccountRepository.save(account);
    }

    @Override
    @Transactional
    public PaymentGatewayAccount deactivateAccount(Long accountId, String reason) {
        PaymentGatewayAccount account = findAccountById(accountId);
        account.setActive(false);
        account.setDeactivatedAt(LocalDateTime.now());
        log.warn("[Payments] Cuenta {} desactivada. Motivo: {}", accountId, reason);
        return gatewayAccountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentGatewayAccount getActiveAccount(Long condominiumId, PaymentGatewayAccount.PaymentProvider provider) {
        return gatewayAccountRepository.findByCondominiumIdAndProviderAndIsActiveTrue(condominiumId, provider)
                .orElseThrow(() -> new ResourceNotFoundException("No existe cuenta activa para el condominio " + condominiumId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentGatewayAccount getPreferredAccount(Long condominiumId) {
        return gatewayAccountRepository.findPreferredActiveAccount(condominiumId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay cuentas activas para el condominio " + condominiumId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentGatewayAccount> getAccounts(Long condominiumId) {
        return gatewayAccountRepository.findByCondominiumId(condominiumId);
    }

    @Override
    @Transactional
    public AccountCreationResponse initiateAccountCreation(AccountCreationRequest request) throws com.jccv.tuprivadaapp.dto.payment.gateway.PaymentGatewayException {
        PaymentGatewayStrategy strategy = gatewayRegistry.getStrategy(PaymentGatewayAccount.PaymentProvider.CONEKTA);
        AccountCreationResponse response = strategy.createConnectedAccount(request);
        log.info("[Payments] Solicitud de creación de cuenta enviada a {} para condominio {}", strategy.getProvider(), request.getCondominiumId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountInfoResponse fetchAccountInfo(Long accountId) throws com.jccv.tuprivadaapp.dto.payment.gateway.PaymentGatewayException {
        PaymentGatewayAccount account = findAccountById(accountId);
        PaymentGatewayStrategy strategy = gatewayRegistry.getStrategy(account.getProvider());
        return strategy.getAccountInfo(account.getAccountId());
    }

    @Override
    public boolean isAccountReadyForPayments(Long condominiumId) {
        return gatewayAccountRepository.findPreferredActiveAccount(condominiumId)
                .map(acc -> acc.isActive() && acc.isVerified())
                .orElse(false);
    }

    private PaymentGatewayAccount findAccountById(Long accountId) {
        return gatewayAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de pasarela no encontrada con id: " + accountId));
    }
    
    // ==================== Métodos CRUD Profesionales ====================
    
    /**
     * Crea una nueva cuenta de pasarela con encriptación de credenciales.
     */
    @Transactional
    public GatewayAccountResponse createGatewayAccount(GatewayAccountCreateRequest request) {
        log.info("[GatewayAccounts] Creando cuenta {} para condominio {}", 
            request.getProvider(), request.getCondominiumId());
        
        // Validar que el condominio existe
        Condominium condominium = condominiumRepository.findById(request.getCondominiumId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Condominio no encontrado con ID: " + request.getCondominiumId()));
        
        // Validar que no existe cuenta duplicada activa para el mismo proveedor
        gatewayAccountRepository.findByCondominiumIdAndProviderAndIsActiveTrue(
            request.getCondominiumId(), request.getProvider()
        ).ifPresent(existing -> {
            throw new IllegalStateException(
                String.format("Ya existe una cuenta activa de %s para el condominio %s", 
                    request.getProvider(), condominium.getName()));
        });
        
        // Encriptar credenciales sensibles
        String encryptedApiKey = encryptionService.encrypt(request.getApiKey());
        String encryptedSecretKey = request.getSecretKey() != null ? 
            encryptionService.encrypt(request.getSecretKey()) : null;
        
        // Construir metadata con credenciales encriptadas
        Map<String, String> metadata = new HashMap<>();
        metadata.put(METADATA_API_KEY, encryptedApiKey);
        if (encryptedSecretKey != null) {
            metadata.put(METADATA_SECRET_KEY, encryptedSecretKey);
        }
        if (request.getMetadata() != null) {
            metadata.putAll(request.getMetadata());
        }
        
        // Crear entidad
        PaymentGatewayAccount account = PaymentGatewayAccount.builder()
            .condominium(condominium)
            .provider(request.getProvider())
            .accountId(request.getAccountId())
            .accountName(request.getAccountName())
            .accountEmail(request.getAccountEmail())
            .commissionPercentage(request.getCommissionPercentage())
            .fixedFee(request.getFixedFee())
            .metadata(metadata)
            .isActive(request.getActivateImmediately())
            .isVerified(false)
            .onboardingStatus(PaymentGatewayAccount.OnboardingStatus.PENDING)
            .build();
        
        if (request.getActivateImmediately()) {
            account.setActivatedAt(LocalDateTime.now());
        }
        
        PaymentGatewayAccount saved = gatewayAccountRepository.save(account);
        
        log.info("[GatewayAccounts] Cuenta {} creada con ID {} (activa: {})",
            saved.getProvider(), saved.getId(), saved.isActive());
        
        return mapToResponse(saved);
    }
    
    /**
     * Actualiza una cuenta existente (re-encripta credenciales si se proveen nuevas).
     */
    @Transactional
    public GatewayAccountResponse updateGatewayAccount(Long accountId, GatewayAccountUpdateRequest request) {
        log.info("[GatewayAccounts] Actualizando cuenta {}", accountId);
        
        PaymentGatewayAccount account = findAccountById(accountId);
        
        if (request.getAccountName() != null) {
            account.setAccountName(request.getAccountName());
        }
        if (request.getAccountEmail() != null) {
            account.setAccountEmail(request.getAccountEmail());
        }
        if (request.getCommissionPercentage() != null) {
            account.setCommissionPercentage(request.getCommissionPercentage());
        }
        if (request.getFixedFee() != null) {
            account.setFixedFee(request.getFixedFee());
        }
        
        // Re-encriptar API key si se provee una nueva
        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            String encryptedApiKey = encryptionService.encrypt(request.getApiKey());
            account.getMetadata().put(METADATA_API_KEY, encryptedApiKey);
            log.info("[GatewayAccounts] API key actualizada y re-encriptada para cuenta {}", accountId);
        }
        
        // Re-encriptar Secret key si se provee una nueva
        if (request.getSecretKey() != null && !request.getSecretKey().isEmpty()) {
            String encryptedSecretKey = encryptionService.encrypt(request.getSecretKey());
            account.getMetadata().put(METADATA_SECRET_KEY, encryptedSecretKey);
            log.info("[GatewayAccounts] Secret key actualizada y re-encriptada para cuenta {}", accountId);
        }
        
        // Mergear metadata adicional
        if (request.getMetadata() != null) {
            account.getMetadata().putAll(request.getMetadata());
        }
        
        account.setUpdatedAt(LocalDateTime.now());
        PaymentGatewayAccount updated = gatewayAccountRepository.save(account);
        
        log.info("[GatewayAccounts] Cuenta {} actualizada exitosamente", accountId);
        
        return mapToResponse(updated);
    }
    
    /**
     * Obtiene los detalles de una cuenta (sin exponer credenciales).
     */
    @Transactional(readOnly = true)
    public GatewayAccountResponse getGatewayAccount(Long accountId) {
        PaymentGatewayAccount account = findAccountById(accountId);
        return mapToResponse(account);
    }
    
    /**
     * Lista todas las cuentas de un condominio.
     */
    @Transactional(readOnly = true)
    public List<GatewayAccountResponse> listGatewayAccountsByCondominium(Long condominiumId) {
        List<PaymentGatewayAccount> accounts = gatewayAccountRepository.findByCondominiumId(condominiumId);
        return accounts.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Elimina (desactiva permanentemente) una cuenta.
     */
    @Transactional
    public void deleteGatewayAccount(Long accountId, String reason) {
        log.warn("[GatewayAccounts] Eliminando cuenta {} - Razón: {}", accountId, reason);
        
        PaymentGatewayAccount account = findAccountById(accountId);
        account.setActive(false);
        account.setDeactivatedAt(LocalDateTime.now());
        gatewayAccountRepository.save(account);
        
        log.info("[GatewayAccounts] Cuenta {} desactivada permanentemente", accountId);
    }
    
    /**
     * Obtiene la API key desencriptada (solo para uso interno del sistema).
     */
    public String getDecryptedApiKey(PaymentGatewayAccount account) {
        String encryptedKey = account.getMetadata().get(METADATA_API_KEY);
        if (encryptedKey == null) {
            throw new IllegalStateException("API key no encontrada para cuenta: " + account.getId());
        }
        return encryptionService.decrypt(encryptedKey);
    }
    
    /**
     * Obtiene la Secret key desencriptada (solo para uso interno del sistema).
     */
    public String getDecryptedSecretKey(PaymentGatewayAccount account) {
        String encryptedKey = account.getMetadata().get(METADATA_SECRET_KEY);
        if (encryptedKey == null) {
            return null;
        }
        return encryptionService.decrypt(encryptedKey);
    }
    
    // ==================== Mapeo a DTO ====================
    
    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public PaymentGatewayAccount getOrCreateDefaultAccount(Long condominiumId, PaymentGatewayAccount.PaymentProvider provider) {
        log.info("[GatewayAccounts] Obteniendo o creando cuenta por defecto para condominio {} y proveedor {}", 
                condominiumId, provider);
        
        try {
            // Generar accountId único
            String accountId = "default_" + provider.toString().toLowerCase() + "_" + condominiumId;
            
            // Buscar por accountId único primero
            Optional<PaymentGatewayAccount> existingByAccountId = gatewayAccountRepository.findByAccountId(accountId);
            if (existingByAccountId.isPresent()) {
                PaymentGatewayAccount account = existingByAccountId.get();
                log.info("[GatewayAccounts] Cuenta encontrada por accountId: {} con ID: {}", accountId, account.getId());
                if (!account.isActive()) {
                    account.setActive(true);
                    account.setActivatedAt(LocalDateTime.now());
                    account = gatewayAccountRepository.saveAndFlush(account);
                    log.info("[GatewayAccounts] Cuenta reactivada con ID: {}", account.getId());
                }
                return account;
            }
            
            // Buscar por condominio y provider
            Optional<PaymentGatewayAccount> existingByCondoProv = 
                    gatewayAccountRepository.findByCondominiumIdAndProviderAndIsActiveTrue(condominiumId, provider);
            if (existingByCondoProv.isPresent()) {
                PaymentGatewayAccount account = existingByCondoProv.get();
                log.info("[GatewayAccounts] Cuenta activa encontrada con ID: {}", account.getId());
                return account;
            }
            
            // Crear nueva cuenta
            Condominium condominium = condominiumRepository.findById(condominiumId)
                    .orElseThrow(() -> new ResourceNotFoundException("Condominio no encontrado: " + condominiumId));
            
            PaymentGatewayAccount newAccount = new PaymentGatewayAccount();
            newAccount.setCondominium(condominium);
            newAccount.setProvider(provider);
            newAccount.setAccountId(accountId);
            newAccount.setAccountName("Configuración por defecto - " + condominium.getName());
            newAccount.setAccountEmail("pagos@" + condominium.getName().toLowerCase().replace(" ", "") + ".com");
            newAccount.setActive(true);
            newAccount.setVerified(true);
            newAccount.setOnboardingStatus(PaymentGatewayAccount.OnboardingStatus.COMPLETED);
            newAccount.setCommissionPercentage(2.9);
            newAccount.setFixedFee(3.0);
            newAccount.setCreatedAt(LocalDateTime.now());
            newAccount.setActivatedAt(LocalDateTime.now());
            
            // Usar saveAndFlush para forzar commit inmediato y generación de ID
            PaymentGatewayAccount savedAccount = gatewayAccountRepository.saveAndFlush(newAccount);
            
            if (savedAccount.getId() == null) {
                log.error("[GatewayAccounts] CRÍTICO: Cuenta guardada no tiene ID después de saveAndFlush()");
                throw new IllegalStateException("No se pudo generar ID para cuenta de pasarela");
            }
            
            log.info("[GatewayAccounts] Cuenta por defecto creada con ID: {} y accountId: {}", 
                    savedAccount.getId(), savedAccount.getAccountId());
            return savedAccount;
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("[GatewayAccounts] Error de integridad de datos: {}", e.getMessage());
            
            // Si hay duplicate key, intentar buscar la cuenta que ya existe
            String accountId = "default_" + provider.toString().toLowerCase() + "_" + condominiumId;
            Optional<PaymentGatewayAccount> existing = gatewayAccountRepository.findByAccountId(accountId);
            if (existing.isPresent()) {
                log.warn("[GatewayAccounts] Cuenta ya existía, retornando la existente con ID: {}", existing.get().getId());
                return existing.get();
            }
            
            throw new IllegalStateException(
                "Error de base de datos al crear cuenta de pasarela. " +
                "Ejecuta: SELECT setval('payment_gateway_accounts_id_seq', " +
                "COALESCE((SELECT MAX(id) FROM payment_gateway_accounts), 0) + 1, false);", 
                e
            );
        } catch (Exception e) {
            log.error("[GatewayAccounts] Error inesperado: {}", e.getMessage(), e);
            throw new IllegalStateException(
                "Error al configurar cuenta de pasarela de pago: " + e.getMessage(), 
                e
            );
        }
    }
    
    private GatewayAccountResponse mapToResponse(PaymentGatewayAccount account) {
        // Obtener API key encriptada y enmascararla para visualización
        String encryptedApiKey = account.getMetadata().get(METADATA_API_KEY);
        String maskedApiKey = null;
        if (encryptedApiKey != null) {
            try {
                String decrypted = encryptionService.decrypt(encryptedApiKey);
                maskedApiKey = encryptionService.maskForLogging(decrypted, 4);
            } catch (Exception e) {
                log.error("[GatewayAccounts] Error enmascarando API key: {}", e.getMessage());
                maskedApiKey = "***error***";
            }
        }
        
        // Filtrar metadata para excluir datos encriptados en la respuesta pública
        Map<String, String> publicMetadata = account.getMetadata().entrySet().stream()
            .filter(entry -> !entry.getKey().startsWith("encrypted_"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        return GatewayAccountResponse.builder()
            .id(account.getId())
            .condominiumId(account.getCondominium().getId())
            .condominiumName(account.getCondominium().getName())
            .provider(account.getProvider())
            .accountId(account.getAccountId())
            .accountName(account.getAccountName())
            .accountEmail(account.getAccountEmail())
            .apiKeyMasked(maskedApiKey)
            .isActive(account.isActive())
            .isVerified(account.isVerified())
            .onboardingStatus(account.getOnboardingStatus())
            .commissionPercentage(account.getCommissionPercentage())
            .fixedFee(account.getFixedFee())
            .publicMetadata(publicMetadata)
            .createdAt(account.getCreatedAt())
            .updatedAt(account.getUpdatedAt())
            .activatedAt(account.getActivatedAt())
            .deactivatedAt(account.getDeactivatedAt())
            .build();
    }
}
