package com.bootcamp.myproject.application.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Reporte de comisiones")
public class CommissionReportResponse {

    private String numAccount;
    private String typeAccount;
    private BigDecimal totalCommissions;
    private int commissionCount;
    private LocalDate startDate;
    private LocalDate endDate;

}
