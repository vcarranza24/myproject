package com.bootcamp.myproject.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movements")
public class Movement {

    @Id
    private String id;
    private String idAccount;
    private String idCredit;
    private String idTypeMovement;
    private String numDocument;
    private String numProduct;
    private String date;
    private BigDecimal amount;
    private String description;
    private BigDecimal commission;
}
