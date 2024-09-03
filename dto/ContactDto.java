package com.monetize360.contact_application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)

public class ContactDto {


    private Integer id;

    @NotBlank(message = "First name is required")
    private String firstname;

    private String lastname;

    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "\\d{10}", message = "Mobile number must be a 10-digit number")
    private String mobile;

    private boolean deleted;


}
