package com.smarttimetable.service;

import com.smarttimetable.dto.DepartmentDTO;
import com.smarttimetable.entity.Department;
import com.smarttimetable.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public DepartmentDTO getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + id));
        return mapToDTO(dept);
    }

    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        if (departmentRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Department with code " + dto.getCode() + " already exists!");
        }

        Department dept = Department.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .hodName(dto.getHodName())
                .building(dto.getBuilding())
                .build();

        return mapToDTO(departmentRepository.save(dept));
    }

    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + id));

        dept.setCode(dto.getCode());
        dept.setName(dto.getName());
        dept.setHodName(dto.getHodName());
        dept.setBuilding(dto.getBuilding());

        return mapToDTO(departmentRepository.save(dept));
    }

    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO mapToDTO(Department dept) {
        return DepartmentDTO.builder()
                .id(dept.getId())
                .code(dept.getCode())
                .name(dept.getName())
                .hodName(dept.getHodName())
                .building(dept.getBuilding())
                .build();
    }
}
