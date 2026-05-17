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
     * Busca un dueño por id.
     *
     * @param id identificador del dueño
     * @return dueño encontrado o null
     */
    public Owner findOwnerById(int id) {
        return ownerDAO.findById(id);
    }

    /**
     * Registra un nuevo dueño/cliente.
     *
     * @param owner datos del dueño
     */
    public void createOwner(Owner owner) {
        ownerDAO.insert(owner);
    }
}
