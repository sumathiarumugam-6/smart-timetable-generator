package com.smarttimetable.controller;

import com.smarttimetable.config.UserPrincipal;
import com.smarttimetable.dto.LeaveRequestDTO;
import com.smarttimetable.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping
    public ResponseEntity<List<LeaveRequestDTO>> getAllLeaveRequests(@RequestParam(required = false) Long facultyId) {
        if (facultyId != null) {
            return ResponseEntity.ok(leaveService.getLeaveRequestsByFaculty(facultyId));
        }
        return ResponseEntity.ok(leaveService.getAllLeaveRequests());
    }

    @PostMapping("/apply")
    public ResponseEntity<LeaveRequestDTO> applyLeave(@Valid @RequestBody LeaveRequestDTO dto) {
        return ResponseEntity.ok(leaveService.applyLeave(dto));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HOD')")
    public ResponseEntity<LeaveRequestDTO> approveLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(leaveService.approveLeave(id, currentUser != null ? currentUser.getUser() : null));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HOD')")
    public ResponseEntity<LeaveRequestDTO> rejectLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(leaveService.rejectLeave(id, currentUser != null ? currentUser.getUser() : null));
    }
}
