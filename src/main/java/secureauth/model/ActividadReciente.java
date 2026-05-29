package secureauth.model;

import java.time.LocalDateTime;

/**
 * Evento visible en el Home del sistema.
 *
 * @param idActividad identificador del evento
 * @param descripcion texto mostrado al usuario
 * @param fechaHora fecha y hora del evento
 * @param tipo categoría del evento: VENTA, CITA o INVENTARIO
 * @param usuario usuario que originó el evento
 */
public record ActividadReciente(int idActividad, String descripcion, LocalDateTime fechaHora, String tipo,
                                String usuario) {
}
