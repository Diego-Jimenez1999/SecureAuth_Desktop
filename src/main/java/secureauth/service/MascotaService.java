package secureauth.service;

import secureauth.model.Mascota;
import secureauth.repository.MascotaRepository;

/**
 * Servicio de negocio de mascotas.
 */
public class MascotaService {

    private final MascotaRepository mascotaRepository;

    public MascotaService(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    public boolean registrar(Mascota mascota) {
        return mascotaRepository.insert(mascota);
    }
}
