package com.smarttimetable.dto;

import com.smarttimetable.enums.SubjectType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectDTO {
    private Long id;
    private String subjectCode;
    private String subjectName;
    private Integer credits;
    private SubjectType subjectType;
    private Integer semester;
    private Integer weeklyPeriods;
    private Long departmentId;
    private String departmentName;
}
