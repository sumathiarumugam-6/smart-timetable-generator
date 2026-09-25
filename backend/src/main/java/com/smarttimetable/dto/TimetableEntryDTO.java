package com.smarttimetable.dto;

import com.smarttimetable.enums.DayOfWeekEnum;
import com.smarttimetable.enums.SubjectType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableEntryDTO {
    private Long id;
    private Long timetableId;
    private DayOfWeekEnum dayOfWeek;
    private Integer periodNumber;
    private String timeSlot;
    
    private Long batchId;
    private String batchSection;
    private Integer semester;
    
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private SubjectType subjectType;
    
    private Long facultyId;
    private String facultyCode;
    private String facultyName;
    
    private Long classroomId;
    private String roomNumber;
    private String building;
    
    private Boolean isLabSession;
    private String labGroup;
}
