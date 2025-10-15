package com.bootcamp.myproject.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product")
public class Product {

    @Id
    private String id;
    private int state;
    private CategoryProduct categoryProduct;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryProduct {
        private String name;
        private List<AccountType> accounts;  // para pasivos
        private List<CreditType> credits;    // para activos
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountType {
        private String id;
        private String nameAccount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditType {
        private String id;
        private String nameCredit;
    }

}
