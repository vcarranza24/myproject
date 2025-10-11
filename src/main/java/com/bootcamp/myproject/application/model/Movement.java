package com.bootcamp.myproject.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private Double amount;
    private String description;

}
