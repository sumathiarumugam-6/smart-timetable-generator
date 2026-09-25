package com.smarttimetable.controller;

import com.smarttimetable.config.UserPrincipal;
import com.smarttimetable.dto.*;
import com.smarttimetable.enums.TimetableStatus;
import com.smarttimetable.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping("/generate")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TimetableDTO> generateTimetable(
            @Valid @RequestBody TimetableGenerationRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(timetableService.generateTimetable(req, currentUser != null ? currentUser.getUser() : null));
    }

    @GetMapping
    public ResponseEntity<List<TimetableDTO>> getAllTimetables() {
        return ResponseEntity.ok(timetableService.getAllTimetables());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimetableDTO> getTimetableById(@PathVariable Long id) {
        return ResponseEntity.ok(timetableService.getTimetableById(id));
    }

    @PostMapping("/manual-swap")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HOD')")
    public ResponseEntity<TimetableDTO> manualSwap(@Valid @RequestBody ManualSwapRequest swapReq) {
        return ResponseEntity.ok(timetableService.performManualSwap(swapReq));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HOD')")
    public ResponseEntity<TimetableDTO> updateStatus(@PathVariable Long id, @RequestParam TimetableStatus status) {
        return ResponseEntity.ok(timetableService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteTimetable(@PathVariable Long id) {
        timetableService.deleteTimetable(id);
        return ResponseEntity.noContent().build();
    }
}
