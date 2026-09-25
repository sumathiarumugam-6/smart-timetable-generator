package com.smarttimetable.controller;

import com.smarttimetable.dto.BatchDTO;
import com.smarttimetable.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BatchController {

    private final BatchService batchService;

    @GetMapping
    public ResponseEntity<List<BatchDTO>> getAllBatches(@RequestParam(required = false) Long departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(batchService.getBatchesByDepartment(departmentId));
        }
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HOD')")
    public ResponseEntity<BatchDTO> createBatch(@Valid @RequestBody BatchDTO dto) {
        return ResponseEntity.ok(batchService.createBatch(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HOD')")
    public ResponseEntity<BatchDTO> updateBatch(@PathVariable Long id, @Valid @RequestBody BatchDTO dto) {
        return ResponseEntity.ok(batchService.updateBatch(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
        batchService.deleteBatch(id);
        return ResponseEntity.noContent().build();
    }
}
