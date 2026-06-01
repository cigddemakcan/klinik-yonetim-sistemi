package com.klinik.controller;

import com.klinik.entity.Doctor;
import com.klinik.entity.Patient;
import com.klinik.entity.Prescription;
import com.klinik.repository.DoctorRepository;
import com.klinik.repository.PatientRepository;
import com.klinik.repository.PrescriptionRepository;
import com.klinik.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    private final PrescriptionRepository prescriptionRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final TokenService tokenService;

    public PrescriptionController(PrescriptionRepository prescriptionRepository,
                                  DoctorRepository doctorRepository,
                                  PatientRepository patientRepository,
                                  TokenService tokenService) {
        this.prescriptionRepository = prescriptionRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.tokenService = tokenService;
    }

    // POST — token ve istek gövdesi doğrulaması ile reçete kaydeder
    @PostMapping
    public ResponseEntity<?> savePrescription(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody Prescription prescriptionRequest) {

        // Token doğrulama
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        if (!tokenService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid or expired token"));
        }

        // Doktor ve hastayı doğrula
        Doctor doctor = doctorRepository.findById(prescriptionRequest.getDoctor().getId())
                .orElse(null);
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Doctor not found"));
        }

        Patient patient = patientRepository.findById(prescriptionRequest.getPatient().getId())
                .orElse(null);
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Patient not found"));
        }

        prescriptionRequest.setDoctor(doctor);
        prescriptionRequest.setPatient(patient);

        Prescription saved = prescriptionRepository.save(prescriptionRequest);

        // Yapılandırılmış başarı yanıtı
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "success", true,
                        "message", "Prescription saved successfully",
                        "prescriptionId", saved.getId()
                ));
    }

    // Hastanın reçetelerini getir
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPrescriptionsByPatient(
            @PathVariable Long patientId,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        String token = authHeader.substring(7);
        if (!tokenService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid token"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "prescriptions", prescriptionRepository.findByPatientId(patientId)
        ));
    }
}
