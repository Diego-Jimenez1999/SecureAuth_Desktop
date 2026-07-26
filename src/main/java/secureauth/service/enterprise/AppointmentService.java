package secureauth.service.enterprise;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import secureauth.dao.PetDAO;
import secureauth.dao.enterprise.ActividadRecienteDAO;
import secureauth.dao.enterprise.AppointmentDAO;
import secureauth.model.Appointment;
import secureauth.model.AppointmentStatus;
import secureauth.model.Pet;

/**
 * Servicio de negocio para citas veterinarias creadas desde ventas.
 *
 * <p>Valida reglas de agenda, coordina persistencia JDBC y emite eventos para
 * que el dashboard se actualice sin reiniciar la aplicación.</p>
 *
 * @author Diego
 * @version 1.0
 */
public class AppointmentService {

    private static final PropertyChangeSupport EVENTS = new PropertyChangeSupport(AppointmentService.class);

    private final AppointmentDAO appointmentDAO;
    private final PetDAO petDAO;
    private final ActividadRecienteDAO actividadDAO;
    private final EnterpriseContext context;

    /**
     * Crea el servicio con DAOs por defecto.
     */
    public AppointmentService() {
        this(new AppointmentDAO(), new PetDAO(), new ActividadRecienteDAO());
    }

    /**
     * Crea el servicio con dependencias inyectadas.
     *
     * @param appointmentDAO DAO de citas
     * @param petDAO DAO de mascotas
     * @param actividadDAO DAO de actividad reciente
     */
    public AppointmentService(AppointmentDAO appointmentDAO, PetDAO petDAO, ActividadRecienteDAO actividadDAO) {
        this.appointmentDAO = appointmentDAO;
        this.petDAO = petDAO;
        this.actividadDAO = actividadDAO;
        this.context = EnterpriseContext.getInstance();
    }

    /**
     * Suscribe un listener global a cambios de citas.
     *
     * @param listener listener Swing o de aplicación
     */
    public static void addAppointmentChangeListener(PropertyChangeListener listener) {
        EVENTS.addPropertyChangeListener("appointments", listener);
    }

    /**
     * Elimina una suscripción global a cambios de citas.
     *
     * @param listener listener previamente registrado
     */
    public static void removeAppointmentChangeListener(PropertyChangeListener listener) {
        EVENTS.removePropertyChangeListener("appointments", listener);
    }

    /**
     * Inicializa las tablas requeridas por citas y actividad reciente.
     *
     * @throws SQLException si falla el esquema
     */
    public void initializeSchema() throws SQLException {
        appointmentDAO.ensureSchema();
        actividadDAO.ensureSchema();
        petDAO.ensureSchema();
    }

    /**
     * Registra una cita nueva con estado inicial {@code PENDIENTE}.
     *
     * @param appointment cita a guardar
     * @return cita persistida
     * @throws SQLException si falla la persistencia
     */
    public Appointment registerAppointment(Appointment appointment) throws SQLException {
        prepareForRegistration(appointment);
        Appointment saved = appointmentDAO.insert(appointment);
        actividadDAO.ensureSchema();
        String auditDesc = "Cita agendada | Mascota: " + saved.getPetName() + " | Servicio: " + saved.getServiceName() + " (" + saved.getAppointmentDate() + " " + saved.getAppointmentTime() + ")";
        actividadDAO.insert(auditDesc, "CITAS", saved.getCreatedBy());
        fireChanged();
        return saved;
    }

    /**
     * Valida y normaliza una cita sin persistirla. Se usa desde ventas para
     * preparar las citas antes de confirmar el pago y guardarlas luego dentro
     * de la transacción de venta.
     *
     * @param appointment cita capturada en UI
     * @return la misma cita normalizada
     */
    public Appointment prepareForRegistration(Appointment appointment) {
        validate(appointment);
        appointment.setStatus(AppointmentDAO.STATUS_PENDING);
        appointment.setCreatedAt(LocalDateTime.now());
        if (isBlank(appointment.getCreatedBy())) {
            appointment.setCreatedBy("Sistema");
        }
        return appointment;
    }

    /**
     * Lista citas para el dashboard.
     *
     * @param limit máximo de filas a retornar
     * @return citas ordenadas por estado, fecha y hora
     * @throws SQLException si falla la consulta
     */
    public List<Appointment> findDashboardAppointments(int limit) throws SQLException {
        return appointmentDAO.findForDashboard(limit);
    }

    /**
     * Consulta avanzada de citas con filtros.
     *
     * @param query búsqueda textual
     * @param dateFilter filtro de fecha
     * @param statusFilter filtro de estado
     * @return lista de citas correspondientes
     * @throws SQLException si falla la base de datos
     */
    public List<Appointment> findAdvanced(String query, String dateFilter, String statusFilter) throws SQLException {
        return appointmentDAO.findAdvanced(query, dateFilter, statusFilter);
    }

    /**
     * Consulta avanzada de citas filtrada.
     */
    public List<Appointment> findAdvancedAppointments(String query, String dateFilter, String statusFilter) throws SQLException {
        return appointmentDAO.findAdvanced(query, dateFilter, statusFilter);
    }

    /**
     * Actualiza los datos de una cita completa en base de datos.
     *
     * @param appointment la cita a actualizar
     * @throws SQLException si falla la base de datos
     */
    public void updateAppointment(Appointment appointment) throws SQLException {
        validate(appointment);
        if (appointmentDAO.update(appointment)) {
            actividadDAO.ensureSchema();
            String auditDesc = "Cita editada | Mascota: " + appointment.getPetName() + " | Servicio: " + appointment.getServiceName() + " (" + appointment.getAppointmentDate() + " " + appointment.getAppointmentTime() + ") | Estado: " + appointment.getStatus();
            actividadDAO.insert(auditDesc, "CITAS", appointment.getCreatedBy());
            fireChanged();
        }
    }

    /**
     * Cambia el estado de una cita y notifica a la interfaz.
     *
     * @param appointmentId identificador de cita
     * @param status nuevo estado soportado
     * @throws SQLException si falla la actualización
     */
    public void updateStatus(int appointmentId, String status) throws SQLException {
        updateStatus(appointmentId, status, null);
    }

    /**
     * Cambia el estado de una cita y conserva un punto de extensión para motivo
     * de cancelación cuando el esquema lo soporte.
     *
     * @param appointmentId identificador de cita
     * @param status nuevo estado soportado
     * @param reason motivo opcional asociado al cambio
     * @throws SQLException si falla la actualización
     */
    public void updateStatus(int appointmentId, String status, String reason) throws SQLException {
        validateStatus(status);
        String normalizedStatus = AppointmentStatus.normalizeForStorage(status);
        if (appointmentDAO.updateStatus(appointmentId, normalizedStatus)) {
            registerStatusActivity(appointmentId, normalizedStatus, reason);
            fireChanged();
        }
    }

    /**
     * Cuenta citas pendientes o en proceso.
     *
     * @return total de citas programadas
     * @throws SQLException si falla la consulta
     */
    public int countScheduledAppointments() throws SQLException {
        return appointmentDAO.countScheduled();
    }

    public List<AppointmentDAO.ServicePopularity> findMostRequestedServices(int limit) throws SQLException {
        return appointmentDAO.findMostRequestedServices(limit);
    }

    /**
     * Cuenta servicios realizados.
     *
     * @return total de citas realizadas
     * @throws SQLException si falla la consulta
     */
    public int countFinishedServices() throws SQLException {
        return appointmentDAO.countFinished();
    }

    /**
     * Obtiene mascotas asociadas a un dueño.
     *
     * @param ownerId identificador del dueño
     * @return lista de mascotas registradas para el dueño
     */
    public List<Pet> findPetsByOwner(int ownerId) {
        return petDAO.findByOwnerId(ownerId, context.getActiveBusinessId());
    }

    private void validate(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("La cita no puede ser nula.");
        }
        if (isBlank(appointment.getServiceName()) || isBlank(appointment.getOwnerName())
                || isBlank(appointment.getPetName()) || appointment.getOwnerId() <= 0
                || appointment.getPetId() <= 0 || appointment.getAppointmentDate() == null
                || appointment.getAppointmentTime() == null) {
            throw new IllegalArgumentException("Completa los datos obligatorios de la cita.");
        }

        // Validar el intervalo temporal (fecha/hora fin no menores a fecha/hora inicio)
        if (appointment.getEndDate() != null && appointment.getEndTime() != null) {
            secureauth.shared.util.ServiceScheduleHelper.validateInterval(
                appointment.getAppointmentDate(), appointment.getAppointmentTime(),
                appointment.getEndDate(), appointment.getEndTime()
            );
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getAppointmentDate(),
                appointment.getAppointmentTime());
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se permiten horas pasadas para la cita.");
        }
    }

    private void validateStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!AppointmentStatus.isSupported(normalized)) {
            throw new IllegalArgumentException("Estado de cita no soportado.");
        }
    }

    private void registerStatusActivity(int appointmentId, String status, String reason) throws SQLException {
        Appointment appointment = appointmentDAO.findById(appointmentId);
        if (appointment == null) {
            return;
        }
        String displayStatus = AppointmentStatus.fromDatabaseValue(status)
                .map(AppointmentStatus::displayName)
                .orElse(status);
        String description = "Cita actualizada | Mascota: " + appointment.getPetName() + " | Estado: " + displayStatus;
        if (!isBlank(reason)) {
            description += ". Motivo: " + reason.trim();
        }
        actividadDAO.ensureSchema();
        actividadDAO.insert(description, "CITAS", appointment.getCreatedBy());
    }

    public static void notifyAppointmentsChanged() {
        fireChanged();
    }

    private static void fireChanged() {
        EVENTS.firePropertyChange("appointments", null, null);
        secureauth.shared.events.DashboardEventBus.notifyDataChanged();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
