package com.smarttimetable.dto;

import com.smarttimetable.enums.TimetableStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableDTO {
    private Long id;
    private String title;
    private String academicYear;
    private Integer semester;
    private Long departmentId;
    private String departmentName;
    private TimetableStatus status;
    private Double fitnessScore;
    private Integer conflictCount;
    private String createdByName;
    private LocalDateTime createdAt;
    private List<TimetableEntryDTO> entries;
}
