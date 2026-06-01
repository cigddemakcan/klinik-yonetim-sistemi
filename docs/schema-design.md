# Schema Design — Akıllı Klinik Yönetim Sistemi

## Veritabanı: `klinik_db`

---

## Tablolar

### 1. `doctors` — Doktorlar

```sql
CREATE TABLE doctors (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100)        NOT NULL,
    email       VARCHAR(150)        NOT NULL UNIQUE,
    password    VARCHAR(255)        NOT NULL,
    specialty   VARCHAR(100)        NOT NULL,
    phone       VARCHAR(20),
    created_at  DATETIME            DEFAULT CURRENT_TIMESTAMP
);
```

| Alan | Tür | Açıklama |
|------|-----|----------|
| id | BIGINT PK | Birincil anahtar |
| full_name | VARCHAR(100) | Doktorun tam adı |
| email | VARCHAR(150) UNIQUE | Giriş için e-posta |
| password | VARCHAR(255) | Şifrelenmiş parola |
| specialty | VARCHAR(100) | Uzmanlık alanı |
| phone | VARCHAR(20) | Telefon numarası |
| created_at | DATETIME | Kayıt tarihi |

---

### 2. `patients` — Hastalar

```sql
CREATE TABLE patients (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100)        NOT NULL,
    email       VARCHAR(150)        NOT NULL UNIQUE,
    password    VARCHAR(255)        NOT NULL,
    phone       VARCHAR(20)         NOT NULL UNIQUE,
    birth_date  DATE,
    created_at  DATETIME            DEFAULT CURRENT_TIMESTAMP
);
```

| Alan | Tür | Açıklama |
|------|-----|----------|
| id | BIGINT PK | Birincil anahtar |
| full_name | VARCHAR(100) | Hastanın tam adı |
| email | VARCHAR(150) UNIQUE | Giriş için e-posta |
| password | VARCHAR(255) | Şifrelenmiş parola |
| phone | VARCHAR(20) UNIQUE | Telefon numarası |
| birth_date | DATE | Doğum tarihi |
| created_at | DATETIME | Kayıt tarihi |

---

### 3. `appointments` — Randevular

```sql
CREATE TABLE appointments (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id        BIGINT      NOT NULL,
    patient_id       BIGINT      NOT NULL,
    appointment_time DATETIME    NOT NULL,
    status           ENUM('PENDING','CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'PENDING',
    notes            TEXT,
    created_at       DATETIME    DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (doctor_id)  REFERENCES doctors(id)  ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
```

| Alan | Tür | Açıklama |
|------|-----|----------|
| id | BIGINT PK | Birincil anahtar |
| doctor_id | BIGINT FK | `doctors.id` referansı |
| patient_id | BIGINT FK | `patients.id` referansı |
| appointment_time | DATETIME | Randevu tarih/saat |
| status | ENUM | Randevu durumu |
| notes | TEXT | Notlar |
| created_at | DATETIME | Oluşturma tarihi |

---

### 4. `prescriptions` — Reçeteler

```sql
CREATE TABLE prescriptions (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id    BIGINT       NOT NULL,
    patient_id   BIGINT       NOT NULL,
    medication   VARCHAR(255) NOT NULL,
    dosage       VARCHAR(100),
    instructions TEXT,
    issued_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (doctor_id)  REFERENCES doctors(id)  ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
```

| Alan | Tür | Açıklama |
|------|-----|----------|
| id | BIGINT PK | Birincil anahtar |
| doctor_id | BIGINT FK | `doctors.id` referansı |
| patient_id | BIGINT FK | `patients.id` referansı |
| medication | VARCHAR(255) | İlaç adı |
| dosage | VARCHAR(100) | Doz bilgisi |
| instructions | TEXT | Kullanım talimatları |
| issued_at | DATETIME | Reçete tarihi |

---

### 5. `doctor_available_times` — Doktor Müsait Saatleri

```sql
CREATE TABLE doctor_available_times (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id  BIGINT       NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME         NOT NULL,
    end_time   TIME         NOT NULL,

    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);
```

| Alan | Tür | Açıklama |
|------|-----|----------|
| id | BIGINT PK | Birincil anahtar |
| doctor_id | BIGINT FK | `doctors.id` referansı |
| day_of_week | VARCHAR(10) | Gün adı (MONDAY vb.) |
| start_time | TIME | Başlangıç saati |
| end_time | TIME | Bitiş saati |

---

## İlişki Diyagramı

```
doctors ──────┬──── appointments ────┬──── patients
              │                      │
              └──── prescriptions ───┘
              │
              └──── doctor_available_times
```

## Saklı Prosedürler

```sql
-- Günlük randevu raporu
DELIMITER //
CREATE PROCEDURE GetDailyAppointmentReportByDoctor(IN report_date DATE, IN doc_id BIGINT)
BEGIN
    SELECT a.id, p.full_name AS patient_name, a.appointment_time, a.status
    FROM appointments a
    JOIN patients p ON a.patient_id = p.id
    WHERE DATE(a.appointment_time) = report_date
      AND a.doctor_id = doc_id
    ORDER BY a.appointment_time;
END //
DELIMITER ;

-- Aylık en çok hastası olan doktor
DELIMITER //
CREATE PROCEDURE GetDoctorWithMostPatientsByMonth(IN p_year INT, IN p_month INT)
BEGIN
    SELECT d.full_name, COUNT(a.patient_id) AS patient_count
    FROM appointments a
    JOIN doctors d ON a.doctor_id = d.id
    WHERE YEAR(a.appointment_time) = p_year
      AND MONTH(a.appointment_time) = p_month
      AND a.status = 'COMPLETED'
    GROUP BY d.id, d.full_name
    ORDER BY patient_count DESC
    LIMIT 1;
END //
DELIMITER ;

-- Yıllık en çok hastası olan doktor
DELIMITER //
CREATE PROCEDURE GetDoctorWithMostPatientsByYear(IN p_year INT)
BEGIN
    SELECT d.full_name, COUNT(a.patient_id) AS patient_count
    FROM appointments a
    JOIN doctors d ON a.doctor_id = d.id
    WHERE YEAR(a.appointment_time) = p_year
      AND a.status = 'COMPLETED'
    GROUP BY d.id, d.full_name
    ORDER BY patient_count DESC
    LIMIT 1;
END //
DELIMITER ;
```
