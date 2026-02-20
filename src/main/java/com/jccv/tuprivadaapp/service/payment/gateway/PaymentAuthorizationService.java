package com.jccv.tuprivadaapp.service.payment.gateway;

import com.jccv.tuprivadaapp.model.User;

/**
 * Servicio de autorización centralizado para validar acceso a recursos de pagos.
 * Implementa reglas de negocio de seguridad:
 * - Los residentes solo pueden acceder a sus propios pagos
 * - Los administradores pueden acceder a todos los pagos de su condominio
 * - Los super administradores tienen acceso total
 * 
 * @author TuPrivada Development Team
 * @version 1.0
 * @since 2026-01-21
 */
public interface PaymentAuthorizationService {
    
    /**
     * Valida si el usuario autenticado tiene permiso para acceder a un Payment específico.
     * 
     * @param paymentId ID del Payment a validar
     * @param user Usuario autenticado que intenta acceder
     * @throws com.jccv.tuprivadaapp.exception.ResourceNotFoundException si el Payment no existe
     * @throws com.jccv.tuprivadaapp.exception.ForbiddenException si el usuario no tiene permiso
     */
    void validateUserCanAccessPayment(Long paymentId, User user);
    
    /**
     * Valida si el usuario autenticado tiene permiso para acceder a una PaymentTransaction.
     * 
     * @param transactionReference Referencia única de la transacción
     * @param user Usuario autenticado que intenta acceder
     * @throws com.jccv.tuprivadaapp.exception.ResourceNotFoundException si la transacción no existe
     * @throws com.jccv.tuprivadaapp.exception.ForbiddenException si el usuario no tiene permiso
     */
    void validateUserCanAccessTransaction(String transactionReference, User user);
    
    /**
     * Valida si el usuario autenticado tiene permiso para acceder a transacciones de un Payment.
     * 
     * @param paymentId ID del Payment cuyas transacciones se quieren consultar
     * @param user Usuario autenticado que intenta acceder
     * @throws com.jccv.tuprivadaapp.exception.ResourceNotFoundException si el Payment no existe
     * @throws com.jccv.tuprivadaapp.exception.ForbiddenException si el usuario no tiene permiso
     */
    void validateUserCanAccessPaymentTransactions(Long paymentId, User user);
    
    /**
     * Verifica si el usuario es administrador del sistema.
     * 
     * @param user Usuario a verificar
     * @return true si es ADMIN o SUPERADMIN, false en caso contrario
     */
    boolean isAdmin(User user);
    
    /**
     * Verifica si el usuario es residente.
     * 
     * @param user Usuario a verificar
     * @return true si es RESIDENT, false en caso contrario
     */
    boolean isResident(User user);
    
    /**
     * Verifica si el usuario pertenece al condominio especificado.
     * 
     * @param user Usuario a verificar
     * @param condominiumId ID del condominio
     * @return true si el usuario pertenece al condominio, false en caso contrario
     */
    boolean belongsToCondominium(User user, Long condominiumId);
}
