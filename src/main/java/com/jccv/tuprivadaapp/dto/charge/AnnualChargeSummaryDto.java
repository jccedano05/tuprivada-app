package com.jccv.tuprivadaapp.dto.charge;

import lombok.Data;

@Data
public class AnnualChargeSummaryDto {
    private int totalCharges;           // Total de cargos creados en el año
    private int fullyPaidCharges;       // Cargos totalmente pagados
    private double successConversionRate; // Tasa de conversión de éxito
    private double totalCollected;       // Monto total recaudado
    private double remainingAmount;      // Monto pendiente por cobrar
    private int residentsWithPending;    // Residentes con pagos pendientes (opcional)
    private int totalPendingPayments;
    private int totalPaidPayments;


    // Nuevos campos para distribución de deudas
    private int residentsWith1Pending;
    private int residentsWith2Pending;
    private int residentsWith3Pending;
    private int residentsWith4Pending;
    private int residentsWith5PlusPending;
}