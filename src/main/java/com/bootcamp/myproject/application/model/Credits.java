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
@Document(collection = "credit")
public class Credits {

    @Id
    private String id;
    private String idTypeCredit; // personal, empresarial, tarjeta crédito
    private String idCustomer;
    private String numDocument;
    private String numCard;
    private String idMovement;
    private BigDecimal lineUsed;   // linea usada de crédito
    private BigDecimal balance;       // saldo disponible del crédito
    private Double limitCredit;   // límite total del crédito
    private int state;

}
