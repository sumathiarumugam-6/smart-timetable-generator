package com.smarttimetable.repository;

import com.smarttimetable.entity.Classroom;
import com.smarttimetable.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findByRoomNumber(String roomNumber);
    List<Classroom> findByDepartmentId(Long departmentId);
    List<Classroom> findByRoomType(RoomType roomType);
    List<Classroom> findByDepartmentIdAndRoomType(Long departmentId, RoomType roomType);
}
