package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.restservice.ReportService;
import com.bootcamp.myproject.application.util.BalanceReportResponse;
import com.bootcamp.myproject.application.util.BalanceResponse;
import com.bootcamp.myproject.application.util.CommissionReportResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Tag(name = "reportes", description = "Saldo promedio diario del mes actual")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly-average/{numDocument}")
    public Flux<BalanceReportResponse> getMonthlyAverage(@PathVariable String numDocument) {
        return reportService.getMonthlyAverage(numDocument);
    }

    @GetMapping("/commissions")
    public Flux<CommissionReportResponse> getCommissions(
            @RequestParam String start,
            @RequestParam String end
    ) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return reportService.getCommissionReport(startDate, endDate);
    }
}
