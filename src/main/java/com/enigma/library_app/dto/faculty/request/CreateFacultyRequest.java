package com.enigma.library_app.dto.faculty.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateFacultyRequest {
    @NotBlank
    private String facultyCode;

    @NotBlank
    private String name;
}
