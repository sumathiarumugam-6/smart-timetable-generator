package com.smarttimetable.dto;

import com.smarttimetable.enums.LeaveStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDTO {
    private Long id;
    private Long facultyId;
    private String facultyCode;
    private String facultyName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private Long substituteFacultyId;
    private String substituteFacultyName;
    private String approvedByName;
    private LocalDateTime createdAt;
}
