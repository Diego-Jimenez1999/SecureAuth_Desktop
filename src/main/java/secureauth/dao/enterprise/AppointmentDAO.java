package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;
import secureauth.config.SchemaInspector;
import secureauth.model.Appointment;
import secureauth.model.AppointmentStatus;

/**
 * DAO JDBC para persistir y consultar citas de servicios veterinarios.
 *
 * <p>Centraliza el acceso a la tabla {@code appointments} para mantener el
 * dashboard y el flujo de ventas sincronizados con datos reales de base de
 * datos.</p>
 *
 * @author Diego
 * @version 1.0
 */
public class AppointmentDAO {

    /**
     * Estados operativos soportados por el módulo de citas.
     */
    public static final String STATUS_PENDING = AppointmentStatus.PENDING.databaseValue();
    public static final String STATUS_CONFIRMED = AppointmentStatus.CONFIRMED.databaseValue();
    public static final String STATUS_IN_PROGRESS = AppointmentStatus.IN_PROGRESS.databaseValue();
    public static final String STATUS_DONE = AppointmentStatus.FINALIZED.databaseValue();
    public static final String STATUS_CANCELLED = AppointmentStatus.CANCELLED.databaseValue();
    public static final String STATUS_ARCHIVED = AppointmentStatus.ARCHIVED.databaseValue();

    /**
     * Crea y migra la tabla {@code appointments} si no existe.
     *
     * @throws SQLException si falla la inicialización de esquema
     */
    public void ensureSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS appointments (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        service_id INT NOT NULL,
                        service_name VARCHAR(180) NOT NULL,
                        owner_id INT NOT NULL,
                        owner_name VARCHAR(180) NOT NULL,
                        pet_id INT NOT NULL,
                        pet_name VARCHAR(140) NOT NULL,
                        appointment_date DATE NOT NULL,
                        appointment_time TIME NOT NULL,
                        end_date DATE NULL,
                        end_time TIME NULL,
                        status VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
                        notes VARCHAR(900) NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        created_by VARCHAR(120) NULL,
                        INDEX idx_appointments_status_date (status, appointment_date, appointment_time),
                        INDEX idx_appointments_owner (owner_id),
                        INDEX idx_appointments_pet (pet_id)
                    )
                    """);
            addColumnIfMissing(conn, st, "service_id", "INT NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, st, "service_name", "VARCHAR(180) NOT NULL DEFAULT ''");
            addColumnIfMissing(conn, st, "owner_id", "INT NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, st, "owner_name", "VARCHAR(180) NOT NULL DEFAULT ''");
            addColumnIfMissing(conn, st, "pet_id", "INT NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, st, "pet_name", "VARCHAR(140) NOT NULL DEFAULT ''");
            addColumnIfMissing(conn, st, "appointment_date", "DATE NULL");
            addColumnIfMissing(conn, st, "appointment_time", "TIME NULL");
            addColumnIfMissing(conn, st, "end_date", "DATE NULL");
            addColumnIfMissing(conn, st, "end_time", "TIME NULL");
            addColumnIfMissing(conn, st, "status", "VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE'");
            addColumnIfMissing(conn, st, "notes", "VARCHAR(900) NULL");
            addColumnIfMissing(conn, st, "created_at", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
            addColumnIfMissing(conn, st, "created_by", "VARCHAR(120) NULL");
        }
    }

    /**
     * Inserta una cita nueva y asigna su identificador generado.
     *
     * @param appointment cita validada por la capa de servicio
     * @return cita con id y fecha de creación actualizados
     * @throws SQLException si falla el registro
     */
    public Appointment insert(Appointment appointment) throws SQLException {
        ensureSchema();
        try (Connection conn = DatabaseConnection.getConnection()) {
            insert(conn, appointment);
        }
        return appointment.getId() == null ? appointment : findById(appointment.getId());
    }

    /**
     * Inserta una cita usando una conexión transaccional externa.
     *
     * @param conn conexión con autocommit controlado por el servicio
     * @param appointment cita validada por la capa de servicio
     * @return cita con id generado asignado
     * @throws SQLException si falla el registro
     */
    public Appointment insert(Connection conn, Appointment appointment) throws SQLException {
        String sql = """
                INSERT INTO appointments(service_id, service_name, owner_id, owner_name, pet_id, pet_name,
                                         appointment_date, appointment_time, end_date, end_time, status, notes, created_by)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appointment.getServiceId());
            ps.setString(2, appointment.getServiceName());
            ps.setInt(3, appointment.getOwnerId());
            ps.setString(4, appointment.getOwnerName());
            ps.setInt(5, appointment.getPetId());
            ps.setString(6, appointment.getPetName());
            ps.setDate(7, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(8, Time.valueOf(appointment.getAppointmentTime()));
            ps.setDate(9, appointment.getEndDate() != null ? Date.valueOf(appointment.getEndDate()) : Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(10, appointment.getEndTime() != null ? Time.valueOf(appointment.getEndTime()) : Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(11, appointment.getStatus());
            ps.setString(12, appointment.getNotes());
            ps.setString(13, appointment.getCreatedBy());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    appointment.setId(keys.getInt(1));
                }
            }
        }
        return appointment;
    }

    /**
     * Busca una cita por identificador.
     *
     * @param id identificador de la cita
     * @return cita encontrada o {@code null}
     * @throws SQLException si falla la consulta
     */
    public Appointment findById(int id) throws SQLException {
        ensureSchema();
        String sql = """
                SELECT id, service_id, service_name, owner_id, owner_name, pet_id, pet_name,
                       appointment_date, appointment_time, end_date, end_time, status, notes, created_at, created_by
                FROM appointments
                WHERE id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Lista citas ordenadas por prioridad operativa, fecha y hora.
     *
     * @param limit cantidad máxima de filas
     * @return citas para el dashboard
     * @throws SQLException si falla la consulta
     */
    public List<Appointment> findForDashboard(int limit) throws SQLException {
        ensureSchema();
        String sql = """
                SELECT id, service_id, service_name, owner_id, owner_name, pet_id, pet_name,
                       appointment_date, appointment_time, end_date, end_time, status, notes, created_at, created_by
                FROM appointments
                WHERE status IN ('PENDIENTE', 'CONFIRMADA', 'EN_PROCESO')
                ORDER BY appointment_date ASC,
                         appointment_time ASC
                LIMIT ?
                """;
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
            }
        }
        return appointments;
    }

    /**
     * Consulta citas con criterios avanzados de búsqueda, filtros de fecha, estado y ordenamiento.
     *
     * @param query término de búsqueda (nombre cliente, mascota, servicio)
     * @param dateFilter filtro de fecha ("HOY", "SEMANA", "MES", "ANIO", "TODAS")
     * @param statusFilter filtro de estado ("PENDIENTES", "FINALIZADAS", "CANCELADAS", "ARCHIVADAS", "TODAS")
     * @return lista de citas filtradas
     * @throws SQLException si falla la base de datos
     */
    public List<Appointment> findAdvanced(String query, String dateFilter, String statusFilter) throws SQLException {
        ensureSchema();
        StringBuilder sql = new StringBuilder("""
                SELECT id, service_id, service_name, owner_id, owner_name, pet_id, pet_name,
                       appointment_date, appointment_time, end_date, end_time, status, notes, created_at, created_by
                FROM appointments
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (owner_name LIKE ? OR pet_name LIKE ? OR service_name LIKE ? OR notes LIKE ?)");
            String likeParam = "%" + query.trim() + "%";
            params.add(likeParam);
            params.add(likeParam);
            params.add(likeParam);
            params.add(likeParam);
        }

        if (dateFilter != null) {
            switch (dateFilter.toUpperCase()) {
                case "HOY" -> {
                    sql.append(" AND (appointment_date = CURRENT_DATE() OR (end_date IS NOT NULL AND appointment_date <= CURRENT_DATE() AND end_date >= CURRENT_DATE()))");
                }
                case "SEMANA" -> {
                    sql.append(" AND (YEARWEEK(appointment_date, 1) = YEARWEEK(CURRENT_DATE(), 1) OR (end_date IS NOT NULL AND appointment_date <= CURRENT_DATE() + INTERVAL (7 - DAYOFWEEK(CURRENT_DATE())) DAY AND end_date >= CURRENT_DATE() - INTERVAL (DAYOFWEEK(CURRENT_DATE()) - 1) DAY))");
                }
                case "MES" -> {
                    sql.append(" AND (YEAR(appointment_date) = YEAR(CURRENT_DATE()) AND MONTH(appointment_date) = MONTH(CURRENT_DATE()))");
                }
                case "ANIO", "AÑO" -> {
                    sql.append(" AND YEAR(appointment_date) = YEAR(CURRENT_DATE())");
                }
            }
        }

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("TODAS")) {
            switch (statusFilter.toUpperCase()) {
                case "PENDIENTES" -> sql.append(" AND status IN ('PENDIENTE', 'CONFIRMADA', 'EN_PROCESO')");
                case "FINALIZADAS", "FINALIZADOS" -> sql.append(" AND status IN ('FINALIZADO', 'FINALIZADA', 'REALIZADO')");
                case "CANCELADAS", "CANCELADOS" -> sql.append(" AND status IN ('CANCELADA', 'CANCELADO')");
                case "ARCHIVADAS", "ARCHIVADOS" -> sql.append(" AND status = 'ARCHIVADA'");
            }
        }

        sql.append(" ORDER BY appointment_date DESC, appointment_time DESC");

        List<Appointment> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /**
     * Lista citas activas del módulo veterinario para una fecha.
     *
     * @param date fecha de agenda
     * @return citas pendientes, confirmadas o en proceso
     * @throws SQLException si falla la consulta
     */
    public List<Appointment> findActiveByDate(LocalDate date) throws SQLException {
        ensureSchema();
        String sql = """
                SELECT id, service_id, service_name, owner_id, owner_name, pet_id, pet_name,
                       appointment_date, appointment_time, end_date, end_time, status, notes, created_at, created_by
                FROM appointments
                WHERE (
                    (end_date IS NULL AND appointment_date = ?)
                    OR (end_date IS NOT NULL AND appointment_date <= ? AND end_date >= ?)
                )
                  AND status IN (?, ?, ?)
                ORDER BY appointment_time
                """;
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setDate(2, Date.valueOf(date));
            ps.setDate(3, Date.valueOf(date));
            ps.setString(4, STATUS_PENDING);
            ps.setString(5, STATUS_CONFIRMED);
            ps.setString(6, STATUS_IN_PROGRESS);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
            }
        }
        return appointments;
    }

    /**
     * Actualiza los datos de una cita completa.
     *
     * @param appointment la cita con sus nuevos datos
     * @return true si se actualizó correctamente
     * @throws SQLException si falla la base de datos
     */
    public boolean update(Appointment appointment) throws SQLException {
        ensureSchema();
        String sql = """
                UPDATE appointments
                SET appointment_date = ?,
                    appointment_time = ?,
                    end_date = ?,
                    end_time = ?,
                    created_by = ?,
                    service_name = ?,
                    notes = ?,
                    status = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(2, Time.valueOf(appointment.getAppointmentTime()));
            ps.setDate(3, appointment.getEndDate() != null ? Date.valueOf(appointment.getEndDate()) : Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(4, appointment.getEndTime() != null ? Time.valueOf(appointment.getEndTime()) : Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(5, appointment.getCreatedBy());
            ps.setString(6, appointment.getServiceName());
            ps.setString(7, appointment.getNotes());
            ps.setString(8, appointment.getStatus());
            ps.setInt(9, appointment.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el estado de una cita.
     *
     * @param appointmentId identificador de cita
     * @param status nuevo estado
     * @return {@code true} si una fila fue modificada
     * @throws SQLException si falla la actualización
     */
    public boolean updateStatus(int appointmentId, String status) throws SQLException {
        ensureSchema();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE appointments SET status = ? WHERE id = ?")) {
            ps.setString(1, AppointmentStatus.normalizeForStorage(status));
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cuenta citas activas programadas.
     *
     * @return total de citas pendientes o en proceso
     * @throws SQLException si falla la consulta
     */
    public int countScheduled() throws SQLException {
        return countByStatuses(STATUS_PENDING, STATUS_CONFIRMED, STATUS_IN_PROGRESS);
    }

    /**
     * Cuenta servicios finalizados.
     *
     * @return total de citas realizadas
     * @throws SQLException si falla la consulta
     */
    public int countFinished() throws SQLException {
        return countByStatuses(STATUS_DONE, "FINALIZADA", "REALIZADO");
    }

    private int countByStatuses(String... statuses) throws SQLException {
        ensureSchema();
        String placeholders = String.join(",", java.util.Collections.nCopies(statuses.length, "?"));
        String sql = "SELECT COUNT(*) FROM appointments WHERE status IN (" + placeholders + ")";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < statuses.length; i++) {
                ps.setString(i + 1, statuses[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Date endDateVal = rs.getDate("end_date");
        Time endTimeVal = rs.getTime("end_time");
        LocalDate startDate = rs.getDate("appointment_date").toLocalDate();
        LocalTime startTime = rs.getTime("appointment_time").toLocalTime();
        LocalDate endDate = endDateVal == null ? startDate : endDateVal.toLocalDate();
        LocalTime endTime = endTimeVal == null ? startTime : endTimeVal.toLocalTime();

        return new Appointment(
                rs.getInt("id"),
                rs.getInt("service_id"),
                rs.getString("service_name"),
                rs.getInt("owner_id"),
                rs.getString("owner_name"),
                rs.getInt("pet_id"),
                rs.getString("pet_name"),
                startDate,
                startTime,
                endDate,
                endTime,
                rs.getString("status"),
                rs.getString("notes"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                rs.getString("created_by"));
    }

    private void addColumnIfMissing(Connection conn, Statement st, String column, String definition)
            throws SQLException {
        if (!SchemaInspector.columnExists(conn, "appointments", column)) {
            st.execute("ALTER TABLE appointments ADD COLUMN " + column + " " + definition);
        }
    }

    public record ServicePopularity(String name, int count) {}

    /**
     * Obtiene los servicios más populares basados en el conteo de citas.
     *
     * @param limit cantidad máxima de registros a retornar
     * @return lista de objetos ServicePopularity
     * @throws SQLException si ocurre un error en la base de datos
     */
    public List<ServicePopularity> getPopularServices(int limit) throws SQLException {
        ensureSchema();
        List<ServicePopularity> popularServices = new ArrayList<>();
        String sql = """
                SELECT service_name, COUNT(*) as qty
                FROM appointments
                GROUP BY service_name
                ORDER BY qty DESC
                LIMIT ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    popularServices.add(new ServicePopularity(rs.getString(1), rs.getInt(2)));
                }
            }
        }
        return popularServices;
    }
}
