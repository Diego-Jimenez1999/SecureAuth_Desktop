package secureauth.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad de dominio para una cita veterinaria asociada a una venta de servicio.
 *
 * <p>La cita conserva una copia de los nombres visibles de servicio, dueño y
 * mascota para mantener trazabilidad histórica aun si los datos maestros son
 * editados posteriormente.</p>
 *
 * @author Diego
 * @version 1.0
 */
public class Appointment {

    private Integer id;
    private int serviceId;
    private String serviceName;
    private int ownerId;
    private String ownerName;
    private int petId;
    private String petName;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;

    /**
     * Constructor vacío requerido para mapeos manuales JDBC.
     */
    public Appointment() {
    }

    /**
     * Construye una cita con todos sus campos.
     *
     * @param id identificador de la cita
     * @param serviceId identificador del servicio vendido
     * @param serviceName nombre visible del servicio
     * @param ownerId identificador del dueño
     * @param ownerName nombre visible del dueño
     * @param petId identificador de la mascota
     * @param petName nombre visible de la mascota
     * @param appointmentDate fecha programada del servicio
     * @param appointmentTime hora programada del servicio
     * @param status estado operativo de la cita
     * @param notes observaciones de la cita
     * @param createdAt fecha y hora de creación
     * @param createdBy usuario responsable del registro
     */
    public Appointment(Integer id, int serviceId, String serviceName, int ownerId, String ownerName, int petId,
            String petName, LocalDate appointmentDate, LocalTime appointmentTime, String status, String notes,
            LocalDateTime createdAt, String createdBy) {
        this.id = id;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.petId = petId;
        this.petName = petName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    /**
     * @return identificador de la cita
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id identificador de la cita
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return identificador del servicio vendido
     */
    public int getServiceId() {
        return serviceId;
    }

    /**
     * @param serviceId identificador del servicio vendido
     */
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * @return nombre del servicio
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName nombre del servicio
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return identificador del dueño
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * @param ownerId identificador del dueño
     */
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return nombre del dueño
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * @param ownerName nombre del dueño
     */
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    /**
     * @return identificador de la mascota
     */
    public int getPetId() {
        return petId;
    }

    /**
     * @param petId identificador de la mascota
     */
    public void setPetId(int petId) {
        this.petId = petId;
    }

    /**
     * @return nombre de la mascota
     */
    public String getPetName() {
        return petName;
    }

    /**
     * @param petName nombre de la mascota
     */
    public void setPetName(String petName) {
        this.petName = petName;
    }

    /**
     * @return fecha programada
     */
    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    /**
     * @param appointmentDate fecha programada
     */
    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    /**
     * @return hora programada
     */
    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    /**
     * @param appointmentTime hora programada
     */
    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    /**
     * @return estado de la cita
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status estado de la cita
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return observaciones
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes observaciones
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return fecha de creación
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt fecha de creación
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return usuario responsable
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy usuario responsable
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
