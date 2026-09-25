package com.smarttimetable.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {
    private Long id;
    private String code;
    private String name;
    private String hodName;
    private String building;
}
