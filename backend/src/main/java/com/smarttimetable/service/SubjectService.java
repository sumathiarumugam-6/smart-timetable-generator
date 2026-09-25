package com.smarttimetable.service;

import com.smarttimetable.dto.SubjectDTO;
import com.smarttimetable.entity.Department;
import com.smarttimetable.entity.Subject;
import com.smarttimetable.repository.DepartmentRepository;
import com.smarttimetable.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;

    public List<SubjectDTO> getAllSubjects() {
        return subjectRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public List<SubjectDTO> getSubjectsByDepartment(Long departmentId) {
        return subjectRepository.findByDepartmentId(departmentId).stream().map(this::mapToDTO).toList();
    }

    public SubjectDTO createSubject(SubjectDTO dto) {
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Subject s = Subject.builder()
                .subjectCode(dto.getSubjectCode())
                .subjectName(dto.getSubjectName())
                .credits(dto.getCredits())
                .subjectType(dto.getSubjectType())
                .semester(dto.getSemester())
                .weeklyPeriods(dto.getWeeklyPeriods() != null ? dto.getWeeklyPeriods() : 3)
                .department(dept)
                .build();

        return mapToDTO(subjectRepository.save(s));
    }

    public SubjectDTO updateSubject(Long id, SubjectDTO dto) {
        Subject s = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        s.setSubjectCode(dto.getSubjectCode());
        s.setSubjectName(dto.getSubjectName());
        s.setCredits(dto.getCredits());
        s.setSubjectType(dto.getSubjectType());
        s.setSemester(dto.getSemester());
        if (dto.getWeeklyPeriods() != null) s.setWeeklyPeriods(dto.getWeeklyPeriods());
        s.setDepartment(dept);

        return mapToDTO(subjectRepository.save(s));
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }

    public SubjectDTO mapToDTO(Subject s) {
        return SubjectDTO.builder()
                .id(s.getId())
                .subjectCode(s.getSubjectCode())
                .subjectName(s.getSubjectName())
                .credits(s.getCredits())
                .subjectType(s.getSubjectType())
                .semester(s.getSemester())
                .weeklyPeriods(s.getWeeklyPeriods())
                .departmentId(s.getDepartment().getId())
                .departmentName(s.getDepartment().getName())
                .build();
    }
}
