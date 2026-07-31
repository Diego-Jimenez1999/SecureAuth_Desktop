package secureauth.service;

/**
 * Excepción de aplicación para errores de negocio al operar sobre mascotas.
 *
 * <p>Introducida en la Fase 3 de estabilización arquitectónica: antes,
 * {@link secureauth.controller.PetController} capturaba directamente
 * {@code secureauth.dao.PetDataAccessException}, una excepción de
 * infraestructura. Ahora {@link PetService} traduce esa excepción a esta,
 * para que el controller (y cualquier otra capa superior) nunca dependa
 * del paquete {@code dao}.</p>
 */
public class PetServiceException extends RuntimeException {

    public PetServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
