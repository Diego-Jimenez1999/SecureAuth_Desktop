package secureauth.application.validation;

import secureauth.application.dto.AppointmentDTO;

public class AppointmentValidator {

    public void validate(AppointmentDTO appointment) {
        if (appointment == null || appointment.ownerId() <= 0 || appointment.petId() <= 0
                || appointment.appointmentDate() == null || appointment.appointmentTime() == null
                || appointment.serviceName() == null || appointment.serviceName().isBlank()) {
            throw new IllegalArgumentException("La cita del servicio esta incompleta.");
        }
    }
}
