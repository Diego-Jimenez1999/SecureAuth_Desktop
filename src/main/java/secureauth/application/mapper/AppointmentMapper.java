package secureauth.application.mapper;

import secureauth.application.dto.AppointmentDTO;
import secureauth.model.Appointment;

public final class AppointmentMapper {

    private AppointmentMapper() {
    }

    public static AppointmentDTO toDTO(Appointment appointment) {
        return new AppointmentDTO(appointment.getId(), appointment.getServiceId(), appointment.getServiceName(),
                appointment.getOwnerId(), appointment.getOwnerName(), appointment.getPetId(), appointment.getPetName(),
                appointment.getAppointmentDate(), appointment.getAppointmentTime(), appointment.getStatus(),
                appointment.getNotes(), appointment.getCreatedAt(), appointment.getCreatedBy());
    }

    public static Appointment toDomain(AppointmentDTO dto) {
        return new Appointment(dto.id(), dto.serviceId(), dto.serviceName(), dto.ownerId(), dto.ownerName(),
                dto.petId(), dto.petName(), dto.appointmentDate(), dto.appointmentTime(), dto.status(), dto.notes(),
                dto.createdAt(), dto.createdBy());
    }
}
