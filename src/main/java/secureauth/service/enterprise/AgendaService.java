package secureauth.service.enterprise;

import java.sql.Connection;
import java.sql.SQLException;

import secureauth.config.DatabaseConnection;
import secureauth.dao.enterprise.ActividadRecienteDAO;
import secureauth.dao.enterprise.AgendaServicioDAO;
import secureauth.model.CitaServicio;

/**
 * Servicio de negocio para agendamiento de servicios caninos.
 */
public class AgendaService {

    private final AgendaServicioDAO agendaDAO = new AgendaServicioDAO();
    private final ActividadRecienteDAO actividadDAO = new ActividadRecienteDAO();

    /**
     * Inicializa las tablas requeridas por agenda y actividad reciente.
     *
     * @throws SQLException si falla el esquema
     */
    public void initializeSchema() throws SQLException {
        agendaDAO.ensureSchema();
        actividadDAO.ensureSchema();
    }

    /**
     * Registra una cita y publica la actividad reciente correspondiente.
     *
     * @param cita datos completos de la cita
     * @throws SQLException si ocurre un error durante la transacción
     */
    public void registrarCita(CitaServicio cita) throws SQLException {
        validate(cita);
        initializeSchema();
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                registrarCita(conn, cita, "Sistema");
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    /**
     * Registra una cita dentro de una transacción existente.
     *
     * @param conn conexión JDBC transaccional
     * @param cita cita validada
     * @param usuario usuario responsable
     * @throws SQLException si falla el registro
     */
    public void registrarCita(Connection conn, CitaServicio cita, String usuario) throws SQLException {
        validate(cita);
        agendaDAO.insert(conn, cita);
        actividadDAO.insert(conn, "Cita agendada para " + cita.nombrePerro(), "CITA", usuario);
    }

    private void validate(CitaServicio cita) {
        if (cita == null) {
            throw new IllegalArgumentException("La cita no puede ser nula.");
        }
        if (isBlank(cita.nombreDueno()) || isBlank(cita.nombrePerro()) || isBlank(cita.telefono())
                || isBlank(cita.servicio()) || cita.fechaServicio() == null || cita.horaServicio() == null
                || cita.horaRecogida() == null) {
            throw new IllegalArgumentException("Completa los datos obligatorios de la cita.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
