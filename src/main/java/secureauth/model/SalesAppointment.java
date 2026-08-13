package secureauth.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cita de servicio canino registrada desde una venta de servicios.
 *
 * <p>La cita alimenta la tabla {@code citas_servicio} y la actividad reciente
 * del Home. El estado por defecto recomendado es {@code AGENDADA}.</p>
 *
 * @param id identificador de base de datos
 * @param ownerName nombre del responsable
 * @param petName nombre de la mascota
 * @param breed raza de la mascota
 * @param phone teléfono de contacto
 * @param service servicio solicitado
 * @param date fecha de la cita
 * @param startTime hora de inicio
 * @param pickupTime hora estimada de recogida
 * @param notes notas adicionales
 * @param status estado de la cita
 */
public record SalesAppointment(Integer id, String ownerName, String petName, String breed, String phone,
                           String service, LocalDate date, LocalTime startTime,
                           LocalTime pickupTime, String notes, String status) {
}
