package com.jccv.tuprivadaapp.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentScheduler {

        // Método programado para ejecutarse diariamente a las 4:00 AM  "segs min hrs dayOfmonth month dayOfWeek"
        @Scheduled(cron = "0 38 6 * 1 *", zone = "America/Mexico_City")
        public void PaymentsScheduled() {
            try {
                // Lógica de cargos
                System.out.println("Verificando si hay cargos pendientes para procesar...");
            } catch (Exception e) {
                // Manejar la excepción
                System.err.println("Error al verificar los cargos: " + e.getMessage());
            }
    }
}
