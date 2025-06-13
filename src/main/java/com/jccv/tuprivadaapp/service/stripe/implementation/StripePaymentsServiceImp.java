package com.jccv.tuprivadaapp.service.stripe.implementation;

import com.jccv.tuprivadaapp.dto.payment.PaymentDto;
import com.jccv.tuprivadaapp.dto.payment.mapper.PaymentMapper;
import com.jccv.tuprivadaapp.dto.stripe.StripePaymentIntentResponse;
import com.jccv.tuprivadaapp.dto.stripe.StripePaymentRequest;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.payment.StripePaymentIntent;
import com.jccv.tuprivadaapp.repository.payment.PaymentRepository;
import com.jccv.tuprivadaapp.repository.stripe.StripePaymentIntentRepository;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.payment.DepositPaymentService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.stripe.StripePaymentsService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.net.RequestOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class StripePaymentsServiceImp implements StripePaymentsService {

    private final CondominiumService condominiumService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final StripePaymentIntentRepository stripePaymentIntentRepository;

    @Autowired
    public StripePaymentsServiceImp(CondominiumService condominiumService, PaymentService paymentService, StripePaymentIntentRepository stripePaymentIntentRepository, PaymentRepository paymentRepository) {
        this.condominiumService = condominiumService;
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.stripePaymentIntentRepository = stripePaymentIntentRepository;
    }


    @Override
    @Transactional
    public StripePaymentIntentResponse createPaymentCardIntent(StripePaymentRequest request) throws StripeException {

        Payment payment = paymentRepository.findById(request.getPaymentId()).orElseThrow(()-> new ResourceNotFoundException("Payment no encontrado con el id: " + request.getPaymentId()));

        if(payment.isPaid()){
            throw new BadRequestException("El payment ya esta marcado como pagado");
        }

        List<String> pendingStatuses = Arrays.asList(
                "requires_payment_method",
                "requires_confirmation",
                "requires_action",
                "processing",
                "requires_capture"
        );

        //Busca si ya hay payments intents pendientes para no crear mas
        Optional<StripePaymentIntent> existingIntent = stripePaymentIntentRepository
                .findFirstByPayment_IdAndStatusIn(request.getPaymentId(), pendingStatuses);

        if (existingIntent.isPresent()) {
            return StripePaymentIntentResponse.builder()
                    .clientSecret(existingIntent.get().getClientSecret())
                    .build();
        }

        Double amountToPay = paymentService.getRemainingAmountByPaymentId(request.getPaymentId());

        String connectedAccountId = condominiumService.findConnectedAccountIdByCondominiumId(request.getCondominiumId());

        // Variables de comisiones
        double stripePercent = 0.036;
        double stripeFixed  = 300; // centavos
        long amount = (long) ( amountToPay * 100); //conversion a centavos
        double ivaRate      = 0.16;
        double platformPct  = 0.015;

        // Monto deseado para el condominio (en centavos)

        // Calcular comisión estimada de Stripe con IVA
        long stripeFeeEstimation = Math.round((amount * stripePercent + stripeFixed) * (1 + ivaRate));

        // Calcular comisión de plataforma
        long platformFeeEstimation = Math.round(amount * platformPct);

        // Calcular el total a cobrar al cliente
        long totalEstimation = amount + stripeFeeEstimation + platformFeeEstimation;


        // Crear el PaymentIntent
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(totalEstimation) // monto total al cliente (en centavos)
                .setCurrency("mxn")
                .addPaymentMethodType("card")
                .setApplicationFeeAmount(platformFeeEstimation) // solo tu comisión, Stripe toma su comisión directo
                .setTransferData(
                        PaymentIntentCreateParams.TransferData.builder()
                                .setDestination(connectedAccountId) // cuenta conectada del condominio
                                .build()
                )
                .build();


        // Crear y devolver el PaymentIntent
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        if(paymentIntent == null || paymentIntent.getClientSecret() == null  ){
            throw new BadRequestException("Error al intentar crear el intento de pago");
        }


        // 4) Mapeamos los datos esenciales a nuestra entidad y guardamos
        StripePaymentIntent spi = StripePaymentIntent.builder()
                .paymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .amount(paymentIntent.getAmount())
                .currency(paymentIntent.getCurrency())
                .status(paymentIntent.getStatus())
                .applicationFeeAmount(paymentIntent.getApplicationFeeAmount())
                .destinationAccountId(paymentIntent.getTransferData().getDestination())
                .paymentMethodId(paymentIntent.getPaymentMethod() != null ? paymentIntent.getPaymentMethod() : null)
                .createdAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(paymentIntent.getCreated()), ZoneOffset.UTC))
                .payment(payment)
                .build();

        stripePaymentIntentRepository.save(spi);


        return StripePaymentIntentResponse.builder()
                .clientSecret(paymentIntent.getClientSecret())
                .build();
    }


    @Override
    @Transactional
    public void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        // Buscar el StripePaymentIntent por paymentIntentId
        StripePaymentIntent spi = stripePaymentIntentRepository
                .findByPaymentIntentId(paymentIntent.getId()).orElseThrow(()-> new ResourceNotFoundException("Payment intent no encontrado con el id: " + paymentIntent.getId()));

        if (spi != null) {
            // Actualizar el estado y otros campos si es necesario
            spi.setStatus(paymentIntent.getStatus());
            spi.setPaymentMethodId(paymentIntent.getPaymentMethod());
            stripePaymentIntentRepository.save(spi);

            // Marcar el pago como realizado
            Payment payment = spi.getPayment();
            payment.setPaid(true);
            payment.setDatePaid(LocalDateTime.now());
            paymentService.update(payment);
            System.out.println("Hizo el update desde el webhook");
        }
    }

}

