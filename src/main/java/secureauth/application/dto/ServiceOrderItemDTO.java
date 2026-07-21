package secureauth.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ServiceOrderItemDTO(
        int serviceId,
        String serviceName,
        String veterinarian,
        LocalDate serviceDate,
        LocalTime serviceTime,
        int durationMinutes,
        String observations,
        double servicePrice) {
}
