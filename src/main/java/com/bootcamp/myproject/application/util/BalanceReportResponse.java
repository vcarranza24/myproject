package com.bootcamp.myproject.application.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Reporte de saldo promedio mensual")
public class BalanceReportResponse {
    @Schema(description = "Tipo de cuenta", example = "Corriente")
    private String typeAccount;

    @Schema(description = "Número de cuenta", example = "1234567890")
    private String numAccount;

    @Schema(description = "Saldo disponible", example = "1500.50")
    private Double balance;

    @Schema(description = "Límite disponible", example = "5000.00")
    private Double limit;

    @Schema(description = "Fecha inicio", example = "01/10/2025")
    private LocalDate startDate;    // Nuevo

    @Schema(description = "Fecha fin", example = "14/10/2025")
    private LocalDate endDate;      // Nuevo

}
