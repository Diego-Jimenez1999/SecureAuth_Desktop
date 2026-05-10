package secureauth.repository;

import secureauth.model.Mascota;

/**
 * Contrato de persistencia para mascotas.
 */
public interface MascotaRepository {
    boolean insert(Mascota mascota);
}
