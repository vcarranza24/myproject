package com.bootcamp.myproject.application.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "parameters")
public class Parameter {

    @Id
    private String id;
    private String key;
    private String value;
}
