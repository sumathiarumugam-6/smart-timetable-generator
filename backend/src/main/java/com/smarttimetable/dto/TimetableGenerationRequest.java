package com.smarttimetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableGenerationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Semester is required")
    private Integer semester;

    @Builder.Default
    private Integer maxGenerations = 1000;

    @Builder.Default
    private Integer populationSize = 100;
}
