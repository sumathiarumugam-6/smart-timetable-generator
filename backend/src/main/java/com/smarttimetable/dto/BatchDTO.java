package com.smarttimetable.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDTO {
    private Long id;
    private String academicYear;
    private Long departmentId;
    private String departmentName;
    private String section;
    private Integer semester;
    private Integer studentCount;
}
