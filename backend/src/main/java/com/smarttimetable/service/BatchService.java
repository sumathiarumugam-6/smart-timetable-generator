package com.smarttimetable.service;

import com.smarttimetable.dto.BatchDTO;
import com.smarttimetable.entity.Batch;
import com.smarttimetable.entity.Department;
import com.smarttimetable.repository.BatchRepository;
import com.smarttimetable.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchRepository batchRepository;
    private final DepartmentRepository departmentRepository;

    public List<BatchDTO> getAllBatches() {
        return batchRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public List<BatchDTO> getBatchesByDepartment(Long departmentId) {
        return batchRepository.findByDepartmentId(departmentId).stream().map(this::mapToDTO).toList();
    }

    public BatchDTO createBatch(BatchDTO dto) {
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Batch b = Batch.builder()
                .academicYear(dto.getAcademicYear())
                .department(dept)
                .section(dto.getSection())
                .semester(dto.getSemester())
                .studentCount(dto.getStudentCount() != null ? dto.getStudentCount() : 60)
                .build();

        return mapToDTO(batchRepository.save(b));
    }

    public BatchDTO updateBatch(Long id, BatchDTO dto) {
        Batch b = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        b.setAcademicYear(dto.getAcademicYear());
        b.setDepartment(dept);
        b.setSection(dto.getSection());
        b.setSemester(dto.getSemester());
        if (dto.getStudentCount() != null) b.setStudentCount(dto.getStudentCount());

        return mapToDTO(batchRepository.save(b));
    }

    public void deleteBatch(Long id) {
        batchRepository.deleteById(id);
    }

    public BatchDTO mapToDTO(Batch b) {
        return BatchDTO.builder()
                .id(b.getId())
                .academicYear(b.getAcademicYear())
                .departmentId(b.getDepartment().getId())
                .departmentName(b.getDepartment().getName())
                .section(b.getSection())
                .semester(b.getSemester())
                .studentCount(b.getStudentCount())
                .build();
    }
}
