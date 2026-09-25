package com.smarttimetable.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyDTO {
    private Long id;
    private Long userId;
    private String facultyCode;
    private String name;
    private String designation;
    private Long departmentId;
    private String departmentName;
    private String email;
    private String phone;
    private Integer maxWorkingHours;
    private Integer currentWorkload;
    private Boolean isActive;
}
