package com.smarttimetable.service;

import com.smarttimetable.dto.FacultyDTO;
import com.smarttimetable.entity.Department;
import com.smarttimetable.entity.Faculty;
import com.smarttimetable.repository.DepartmentRepository;
import com.smarttimetable.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    public List<FacultyDTO> getAllFaculty() {
        return facultyRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public List<FacultyDTO> getFacultyByDepartment(Long departmentId) {
        return facultyRepository.findByDepartmentId(departmentId).stream().map(this::mapToDTO).toList();
    }

    public FacultyDTO getFacultyById(Long id) {
        Faculty f = facultyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Faculty not found with ID: " + id));
        return mapToDTO(f);
    }

    public FacultyDTO createFaculty(FacultyDTO dto) {
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Faculty f = Faculty.builder()
                .facultyCode(dto.getFacultyCode())
                .name(dto.getName())
                .designation(dto.getDesignation())
                .department(dept)
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .maxWorkingHours(dto.getMaxWorkingHours() != null ? dto.getMaxWorkingHours() : 20)
                .currentWorkload(dto.getCurrentWorkload() != null ? dto.getCurrentWorkload() : 0)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        return mapToDTO(facultyRepository.save(f));
    }

    public FacultyDTO updateFaculty(Long id, FacultyDTO dto) {
        Faculty f = facultyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Faculty not found with ID: " + id));

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        f.setFacultyCode(dto.getFacultyCode());
        f.setName(dto.getName());
        f.setDesignation(dto.getDesignation());
        f.setDepartment(dept);
        f.setEmail(dto.getEmail());
        f.setPhone(dto.getPhone());
        if (dto.getMaxWorkingHours() != null) f.setMaxWorkingHours(dto.getMaxWorkingHours());
        if (dto.getIsActive() != null) f.setIsActive(dto.getIsActive());

        return mapToDTO(facultyRepository.save(f));
    }

    public void deleteFaculty(Long id) {
        facultyRepository.deleteById(id);
    }

    public FacultyDTO mapToDTO(Faculty f) {
        return FacultyDTO.builder()
                .id(f.getId())
                .userId(f.getUser() != null ? f.getUser().getId() : null)
                .facultyCode(f.getFacultyCode())
                .name(f.getName())
                .designation(f.getDesignation())
                .departmentId(f.getDepartment().getId())
                .departmentName(f.getDepartment().getName())
                .email(f.getEmail())
                .phone(f.getPhone())
                .maxWorkingHours(f.getMaxWorkingHours())
                .currentWorkload(f.getCurrentWorkload())
                .isActive(f.getIsActive())
                .build();
    }
}
