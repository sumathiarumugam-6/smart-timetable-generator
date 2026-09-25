package com.smarttimetable.dto;

import com.smarttimetable.enums.DayOfWeekEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualSwapRequest {

    @NotNull
    private Long timetableId;

    @NotNull
    private Long entryId1;

    @NotNull
    private DayOfWeekEnum targetDay;

    @NotNull
    private Integer targetPeriodNumber;

    private Long targetClassroomId;
}
