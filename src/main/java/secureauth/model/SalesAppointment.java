package secureauth.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cita de servicio canino registrada desde una venta de servicios.
 *
 * <p>La cita alimenta la tabla {@code citas_servicio} y la actividad reciente
 * del Home. El estado por defecto recomendado es {@code AGENDADA}.</p>
 *
 * @param idCita identificador de base de datos
 * @param nombreDueno nombre del responsable
 * @param nombrePerro nombre de la mascota
 * @param raza raza de la mascota
 * @param telefono teléfono de contacto
 * @param servicio servicio solicitado
 * @param fechaServicio fecha de la cita
 * @param horaServicio hora de inicio
 * @param horaRecogida hora estimada de recogida
 * @param observaciones notas adicionales
 * @param estado estado de la cita
 */
public record SalesAppointment(Integer idCita, String nombreDueno, String nombrePerro, String raza, String telefono,
                           String servicio, LocalDate fechaServicio, LocalTime horaServicio,
                           LocalTime horaRecogida, String observaciones, String estado) {
}
