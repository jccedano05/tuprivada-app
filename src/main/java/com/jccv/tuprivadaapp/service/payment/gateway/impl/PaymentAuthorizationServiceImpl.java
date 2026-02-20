package com.jccv.tuprivadaapp.service.payment.gateway.impl;

import com.jccv.tuprivadaapp.exception.ForbiddenException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import com.jccv.tuprivadaapp.repository.payment.PaymentRepository;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentTransactionRepository;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de autorización para recursos de pagos.
 * Centraliza toda la lógica de validación de permisos siguiendo principios SOLID.
 * 
 * Reglas de negocio:
 * - RESIDENT: Solo puede acceder a sus propios pagos
 * - ADMIN: Puede acceder a todos los pagos de su condominio
 * - SUPERADMIN: Acceso total sin restricciones
 * 
 * @author TuPrivada Development Team
 * @version 1.0
 * @since 2026-01-21
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentAuthorizationServiceImpl implements PaymentAuthorizationService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    
    @Override
    @Transactional(readOnly = true)
    public void validateUserCanAccessPayment(Long paymentId, User user) {
        if (user == null) {
            log.warn("[Security] Intento de acceso con usuario nulo a payment {}", paymentId);
            throw new ForbiddenException("Usuario no autenticado");
        }
        
        // Usar JOIN FETCH para cargar Payment con Resident y Condominium
        Payment payment = paymentRepository.findByIdWithResidentAndCondominium(paymentId)
                .orElseThrow(() -> {
                    log.warn("[Security] Payment {} no encontrado solicitado por usuario {}", 
                            paymentId, user.getId());
                    return new ResourceNotFoundException(
                            "No se encontró el pago con ID: " + paymentId);
                });
        
        // SUPERADMIN tiene acceso total
        if (user.getRole() == Role.SUPERADMIN) {
            log.debug("[Security] SUPERADMIN {} accediendo a payment {}", user.getId(), paymentId);
            return;
        }
        
        // ADMIN puede acceder a todos los pagos de su condominio
        // Si el ADMIN no tiene condominio asignado, puede acceder a todos los payments
        if (isAdmin(user)) {
            log.info("[Security] 🔍 Validando acceso ADMIN - Usuario: {}, Role: {}, Condominio Usuario: {}, Payment: {}", 
                    user.getId(), user.getRole(), 
                    user.getCondominium() != null ? user.getCondominium().getId() : "NULL",
                    paymentId);
            
            if (payment.getResident() != null && payment.getResident().getCondominium() != null) {
                log.info("[Security] 🔍 Condominio del Payment: {}", 
                        payment.getResident().getCondominium().getId());
            } else {
                log.warn("[Security] ⚠️ Payment {} no tiene resident o condominio asociado", paymentId);
            }
            
            // Si el admin no tiene condominio asignado, permitir acceso a todos
            if (user.getCondominium() == null) {
                log.info("[Security] ✅ ADMIN {} sin condominio asignado - acceso permitido a payment {}", 
                        user.getId(), paymentId);
                return;
            }
            
            // Si tiene condominio, validar que sea el mismo
            if (belongsToSameCondominium(user, payment)) {
                log.info("[Security] ✅ ADMIN {} autorizado para payment {} del mismo condominio", 
                        user.getId(), paymentId);
                return;
            } else {
                log.error("[Security] ❌ ADMIN {} RECHAZADO para payment {} - Condominios diferentes", 
                        user.getId(), paymentId);
                throw new ForbiddenException(
                        "No tienes permiso para acceder a pagos de otros condominios");
            }
        }
        
        // RESIDENT solo puede acceder a sus propios pagos
        if (isResident(user)) {
            if (isOwnerOfPayment(user, payment)) {
                log.debug("[Security] RESIDENT {} accediendo a su propio payment {}", 
                        user.getId(), paymentId);
                return;
            } else {
                log.warn("[Security] RESIDENT {} intentó acceder a payment {} de otro residente", 
                        user.getId(), paymentId);
                throw new ForbiddenException(
                        "No tienes permiso para acceder a pagos de otros residentes");
            }
        }
        
        // Rol no reconocido o sin permisos
        log.warn("[Security] Usuario {} con rol {} intentó acceder a payment {} sin permisos", 
                user.getId(), user.getRole(), paymentId);
        throw new ForbiddenException("No tienes permisos suficientes para acceder a este recurso");
    }
    
    @Override
    @Transactional(readOnly = true)
    public void validateUserCanAccessTransaction(String transactionReference, User user) {
        if (user == null) {
            log.warn("[Security] Intento de acceso con usuario nulo a transacción {}", 
                    transactionReference);
            throw new ForbiddenException("Usuario no autenticado");
        }
        
        PaymentTransaction transaction = transactionRepository
                .findByTransactionReference(transactionReference)
                .orElseThrow(() -> {
                    log.warn("[Security] Transacción {} no encontrada solicitada por usuario {}", 
                            transactionReference, user.getId());
                    return new ResourceNotFoundException(
                            "No se encontró la transacción con referencia: " + transactionReference);
                });
        
        // Delegar validación al Payment asociado
        if (transaction.getPayment() != null) {
            validateUserCanAccessPayment(transaction.getPayment().getId(), user);
        } else {
            log.error("[Security] Transacción {} sin Payment asociado", transactionReference);
            throw new ResourceNotFoundException(
                    "La transacción no tiene un pago asociado válido");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public void validateUserCanAccessPaymentTransactions(Long paymentId, User user) {
        // Misma lógica que validateUserCanAccessPayment
        // Si puede acceder al Payment, puede ver sus transacciones
        validateUserCanAccessPayment(paymentId, user);
        
        log.debug("[Security] Usuario {} autorizado para ver transacciones del payment {}", 
                user.getId(), paymentId);
    }
    
    @Override
    public boolean isAdmin(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole() == Role.ADMIN || user.getRole() == Role.SUPERADMIN;
    }
    
    @Override
    public boolean isResident(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole() == Role.RESIDENT;
    }
    
    @Override
    public boolean belongsToCondominium(User user, Long condominiumId) {
        if (user == null || condominiumId == null) {
            return false;
        }
        
        if (user.getCondominium() == null) {
            log.warn("[Security] Usuario {} no tiene condominio asignado", user.getId());
            return false;
        }
        
        return user.getCondominium().getId().equals(condominiumId);
    }
    
    /**
     * Verifica si el usuario pertenece al mismo condominio que el pago.
     * 
     * @param user Usuario a verificar
     * @param payment Pago a comparar
     * @return true si pertenecen al mismo condominio
     */
    private boolean belongsToSameCondominium(User user, Payment payment) {
        log.info("[Security] 🔍 belongsToSameCondominium - Verificando...");
        log.info("[Security] 🔍 User ID: {}, User.getCondominium(): {}", 
                user.getId(), user.getCondominium());
        
        if (user.getCondominium() == null) {
            log.error("[Security] ❌ Usuario {} no tiene condominio asignado (NULL)", user.getId());
            return false;
        }
        
        log.info("[Security] 🔍 Payment ID: {}, Payment.getResident(): {}", 
                payment.getId(), payment.getResident());
        
        if (payment.getResident() == null) {
            log.error("[Security] ❌ Payment {} no tiene resident asociado (NULL)", payment.getId());
            return false;
        }
        
        log.info("[Security] 🔍 Resident.getCondominium(): {}", 
                payment.getResident().getCondominium());
        
        if (payment.getResident().getCondominium() == null) {
            log.error("[Security] ❌ Resident del payment {} no tiene condominio (NULL)", payment.getId());
            return false;
        }
        
        Long userCondominiumId = user.getCondominium().getId();
        Long paymentCondominiumId = payment.getResident().getCondominium().getId();
        
        log.info("[Security] 🔍 Comparando condominios - User: {}, Payment: {}", 
                userCondominiumId, paymentCondominiumId);
        
        boolean result = userCondominiumId.equals(paymentCondominiumId);
        
        if (result) {
            log.info("[Security] ✅ Condominios coinciden: {} == {}", userCondominiumId, paymentCondominiumId);
        } else {
            log.error("[Security] ❌ Condominios NO coinciden: {} != {}", userCondominiumId, paymentCondominiumId);
        }
        
        return result;
    }
    
    /**
     * Verifica si el usuario es el dueño del pago.
     * Un residente es dueño si el pago pertenece a su cuenta.
     * 
     * @param user Usuario a verificar
     * @param payment Pago a comparar
     * @return true si el usuario es el dueño
     */
    private boolean isOwnerOfPayment(User user, Payment payment) {
        if (payment.getResident() == null || 
            payment.getResident().getUser() == null) {
            log.warn("[Security] Payment {} sin residente o usuario asociado", payment.getId());
            return false;
        }
        
        Long paymentOwnerId = payment.getResident().getUser().getId();
        return user.getId().equals(paymentOwnerId);
    }
}
