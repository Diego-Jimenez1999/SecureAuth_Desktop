package secureauth.service.enterprise;

import java.sql.SQLException;
import java.util.List;

import secureauth.dao.enterprise.ActividadRecienteDAO;
import secureauth.model.ActividadReciente;

/**
 * Servicio de consulta y registro de actividad reciente.
 *
 * <p>Expone una API simple para que Home, ventas, inventario y agenda compartan
 * el mismo historial de eventos.</p>
 */
public class ActividadRecienteService {

    private final ActividadRecienteDAO dao;

    public ActividadRecienteService() {
        this(new ActividadRecienteDAO());
    }

    public ActividadRecienteService(ActividadRecienteDAO dao) {
        this.dao = dao;
    }

    /**
     * Inicializa la tabla de actividad reciente.
     *
     * @throws SQLException si falla el esquema
     */
    public void initializeSchema() throws SQLException {
        dao.ensureSchema();
    }

    /**
     * Registra un evento de actividad reciente.
     *
     * @param descripcion texto visible del evento
     * @param tipo tipo de evento
     * @param usuario usuario responsable
     * @throws SQLException si falla el registro
     */
    public void registrarActividad(String descripcion, String tipo, String usuario) throws SQLException {
        dao.ensureSchema();
        dao.insert(descripcion, tipo, usuario);
    }

    /**
     * Lista los eventos más recientes.
     *
     * @param limit máximo de filas
     * @return eventos recientes
     * @throws SQLException si falla la consulta
     */
    public List<ActividadReciente> recientes(int limit) throws SQLException {
        dao.ensureSchema();
        return dao.findRecent(limit);
    }

    /**
     * Consulta registros de auditoría avanzados.
     *
     * @param query búsqueda textual
     * @param moduleFilter filtro de módulo
     * @param dateFilter filtro de fecha
     * @param userFilter filtro de usuario
     * @return registros de auditoría correspondientes
     * @throws SQLException si falla la base de datos
     */
    public List<ActividadReciente> findAdvanced(String query, String moduleFilter, String dateFilter, String userFilter) throws SQLException {
        dao.ensureSchema();
        return dao.findAdvanced(query, moduleFilter, dateFilter, userFilter);
    }
}
