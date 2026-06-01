package com.klinik.service;

import com.klinik.entity.Doctor;
import com.klinik.repository.AppointmentRepository;
import com.klinik.repository.DoctorRepository;
import com.klinik.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService,
                         PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    // Belirli bir tarihte doktor için müsait zaman dilimlerini döner
    public List<String> getAvailableTimesForDate(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<String> allSlots = doctor.getAvailableTimes();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        Set<String> bookedTimes = appointmentRepository
                .findByDoctorIdAndDate(doctorId, startOfDay, endOfDay)
                .stream()
                .map(a -> a.getAppointmentTime().toLocalTime().toString())
                .collect(Collectors.toSet());

        return allSlots.stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .collect(Collectors.toList());
    }

    // Doktor giriş kimlik bilgilerini doğrular ve yapılandırılmış yanıt döner
    public Map<String, Object> login(String email, String password) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, doctor.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = tokenService.generateToken(email);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("doctorId", doctor.getId());
        response.put("fullName", doctor.getFullName());
        response.put("specialty", doctor.getSpecialty());
        return response;
    }

    // Tüm doktorları listeler
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // Uzmanlığa göre doktor listeler
    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        return doctorRepository.findBySpecialty(specialty);
    }

    // Yeni doktor ekler
    public Doctor addDoctor(Doctor doctor) {
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        return doctorRepository.save(doctor);
    }
}
