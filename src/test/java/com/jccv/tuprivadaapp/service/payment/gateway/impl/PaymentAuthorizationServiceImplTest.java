package com.jccv.tuprivadaapp.service.payment.gateway.impl;

import com.jccv.tuprivadaapp.exception.ForbiddenException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import com.jccv.tuprivadaapp.repository.payment.PaymentRepository;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PaymentAuthorizationServiceImpl.
 * Verifica las reglas de seguridad y autorización de acceso a pagos.
 * 
 * @author TuPrivada Development Team
 * @version 1.0
 * @since 2026-01-21
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentAuthorizationService - Tests de Seguridad")
class PaymentAuthorizationServiceImplTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private PaymentTransactionRepository transactionRepository;
    
    @InjectMocks
    private PaymentAuthorizationServiceImpl authorizationService;
    
    private Condominium condominium;
    private User residentUser;
    private User adminUser;
    private User superAdminUser;
    private User otherResidentUser;
    private Resident resident;
    private Resident otherResident;
    private Payment payment;
    private PaymentTransaction transaction;
    
    @BeforeEach
    void setUp() {
        // Crear condominio
        condominium = new Condominium();
        condominium.setId(1L);
        condominium.setName("Condominio Test");
        
        // Crear usuarios
        residentUser = User.builder()
                .id(1L)
                .email("resident@test.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.RESIDENT)
                .condominium(condominium)
                .build();
        
        adminUser = User.builder()
                .id(2L)
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .condominium(condominium)
                .build();
        
        superAdminUser = User.builder()
                .id(3L)
                .email("superadmin@test.com")
                .firstName("Super")
                .lastName("Admin")
                .role(Role.SUPERADMIN)
                .condominium(condominium)
                .build();
        
        otherResidentUser = User.builder()
                .id(4L)
                .email("other@test.com")
                .firstName("Other")
                .lastName("Resident")
                .role(Role.RESIDENT)
                .condominium(condominium)
                .build();
        
        // Crear residentes
        resident = new Resident();
        resident.setId(1L);
        resident.setUser(residentUser);
        resident.setCondominium(condominium);
        
        otherResident = new Resident();
        otherResident.setId(2L);
        otherResident.setUser(otherResidentUser);
        otherResident.setCondominium(condominium);
        
        // Crear payment
        payment = Payment.builder()
                .id(100L)
                .resident(resident)
                .isPaid(false)
                .build();
        
        // Crear transacción
        transaction = PaymentTransaction.builder()
                .id(1L)
                .transactionReference("TXN-TEST-001")
                .payment(payment)
                .build();
    }
    
    @Test
    @DisplayName("Residente puede acceder a su propio pago")
    void testResidentCanAccessOwnPayment() {
        // Given
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        
        // When & Then
        assertDoesNotThrow(() -> 
            authorizationService.validateUserCanAccessPayment(100L, residentUser));
        
        verify(paymentRepository).findById(100L);
    }
    
    @Test
    @DisplayName("Residente NO puede acceder al pago de otro residente")
    void testResidentCannotAccessOtherResidentPayment() {
        // Given
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        
        // When & Then
        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
            authorizationService.validateUserCanAccessPayment(100L, otherResidentUser));
        
        assertTrue(exception.getMessage().contains("No tienes permiso"));
        verify(paymentRepository).findById(100L);
    }
    
    @Test
    @DisplayName("Admin puede acceder a pagos del mismo condominio")
    void testAdminCanAccessPaymentInSameCondominium() {
        // Given
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        
        // When & Then
        assertDoesNotThrow(() ->
            authorizationService.validateUserCanAccessPayment(100L, adminUser));
        
        verify(paymentRepository).findById(100L);
    }
    
    @Test
    @DisplayName("Admin NO puede acceder a pagos de otro condominio")
    void testAdminCannotAccessPaymentInDifferentCondominium() {
        // Given
        Condominium otherCondominium = new Condominium();
        otherCondominium.setId(2L);
        
        User adminOtherCondo = User.builder()
                .id(5L)
                .role(Role.ADMIN)
                .condominium(otherCondominium)
                .build();
        
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        
        // When & Then
        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
            authorizationService.validateUserCanAccessPayment(100L, adminOtherCondo));
        
        assertTrue(exception.getMessage().contains("otros condominios"));
        verify(paymentRepository).findById(100L);
    }
    
    @Test
    @DisplayName("SuperAdmin puede acceder a cualquier pago")
    void testSuperAdminCanAccessAnyPayment() {
        // Given
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        
        // When & Then
        assertDoesNotThrow(() ->
            authorizationService.validateUserCanAccessPayment(100L, superAdminUser));
        
        verify(paymentRepository).findById(100L);
    }
    
    @Test
    @DisplayName("Lanza excepción si el payment no existe")
    void testThrowsExceptionWhenPaymentNotFound() {
        // Given
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            authorizationService.validateUserCanAccessPayment(999L, residentUser));
        
        assertTrue(exception.getMessage().contains("No se encontró el pago"));
        verify(paymentRepository).findById(999L);
    }
    
    @Test
    @DisplayName("Lanza excepción si el usuario es null")
    void testThrowsExceptionWhenUserIsNull() {
        // When & Then
        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
            authorizationService.validateUserCanAccessPayment(100L, null));
        
        assertTrue(exception.getMessage().contains("no autenticado"));
        verify(paymentRepository, never()).findById(anyLong());
    }
    
    @Test
    @DisplayName("Valida acceso a transacción correctamente")
    void testValidateUserCanAccessTransaction() {
        // Given
        when(transactionRepository.findByTransactionReference("TXN-TEST-001"))
                .thenReturn(Optional.of(transaction));
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        
        // When & Then
        assertDoesNotThrow(() ->
            authorizationService.validateUserCanAccessTransaction("TXN-TEST-001", residentUser));
        
        verify(transactionRepository).findByTransactionReference("TXN-TEST-001");
        verify(paymentRepository).findById(100L);
    }
    
    @Test
    @DisplayName("Lanza excepción si la transacción no existe")
    void testThrowsExceptionWhenTransactionNotFound() {
        // Given
        when(transactionRepository.findByTransactionReference(anyString()))
                .thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            authorizationService.validateUserCanAccessTransaction("TXN-INVALID", residentUser));
        
        assertTrue(exception.getMessage().contains("No se encontró la transacción"));
        verify(transactionRepository).findByTransactionReference("TXN-INVALID");
    }
    
    @Test
    @DisplayName("isAdmin retorna true para ADMIN")
    void testIsAdminReturnsTrueForAdmin() {
        assertTrue(authorizationService.isAdmin(adminUser));
    }
    
    @Test
    @DisplayName("isAdmin retorna true para SUPERADMIN")
    void testIsAdminReturnsTrueForSuperAdmin() {
        assertTrue(authorizationService.isAdmin(superAdminUser));
    }
    
    @Test
    @DisplayName("isAdmin retorna false para RESIDENT")
    void testIsAdminReturnsFalseForResident() {
        assertFalse(authorizationService.isAdmin(residentUser));
    }
    
    @Test
    @DisplayName("isResident retorna true para RESIDENT")
    void testIsResidentReturnsTrueForResident() {
        assertTrue(authorizationService.isResident(residentUser));
    }
    
    @Test
    @DisplayName("isResident retorna false para ADMIN")
    void testIsResidentReturnsFalseForAdmin() {
        assertFalse(authorizationService.isResident(adminUser));
    }
    
    @Test
    @DisplayName("belongsToCondominium valida correctamente")
    void testBelongsToCondominiumValidatesCorrectly() {
        assertTrue(authorizationService.belongsToCondominium(residentUser, 1L));
        assertFalse(authorizationService.belongsToCondominium(residentUser, 2L));
    }
    
    @Test
    @DisplayName("belongsToCondominium retorna false si usuario sin condominio")
    void testBelongsToCondominiumReturnsFalseWhenUserHasNoCondominium() {
        User userWithoutCondo = User.builder()
                .id(99L)
                .role(Role.RESIDENT)
                .condominium(null)
                .build();
        
        assertFalse(authorizationService.belongsToCondominium(userWithoutCondo, 1L));
    }
}
