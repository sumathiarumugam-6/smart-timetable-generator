package com.smarttimetable.service;

import com.smarttimetable.dto.ClassroomDTO;
import com.smarttimetable.entity.Classroom;
import com.smarttimetable.entity.Department;
import com.smarttimetable.repository.ClassroomRepository;
import com.smarttimetable.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final DepartmentRepository departmentRepository;

    public List<ClassroomDTO> getAllClassrooms() {
        return classroomRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public ClassroomDTO createClassroom(ClassroomDTO dto) {
        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
        }

        Classroom c = Classroom.builder()
                .roomNumber(dto.getRoomNumber())
                .building(dto.getBuilding())
                .capacity(dto.getCapacity())
                .roomType(dto.getRoomType())
                .department(dept)
                .isAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true)
                .build();

        return mapToDTO(classroomRepository.save(c));
    }

    public ClassroomDTO updateClassroom(Long id, ClassroomDTO dto) {
        Classroom c = classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        if (dto.getDepartmentId() != null) {
            c.setDepartment(departmentRepository.findById(dto.getDepartmentId()).orElse(null));
        }

        c.setRoomNumber(dto.getRoomNumber());
        c.setBuilding(dto.getBuilding());
        c.setCapacity(dto.getCapacity());
        c.setRoomType(dto.getRoomType());
        if (dto.getIsAvailable() != null) c.setIsAvailable(dto.getIsAvailable());

        return mapToDTO(classroomRepository.save(c));
    }

    public void deleteClassroom(Long id) {
        classroomRepository.deleteById(id);
    }

    public ClassroomDTO mapToDTO(Classroom c) {
        return ClassroomDTO.builder()
                .id(c.getId())
                .roomNumber(c.getRoomNumber())
                .building(c.getBuilding())
                .capacity(c.getCapacity())
                .roomType(c.getRoomType())
                .departmentId(c.getDepartment() != null ? c.getDepartment().getId() : null)
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : null)
                .isAvailable(c.getIsAvailable())
                .build();
    }
}
