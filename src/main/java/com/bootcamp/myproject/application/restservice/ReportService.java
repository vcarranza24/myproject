package com.bootcamp.myproject.application.restservice;

import com.bootcamp.myproject.application.model.Accounts;
import com.bootcamp.myproject.application.model.DailyBalance;
import com.bootcamp.myproject.application.model.Movement;
import com.bootcamp.myproject.application.restrepository.AccountsRepository;
import com.bootcamp.myproject.application.restrepository.MovementsRepository;
import com.bootcamp.myproject.application.restrepository.TypeAccountRepository;
import com.bootcamp.myproject.application.restrepository.TypeCreditRepository;
import com.bootcamp.myproject.application.util.BalanceReportResponse;
import com.bootcamp.myproject.application.util.BalanceResponse;
import com.bootcamp.myproject.application.util.CommissionReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MovementsRepository movementsRepository;
    private final TypeAccountRepository typeAccountRepository;
    private final TypeCreditRepository typeCreditRepository;
    private final AccountsRepository accountsRepository;

    //Saldo promedio diario del mes actual
    public Flux<BalanceReportResponse> getMonthlyAverage(String numDocument) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();

        return movementsRepository.findByNumDocumentAndDateBetween(numDocument, start.toString(), end.toString())
                .collectList()
                .flatMapMany(movements -> {
                    if (movements.isEmpty()) {
                        return Flux.empty();
                    }

                    // Agrupar movimientos por producto y día
                    Map<String, Map<LocalDate, List<Movement>>> movementsByProductAndDate = movements.stream()
                            .collect(Collectors.groupingBy(
                                    Movement::getNumProduct,
                                    Collectors.groupingBy(m -> LocalDateTime.parse(m.getDate()).toLocalDate())
                            ));

                    // Para cada producto calculamos saldo diario acumulado y luego promedio
                    List<Mono<BalanceReportResponse>> monos = new ArrayList<>();

                    for (String numProduct : movementsByProductAndDate.keySet()) {
                        Map<LocalDate, List<Movement>> productMovements = movementsByProductAndDate.get(numProduct);
                        BigDecimal runningBalance = BigDecimal.ZERO;
                        Map<LocalDate, BigDecimal> dailyBalances = new HashMap<>();

                        List<LocalDate> dates = new ArrayList<>(productMovements.keySet());
                        Collections.sort(dates);

                        for (LocalDate date : dates) {
                            BigDecimal dailySum = productMovements.get(date).stream()
                                    .map(m -> m.getAmount())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            runningBalance = runningBalance.add(dailySum);
                            dailyBalances.put(date, runningBalance);
                        }

                        // Promedio mensual para este producto
                        BigDecimal total = dailyBalances.values().stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        int daysCount = dailyBalances.size();
                        BigDecimal average = daysCount > 0
                                ? total.divide(BigDecimal.valueOf(daysCount), 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                        // Ahora necesitamos obtener el tipo y nombre de producto
                        // Asumimos que podemos identificar si es cuenta o crédito desde movimientos:
                        Movement sampleMovement = productMovements.values().stream()
                                .flatMap(List::stream)
                                .findFirst()
                                .orElse(null);

                        if (sampleMovement == null) continue;

                        Mono<String> typeMono;
                        if (sampleMovement.getIdAccount() != null) {
                            typeMono = typeAccountRepository.findById(sampleMovement.getIdAccount())
                                    .map(type -> type.getNameAccount())
                                    .defaultIfEmpty("Cuenta");
                        } else if (sampleMovement.getIdCredit() != null) {
                            typeMono = typeCreditRepository.findById(sampleMovement.getIdCredit())
                                    .map(type -> type.getNameCredit())
                                    .defaultIfEmpty("Crédito");
                        } else {
                            typeMono = Mono.just("Desconocido");
                        }

                        Mono<BalanceReportResponse> balanceResponseMono = typeMono.map(type ->
                                new BalanceReportResponse(type, numProduct, average.doubleValue(), null,start,end)
                        );

                        monos.add(balanceResponseMono);
                    }

                    return Flux.merge(monos);
                });
    }

    public Flux<CommissionReportResponse> getCommissionReport(LocalDate start, LocalDate end) {
        return accountsRepository.findAll()
                .flatMap(account ->
                        typeAccountRepository.findById(account.getIdTypeAccount())
                                .map(type -> Tuples.of(account, type.getNameAccount()))
                )
                .flatMap(tuple -> {
                    Accounts account = tuple.getT1();
                    String typeAccount = tuple.getT2();

                    return movementsRepository
                            .findByNumProductAndDateBetween(account.getNumAccount(), start.toString(), end.toString())
                            .filter(m -> m.getDescription().toLowerCase().contains("comision"))
                            .collectList()
                            .map(commissions -> {
                                BigDecimal total = commissions.stream()
                                        .map(c -> c.getAmount())
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                return new CommissionReportResponse(
                                        account.getNumAccount(),
                                        typeAccount,
                                        total,
                                        commissions.size(),
                                        start,
                                        end
                                );
                            });
                });
    }


}
