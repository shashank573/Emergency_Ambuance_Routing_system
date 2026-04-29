package com.emergency.routing.controller;

import com.emergency.routing.dto.EmergencyRequestDto;
import com.emergency.routing.dto.EmergencyResponseDto;
import com.emergency.routing.model.EmergencyRequest;
import com.emergency.routing.service.EmergencyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emergency")
public class EmergencyController {

    private final EmergencyService emergencyService;

    public EmergencyController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    @PostMapping("/request")
    public ResponseEntity<EmergencyResponseDto> requestEmergency(
        @Valid @RequestBody EmergencyRequestDto requestDto
    ) {
        return ResponseEntity.ok(
            emergencyService.handleEmergencyRequest(requestDto)
        );
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<EmergencyRequest> getStatus(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyService.getStatus(id));
    }
}
