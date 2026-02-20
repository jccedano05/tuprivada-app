package com.jccv.tuprivadaapp.repository.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de cuentas de pasarelas de pago
 */
@Repository
public interface PaymentGatewayAccountRepository extends JpaRepository<PaymentGatewayAccount, Long> {
    
    /**
     * Busca una cuenta activa por condominio y proveedor
     */
    Optional<PaymentGatewayAccount> findByCondominiumIdAndProviderAndIsActiveTrue(
            Long condominiumId, 
            PaymentGatewayAccount.PaymentProvider provider
    );
    
    /**
     * Busca todas las cuentas de un condominio
     */
    List<PaymentGatewayAccount> findByCondominiumId(Long condominiumId);
    
    /**
     * Busca todas las cuentas activas de un condominio
     */
    List<PaymentGatewayAccount> findByCondominiumIdAndIsActiveTrue(Long condominiumId);
    
    /**
     * Busca por ID de cuenta en el proveedor
     */
    Optional<PaymentGatewayAccount> findByAccountId(String accountId);
    
    /**
     * Verifica si existe una cuenta activa para un condominio
     */
    boolean existsByCondominiumIdAndIsActiveTrue(Long condominiumId);
    
    /**
     * Obtiene la cuenta preferida activa de un condominio
     */
    @Query("SELECT pga FROM PaymentGatewayAccount pga " +
           "WHERE pga.condominium.id = :condominiumId " +
           "AND pga.isActive = true " +
           "AND pga.isVerified = true " +
           "ORDER BY pga.createdAt DESC " +
           "LIMIT 1")
    Optional<PaymentGatewayAccount> findPreferredActiveAccount(@Param("condominiumId") Long condominiumId);
    
    /**
     * Busca cuentas que requieren acción
     */
    List<PaymentGatewayAccount> findByOnboardingStatus(
            PaymentGatewayAccount.OnboardingStatus status
    );
}
