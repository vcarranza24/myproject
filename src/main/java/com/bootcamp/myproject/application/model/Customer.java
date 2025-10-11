package com.bootcamp.myproject.application.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customer")
public class Customer {

    @Id
    private String id;

    @NotBlank(message = "idTypeCustomer is required")
    private String idTypeCustomer;

    @NotBlank(message = "idTypeDocument is required")
    private String idTypeDocument;

    @NotBlank(message = "numDocument is required")
    @Pattern(regexp = "^[0-9]{8,11}$", message = "numDocument must be numeric and have between 8 and 11 digits")
    private String numDocument;

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be less than 100 characters")
    private String name;

    @NotBlank(message = "lastName is required")
    @Size(max = 100, message = "lastName must be less than 100 characters")
    private String lastName;

    @Email(message = "email must be valid")
    private String email;

    private String address;

    @Pattern(regexp = "^[0-9]{9}$", message = "phone must have 9 digits")
    private String phone;

}
