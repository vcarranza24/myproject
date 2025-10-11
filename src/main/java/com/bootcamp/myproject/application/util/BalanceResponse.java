package com.bootcamp.myproject.application.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceResponse {

    private String TypeAccount;
    private String numAccount;
    private Double balance;
    private Double limit;
}
