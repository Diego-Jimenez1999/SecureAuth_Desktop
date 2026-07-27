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
public record RecentActivity(
        int idActividad,
        String descripcion,
        LocalDateTime fechaHora,
        String tipo,
        String usuario,
        String fechaReal,
        String horaReal,
        String timestampReal) {

    /**
     * Constructor de compatibilidad legacy.
     */
    public RecentActivity(int idActividad, String descripcion, LocalDateTime fechaHora, String tipo, String usuario) {
        this(idActividad, descripcion, fechaHora, tipo, usuario,
             fechaHora != null ? fechaHora.toLocalDate().toString() : "",
             fechaHora != null ? fechaHora.toLocalTime().toString() : "",
             fechaHora != null ? fechaHora.toString() : "");
    }
}
