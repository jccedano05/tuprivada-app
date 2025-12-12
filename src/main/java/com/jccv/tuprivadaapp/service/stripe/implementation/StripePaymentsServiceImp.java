package com.jccv.tuprivadaapp.service.stripe.implementation;

import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.dto.payment.PaymentDto;
import com.jccv.tuprivadaapp.dto.payment.mapper.PaymentMapper;
import com.jccv.tuprivadaapp.dto.stripe.StripeOxxoVoucherResponse;
import com.jccv.tuprivadaapp.dto.stripe.StripePaymentIntentResponse;
import com.jccv.tuprivadaapp.dto.stripe.StripePaymentRequest;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.payment.StripePaymentIntent;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.payment.PaymentRepository;
import com.jccv.tuprivadaapp.repository.stripe.StripePaymentIntentRepository;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.email.EmailService;
import com.jccv.tuprivadaapp.service.payment.DepositPaymentService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
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
import java.util.*;

@Service
public class StripePaymentsServiceImp implements StripePaymentsService {

    private final CondominiumService condominiumService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final StripePaymentIntentRepository stripePaymentIntentRepository;
    private final EmailService emailService;
    private final OneSignalPushNotificationService oneSignalPushNotificationService;

    @Autowired
    public StripePaymentsServiceImp(CondominiumService condominiumService, PaymentService paymentService, StripePaymentIntentRepository stripePaymentIntentRepository, PaymentRepository paymentRepository, EmailService emailService, OneSignalPushNotificationService oneSignalPushNotificationService) {
        this.condominiumService = condominiumService;
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.stripePaymentIntentRepository = stripePaymentIntentRepository;
        this.emailService = emailService;
        this.oneSignalPushNotificationService = oneSignalPushNotificationService;
    }


    @Override
    @Transactional
    public StripePaymentIntentResponse createPaymentCardIntent(StripePaymentRequest request) throws StripeException {

        Payment payment = paymentRepository.findById(request.getPaymentId()).orElseThrow(()-> new ResourceNotFoundException("Payment no encontrado con el id: " + request.getPaymentId()));

        Long condominiumId = payment.getCharge().getCondominium().getId();

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

        String connectedAccountId = condominiumService.findConnectedAccountIdByCondominiumId(condominiumId);

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


        // Crear el PaymentIntent en CARD
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

            if (payment.isPaid()) {
                // Ya estaba pagado: se va a saldo
                Resident resident = payment.getResident();
                resident.setBalance(resident.getBalance() + spi.getAmount()); // asumiendo que tienes setBalance

                oneSignalPushNotificationService.sendPushToUser(
                        PushNotificationRequest.builder()
                                .title("Pago duplicado detectado")
                                .message("El pago '"+ payment.getCharge().getTitleTypePayment() +"' ya estaba pagado previamente." +
                                        "El dinero se ha sumado a tu saldo disponible. Contacta con administración para más detalles. ") // O cualquier mensaje relacionado

                                .userId(payment.getResident().getUser().getId())
                                .build()
                );

                // Notificamos al usuario por correo
                emailService.sendHtmlEmail(
                        resident.getUser().getEmail(),
                        "Pago duplicado detectado",
                        "payment-duplicate",
                        Map.of(
                                "nombre", resident.getUser().getFirstName(),
                                "mensaje", "Se ha recibido un pago duplicado. El dinero se ha sumado a tu saldo disponible. Contacta con administración para más detalles."
                        )
                );
            }else{
                payment.setPaid(true);
                payment.setDatePaid(LocalDateTime.now());
                paymentService.update(payment);

                oneSignalPushNotificationService.sendPushToUser(
                        PushNotificationRequest.builder()
                                .title("¡Pago Exitoso!")
                                .message(payment.getCharge().getTitleTypePayment()) // O cualquier mensaje relacionado
                                .userId(payment.getResident().getUser().getId())
                                .build()
                );

                emailService.sendHtmlEmail(
                        payment.getResident().getUser().getEmail(),
                        "¡Pago realizado con éxito!",
                        "payment-success",
                        Map.of(
                                "nombre", payment.getResident().getUser().getFirstName(),
                                "monto", String.format("$%.2f", spi.getAmount() / 100.0) // si es en centavos
                        )
                );


            }


            List<String> methodTypes = paymentIntent.getPaymentMethodTypes();
            if (methodTypes.contains("oxxo")) {
                // lógica específica para OXXO si necesitas
            }

//            TODO - VERIFICAR SI SOLO SE CREA UN PAYMENT INTENT, YA SEA CON CARD, OXXO O SPEI.. SI ES ASI, NOTIFICAR QUE YA HAY UN PROCESO
//             DE PAGO PARA ESE PAYMENT (SI ES DE OXXO, DEVOLVER EL URL VOUCHER SOLO NOTIFICAR)

//            TODO - SI YA ESTA PAGADO EL PAYMENT, COMO SEGUIRDAD METERLO A SALDO Y ENVIAR UN CORREO AL RESIDENTE DE PAGO DUPLICADO Y QUE EL DINERO
//             SE VA A IR A SALDO DEL RESIDENTE, QUE SE COMUNIQUE CON ADMINISTRACION PARA MAS INFORMACION O SOPORTE

//            TODO - HACER EL ENVIO DE LA PUSH NOTIFICATION Y ENVIAR CORREO DE PAGO EXITOSO A LAS CUENTAS ADMINISTRADORAS Y A EL RESIDENTE

        }
    }



    @Override
    @Transactional
    public StripeOxxoVoucherResponse createPaymentOxxoIntent(StripePaymentRequest request) throws StripeException {

        Payment payment = paymentRepository.findById(request.getPaymentId()).orElseThrow(()-> new ResourceNotFoundException("Payment no encontrado con el id: " + request.getPaymentId()));

        Long condominiumId = payment.getCharge().getCondominium().getId();

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


        String residentFullName = payment.getResident().getUser().getFirstName() + " " + payment.getResident().getUser().getLastName();
        String residentReference = payment.getResident().getUser().getBankPersonalReference();
        String addressResident = payment.getResident().getAddressResident().getStreet() + " " +payment.getResident().getAddressResident().getExtNumber();

        if (existingIntent.isPresent()) {
            StripePaymentIntent spi = existingIntent.get();

            return StripeOxxoVoucherResponse.builder()
                    .paymentId(payment.getId())
                    .voucherUrl(spi.getVoucherUrl()) // asegúrate que esta columna exista en tu entidad
                    .expiresAt(spi.getCreatedAt().plusDays(3)) // suponiendo que usas expires_after_days: 3
                    .totalToPay(spi.getAmount()) // en centavos
                    .clientSecret(spi.getClientSecret())
                    .residentFullName(residentFullName)
                    .paymentReference(residentReference != null ? residentReference : addressResident)
                    .build();
        }


        Double amountToPay = paymentService.getRemainingAmountByPaymentId(request.getPaymentId());

        String connectedAccountId = condominiumService.findConnectedAccountIdByCondominiumId(condominiumId);

        // Variables de comisiones
        double stripePercent = 0.036;
        double stripeFixed  = 300; // centavos
        long amount = (long) ( amountToPay * 100); //conversion a centavos
        double ivaRate      = 0.16;
        double platformPct  = 0.01 ;

        // Monto deseado para el condominio (en centavos)

        // Calcular comisión estimada de Stripe con IVA
        long stripeFeeEstimation = Math.round((amount * stripePercent + stripeFixed) * (1 + ivaRate));

        // Calcular comisión de plataforma
        long platformFeeEstimation = Math.round(amount * platformPct);

        // Calcular el total a cobrar al cliente
        long totalEstimation = amount + stripeFeeEstimation + platformFeeEstimation;



        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(totalEstimation)
                .setCurrency("mxn")
                .setConfirm(true) // ⬅️ MUY IMPORTANTE para OXXO
                .addPaymentMethodType("oxxo")
                .setPaymentMethodData(
                        PaymentIntentCreateParams.PaymentMethodData.builder()
                                .setType(PaymentIntentCreateParams.PaymentMethodData.Type.OXXO)
                                .setBillingDetails(
                                        PaymentIntentCreateParams.PaymentMethodData.BillingDetails.builder()
                                                .setEmail(request.getEmail())
                                                .setName(payment.getResident().getUser() != null ?
                                                        residentFullName
                                                        : "Cliente") // obligatorio
                                                .build()
                                )
                                .build()
                )
                .setPaymentMethodOptions(
                        PaymentIntentCreateParams.PaymentMethodOptions.builder()
                                .setOxxo(
                                        PaymentIntentCreateParams.PaymentMethodOptions.Oxxo.builder()
                                                .setExpiresAfterDays(3L)
                                                .build()
                                )
                                .build()
                )

                .setApplicationFeeAmount(platformFeeEstimation)
                .putMetadata("payment_id", payment.getId().toString())
                .putMetadata("email", request.getEmail())
                .putMetadata("resident_id", payment.getResident().getId().toString())
                .setDescription(residentReference != null? residentReference : addressResident) //colocar referencia de pago
                .setTransferData(
                        PaymentIntentCreateParams.TransferData.builder()
                                .setDestination(connectedAccountId)
                                .build()
                )
                .build();


        // Crear y devolver el PaymentIntent
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        if(paymentIntent == null || paymentIntent.getClientSecret() == null  ){
            throw new BadRequestException("Error al intentar crear el intento de pago");
        }

        String voucherUrl = paymentIntent.getNextAction().getOxxoDisplayDetails().getHostedVoucherUrl();
        Long expiresAtUnix = paymentIntent.getNextAction().getOxxoDisplayDetails().getExpiresAfter();
        LocalDateTime expiresAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(expiresAtUnix), ZoneOffset.UTC);


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
                .voucherUrl(voucherUrl)
                .payment(payment)
                .build();

        stripePaymentIntentRepository.save(spi);


        return StripeOxxoVoucherResponse.builder()
                .paymentId(payment.getId())
                .voucherUrl(voucherUrl)
                .expiresAt(expiresAt)
                .totalToPay(totalEstimation)
                .clientSecret(paymentIntent.getClientSecret())
                .residentFullName(residentFullName)
                .paymentReference(residentReference != null ? residentReference : addressResident)
                .build();
    }

}

