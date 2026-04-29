package com.emergency.routing.controller;

import com.emergency.routing.dto.HandoverRequestDto;
import com.emergency.routing.model.PatientHandover;
import com.emergency.routing.service.HandoverService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/handover")
public class HandoverController {

    private final HandoverService handoverService;

    public HandoverController(HandoverService handoverService) {
        this.handoverService = handoverService;
    }

    @PostMapping
    public ResponseEntity<PatientHandover> create(
        @Valid @RequestBody HandoverRequestDto requestDto
    ) {
        return ResponseEntity.ok(handoverService.create(requestDto));
    }

    @GetMapping("/{hospitalId}")
    public ResponseEntity<List<PatientHandover>> getByHospital(
        @PathVariable Long hospitalId
    ) {
        return ResponseEntity.ok(handoverService.getByHospital(hospitalId));
    }
}
