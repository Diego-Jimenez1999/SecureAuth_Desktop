package secureauth.domain.services;

import java.time.LocalDate;
import java.time.LocalTime;

public record ServiceOrderItem(
        int serviceId,
        String serviceName,
        String veterinarian,
        LocalDate serviceDate,
        LocalTime serviceTime,
        int durationMinutes,
        String observations,
        double servicePrice) {
}
