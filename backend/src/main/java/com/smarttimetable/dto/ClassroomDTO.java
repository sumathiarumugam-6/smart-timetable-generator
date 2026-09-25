package com.smarttimetable.dto;

import com.smarttimetable.enums.RoomType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomDTO {
    private Long id;
    private String roomNumber;
    private String building;
    private Integer capacity;
    private RoomType roomType;
    private Long departmentId;
    private String departmentName;
    private Boolean isAvailable;
}
