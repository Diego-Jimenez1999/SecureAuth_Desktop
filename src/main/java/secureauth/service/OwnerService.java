package secureauth.service;

import java.util.List;
import java.util.Objects;

import secureauth.dao.OwnerDAO;
import secureauth.model.Owner;

/**
 * Servicio de negocio para dueños de mascotas.
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class OwnerService {

    private final OwnerDAO ownerDAO;

    /**
     * Constructor por inyección.
     *
     * @param ownerDAO acceso a datos de dueños
     */
    public OwnerService(OwnerDAO ownerDAO) {
        this.ownerDAO = Objects.requireNonNull(ownerDAO, "OwnerDAO es requerido");
    }

    /**
     * Obtiene todos los dueños para poblar componentes de UI.
     *
     * @return lista de dueños
     */
    public List<Owner> findAllOwners() {
        return ownerDAO.findAll();
    }

    /**
     * Busca dueños por texto.
     *
     * @param query filtro por nombre, teléfono, correo o dirección
     * @return lista filtrada
     */
    public List<Owner> searchOwners(String query) {
        return ownerDAO.search(query);
    }

    /**
     * Busca un dueño por id.
     *
     * @param id identificador del dueño
     * @return dueño encontrado o null
     */
    public Owner findOwnerById(int id) {
        return ownerDAO.findById(id);
    }

    /**
     * Crea un dueño validado.
     *
     * @param owner dueño nuevo
     * @return dueño con id asignado
     */
    public Owner createOwner(Owner owner) {
        validateOwner(owner);
        Owner created = ownerDAO.insert(owner);
        secureauth.shared.events.DashboardEventBus.notifyDataChanged();
        return created;
    }

    /**
     * Actualiza un dueño validado.
     *
     * @param owner dueño existente
     */
    public void updateOwner(Owner owner) {
        if (owner == null || owner.getId() <= 0) {
            throw new IllegalArgumentException("Selecciona un dueño válido.");
        }
        validateOwner(owner);
        ownerDAO.update(owner);
        secureauth.shared.events.DashboardEventBus.notifyDataChanged();
    }

    /**
     * Elimina un dueño por id.
     *
     * @param id identificador
     */
    public void deleteOwner(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un dueño válido.");
        }
        ownerDAO.delete(id);
        secureauth.shared.events.DashboardEventBus.notifyDataChanged();
    }

    /**
     * Asegura que el esquema de la base de datos de dueños exista.
     */
    public void ensureSchema() {
        ownerDAO.ensureSchema();
    }

    /**
     * Cuenta dueños registrados durante el mes actual.
     *
     * @return total de clientes nuevos del mes
     */
    public int countNewThisMonth() {
        return ownerDAO.countNewThisMonth();
    }

    /**
     * Carga estadísticas de dueños registrados hoy, en la semana y en el mes.
     *
     * @return estadísticas de dueños
     */
    public OwnerDAO.OwnerStats loadOwnerStats() {
        return ownerDAO.loadOwnerStats();
    }

    private void validateOwner(Owner owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Dueño requerido.");
        }
        if (owner.getNombreCompleto() == null || owner.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del dueño es obligatorio.");
        }
        if (owner.getTelefono() == null || owner.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono del dueño es obligatorio.");
        }
        if (owner.getCorreo() != null && !owner.getCorreo().isBlank()
                && !owner.getCorreo().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("El correo del dueño no tiene un formato válido.");
        }
    }
}
