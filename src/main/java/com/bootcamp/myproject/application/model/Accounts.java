package com.bootcamp.myproject.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "account")
public class Accounts {

    @Id
    private String id;
    private String idTypeAccount; // ahorro, corriente, plazo fijo
    private String idCustomer;
    private String numDocument;
    private String numAccount;
    private BigDecimal balance;       // saldo disponible
    private BigDecimal commission;    // comisión mensual o por transacción
    private List<Movement> movements;
    private List<String> headlines;    // solo para empresarial
    private List<String> signatories;    // solo para empresarial
    private String dayOpe;        // día de operación o corte
    private int state;
    private int maxFreeTransactions;
    private int currentTransactions;
    private BigDecimal transactionCommission;
    private BigDecimal minMonthlyAverage;    // monto mínimo requerido mensual
    private BigDecimal accumulatedDailyBalance; // suma acumulada de saldos diarios
    private int daysAccumulated;    // cuántos días se han contado en el mes

}
