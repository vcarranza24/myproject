package com.bootcamp.myproject.application.model;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
public class DailyBalance {

    private LocalDate date;
    private BigDecimal balance;
}
