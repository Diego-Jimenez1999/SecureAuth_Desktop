package secureauth.domain.services;

import java.time.LocalDate;
import java.time.LocalTime;

public record ServiceOrderItem(
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
     * Constructor secundario de compatibilidad para mapear ServiceOrderItem sin rango final explícito.
     */
    public ServiceOrderItem(
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
