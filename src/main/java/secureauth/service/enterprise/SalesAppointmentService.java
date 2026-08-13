package secureauth.service.enterprise;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;
import secureauth.dao.enterprise.RecentActivityDAO;
import secureauth.dao.enterprise.SalesAppointmentDAO;
import secureauth.dao.enterprise.AppointmentDAO;
import secureauth.model.Appointment;
import secureauth.model.SalesAppointment;

/**
 * Servicio de negocio para agendamiento de servicios caninos (Sales Appointments).
 */
public class SalesAppointmentService {

    private final SalesAppointmentDAO agendaDAO;
    private final AppointmentDAO appointmentDAO;
    private final RecentActivityDAO actividadDAO;

    public SalesAppointmentService() {
        this(new SalesAppointmentDAO(), new AppointmentDAO(), new RecentActivityDAO());
    }

    public SalesAppointmentService(SalesAppointmentDAO agendaDAO, RecentActivityDAO actividadDAO) {
        this(agendaDAO, new AppointmentDAO(), actividadDAO);
    }

    public SalesAppointmentService(SalesAppointmentDAO agendaDAO, AppointmentDAO appointmentDAO, RecentActivityDAO actividadDAO) {
        this.agendaDAO = agendaDAO;
        this.appointmentDAO = appointmentDAO;
        this.actividadDAO = actividadDAO;
    }

    /**
     * Inicializa las tablas requeridas por agenda y actividad reciente.
     *
     * @throws SQLException si falla el esquema
     */
    public void initializeSchema() throws SQLException {
        agendaDAO.ensureSchema();
        appointmentDAO.ensureSchema();
        actividadDAO.ensureSchema();
    }

    /**
     * Registra una cita y publica la actividad reciente correspondiente.
     *
     * @param cita datos completos de la cita
     * @throws SQLException si ocurre un error durante la transacción
     */
    public void registerAppointment(SalesAppointment cita) throws SQLException {
        validate(cita);
        if (hasConflict(cita.date(), cita.startTime(), cita.pickupTime())) {
            throw new IllegalArgumentException("Ya existe una cita activa en ese horario.");
        }
        initializeSchema();
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                registerAppointment(conn, cita, "Sistema");
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
    public void registerAppointment(Connection conn, SalesAppointment cita, String usuario) throws SQLException {
        validate(cita);
        agendaDAO.insert(conn, cita);
        actividadDAO.insert(conn, "Cita agendada para " + cita.petName(), "CITA", usuario);
    }

    /**
     * Verifica si una ventana horaria se cruza con citas activas.
     *
     * @param date fecha
     * @param start hora inicial
     * @param end hora final
     * @return true si hay solapamiento
     * @throws SQLException si falla la consulta
     */
    public boolean hasConflict(LocalDate date, LocalTime start, LocalTime end) throws SQLException {
        validateRange(date, start, end);
        for (TimeBlock block : busyBlocks(date)) {
            if (overlaps(start, end, block.start(), block.end())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sugiere horarios libres en bloques de 30 minutos dentro de la jornada.
     *
     * @param date fecha
     * @param durationMinutes duración requerida
     * @param maxSuggestions máximo de opciones
     * @return horarios iniciales disponibles
     * @throws SQLException si falla la consulta
     */
    public List<LocalTime> suggestAvailableTimes(LocalDate date, int durationMinutes, int maxSuggestions)
            throws SQLException {
        if (date == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Fecha y duración válidas son requeridas.");
        }
        List<TimeBlock> blocks = busyBlocks(date);
        List<LocalTime> suggestions = new ArrayList<>();
        LocalTime candidate = LocalTime.of(8, 0);
        LocalTime close = LocalTime.of(18, 0);
        LocalDateTime now = LocalDateTime.now();
        while (!candidate.plusMinutes(durationMinutes).isAfter(close) && suggestions.size() < maxSuggestions) {
            LocalTime end = candidate.plusMinutes(durationMinutes);
            LocalDateTime candidateDateTime = LocalDateTime.of(date, candidate);
            boolean future = !candidateDateTime.isBefore(now);
            boolean available = future;
            for (TimeBlock block : blocks) {
                if (overlaps(candidate, end, block.start(), block.end())) {
                    available = false;
                    break;
                }
            }
            if (available) {
                suggestions.add(candidate);
            }
            candidate = candidate.plusMinutes(30);
        }
        return suggestions;
    }

    private List<TimeBlock> busyBlocks(LocalDate date) throws SQLException {
        initializeSchema();
        List<TimeBlock> blocks = new ArrayList<>();
        for (SalesAppointment cita : agendaDAO.findActiveByDate(date)) {
            blocks.add(new TimeBlock(cita.startTime(), cita.pickupTime(), cita.service()));
        }
        for (Appointment appointment : appointmentDAO.findActiveByDate(date)) {
            LocalTime start = appointment.getAppointmentTime();
            blocks.add(new TimeBlock(start, start.plusMinutes(60), appointment.getServiceName()));
        }
        return blocks;
    }

    private void validate(SalesAppointment cita) {
        if (cita == null) {
            throw new IllegalArgumentException("La cita no puede ser nula.");
        }
        if (isBlank(cita.ownerName()) || isBlank(cita.petName()) || isBlank(cita.phone())
                || isBlank(cita.service()) || cita.date() == null || cita.startTime() == null
                || cita.pickupTime() == null) {
            throw new IllegalArgumentException("Completa los datos obligatorios de la cita.");
        }
        if (LocalDateTime.of(cita.date(), cita.startTime()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se permiten horas pasadas para la cita.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void validateRange(LocalDate date, LocalTime start, LocalTime end) {
        if (date == null || start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("La hora final debe ser posterior a la hora inicial.");
        }
    }

    private boolean overlaps(LocalTime start, LocalTime end, LocalTime busyStart, LocalTime busyEnd) {
        return start.isBefore(busyEnd) && end.isAfter(busyStart);
    }

    private record TimeBlock(LocalTime start, LocalTime end, String label) {
    }
}
