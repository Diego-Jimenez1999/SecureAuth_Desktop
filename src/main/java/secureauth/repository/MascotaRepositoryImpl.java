package secureauth.repository;

import secureauth.dao.MascotaDAO;
import secureauth.model.Mascota;

/**
 * Implementación de repositorio para mascotas.
 */
public class MascotaRepositoryImpl implements MascotaRepository {

    private final MascotaDAO mascotaDAO;

    public MascotaRepositoryImpl() {
        this.mascotaDAO = new MascotaDAO();
    }

    @Override
    public boolean insert(Mascota mascota) {
        return mascotaDAO.insert(mascota);
    }
}
