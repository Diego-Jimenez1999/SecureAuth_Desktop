package secureauth.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentDTO(
        Integer id,
        int serviceId,
        String serviceName,
        int ownerId,
        String ownerName,
        int petId,
        String petName,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        String status,
        String notes,
        LocalDateTime createdAt,
        String createdBy) {
}
