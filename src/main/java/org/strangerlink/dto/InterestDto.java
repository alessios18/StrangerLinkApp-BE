package org.strangerlink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterestDto {
    @NotBlank(message = "Interest name cannot be empty")
    private String name;
}
