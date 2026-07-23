package secureauth.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ServiceOrderItemDTO(
        int serviceId,
        String serviceName,
        String veterinarian,
        LocalDate serviceDate,
        LocalTime serviceTime,
        LocalDate endDate,
        LocalTime endTime,
        int durationMinutes,
        String observations,
        double servicePrice) {

    /**
     * Constructor secundario de compatibilidad para mapear ServiceOrderItemDTO sin rango final explícito.
     */
    public ServiceOrderItemDTO(
            int serviceId,
            String serviceName,
            String veterinarian,
            LocalDate serviceDate,
            LocalTime serviceTime,
            int durationMinutes,
            String observations,
            double servicePrice) {
        this(serviceId, serviceName, veterinarian, serviceDate, serviceTime,
                serviceDate, serviceTime != null ? serviceTime.plusMinutes(durationMinutes) : null,
                durationMinutes, observations, servicePrice);
    }
}
