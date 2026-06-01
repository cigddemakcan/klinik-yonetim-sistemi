package com.klinik.repository;

import com.klinik.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Derived query — e-posta ile hasta getir
    Optional<Patient> findByEmail(String email);

    // Custom query — e-posta VEYA telefon numarası ile hasta getir
    @Query("SELECT p FROM Patient p WHERE p.email = :email OR p.phone = :phone")
    Optional<Patient> findByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);
}
