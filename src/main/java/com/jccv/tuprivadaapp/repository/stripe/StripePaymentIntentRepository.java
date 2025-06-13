package com.jccv.tuprivadaapp.repository.stripe;

import com.jccv.tuprivadaapp.model.payment.StripePaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StripePaymentIntentRepository extends JpaRepository<StripePaymentIntent, Long> {
    /**
     * Busca un StripePaymentIntent por su ID de Stripe (paymentIntentId).
     *
     * @param paymentIntentId el ID del PaymentIntent en Stripe (p.ej. "pi_123456789")
     * @return un Optional conteniendo el StripePaymentIntent si existe, o vacío si no
     */
    Optional<StripePaymentIntent> findByPaymentIntentId(String paymentIntentId);


    /**
     * Busca el primer StripePaymentIntent para un dado payment.id cuyo status esté en la lista proporcionada.
     *
     * @param paymentId   el ID de la entidad Payment
     * @param statuses    lista de estados de PaymentIntent a considerar (p.ej. "requires_payment_method", "processing", etc.)
     * @return Optional con el primer StripePaymentIntent que coincida, o vacío si no existe ninguno en esos estados
     */
    Optional<StripePaymentIntent> findFirstByPayment_IdAndStatusIn(Long paymentId, List<String> statuses);

}
