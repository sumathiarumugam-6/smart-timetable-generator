package com.smarttimetable.service.scheduler;

import com.smarttimetable.enums.DayOfWeekEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gene implements Cloneable {
    private DayOfWeekEnum dayOfWeek;
    private Integer periodNumber;
    private String timeSlot;
    private Long batchId;
    private String batchSection;
    private Long subjectId;
    private String subjectCode;
    private String subjectType; // CORE, THEORY, LAB, ELECTIVE
    private Long facultyId;
    private String facultyName;
    private Long classroomId;
    private String roomNumber;
    private String roomType; // CLASSROOM, LABORATORY
    private Boolean isLabSession;

    @Override
    public Gene clone() {
        try {
            return (Gene) super.clone();
        } catch (CloneNotSupportedException e) {
            return Gene.builder()
                    .dayOfWeek(dayOfWeek)
                    .periodNumber(periodNumber)
                    .timeSlot(timeSlot)
                    .batchId(batchId)
                    .batchSection(batchSection)
                    .subjectId(subjectId)
                    .subjectCode(subjectCode)
                    .subjectType(subjectType)
                    .facultyId(facultyId)
                    .facultyName(facultyName)
                    .classroomId(classroomId)
                    .roomNumber(roomNumber)
                    .roomType(roomType)
                    .isLabSession(isLabSession)
                    .build();
        }
    }
}
