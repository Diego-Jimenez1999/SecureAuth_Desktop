package secureauth.application.validation;

import secureauth.application.dto.AppointmentDTO;

public class AppointmentValidator {

    public void validate(AppointmentDTO appointment) {
        if (appointment == null || appointment.ownerId() <= 0 || appointment.petId() <= 0
                || appointment.appointmentDate() == null || appointment.appointmentTime() == null
                || appointment.serviceName() == null || appointment.serviceName().isBlank()) {
            throw new IllegalArgumentException("La cita del servicio esta incompleta.");
        }
        if (appointment.endDate() != null && appointment.endTime() != null) {
            secureauth.shared.util.ServiceScheduleHelper.validateInterval(
                appointment.appointmentDate(), appointment.appointmentTime(),
                appointment.endDate(), appointment.endTime()
            );
        }
    }
}
