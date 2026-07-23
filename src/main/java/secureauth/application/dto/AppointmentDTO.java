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
        LocalDate endDate,
        LocalTime endTime,
        String status,
        String notes,
        LocalDateTime createdAt,
        String createdBy) {

    /**
     * Constructor secundario de compatibilidad que mapea end date y end time
     * a partir de los datos de inicio.
     */
    public AppointmentDTO(
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
        this(id, serviceId, serviceName, ownerId, ownerName, petId, petName,
                appointmentDate, appointmentTime, appointmentDate, appointmentTime,
                status, notes, createdAt, createdBy);
    }
}
