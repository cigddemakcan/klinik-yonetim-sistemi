# Akıllı Klinik Yönetim Sistemi

Gerçek bir yazılım geliştirme yaşam döngüsünü simüle eden, mikro hizmet mimarisi temelli klinik yönetim sistemi.

## Teknoloji Stack

| Katman | Teknoloji |
|--------|-----------|
| Backend | Java 21, Spring Boot 3.2 |
| Veritabanı | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Güvenlik | JWT (jjwt 0.11.5) |
| Frontend | HTML5 / CSS3 / Vanilla JS |
| Container | Docker (multi-stage build) |
| CI/CD | GitHub Actions |

## Proje Yapısı

```
klinik-yonetim-sistemi/
├── docs/
│   ├── user-stories.md        # Kullanıcı hikayeleri (S1)
│   └── schema-design.md       # MySQL şema tasarımı (S2)
├── src/main/java/com/klinik/
│   ├── entity/
│   │   ├── Doctor.java        # S3
│   │   ├── Patient.java
│   │   ├── Appointment.java   # S4
│   │   └── Prescription.java
│   ├── repository/
│   │   ├── DoctorRepository.java
│   │   ├── PatientRepository.java   # S8
│   │   └── AppointmentRepository.java
│   ├── service/
│   │   ├── DoctorService.java       # S10
│   │   └── AppointmentService.java  # S6
│   ├── controller/
│   │   ├── DoctorController.java    # S5
│   │   └── PrescriptionController.java # S7
│   └── security/
│       └── TokenService.java        # S9
├── frontend/
│   ├── admin/      # Admin portalı (S13, S16)
│   ├── doctor/     # Doktor portalı (S14, S18)
│   └── patient/    # Hasta portalı (S15, S17)
├── .github/workflows/build.yml  # CI/CD (S12)
├── Dockerfile                   # S11
└── pom.xml
```

## Çalıştırma

```bash
# MySQL veritabanı oluştur
mysql -u root -p -e "CREATE DATABASE klinik_db;"

# Uygulamayı başlat
mvn spring-boot:run

# Docker ile çalıştır
docker build -t klinik-app .
docker run -p 8080:8080 klinik-app
```

## API Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/doctors` | Tüm doktorları listele |
| GET | `/api/doctors/specialty?specialty=Kardiyoloji` | Uzmanlığa göre doktor |
| GET | `/api/doctors/availability?doctorId=1&date=2024-06-01` | Müsait saatler |
| POST | `/api/doctors/login` | Doktor girişi |
| POST | `/api/doctors` | Yeni doktor ekle (admin) |
| POST | `/api/prescriptions` | Reçete kaydet |
| GET | `/api/prescriptions/patient/{id}` | Hasta reçeteleri |
